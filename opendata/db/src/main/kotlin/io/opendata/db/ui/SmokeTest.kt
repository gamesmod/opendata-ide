package io.opendata.db.ui

import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.model.DataSourceStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 * Самопроверка продукта (-Dopendata.smoke=<id источника данных>): через настоящие UI-компоненты раскрывает
 * источник в Database Explorer, выполняет запрос из консоли (результат в DataGrid), открывает редактор таблицы
 * (-Dopendata.smoke.table=schema.table) и пишет отчёт в -Dopendata.diagnostics.file. Используется в CI и scripts/run.* --smoke.
 */
object SmokeTest {
    suspend fun run(project: Project, dsId: String, report: StringBuilder) {
        val ds = DataSourceStorage.getInstance().find(dsId) ?: run { report.appendLine("smoke.ds=NOT_FOUND"); return }
        report.appendLine("smoke.ds=${ds.name} (${ds.kind.displayName})")

        // 1. Database Explorer: раскрыть источник данных (introspection в фоне).
        val explorer = withContext(Dispatchers.EDT) {
            val tw = ToolWindowManager.getInstance(project).getToolWindow(DatabaseToolWindowFactory.ID)!!
            tw.show()
            val ex = tw.contentManager.contents.firstNotNullOf { it.component as? DatabaseExplorer }
            ex.selectDataSource(dsId)
            ex.tree.expandPath(ex.tree.selectionPath)
            ex
        }
        val containers = waitFor { withContext(Dispatchers.EDT) { childNames(explorer, explorer.tree.selectionPath) }.takeIf { it.isNotEmpty() && it.first() != "Загрузка…" } }
        report.appendLine("smoke.explorer.containers=${containers?.joinToString(",")}")

        // 2. Консоль: SELECT → DataGrid в окне Results.
        val sql = System.getProperty("opendata.smoke.sql")
            ?: "SELECT generate_series AS id, md5(generate_series::text) AS value FROM generate_series(1,100)"
        val editor: Editor = withContext(Dispatchers.EDT) {
            Consoles.open(project, ds)
            val ed = (FileEditorManager.getInstance(project).selectedEditor as TextEditor).editor
            WriteCommandAction.runWriteCommandAction(project) { ed.document.setText("SELECT version();\n\n$sql;\n") }
            ed.caretModel.moveToOffset(ed.document.text.indexOf(sql) + 1)
            ConsoleRunner.execute(project, ed, FileEditorManager.getInstance(project).selectedFiles.first(), ConsoleRunner.Mode.CURRENT)
            ed
        }
        val rows = waitFor {
            withContext(Dispatchers.EDT) {
                ResultsView.getInstance(project).lastGrids.firstOrNull()?.let {
                    it.getDataModel(com.intellij.database.run.ui.DataAccessType.DATA_WITH_MUTATIONS).rowCount
                }
            }
        }
        report.appendLine("smoke.console.rows=$rows")
        report.appendLine("smoke.console.file=${editor.virtualFile?.name}")

        // 3. Редактор данных таблицы.
        System.getProperty("opendata.smoke.table")?.let { qn ->
            val (schema, table) = qn.split('.', limit = 2)
            val obj = if (ds.kind.isPostgresFamily) DbObject(DbObjectKind.TABLE, table, schema = schema, table = table)
            else DbObject(DbObjectKind.TABLE, table, schema = schema, table = table)
            withContext(Dispatchers.EDT) { TableDataEditorProvider.open(project, dsId, obj) }
            val tableRows = waitFor {
                withContext(Dispatchers.EDT) {
                    val ed = FileEditorManager.getInstance(project).allEditors.filterIsInstance<TableDataEditor>().firstOrNull()
                    // Строки в видимой таблице (JTable результата), а не только в модели — проверка, что UI обновился.
                    (ed?.grid?.resultView?.component as? javax.swing.JTable)?.rowCount?.takeIf { it > 0 }
                        ?.let { it to ed.controller.isEditable }
                }
            }
            report.appendLine("smoke.table.rows=${tableRows?.first} editable=${tableRows?.second}")
        }
        delay(1500)
        val ok = containers != null && (rows ?: 0) > 0
        report.appendLine("SMOKE=" + if (ok) "OK" else "FAIL")
    }

    private fun childNames(ex: DatabaseExplorer, path: TreePath?): List<String> {
        val node = path?.lastPathComponent as? DefaultMutableTreeNode ?: return emptyList()
        return (0 until node.childCount).map { i ->
            when (val u = (node.getChildAt(i) as DefaultMutableTreeNode).userObject) {
                is ExplorerNode -> u.obj?.name ?: "?"
                else -> u.toString()
            }
        }
    }

    private suspend fun <T> waitFor(timeoutMs: Long = 60_000, probe: suspend () -> T?): T? {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            probe()?.let { return it }
            delay(300)
        }
        return null
    }

    fun write(file: String, text: String) {
        runCatching {
            val p = Path.of(file)
            Files.createDirectories(p.parent)
            Files.writeString(p, text)
        }
    }
}
