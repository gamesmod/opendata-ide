package io.opendata.db.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import io.opendata.db.history.QueryHistory
import io.opendata.db.lang.SqlFileType
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.model.DataSources
import java.text.SimpleDateFormat
import java.util.Date
import java.util.function.Function
import javax.swing.JComponent

/** Общая часть действий консоли: доступны в .sql-файле, привязанном к источнику данных. */
abstract class ConsoleAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = e.project != null && e.getData(CommonDataKeys.EDITOR) != null &&
            file != null && file.fileType == SqlFileType && ConsoleFiles.dataSourceIdOf(file) != null && isEnabled(file)
    }

    open fun isEnabled(file: VirtualFile): Boolean = true
}

/** Ctrl+Enter: выражение под кареткой или выделенный текст (ТЗ 14). */
class ExecuteStatementAction : ConsoleAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ConsoleRunner.execute(e.project ?: return, e.getData(CommonDataKeys.EDITOR) ?: return,
            e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return, ConsoleRunner.Mode.CURRENT)
    }
}

/** Выполнить весь скрипт. */
class ExecuteScriptAction : ConsoleAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ConsoleRunner.execute(e.project ?: return, e.getData(CommonDataKeys.EDITOR) ?: return,
            e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return, ConsoleRunner.Mode.SCRIPT)
    }
}

class CancelQueryAction : ConsoleAction() {
    override fun isEnabled(file: VirtualFile) = ConsoleRunner.session(file)?.isRunning == true
    override fun actionPerformed(e: AnActionEvent) {
        val session = ConsoleRunner.session(e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return) ?: return
        ApplicationManager.getApplication().executeOnPooledThread { session.cancel() }
    }
}

abstract class TxAction : ConsoleAction() {
    override fun isEnabled(file: VirtualFile): Boolean {
        val s = ConsoleRunner.session(file) ?: return false
        return s.config.kind.supportsTransactions && !s.autoCommit && s.isConnected
    }

    fun run(e: AnActionEvent, title: String, op: (io.opendata.db.session.DbSession) -> Unit) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val session = ConsoleRunner.session(file) ?: return
        ApplicationManager.getApplication().executeOnPooledThread {
            val error = runCatching { op(session) }.exceptionOrNull()
            ApplicationManager.getApplication().invokeLater {
                val panel = ResultsView.getInstance(project).panel(ConsoleRunner.sessionKey(file), "${session.config.name}: ${file.nameWithoutExtension}")
                panel.log(if (error == null) title else "$title: ${error.message}")
            }
        }
    }
}

class CommitAction : TxAction() {
    override fun actionPerformed(e: AnActionEvent) = run(e, "COMMIT") { it.commit() }
}

class RollbackAction : TxAction() {
    override fun actionPerformed(e: AnActionEvent) = run(e, "ROLLBACK") { it.rollback() }
}

/** Переключение Auto-commit / Manual (ТЗ 14, 19). */
class ToggleAutoCommitAction : ConsoleAction() {
    override fun isEnabled(file: VirtualFile) = ConsoleRunner.session(file)?.config?.kind?.supportsTransactions == true

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val session = ConsoleRunner.session(file) ?: return
        val target = !session.autoCommit
        if (target && session.isConnected) {
            // Выход из ручного режима: незафиксированная транзакция фиксируется (как в JDBC setAutoCommit(true)).
            if (Messages.showYesNoDialog(project, "Включить Auto-commit? Текущая транзакция будет зафиксирована.", "Auto-commit", null) != Messages.YES) return
        }
        ApplicationManager.getApplication().executeOnPooledThread {
            session.autoCommit = target
            ApplicationManager.getApplication().invokeLater { EditorNotifications.getInstance(project).updateNotifications(file) }
        }
    }
}

/** Выбор источника данных для текущего .sql-файла. */
class ChooseDataSourceAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.fileType == SqlFileType
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        choose(project, file)
    }

    companion object {
        fun choose(project: Project, file: VirtualFile) {
            val list = DataSourceStorage.getInstance(project).dataSources
            if (list.isEmpty()) {
                Messages.showInfoMessage(project, "Сначала создайте источник данных в окне Database.", "OpenData")
                return
            }
            JBPopupFactory.getInstance().createPopupChooserBuilder(list)
                .setTitle("Источник данных")
                .setRenderer(com.intellij.ui.SimpleListCellRenderer.create("") { ds: DataSourceConfig -> "${ds.name} (${ds.kind.displayName})" })
                .setItemChosenCallback { ds ->
                    ConsoleFiles.bind(file, ds.id)
                    ConsoleRunner.session(file)
                    EditorNotifications.getInstance(project).updateNotifications(file)
                }
                .createPopup().showCenteredInCurrentWindow(project)
        }
    }
}

