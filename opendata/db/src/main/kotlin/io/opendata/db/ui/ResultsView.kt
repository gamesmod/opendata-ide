package io.opendata.db.ui

import com.intellij.database.datagrid.DataGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import io.opendata.db.grid.ResultGrids
import io.opendata.db.session.StatementResult
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JPanel

/** Окно результатов (ТЗ 14, 18): вкладка на консоль — Output (лог) + результаты в DataGrid. */
class ResultsToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.setStripeTitle("Results")
    }

    override fun shouldBeAvailable(project: Project): Boolean = true

    companion object {
        const val ID = "Results"
    }
}

@Service(Service.Level.PROJECT)
class ResultsView(private val project: Project) {

    /** Панель результатов одной консоли. */
    class ConsoleResults(val key: String, parent: Disposable) : JPanel(BorderLayout()), Disposable {
        private val tabs = JBTabbedPane()
        private val log = JBTextArea().apply {
            isEditable = false
            font = JBUI.Fonts.create(java.awt.Font.MONOSPACED, 12)
        }
        private val gridDisposables = ArrayList<Disposable>()
        private val time = SimpleDateFormat("HH:mm:ss")
        private var shown: List<StatementResult.Rows> = emptyList()
        private var dataSourceId: String? = null
        private var project: Project? = null

        init {
            tabs.addTab("Output", JBScrollPane(log))
            add(tabs, BorderLayout.CENTER)
            val actions = com.intellij.openapi.actionSystem.DefaultActionGroup(ExportResultAction())
            val toolbar = com.intellij.openapi.actionSystem.ActionManager.getInstance().createActionToolbar("OpenDataResults", actions, false)
            toolbar.targetComponent = this
            add(toolbar.component, BorderLayout.WEST)
            Disposer.register(parent, this)
        }

        /** Результат на выбранной вкладке (вкладка 0 — Output). */
        val selectedResult: StatementResult.Rows? get() = shown.getOrNull(tabs.selectedIndex - 1)

        private inner class ExportResultAction : com.intellij.openapi.project.DumbAwareAction(
            "Экспорт результата…", "Сохранить результат в CSV, TSV, JSON, SQL INSERT или Markdown", com.intellij.icons.AllIcons.ToolbarDecorator.Export,
        ) {
            override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.EDT
            override fun update(e: com.intellij.openapi.actionSystem.AnActionEvent) {
                e.presentation.isEnabled = selectedResult != null
            }
            override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) {
                val r = selectedResult ?: return
                io.opendata.db.data.DataTransferUi.exportResult(project ?: e.project ?: return, dataSourceId, r)
            }
        }

        fun log(line: String) {
            log.append("[${time.format(Date())}] $line\n")
            log.caretPosition = log.document.length
        }

        /** Заменяет предыдущие результаты новыми (по одной вкладке на result set). */
        fun showResults(project: Project, results: List<StatementResult>, dataSourceId: String? = null): List<DataGrid> {
            this.project = project
            this.dataSourceId = dataSourceId
            shown = results.filterIsInstance<StatementResult.Rows>()
            while (tabs.tabCount > 1) tabs.removeTabAt(1)
            gridDisposables.forEach { Disposer.dispose(it) }
            gridDisposables.clear()
            val grids = ArrayList<DataGrid>()
            results.filterIsInstance<StatementResult.Rows>().forEachIndexed { i, r ->
                val holder = Disposer.newDisposable("result-$i")
                Disposer.register(this, holder)
                gridDisposables += holder
                val grid = ResultGrids.createReadOnlyGrid(project, r, holder)
                grids += grid
                val title = if (r.truncated) "Result ${i + 1} (${r.rows.size}+)" else "Result ${i + 1}"
                tabs.addTab(title, ResultGrids.component(grid))
            }
            tabs.selectedIndex = if (tabs.tabCount > 1) 1 else 0
            getInstance(project).lastGrids = grids
            return grids
        }

        fun showOutput() {
            tabs.selectedIndex = 0
        }

        val selectedTitle: String get() = tabs.getTitleAt(tabs.selectedIndex)

        override fun dispose() {}
    }

    private val panels = HashMap<String, Pair<Content, ConsoleResults>>()

    /** DataGrid последнего выполнения (для самопроверки). */
    @Volatile var lastGrids: List<DataGrid> = emptyList()
        internal set

    private fun toolWindow(): ToolWindow? = ToolWindowManager.getInstance(project).getToolWindow(ResultsToolWindowFactory.ID)

    fun panel(key: String, title: String): ConsoleResults {
        val tw = toolWindow() ?: error("Окно ${ResultsToolWindowFactory.ID} не зарегистрировано")
        panels[key]?.let { (content, panel) ->
            if (!content.isValid) panels.remove(key) else {
                tw.contentManager.setSelectedContent(content)
                return panel
            }
        }
        val holder = Disposer.newDisposable("results:$key")
        val panel = ConsoleResults(key, holder)
        val content = ContentFactory.getInstance().createContent(panel, title, false)
        content.setDisposer(holder)
        tw.contentManager.addContent(content)
        tw.contentManager.setSelectedContent(content)
        panels[key] = content to panel
        return panel
    }

    fun activate() {
        toolWindow()?.activate(null, false)
    }

    companion object {
        fun getInstance(project: Project): ResultsView = project.service()
    }
}
