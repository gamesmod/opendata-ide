package io.opendata.db.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_TINY
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import io.opendata.db.drivers.DriverService
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.model.DbKind
import io.opendata.db.session.DbSession
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/** Add / Edit Data Source (ТЗ 11): host, port, database, user, password, URL, драйвер, свойства. */
class DataSourceDialog(private val project: Project?, initial: DataSourceConfig) : DialogWrapper(project) {
    val config: DataSourceConfig = initial.copy()

    private val kindBox = com.intellij.openapi.ui.ComboBox(DefaultComboBoxModel(DbKind.entries.toTypedArray())).apply {
        renderer = com.intellij.ui.SimpleListCellRenderer.create("") { k: DbKind -> k.displayName }
        selectedItem = config.kind
    }
    private val name = JBTextField(config.name)
    private val host = JBTextField(config.host)
    private val port = JBTextField(config.port.toString())
    private val database = JBTextField(config.database)
    private val user = JBTextField(config.user)
    private val password = JBPasswordField().apply { text = DataSourceStorage.getInstance().getPassword(config.id).orEmpty() }
    private val url = JBTextField(config.url).apply { emptyText.text = config.effectiveUrl() }
    private val driverJar = TextFieldWithBrowseButton().apply {
        text = config.driverJar
        addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFileDescriptor("jar").withTitle("JAR драйвера"))
    }
    private val properties = JBTextField(config.properties.entries.joinToString("; ") { "${it.key}=${it.value}" }).apply {
        emptyText.text = "ssl=true; sslmode=require; …"
    }

    init {
        title = if (DataSourceStorage.getInstance().find(config.id) == null) "Новый источник данных" else "Источник данных: ${config.name}"
        kindBox.addActionListener { onKindChanged() }
        listOf(host, port, database).forEach { it.document.addDocumentListener(simpleListener { refreshUrlHint() }) }
        init()
    }

    private fun simpleListener(f: () -> Unit) = object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) = f()
        override fun removeUpdate(e: DocumentEvent) = f()
        override fun changedUpdate(e: DocumentEvent) = f()
    }

    private fun onKindChanged() {
        val k = kindBox.selectedItem as DbKind
        val previous = config.kind
        if (port.text == previous.defaultPort.toString() || port.text.isBlank()) port.text = k.defaultPort.toString()
        if (database.text == previous.defaultDatabase || database.text.isBlank()) database.text = k.defaultDatabase
        config.kind = k
        refreshUrlHint()
    }

    private fun refreshUrlHint() {
        val k = kindBox.selectedItem as DbKind
        url.emptyText.text = k.buildUrl(host.text.trim(), port.text.trim().toIntOrNull() ?: k.defaultPort, database.text.trim())
        url.repaint()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Тип:") { cell(kindBox) }
        row("Имя:") { cell(name).align(AlignX.FILL) }
        row("Хост:") {
            cell(host).align(AlignX.FILL).resizableColumn()
            label("Порт:")
            cell(port).columns(COLUMNS_TINY)
        }
        row("База данных:") { cell(database).align(AlignX.FILL) }
        row("Пользователь:") { cell(user).align(AlignX.FILL) }
        row("Пароль:") { cell(password).align(AlignX.FILL) }
        row("JDBC URL:") { cell(url).align(AlignX.FILL).comment("Пусто — URL собирается из полей выше") }
        row("Свойства:") { cell(properties).align(AlignX.FILL).comment("Свойства драйвера: ключ=значение через ';' (SSL и др.)") }
        row("JAR драйвера:") {
            cell(driverJar).align(AlignX.FILL).comment("Пусто — драйвер из комплекта / загрузка из Maven Central")
        }
        row {
            button("Test Connection") { testConnection() }
        }
    }

    private fun apply(target: DataSourceConfig) {
        target.kind = kindBox.selectedItem as DbKind
        target.name = name.text.trim().ifEmpty { "${target.kind.displayName} ${host.text.trim()}" }
        target.host = host.text.trim()
        target.port = port.text.trim().toIntOrNull() ?: target.kind.defaultPort
        target.database = database.text.trim()
        target.user = user.text.trim()
        target.url = url.text.trim()
        target.driverJar = driverJar.text.trim()
        target.properties = properties.text.split(';').mapNotNull { p ->
            val i = p.indexOf('=')
            if (i <= 0) null else p.substring(0, i).trim() to p.substring(i + 1).trim()
        }.toMap(LinkedHashMap())
    }

    override fun doValidate(): ValidationInfo? {
        if (port.text.trim().toIntOrNull() == null && url.text.isBlank()) return ValidationInfo("Порт должен быть числом", port)
        if (host.text.isBlank() && url.text.isBlank()) return ValidationInfo("Укажите хост или JDBC URL", host)
        return null
    }

    private fun testConnection() {
        val probe = config.copy()
        apply(probe)
        val pwd = String(password.password)
        var result: Result<String>? = null
        ProgressManager.getInstance().run(object : Task.Modal(project, "Test Connection", true) {
            override fun run(indicator: ProgressIndicator) {
                result = runCatching { DbSession.testConnection(probe, pwd, indicator) }
            }
        })
        result?.onSuccess { Messages.showInfoMessage(contentPanel, "Соединение установлено.\n\n$it", "Test Connection") }
            ?.onFailure { Messages.showErrorDialog(contentPanel, io.opendata.db.session.DbError.from(it).render(), "Test Connection") }
    }

    override fun doOKAction() {
        apply(config)
        DataSourceStorage.getInstance().setPassword(config.id, String(password.password).ifEmpty { null })
        super.doOKAction()
    }

    override fun getPreferredFocusedComponent(): JComponent = if (name.text.isEmpty()) name else host

    companion object {
        /** Показывает диалог; при OK сохраняет источник и возвращает его. */
        fun edit(project: Project?, initial: DataSourceConfig): DataSourceConfig? {
            val d = DataSourceDialog(project, initial)
            if (!d.showAndGet()) return null
            DataSourceStorage.getInstance().addOrUpdate(d.config)
            return d.config
        }

        @Suppress("unused")
        fun driverState(cfg: DataSourceConfig): String =
            if (cfg.driverJar.isNotBlank() || DriverService.getInstance().isAvailable(cfg.kind.driver)) "драйвер установлен" else "драйвер будет загружен"
    }
}
