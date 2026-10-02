package io.opendata.db.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotifications
import com.intellij.util.concurrency.AppExecutorUtil
import io.opendata.db.history.HistoryEntry
import io.opendata.db.history.QueryHistory
import io.opendata.db.meta.MetadataCache
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.session.DbSession
import io.opendata.db.session.DbSessions
import io.opendata.db.session.SqlChunk
import io.opendata.db.session.SqlSplitter
import io.opendata.db.session.StatementResult
import java.util.concurrent.TimeUnit

/** Выполнение SQL из консоли (ТЗ 14, 17, 24–27). */
object ConsoleRunner {
    private val ERROR_HIGHLIGHTER = Key.create<RangeHighlighter>("opendata.error.highlighter")

    enum class Mode { CURRENT, SCRIPT }

    fun sessionKey(file: VirtualFile) = "console:${file.url}"

    fun session(file: VirtualFile): DbSession? {
        val dsId = ConsoleFiles.dataSourceIdOf(file) ?: return null
        DataSourceStorage.getInstance().find(dsId) ?: return null
        return DbSessions.getInstance().get(sessionKey(file), dsId)
    }

    /** Выражения для выполнения: выделение (может содержать несколько выражений), выражение под кареткой или весь скрипт. */
    fun chunks(editor: Editor, mode: Mode): List<SqlChunk> {
        val text = editor.document.text
        val sel = editor.selectionModel
        if (sel.hasSelection()) {
            val base = sel.selectionStart
            return SqlSplitter.split(text.substring(sel.selectionStart, sel.selectionEnd)).map { it.copy(start = it.start + base, end = it.end + base) }
        }
        return when (mode) {
            Mode.SCRIPT -> SqlSplitter.split(text)
            Mode.CURRENT -> listOfNotNull(SqlSplitter.statementAt(text, editor.caretModel.offset))
        }
    }

    fun execute(project: Project, editor: Editor, file: VirtualFile, mode: Mode) {
        val session = session(file) ?: return
        val chunks = chunks(editor, mode)
        if (chunks.isEmpty()) return
        FileDocumentManager.getInstance().saveDocument(editor.document)
        clearError(editor)
        val ds = session.config
        val panel = ResultsView.getInstance(project).panel(sessionKey(file), "${ds.name}: ${file.nameWithoutExtension}")
        ResultsView.getInstance(project).activate()
        MetadataCache.getInstance().prefetch(ds.id)

        object : Task.Backgroundable(project, "Выполнение SQL: ${ds.name}", true) {
            override fun run(indicator: ProgressIndicator) {
                // Отмена из индикатора прогресса → Statement.cancel() (ТЗ 25).
                val watcher = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
                    { if (indicator.isCanceled) session.cancel() }, 200, 200, TimeUnit.MILLISECONDS)
                try {
                    val all = ArrayList<StatementResult>()
                    for (chunk in chunks) {
                        if (indicator.isCanceled) break
                        indicator.text = chunk.text.lineSequence().first().take(120)
                        val results = session.execute(chunk.text, indicator = indicator)
                        all += results
                        record(ds.name, ds.database, results)
                        val failure = results.filterIsInstance<StatementResult.Failure>().firstOrNull()
                        ApplicationManager.getApplication().invokeLater {
                            results.forEach { panel.log(describe(it)) }
                            if (failure != null) highlightError(editor, chunk, failure)
                        }
                        if (failure != null) break
                    }
                    ApplicationManager.getApplication().invokeLater {
                        if (!project.isDisposed) {
                            if (all.any { it is StatementResult.Rows }) panel.showResults(project, all) else panel.showOutput()
                            EditorNotifications.getInstance(project).updateNotifications(file)
                        }
                    }
                } finally {
                    watcher.cancel(false)
                }
            }
        }.queue()
    }

    fun describe(r: StatementResult): String = when (r) {
        is StatementResult.Rows -> "${oneLine(r.sql)}\n    ${r.rows.size}${if (r.truncated) "+" else ""} row(s) fetched in ${r.durationMs} ms"
        is StatementResult.Update -> "${oneLine(r.sql)}\n    ${r.count} row(s) affected in ${r.durationMs} ms"
        is StatementResult.Failure -> "${oneLine(r.sql)}\n" + r.error.render().prependIndent("    ")
    }

    private fun oneLine(sql: String) = sql.replace(Regex("\\s+"), " ").take(200)

    private fun record(dsName: String, database: String, results: List<StatementResult>) {
        results.forEach { r ->
            QueryHistory.getInstance().add(HistoryEntry().apply {
                timestamp = System.currentTimeMillis()
                dataSource = dsName
                this.database = database
                sql = r.sql
                durationMs = r.durationMs
                rows = when (r) { is StatementResult.Rows -> r.rows.size.toLong(); is StatementResult.Update -> r.count; else -> -1 }
                status = if (r is StatementResult.Failure) "ERROR ${r.error.sqlState ?: ""}".trim() else "OK"
            })
        }
    }

    /** Подсветка позиции ошибки в редакторе (ТЗ 26). */
    private fun highlightError(editor: Editor, chunk: SqlChunk, failure: StatementResult.Failure) {
        if (editor.isDisposed) return
        val pos = failure.error.position ?: return
        val doc = editor.document
        val start = (chunk.start + pos - 1).coerceIn(0, doc.textLength)
        var end = start
        val text = doc.charsSequence
        while (end < doc.textLength && end < chunk.end && !text[end].isWhitespace() && text[end] != ';') end++
        if (end == start) end = minOf(start + 1, doc.textLength)
        val attrs = EditorColorsManager.getInstance().globalScheme.getAttributes(CodeInsightColors.ERRORS_ATTRIBUTES)
        val h = editor.markupModel.addRangeHighlighter(start, end, HighlighterLayer.ERROR, attrs, HighlighterTargetArea.EXACT_RANGE)
        h.errorStripeTooltip = failure.error.message
        editor.putUserData(ERROR_HIGHLIGHTER, h)
        editor.caretModel.moveToOffset(start)
    }

    fun clearError(editor: Editor) {
        editor.getUserData(ERROR_HIGHLIGHTER)?.let { editor.markupModel.removeHighlighter(it) }
        editor.putUserData(ERROR_HIGHLIGHTER, null)
    }
}
