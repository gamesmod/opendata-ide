package io.opendata.db.ui

import com.intellij.database.datagrid.DataGrid
import com.intellij.database.datagrid.GridRequestSource
import com.intellij.database.run.ui.DataGridRequestPlace
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import io.opendata.db.grid.ResultGrids
import io.opendata.db.grid.TableDataController
import io.opendata.db.meta.DbObject
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.session.DbSessions
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel

/** Виртуальный файл «данные таблицы» — открывается в редакторе двойным кликом в Database Explorer (ТЗ 20). */
class TableDataFile(val dataSourceId: String, val obj: DbObject) :
    LightVirtualFile("${obj.schema ?: obj.catalog ?: ""}.${obj.name}".trimStart('.')) {
    init {
        isWritable = false
    }

    override fun getPath(): String = "opendata-table://$dataSourceId/${obj.schema ?: obj.catalog}/${obj.name}"
}

class TableDataEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean = file is TableDataFile
    override fun createEditor(project: Project, file: VirtualFile): FileEditor = TableDataEditor(project, file as TableDataFile)
    override fun getEditorTypeId(): String = "opendata-table-data"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

    companion object {
        /** Открывает данные таблицы; повторное открытие той же таблицы активирует существующую вкладку. */
        fun open(project: Project, dataSourceId: String, obj: DbObject) {
            val fem = FileEditorManager.getInstance(project)
            val existing = fem.openFiles.filterIsInstance<TableDataFile>().firstOrNull { it.dataSourceId == dataSourceId && it.obj == obj }
            fem.openFile(existing ?: TableDataFile(dataSourceId, obj), true)
        }
    }
}

class TableDataEditor(private val project: Project, private val file: TableDataFile) : UserDataHolderBase(), FileEditor {
    private val session = DbSessions.getInstance().get("table:${System.identityHashCode(this)}", file.dataSourceId)
    val controller = TableDataController(project, session, file.obj)
    private val root = JPanel(BorderLayout())
    private val status = JBLabel().apply { foreground = UIUtil.getContextHelpForeground() }
    private val whereField = JBTextField().apply { emptyText.text = "WHERE …" }
    private val orderField = JBTextField().apply { emptyText.text = "ORDER BY …" }
    var grid: DataGrid? = null
        private set

    init {
        Disposer.register(this, controller)
        root.add(JBLabel("Загрузка…").apply { border = JBUI.Borders.empty(8) }, BorderLayout.CENTER)
        // Метаданные (ключ таблицы) читаются вне EDT, затем строится grid.
        ApplicationManager.getApplication().executeOnPooledThread {
            val error = runCatching { controller.isEditable }.exceptionOrNull()
            ApplicationManager.getApplication().invokeLater {
                if (Disposer.isDisposed(this)) return@invokeLater
                if (error != null) {
                    root.removeAll()
                    root.add(JBLabel(io.opendata.db.session.DbError.from(error).render()).apply { border = JBUI.Borders.empty(8) })
                    root.revalidate()
                } else buildUi()
            }
        }
    }

    private fun buildUi() {
        val g = ResultGrids.create(project, controller.hookUp, this)
        grid = g
        g.putUserData(EDITOR_KEY_USERDATA, this)

        val am = ActionManager.getInstance()
        val group = DefaultActionGroup().apply {
            add(ReloadAction())
            if (controller.isEditable) {
                addSeparator()
                am.getAction("Console.TableResult.AddRow")?.let { add(it) }
                am.getAction("Console.TableResult.DeleteRows")?.let { add(it) }
                am.getAction("Console.TableResult.CloneRow")?.let { add(it) }
                add(SubmitAction())
                add(RevertAction())
            }
            addSeparator()
            am.getAction("Console.TableResult.Pagination.Group")?.let { add(it) }
        }
        val toolbar = am.createActionToolbar("OpenDataTableEditor", group, true)
        toolbar.targetComponent = ResultGrids.component(g)

        val filter = JPanel(BorderLayout(JBUI.scale(6), 0)).apply {
            border = JBUI.Borders.empty(2, 4)
            val fields = JPanel(java.awt.GridLayout(1, 2, JBUI.scale(6), 0))
            fields.add(whereField); fields.add(orderField)
            add(fields, BorderLayout.CENTER)
        }
        listOf(whereField, orderField).forEach { f -> f.addActionListener { applyFilter() } }

        val top = JPanel(BorderLayout()).apply {
            add(toolbar.component, BorderLayout.WEST)
            add(JPanel(FlowLayout(FlowLayout.RIGHT)).apply { add(status) }, BorderLayout.EAST)
            add(filter, BorderLayout.SOUTH)
        }
        root.removeAll()
        root.add(top, BorderLayout.NORTH)
        root.add(ResultGrids.component(g), BorderLayout.CENTER)
        root.revalidate()
        status.text = if (controller.isEditable) "ключ: ${controller.keyColumns.joinToString()}" else "только чтение"
        controller.pager.reloadCurrentPage(source())
    }

