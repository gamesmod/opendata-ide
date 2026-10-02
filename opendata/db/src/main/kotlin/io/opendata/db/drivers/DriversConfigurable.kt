package io.opendata.db.drivers

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import io.opendata.db.model.DbKind
import java.io.File
import javax.swing.JComponent
import javax.swing.ListSelectionModel

/**
 * Менеджер драйверов (ТЗ 13): Settings → OpenData: драйверы. Для каждой СУБД — состояние, версия драйвера,
 * свои JAR, класс драйвера, шаблон JDBC URL и свойства по умолчанию; загрузка, удаление и проверка.
 */
class DriversConfigurable : Configurable {
    private val settings get() = DriverSettings.getInstance()
    private val edited = LinkedHashMap<DbKind, DriverOverride>()
    private var current: DbKind? = null

    private val list = JBList(DbKind.entries).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = SimpleListCellRenderer.create("") { k: DbKind -> k.displayName }
    }
    private val status = JBLabel()
    private val artifacts = JBLabel()
    private val version = JBTextField()
    private val jars = TextFieldWithBrowseButton().apply {
        addActionListener {
            val chosen = com.intellij.openapi.fileChooser.FileChooser.chooseFiles(
                FileChooserDescriptorFactory.createMultipleFilesNoJarsDescriptor().withExtensionFilter("jar").withTitle("JAR драйвера"), null, null)
            if (chosen.isNotEmpty()) text = chosen.joinToString(File.pathSeparator) { it.path }
        }
    }
    private val driverClass = JBTextField()
    private val urlTemplate = JBTextField()
    private val properties = JBTextField()

    override fun getDisplayName(): String = "OpenData: драйверы"

    override fun createComponent(): JComponent {
        val form = panel {
            row("Состояние:") { cell(status).align(AlignX.FILL) }
            row("Файлы:") { cell(artifacts).align(AlignX.FILL) }
            separator()
            row("Версия драйвера:") { cell(version).align(AlignX.FILL).comment("Пусто — встроенная версия из Maven Central") }
            row("Свои JAR:") { cell(jars).align(AlignX.FILL).comment("Вместо загрузки из Maven Central") }
            row("Класс драйвера:") { cell(driverClass).align(AlignX.FILL) }
            row("Шаблон URL:") { cell(urlTemplate).align(AlignX.FILL).comment("{host}, {port}, {database}") }
            row("Свойства:") { cell(properties).align(AlignX.FILL).comment("Для всех подключений: ключ=значение; …") }
            separator()
            row {
                button("Загрузить") { run("Загрузка драйвера") { k, ind -> DriverService.getInstance().ensureDownloaded(k, ind).joinToString("\n") { it.fileName.toString() } } }
                button("Проверить") { run("Проверка драйвера") { k, ind -> DriverService.getInstance().probe(k, ind) } }
                button("Удалить загруженные") {
                    val k = current ?: return@button
                    apply()
                    val n = DriverService.getInstance().deleteDownloaded(k)
                    refreshStatus()
                    Messages.showInfoMessage(list, "Удалено файлов: $n", k.displayName)
                }
                button("Открыть папку") {
                    java.nio.file.Files.createDirectories(DriverService.getInstance().driversDir)
                    RevealFileAction.openDirectory(DriverService.getInstance().driversDir)
                }
            }
        }
        form.border = JBUI.Borders.empty(0, 10)
        list.addListSelectionListener { if (!it.valueIsAdjusting) select(list.selectedValue) }
        list.selectedIndex = 0
        return JBSplitter(false, 0.25f).apply {
            firstComponent = ScrollPaneFactory.createScrollPane(list)
            secondComponent = form
        }
    }

    private fun run(title: String, action: (DbKind, ProgressIndicator) -> String) {
        val k = current ?: return
        apply()
        var result: Result<String>? = null
        ProgressManager.getInstance().run(object : Task.Modal(null, "$title: ${k.displayName}", true) {
            override fun run(indicator: ProgressIndicator) {
                result = runCatching { action(k, indicator) }
            }
        })
        refreshStatus()
        result?.onSuccess { Messages.showInfoMessage(list, it, "$title: ${k.displayName}") }
            ?.onFailure { Messages.showErrorDialog(list, it.message ?: it.toString(), "$title: ${k.displayName}") }
    }

    private fun select(kind: DbKind?) {
        current?.let { edited[it] = readForm(it) }
        current = kind ?: return
        val o = edited.getOrPut(kind) { settings.get(kind) }
        val defaults = kind.driver.jars.first()
        version.text = o.version; version.emptyText.text = defaults.version
        jars.text = o.jars
        driverClass.text = o.driverClass; driverClass.emptyText.text = kind.driverClass
        urlTemplate.text = o.urlTemplate; urlTemplate.emptyText.text = kind.urlTemplate
        properties.text = o.properties.entries.joinToString("; ") { "${it.key}=${it.value}" }
        properties.emptyText.text = kind.defaultProperties.entries.joinToString("; ") { "${it.key}=${it.value}" }.ifEmpty { "—" }
        refreshStatus()
    }

    private fun refreshStatus() {
        val k = current ?: return
        val s = DriverService.getInstance().status(k)
        status.text = (if (s.isReady) "✔ " else "⚠ ") + s.summary
        // Имена файлов и источник; полные пути — во всплывающей подсказке.
        artifacts.text = "<html>" + (s.customJars?.joinToString("<br>") { it.fileName.toString() }
            ?: s.jars.joinToString("<br>") { j -> "${j.jar.fileName} — ${j.origin?.title ?: "нет"}" }) + "</html>"
        artifacts.toolTipText = "<html>" + (s.customJars?.joinToString("<br>") { it.toString() }
            ?: s.jars.joinToString("<br>") { j -> "${j.jar.group}:${j.jar.artifact}:${j.jar.version}<br>&nbsp;&nbsp;${j.path ?: j.jar.url()}" }) + "</html>"
    }

    private fun readForm(kind: DbKind) = DriverOverride().also { o ->
        o.kind = kind
        o.version = version.text.trim()
        o.jars = jars.text.trim()
        o.driverClass = driverClass.text.trim()
        o.urlTemplate = urlTemplate.text.trim()
        o.properties = parseProperties(properties.text)
    }

    private fun snapshot(): Map<DbKind, DriverOverride> {
        current?.let { edited[it] = readForm(it) }
        return edited
    }

    override fun isModified(): Boolean = snapshot().any { (k, o) -> o != settings.get(k) }

    override fun apply() {
        snapshot().values.forEach { settings.set(it) }
        refreshStatus()
    }

    override fun reset() {
        edited.clear()
        current?.let { k -> current = null; select(k) }
    }

    companion object {
        fun parseProperties(text: String): MutableMap<String, String> = text.split(';').mapNotNull { p ->
            val i = p.indexOf('=')
            if (i <= 0) null else p.substring(0, i).trim() to p.substring(i + 1).trim()
        }.toMap(LinkedHashMap())

        fun show(project: Project?) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, DriversConfigurable::class.java)
        }
    }
}

/** SQL → Драйверы… и кнопка в окне Database. */
class ManageDriversAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) = DriversConfigurable.show(e.project)
}
