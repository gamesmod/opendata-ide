package io.opendata.integration

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

/**
 * При открытии проекта проверяет интеграцию с Database Tools и, если задано системное свойство
 * `opendata.diagnostics.file`, пишет отчёт в файл (используется scripts/run.ps1 -Poc).
 */
class OpenDataStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Список tool windows читаем на EDT: менеджер окон инициализируется в UI-потоке.
        val report = withContext(Dispatchers.EDT) { OpenDataDiagnostics.collect(project, includeToolWindows = true) }
        thisLogger().info("OpenData diagnostics:\n" + report.render())

        System.getProperty("opendata.diagnostics.file")?.takeIf { it.isNotBlank() }?.let { file ->
            runCatching {
                val path = Path.of(file)
                Files.createDirectories(path.parent)
                Files.writeString(path, report.render())
            }.onFailure { thisLogger().warn("Cannot write diagnostics to $file", it) }
        }

        if (!report.ok) {
            NotificationGroupManager.getInstance().getNotificationGroup("OpenData")
                .createNotification(
                    "OpenData: интеграция с Database Tools неполная",
                    report.checks.filterNot { it.ok }.joinToString("<br>") { "${it.name}: ${it.details}" },
                    NotificationType.WARNING,
                )
                .notify(project)
        }
    }
}