    fun source(): GridRequestSource = GridRequestSource(grid?.let { DataGridRequestPlace(it) })

    private fun applyFilter() {
        controller.where = whereField.text
        controller.orderBy = orderField.text
        controller.pager.loadFirstPage(source())
    }

    /** Ctrl+Enter в редакторе таблицы: Submit (ТЗ 19). */
    inner class SubmitAction : DumbAwareAction("Submit", "Применить изменения (DML в транзакции)", AllIcons.Actions.Commit) {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = controller.hookUp.mutator.hasPendingChanges()
        }
        override fun actionPerformed(e: AnActionEvent) = submit()
    }

    inner class RevertAction : DumbAwareAction("Revert", "Отменить несохранённые изменения", AllIcons.Actions.Rollback) {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = controller.hookUp.mutator.hasPendingChanges()
        }
        override fun actionPerformed(e: AnActionEvent) = controller.hookUp.mutator.revertAll(source())
    }

    inner class ReloadAction : DumbAwareAction("Reload", "Перечитать данные", AllIcons.Actions.Refresh) {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        override fun actionPerformed(e: AnActionEvent) {
            if (controller.isEditable && controller.hookUp.mutator.hasPendingChanges() &&
                Messages.showYesNoDialog(project, "Несохранённые изменения будут потеряны. Перечитать?", "Reload", null) != Messages.YES) return
            if (controller.isEditable) controller.hookUp.mutator.revertAll(source())
            controller.pager.reloadCurrentPage(source())
        }
    }

    fun submit() {
        if (!controller.isEditable) return
        val m = controller.hookUp.mutator
        if (!m.hasPendingChanges()) return
        val src = source()
        src.actionCallback.doWhenRejected(Runnable {
            Messages.showErrorDialog(project, src.errorMessage ?: "Ошибка применения изменений", "Submit")
        })
        m.submit(src, true)
    }

    override fun getComponent(): JComponent = root
    override fun getPreferredFocusedComponent(): JComponent? = grid?.preferredFocusedComponent
    override fun getName(): String = "Data"
    override fun getFile(): VirtualFile = file
    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean = controller.isEditable && grid != null && controller.hookUp.mutator.hasPendingChanges()
    override fun isValid(): Boolean = true
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun dispose() {
        DbSessions.getInstance().close("table:${System.identityHashCode(this)}")
    }

    companion object {
        val EDITOR_KEY_USERDATA = com.intellij.openapi.util.Key.create<TableDataEditor>("opendata.table.editor")
        @Suppress("unused") val DATA_KEY = DataKey.create<TableDataEditor>("opendata.table.editor")

        fun fromEvent(e: AnActionEvent): TableDataEditor? {
            val editor = e.getData(com.intellij.openapi.actionSystem.PlatformCoreDataKeys.FILE_EDITOR) as? TableDataEditor
            if (editor != null) return editor
            val grid = e.getData(com.intellij.database.DatabaseDataKeys.DATA_GRID_KEY) ?: return null
            return grid.getUserData(EDITOR_KEY_USERDATA)
        }
    }
}

/** Глобальное действие Submit (Ctrl+Enter в grid редактора таблицы). */
class SubmitTableChangesAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT
    override fun update(e: AnActionEvent) {
        val ed = TableDataEditor.fromEvent(e)
        e.presentation.isEnabledAndVisible = ed != null && ed.controller.isEditable && ed.controller.hookUp.mutator.hasPendingChanges()
    }
    override fun actionPerformed(e: AnActionEvent) {
        TableDataEditor.fromEvent(e)?.submit()
    }
}

@Suppress("unused")
private fun dsName(id: String) = DataSourceStorage.getInstance().find(id)?.name ?: id
