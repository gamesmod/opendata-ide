package io.opendata.integration

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/** Tools → OpenData: Integration Diagnostics. */
class OpenDataDiagnosticsAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val report = OpenDataDiagnostics.collect(e.project, includeToolWindows = true)
        val text = report.render()
        if (report.ok) Messages.showInfoMessage(e.project, text, "OpenData Diagnostics")
        else Messages.showWarningDialog(e.project, text, "OpenData Diagnostics")
    }
}
