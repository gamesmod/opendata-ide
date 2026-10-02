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
 * Проекты OpenData IDE. Проект — это папка: подключения хранятся в её `.idea/opendata-datasources.xml`,
 * консоли — в `.idea/opendata/consoles`, рядом лежат пользовательские SQL-файлы.
 * При самом первом запуске (нет недавних проектов) открывается проект по умолчанию ~/OpenData;
 * дальше платформа сама открывает последний проект, а новые создаются действием «Новый проект…».
 */
object Workspace {
    /** Проект по умолчанию: -Dopendata.workspace или ~/OpenData. */
    val path: Path
        get() = System.getProperty("opendata.workspace")?.let { Path.of(it) }
            ?: Path.of(System.getProperty("user.home"), "OpenData")

    /** Каталог новых проектов по умолчанию — тот же, что у кнопки New Project на Welcome-экране. */
    val projectsHome: Path get() = Path.of(ProjectUtil.getBaseDir())

    fun open() = openProject(path)

    /** Создаёт папку (если нужно), помечает её доверенной и открывает как проект. */
    fun openProject(dir: Path): Project? {
        Files.createDirectories(dir)
        // Папку создаёт сама IDE по выбору пользователя — помечаем её доверенной, без диалога Trust Project.
        com.intellij.ide.trustedProjects.TrustedProjects.setProjectTrusted(dir, true)
        return ProjectUtil.openOrImport(dir, null, false)
    }
}

class WorkspaceLifecycleListener : AppLifecycleListener {
    override fun welcomeScreenDisplayed() {
        if (System.getProperty("opendata.no.workspace") != null) return
        // Явно заданный проект (-Dopendata.workspace, smoke/CI) или самый первый запуск — открываем проект по умолчанию.
        val firstRun = com.intellij.ide.RecentProjectsManagerBase.getInstanceEx().getRecentPaths().isEmpty()
        if (System.getProperty("opendata.workspace") == null && !firstRun) return
        ApplicationManager.getApplication().invokeLater { Workspace.open() }
    }
}

/** File → Новый проект…: имя и расположение папки проекта (на Welcome-экране — штатная кнопка New Project). */
class NewProjectAction : com.intellij.openapi.project.DumbAwareAction() {
    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT

    override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) {
        val dialog = NewProjectDialog(e.project)
        if (!dialog.showAndGet()) return
        val dir = dialog.location
        if (Files.isDirectory(dir) && Files.list(dir).use { it.findAny().isPresent } &&
            com.intellij.openapi.ui.Messages.showYesNoDialog(e.project, "Папка $dir не пуста. Открыть её как проект?", "Новый проект", null) != com.intellij.openapi.ui.Messages.YES
        ) return
        Workspace.openProject(dir)
    }
}

class NewProjectDialog(project: Project?) : com.intellij.openapi.ui.DialogWrapper(project) {
    private val name = com.intellij.ui.components.JBTextField(nextName())
    private val parent = com.intellij.openapi.ui.TextFieldWithBrowseButton().apply {
        text = Workspace.projectsHome.toString()
        addBrowseFolderListener(project, com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Расположение проекта"))
    }

    val location: Path get() = Path.of(parent.text.trim()).resolve(name.text.trim())

    init {
        title = "Новый проект"
        init()
    }

    private fun nextName(): String {
        var i = 1
        while (Files.exists(Workspace.projectsHome.resolve("project$i"))) i++
        return "project$i"
    }

    override fun createCenterPanel(): javax.swing.JComponent = com.intellij.ui.dsl.builder.panel {
        row("Имя:") { cell(name).align(com.intellij.ui.dsl.builder.AlignX.FILL).focused() }
        row("Расположение:") { cell(parent).align(com.intellij.ui.dsl.builder.AlignX.FILL) }
        row { comment("Подключения к базам данных и консоли хранятся в проекте (папка .idea).") }
    }

    override fun doValidate(): com.intellij.openapi.ui.ValidationInfo? = when {
        name.text.isBlank() -> com.intellij.openapi.ui.ValidationInfo("Укажите имя проекта", name)
        name.text.any { it in "\\/:*?\"<>|" } -> com.intellij.openapi.ui.ValidationInfo("Недопустимые символы в имени", name)
        parent.text.isBlank() -> com.intellij.openapi.ui.ValidationInfo("Укажите расположение", parent.textField)
        else -> null
    }
}

/**
 * Перенос подключений 0.2.0 (уровень приложения) в первый открытый проект: после обновления
 * пользователь видит свои подключения в проекте, а не теряет их.
 */
object LegacyDataSourcesMigration {
    /** true — подключения были перенесены. */
    fun run(project: Project): Boolean {
        val legacy = io.opendata.db.model.LegacyDataSourceStorage.getInstance().takeAll()
        if (legacy.isEmpty()) return false
        val storage = io.opendata.db.model.DataSourceStorage.getInstance(project)
        legacy.filter { storage.find(it.id) == null }.forEach { storage.addOrUpdate(it) }
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("OpenData")
            .createNotification("Подключения перенесены в проект", "${legacy.size} подключений из прежней версии теперь хранятся в проекте ${project.name}.",
                com.intellij.notification.NotificationType.INFORMATION)
            .notify(project)
        return true
    }
}

/** При открытии рабочей папки показывает Database Explorer. */
class WorkspaceStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (LegacyDataSourcesMigration.run(project)) {
            // Сразу записываем: подключения — в .idea проекта, прежний глобальный список — очищенным.
            com.intellij.configurationStore.saveSettings(project, false)
            com.intellij.configurationStore.saveSettings(ApplicationManager.getApplication(), false)
        }
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