/** История запросов (ТЗ 27): выбор вставляет SQL в консоль. */
class QueryHistoryAction : ConsoleAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        show(project, editor)
    }

    companion object {
        fun show(project: Project, editor: com.intellij.openapi.editor.Editor) {
            val entries = QueryHistory.getInstance().entries
            if (entries.isEmpty()) {
                Messages.showInfoMessage(project, "История запросов пуста.", "History")
                return
            }
            val fmt = SimpleDateFormat("dd.MM HH:mm:ss")
            JBPopupFactory.getInstance().createPopupChooserBuilder(entries)
                .setTitle("История запросов")
                .setRenderer(com.intellij.ui.SimpleListCellRenderer.create("") { h: io.opendata.db.history.HistoryEntry ->
                    "${fmt.format(Date(h.timestamp))}  ${h.dataSource}  ${h.status}  ${h.durationMs} ms  " +
                        (if (h.rows >= 0) "${h.rows} rows  " else "") + h.sql.replace(Regex("\\s+"), " ").take(100)
                })
                .setNamerForFiltering { it.sql }
                .setItemChosenCallback { h ->
                    WriteCommandAction.runWriteCommandAction(project) {
                        val offset = editor.caretModel.offset
                        editor.document.insertString(offset, h.sql.trimEnd().removeSuffix(";") + ";\n")
                    }
                }
                .createPopup().showInBestPositionFor(editor)
        }
    }
}

/**
 * Полоса над консолью: источник данных, режим транзакций и основные действия.
 * Простой текст и ссылки — без отдельной панели инструментов.
 */
class ConsoleNotificationProvider : EditorNotificationProvider, DumbAware {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.fileType != SqlFileType) return null
        return Function { fileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
            val dsId = ConsoleFiles.dataSourceIdOf(file)
            val ds = dsId?.let { DataSources.find(it) }
            if (ds == null) {
                panel.text = "Источник данных не выбран"
                panel.createActionLabel("Выбрать…") { ChooseDataSourceAction.choose(project, file) }
                return@Function panel
            }
            val session = ConsoleRunner.session(file)
            val tx = when {
                !ds.kind.supportsTransactions -> "без транзакций"
                session?.autoCommit != false -> "Auto-commit"
                else -> "Manual (транзакция)"
            }
            panel.text = "${ds.name} · ${ds.kind.displayName} · $tx"
            panel.createActionLabel("Execute (Ctrl+Enter)", "OpenData.Console.Execute")
            panel.createActionLabel("Cancel", "OpenData.Console.Cancel")
            if (ds.kind.supportsTransactions) {
                panel.createActionLabel(if (session?.autoCommit != false) "Manual" else "Auto-commit", "OpenData.Console.ToggleAutoCommit")
                panel.createActionLabel("Commit", "OpenData.Console.Commit")
                panel.createActionLabel("Rollback", "OpenData.Console.Rollback")
            }
            panel.createActionLabel("History", "OpenData.Console.History")
            panel.createActionLabel("Источник…") { ChooseDataSourceAction.choose(project, file) }
            panel
        }
    }
}

object Consoles {
    fun open(project: Project, ds: DataSourceConfig, newConsole: Boolean = false) {
        val file = if (newConsole) ConsoleFiles.nextConsoleFile(project, ds) else ConsoleFiles.consoleFile(project, ds)
        FileEditorManager.getInstance(project).openFile(file, true)
        io.opendata.db.meta.MetadataCache.getInstance().prefetch(ds.id)
    }
}

/**
 * Ctrl+Enter в SQL-консоли и редакторе таблицы конфликтует со штатным «Split Line» (а в некоторых раскладках
 * replace-all не снимает его привязку). Промоутер ставит действия OpenData первыми — если они доступны в контексте.
 */
class ConsoleActionPromoter : com.intellij.openapi.actionSystem.ActionPromoter {
    override fun promote(actions: List<AnAction>, context: com.intellij.openapi.actionSystem.DataContext): List<AnAction> =
        actions.filter { it is ExecuteStatementAction || it is ExecuteScriptAction || it is SubmitTableChangesAction || it is CommitAction }
}
