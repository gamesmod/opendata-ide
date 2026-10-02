package io.opendata.db.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.PopupHandler
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import io.opendata.db.lang.SqlFileType
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.DdlGenerator
import io.opendata.db.meta.MetadataCache
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.model.DataSources
import io.opendata.db.model.DbKind
import io.opendata.db.session.DbError
import io.opendata.db.session.DbSessions
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeWillExpandListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class DatabaseToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val explorer = DatabaseExplorer(project)
        val content = ContentFactory.getInstance().createContent(explorer, "", false)
        content.setDisposer(explorer)
        toolWindow.contentManager.addContent(content)
    }

    companion object {
        const val ID = "Database"
    }
}

/** Узел дерева: источник данных (obj == null) или объект БД. */
data class ExplorerNode(val dataSourceId: String, val obj: DbObject?)

private object LoadingNode {
    override fun toString() = "Загрузка…"
}

private data class ErrorNode(val message: String)

/** Database Explorer (ТЗ 12): дерево источников данных и объектов БД. */
class DatabaseExplorer(private val project: Project) : SimpleToolWindowPanel(true, true), com.intellij.openapi.Disposable {
    private val root = DefaultMutableTreeNode()
    private val model = DefaultTreeModel(root)
    val tree = Tree(model)

    init {
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        tree.cellRenderer = Renderer()
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        tree.addTreeWillExpandListener(object : TreeWillExpandListener {
            override fun treeWillExpand(event: TreeExpansionEvent) = loadChildren(event.path.lastPathComponent as DefaultMutableTreeNode)
            override fun treeWillCollapse(event: TreeExpansionEvent) {}
        })
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && e.button == MouseEvent.BUTTON1) {
                    val node = selectedNode() ?: return
                    val o = node.obj ?: return
                    if (o.kind == DbObjectKind.TABLE || o.kind == DbObjectKind.VIEW) {
                        TableDataEditorProvider.open(project, node.dataSourceId, o)
                        e.consume()
                    }
                }
            }
        })

        val actions = DefaultActionGroup().apply {
            add(AddDataSourceAction())
            add(EditDataSourceAction())
            add(DuplicateDataSourceAction())
            add(DeleteDataSourceAction())
            addSeparator()
            add(RefreshAction())
            add(DisconnectAction())
            addSeparator()
            add(OpenConsoleAction())
            add(OpenDataAction())
            add(ShowDdlAction())
            addSeparator()
            add(ExportDataAction())
            add(ImportDataAction())
            addSeparator()
            add(DefaultActionGroup("Ещё", true).apply {
                templatePresentation.icon = AllIcons.Actions.More
                add(ExportConnectionsAction()); add(ImportConnectionsAction())
                addSeparator()
                add(DriversAction())
            })
        }
        val toolbar = ActionManager.getInstance().createActionToolbar("OpenDataDatabaseExplorer", actions, true)
        toolbar.targetComponent = tree
        setToolbar(toolbar.component)
        setContent(ScrollPaneFactory.createScrollPane(tree))
        PopupHandler.installPopupMenu(tree, DefaultActionGroup().apply {
            add(OpenConsoleAction()); add(OpenDataAction()); add(ShowDdlAction()); addSeparator()
            add(ExportDataAction()); add(ImportDataAction()); addSeparator()
            add(RefreshAction()); add(DisconnectAction()); addSeparator()
            add(EditDataSourceAction()); add(DuplicateDataSourceAction()); add(DeleteDataSourceAction())
        }, "OpenDataDatabaseExplorerPopup")

        DataSourceStorage.getInstance(project).addListener({ ApplicationManager.getApplication().invokeLater { rebuild() } }, this)
        rebuild()
    }

    // ------------------------------------------------------------------ дерево

    fun rebuild() {
        val expanded = TreeUtil.collectExpandedUserObjects(tree).filterIsInstance<ExplorerNode>().toSet()
        root.removeAllChildren()
        DataSourceStorage.getInstance(project).dataSources.sortedBy { it.name.lowercase() }.forEach { ds ->
            root.add(DefaultMutableTreeNode(ExplorerNode(ds.id, null)).apply { add(DefaultMutableTreeNode(LoadingNode)) })
        }
        model.reload()
        if (expanded.isNotEmpty()) {
            for (i in 0 until root.childCount) {
                val n = root.getChildAt(i) as DefaultMutableTreeNode
                if (n.userObject in expanded) tree.expandPath(TreePath(n.path))
            }
        }
    }

    private fun dsOf(node: ExplorerNode): DataSourceConfig? = DataSourceStorage.getInstance(project).find(node.dataSourceId)

    private fun hasChildren(o: DbObject): Boolean = !o.isLeaf

    private fun loadChildren(node: DefaultMutableTreeNode) {
        if (node.childCount != 1 || (node.getChildAt(0) as DefaultMutableTreeNode).userObject !== LoadingNode) return
        val en = node.userObject as? ExplorerNode ?: return
        val ds = dsOf(en) ?: return
        if (node.getUserObject() == null) return
        (node.getChildAt(0) as DefaultMutableTreeNode).userObject = LoadingNode
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = runCatching {
                val session = DbSessions.getInstance().explorer(ds.id)
                synchronized(session) {
                    val loader = MetadataLoader(session.connection(), ds.kind)
                    if (en.obj == null) loader.containers() else loader.children(en.obj)
                }
            }
            if (en.obj == null && result.isSuccess) MetadataCache.getInstance().prefetch(ds.id)
            ApplicationManager.getApplication().invokeLater {
                node.removeAllChildren()
                result.onSuccess { children ->
                    children.forEach { o ->
                        val child = DefaultMutableTreeNode(ExplorerNode(ds.id, o))
                        if (hasChildren(o)) child.add(DefaultMutableTreeNode(LoadingNode))
                        node.add(child)
                    }
                }.onFailure { e -> node.add(DefaultMutableTreeNode(ErrorNode(DbError.from(e).message))) }
                model.nodeStructureChanged(node)
                tree.repaint()
            }
        }
    }

    private fun resetNode(node: DefaultMutableTreeNode) {
        val wasExpanded = tree.isExpanded(TreePath(node.path))
        node.removeAllChildren()
        node.add(DefaultMutableTreeNode(LoadingNode))
        model.nodeStructureChanged(node)
        if (wasExpanded) {
            loadChildren(node)
            tree.expandPath(TreePath(node.path))
        }
    }

    private fun selectedTreeNode(): DefaultMutableTreeNode? = tree.selectionPath?.lastPathComponent as? DefaultMutableTreeNode
    fun selectedNode(): ExplorerNode? = selectedTreeNode()?.userObject as? ExplorerNode
    private fun selectedDs(): DataSourceConfig? = selectedNode()?.let { dsOf(it) }

    fun selectDataSource(id: String) {
        for (i in 0 until root.childCount) {
            val n = root.getChildAt(i) as DefaultMutableTreeNode
            if ((n.userObject as? ExplorerNode)?.dataSourceId == id) {
                tree.selectionPath = TreePath(n.path)
                return
            }
        }
    }

    override fun dispose() {}

    // ------------------------------------------------------------------ отрисовка

    private inner class Renderer : ColoredTreeCellRenderer() {
        override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
            when (val u = (value as? DefaultMutableTreeNode)?.userObject) {
                is ExplorerNode -> {
                    val o = u.obj
                    if (o == null) {
                        val ds = dsOf(u)
                        icon = AllIcons.Nodes.DataTables
                        append(ds?.name ?: "?", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                        ds?.let { append("  ${it.kind.displayName} · ${it.host}:${it.port}", SimpleTextAttributes.GRAYED_ATTRIBUTES) }
                        if (ds != null && DbSessions.getInstance().isConnected(ds.id)) append("  ●", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    } else {
                        icon = iconOf(o.kind)
                        append(o.name)
                        o.detail?.let { append("  $it", SimpleTextAttributes.GRAYED_ATTRIBUTES) }
                    }
                }
                is ErrorNode -> {
                    icon = AllIcons.General.Error
                    append(u.message, SimpleTextAttributes.ERROR_ATTRIBUTES)
                }
                else -> append(u?.toString() ?: "", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        }
    }

    private fun iconOf(kind: DbObjectKind): Icon = when (kind) {
        DbObjectKind.CATALOG, DbObjectKind.SCHEMA -> AllIcons.Nodes.Package
        DbObjectKind.TABLE -> AllIcons.Nodes.DataTables
        DbObjectKind.VIEW -> AllIcons.Nodes.DataTables
        DbObjectKind.FUNCTION -> AllIcons.Nodes.Function
        DbObjectKind.SEQUENCE -> AllIcons.Nodes.Variable
        DbObjectKind.TYPE -> AllIcons.Nodes.Type
        DbObjectKind.COLUMN -> AllIcons.Nodes.Field
        DbObjectKind.PRIMARY_KEY, DbObjectKind.FOREIGN_KEY -> AllIcons.Nodes.Parameter
        DbObjectKind.INDEX -> AllIcons.Nodes.Property
        DbObjectKind.TRIGGER -> AllIcons.Nodes.Method
        DbObjectKind.CONSTRAINT -> AllIcons.Nodes.Annotationtype
        else -> AllIcons.Nodes.Folder
    }

    // ------------------------------------------------------------------ действия

    private abstract inner class ExplorerAction(text: String, icon: Icon?) : DumbAwareAction(text, null, icon) {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
    }

    private inner class AddDataSourceAction : ExplorerAction("Новый источник данных", AllIcons.General.Add) {
        override fun actionPerformed(e: AnActionEvent) {
            val kinds = DbKind.entries
            JBPopupFactory.getInstance().createPopupChooserBuilder(kinds)
                .setTitle("Тип источника данных")
                .setRenderer(com.intellij.ui.SimpleListCellRenderer.create("") { k: DbKind -> k.displayName })
                .setItemChosenCallback { k ->
                    val cfg = DataSourceConfig().apply {
                        kind = k; port = k.defaultPort; database = k.defaultDatabase
                        name = "${k.displayName} localhost"
                    }
                    DataSourceDialog.edit(project, cfg)?.let { selectDataSource(it.id) }
                }
                .createPopup().showUnderneathOf(this@DatabaseExplorer.toolbar ?: tree)
        }
    }

    private inner class EditDataSourceAction : ExplorerAction("Свойства источника данных", AllIcons.Actions.Edit) {
        override fun update(e: AnActionEvent) { e.presentation.isEnabled = selectedDs() != null }
        override fun actionPerformed(e: AnActionEvent) {
            val ds = selectedDs() ?: return
            DataSourceDialog.edit(project, ds)?.let {
                DbSessions.getInstance().disconnect(it.id)
                MetadataCache.getInstance().invalidate(it.id)
                selectDataSource(it.id)
            }
        }
    }

    private inner class DuplicateDataSourceAction : ExplorerAction("Дублировать", AllIcons.Actions.Copy) {
        override fun update(e: AnActionEvent) { e.presentation.isEnabled = selectedNode()?.obj == null && selectedDs() != null }
        override fun actionPerformed(e: AnActionEvent) {
            val ds = selectedDs() ?: return
            val copy = ds.copy(newId = true).apply { name = "${ds.name} (копия)" }
            DataSources.getPassword(ds.id)?.let { DataSources.setPassword(copy.id, it) }
            DataSourceStorage.getInstance(project).addOrUpdate(copy)
            selectDataSource(copy.id)
        }
    }

    private inner class DeleteDataSourceAction : ExplorerAction("Удалить источник данных", AllIcons.General.Remove) {
        override fun update(e: AnActionEvent) { e.presentation.isEnabled = selectedNode()?.obj == null && selectedDs() != null }
        override fun actionPerformed(e: AnActionEvent) {
            val ds = selectedDs() ?: return
            if (Messages.showYesNoDialog(project, "Удалить источник данных «${ds.name}»?", "Удаление", null) != Messages.YES) return
            DbSessions.getInstance().disconnect(ds.id)
            DataSourceStorage.getInstance(project).remove(ds.id)
        }
    }

    private inner class RefreshAction : ExplorerAction("Обновить", AllIcons.Actions.Refresh) {
        override fun update(e: AnActionEvent) { e.presentation.isEnabled = selectedTreeNode() != null }
        override fun actionPerformed(e: AnActionEvent) {
            val node = selectedTreeNode() ?: return
            (node.userObject as? ExplorerNode)?.let { MetadataCache.getInstance().invalidate(it.dataSourceId) }
            resetNode(node)
        }
    }

    private inner class DisconnectAction : ExplorerAction("Отключиться", AllIcons.Actions.Suspend) {
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = selectedDs()?.let { DbSessions.getInstance().isConnected(it.id) } == true
        }
        override fun actionPerformed(e: AnActionEvent) {
            val ds = selectedDs() ?: return
            ApplicationManager.getApplication().executeOnPooledThread {
                DbSessions.getInstance().disconnect(ds.id)
                ApplicationManager.getApplication().invokeLater { tree.repaint() }
            }
        }
    }

    private inner class OpenConsoleAction : ExplorerAction("SQL-консоль", AllIcons.Nodes.Console) {
        override fun update(e: AnActionEvent) { e.presentation.isEnabled = selectedDs() != null }
        override fun actionPerformed(e: AnActionEvent) {
            Consoles.open(project, selectedDs() ?: return)
        }
    }

    private inner class OpenDataAction : ExplorerAction("Данные таблицы", AllIcons.Nodes.DataTables) {
        override fun update(e: AnActionEvent) {
            val k = selectedNode()?.obj?.kind
            e.presentation.isEnabled = k == DbObjectKind.TABLE || k == DbObjectKind.VIEW
        }
        override fun actionPerformed(e: AnActionEvent) {
            val n = selectedNode() ?: return
            TableDataEditorProvider.open(project, n.dataSourceId, n.obj ?: return)
        }
    }

    /** SQL Scripts → DDL (ТЗ 21): DDL открывается в SQL-редакторе, привязанном к источнику данных. */
    private inner class ShowDdlAction : ExplorerAction("DDL", AllIcons.Actions.Preview) {
        override fun update(e: AnActionEvent) {
            val o = selectedNode()?.obj
            e.presentation.isEnabled = o != null && !o.kind.isFolder && o.kind != DbObjectKind.COLUMN
        }
        override fun actionPerformed(e: AnActionEvent) {
            val n = selectedNode() ?: return
            val o = n.obj ?: return
            val ds = dsOf(n) ?: return
            ApplicationManager.getApplication().executeOnPooledThread {
                val ddl = runCatching {
                    val session = DbSessions.getInstance().explorer(ds.id)
                    synchronized(session) { DdlGenerator(session.connection(), ds.kind).ddl(o) }
                }.getOrElse { "-- " + DbError.from(it).render().replace("\n", "\n-- ") }
                ApplicationManager.getApplication().invokeLater {
                    val file = LightVirtualFile("${o.name}.sql", SqlFileType, ddl + "\n")
                    ConsoleFiles.bind(file, ds.id)
                    FileEditorManager.getInstance(project).openFile(file, true)
                }
            }
        }
    }

    private fun isContainer(o: DbObject?) = o != null && (o.kind == DbObjectKind.SCHEMA || o.kind == DbObjectKind.CATALOG)

    /** Экспорт данных таблицы или представления в файл. */
    private inner class ExportDataAction : ExplorerAction("Экспорт данных…", AllIcons.ToolbarDecorator.Export) {
        override fun update(e: AnActionEvent) {
            val k = selectedNode()?.obj?.kind
            e.presentation.isEnabled = k == DbObjectKind.TABLE || k == DbObjectKind.VIEW
        }
        override fun actionPerformed(e: AnActionEvent) {
            val n = selectedNode() ?: return
            io.opendata.db.data.DataTransferUi.exportTable(project, dsOf(n) ?: return, n.obj ?: return)
        }
    }

    /** Импорт CSV/TSV: в выбранную таблицу или в новую таблицу выбранной схемы/базы. */
    private inner class ImportDataAction : ExplorerAction("Импорт данных…", AllIcons.ToolbarDecorator.Import) {
        override fun update(e: AnActionEvent) {
            val n = selectedNode()
            val o = n?.obj
            e.presentation.isEnabled = n != null && dsOf(n)?.kind != DbKind.DREMIO && (o?.kind == DbObjectKind.TABLE || isContainer(o))
        }
        override fun actionPerformed(e: AnActionEvent) {
            val n = selectedNode() ?: return
            val o = n.obj ?: return
            val ds = dsOf(n) ?: return
            val treeNode = selectedTreeNode() ?: return
            // После импорта в новую таблицу перечитываем схему, чтобы таблица появилась в дереве.
            if (o.kind == DbObjectKind.TABLE) io.opendata.db.data.DataTransferUi.import(project, ds, o, null)
            else io.opendata.db.data.DataTransferUi.import(project, ds, null, o) { MetadataCache.getInstance().invalidate(ds.id); resetNode(treeNode) }
        }
    }

    /** Подключения проекта в XML-файл (без паролей) — чтобы перенести в другой проект или передать коллеге. */
    private inner class ExportConnectionsAction : ExplorerAction("Экспорт подключений…", AllIcons.ToolbarDecorator.Export) {
        override fun actionPerformed(e: AnActionEvent) {
            val storage = DataSourceStorage.getInstance(project)
            if (storage.dataSources.isEmpty()) { Messages.showInfoMessage(project, "В проекте нет подключений.", "Экспорт подключений"); return }
            val wrapper = com.intellij.openapi.fileChooser.FileChooserFactory.getInstance().createSaveFileDialog(
                com.intellij.openapi.fileChooser.FileSaverDescriptor("Экспорт подключений", "Пароли не сохраняются", "xml"), project)
            val target = wrapper.save(project.basePath?.let { java.nio.file.Path.of(it) }, "opendata-connections.xml") ?: return
            java.nio.file.Files.writeString(target.file.toPath(), ConnectionsTransfer.toXml(storage.dataSources))
            Messages.showInfoMessage(project, "Сохранено подключений: ${storage.dataSources.size}\n${target.file}", "Экспорт подключений")
        }
    }

    private inner class ImportConnectionsAction : ExplorerAction("Импорт подключений…", AllIcons.ToolbarDecorator.Import) {
        override fun actionPerformed(e: AnActionEvent) {
            val vf = com.intellij.openapi.fileChooser.FileChooser.chooseFile(
                com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFileDescriptor("xml").withTitle("Файл подключений"), project, null) ?: return
            val result = runCatching { ConnectionsTransfer.importInto(DataSourceStorage.getInstance(project), String(vf.contentsToByteArray(), Charsets.UTF_8)) }
            result.onSuccess { Messages.showInfoMessage(project, "Добавлено подключений: $it. Пароли нужно ввести заново.", "Импорт подключений") }
                .onFailure { Messages.showErrorDialog(project, "Не удалось прочитать файл: ${it.message}", "Импорт подключений") }
        }
    }

    private inner class DriversAction : ExplorerAction("Драйверы…", AllIcons.General.Settings) {
        override fun actionPerformed(e: AnActionEvent) = io.opendata.db.drivers.DriversConfigurable.show(project)
    }

    @Suppress("unused")
    private fun disposeLater(d: com.intellij.openapi.Disposable) = Disposer.register(this, d)
}

/** «Новый источник данных» в текущем проекте (меню SQL). */
class NewDataSourceAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        DataSourceDialog.edit(project, DataSourceConfig().apply { name = "PostgreSQL localhost" })
    }
}

/** Экспорт/импорт подключений: тот же XML, что в `.idea/opendata-datasources.xml`, без паролей. */
object ConnectionsTransfer {
    fun toXml(list: List<DataSourceConfig>): String {
        val state = DataSourceStorage.State().apply { dataSources = list.map { it.copy() }.toMutableList() }
        val element = com.intellij.util.xmlb.XmlSerializer.serialize(state)
        return com.intellij.openapi.util.JDOMUtil.write(element) + "\n"
    }

    /** Добавляет подключения из XML; при совпадении id создаётся копия с новым id. Возвращает число добавленных. */
    fun importInto(storage: DataSourceStorage, xml: String): Int {
        val state = com.intellij.util.xmlb.XmlSerializer.deserialize(com.intellij.openapi.util.JDOMUtil.load(xml), DataSourceStorage.State::class.java)
        state.dataSources.forEach { ds -> storage.addOrUpdate(if (storage.find(ds.id) != null) ds.copy(newId = true) else ds) }
        return state.dataSources.size
    }
}
