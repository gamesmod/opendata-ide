package io.opendata.db.ui

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.application.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

/**
 * OpenData IDE работает без понятия «проект»: вместо Welcome-экрана сразу открывается рабочая папка
 * (~/OpenData или -Dopendata.workspace), где лежат пользовательские SQL-файлы.
 */
object Workspace {
    val path: Path
        get() = System.getProperty("opendata.workspace")?.let { Path.of(it) }
            ?: Path.of(System.getProperty("user.home"), "OpenData")

    fun open() {
        Files.createDirectories(path)
        // Рабочая папка создаётся самой IDE и содержит только SQL-файлы — помечаем её доверенной, без диалога Trust Project.
        com.intellij.ide.trustedProjects.TrustedProjects.setProjectTrusted(path, true)
        ProjectUtil.openOrImport(path, null, false)
    }
}

class WorkspaceLifecycleListener : AppLifecycleListener {
    override fun welcomeScreenDisplayed() {
        if (System.getProperty("opendata.no.workspace") != null) return
        ApplicationManager.getApplication().invokeLater { Workspace.open() }
    }
}

/** При открытии рабочей папки показывает Database Explorer. */
class WorkspaceStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        withContext(Dispatchers.EDT) {
            ToolWindowManager.getInstance(project).getToolWindow(DatabaseToolWindowFactory.ID)?.show()
        }
        diagnostics(project)
    }

    /** Отчёт для автоматической проверки запуска (scripts/run.* --smoke): -Dopendata.diagnostics.file. */
    private suspend fun diagnostics(project: Project) {
        val file = System.getProperty("opendata.diagnostics.file")?.takeIf { it.isNotBlank() } ?: return
        val report = StringBuilder()
        withContext(Dispatchers.EDT) {
            val ids = ToolWindowManager.getInstance(project).toolWindowIds.toList()
            report.appendLine("product=${com.intellij.openapi.application.ApplicationNamesInfo.getInstance().fullProductName}")
            report.appendLine("build=${com.intellij.openapi.application.ApplicationInfo.getInstance().build.asString()}")
            report.appendLine("config=${PathManager.getConfigPath()}")
            report.appendLine("toolWindows=${ids.sorted().joinToString(",")}")
            report.appendLine("plugins=" + com.intellij.ide.plugins.PluginManagerCore.loadedPlugins.map { it.pluginId.idString }.sorted().joinToString(","))
            report.appendLine("RESULT=" + if (DatabaseToolWindowFactory.ID in ids && ResultsToolWindowFactory.ID in ids) "OK" else "FAIL")
        }
        System.getProperty("opendata.smoke")?.takeIf { it.isNotBlank() }?.let { dsId ->
            runCatching { SmokeTest.run(project, dsId, report) }.onFailure { report.appendLine("SMOKE=FAIL ${it}") }
        }
        SmokeTest.write(file, report.toString())
    }
}
