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
import io.opendata.db.model.DataSources
import io.opendata.db.model.DbKind
import io.opendata.db.session.DbSession
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/** Add / Edit Data Source (ТЗ 11): host, port, database, user, password, URL, драйвер, свойства. */
class DataSourceDialog(private val project: Project, initial: DataSourceConfig) : DialogWrapper(project) {
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
    private val password = JBPasswordField().apply { text = DataSources.getPassword(config.id).orEmpty() }
    /** URL всегда показывает фактический адрес: собранный из полей или заданный вручную (с параметрами). */
    private val url = JBTextField(config.effectiveUrl())
    /** Идёт программное обновление полей — слушатели не должны реагировать. */
    private var syncing = false
    private val driverJar = TextFieldWithBrowseButton().apply {
        text = config.driverJar
        addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFileDescriptor("jar").withTitle("JAR драйвера"))
    }
    private val properties = JBTextField(config.properties.entries.joinToString("; ") { "${it.key}=${it.value}" }).apply {
        emptyText.text = "ssl=true; sslmode=require; …"
    }

    private val driverStatus = com.intellij.ui.components.JBLabel()

    private fun refreshDriverStatus() {
        val k = kindBox.selectedItem as DbKind
        driverStatus.text = DriverService.getInstance().status(k).summary
    }

    init {
        refreshDriverStatus()
        title = if (DataSourceStorage.getInstance(project).find(config.id) == null) "Новый источник данных" else "Источник данных: ${config.name}"
        kindBox.addActionListener { onKindChanged() }
        listOf(host, port, database).forEach { it.document.addDocumentListener(simpleListener { if (!syncing) fieldsChanged() }) }
        url.document.addDocumentListener(simpleListener { if (!syncing) urlChanged() })
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
        if (!syncing) fieldsChanged()
        refreshDriverStatus()
    }

    private fun builtUrl(): String {
        val k = kindBox.selectedItem as DbKind
        return io.opendata.db.drivers.DriverSettings.getInstance().buildUrl(k, host.text.trim(), port.text.trim().toIntOrNull() ?: k.defaultPort, database.text.trim())
    }

    /** URL задан вручную, если он отличается от собранного из полей (например, содержит параметры). */
    private val isCustomUrl: Boolean get() = url.text.trim().isNotEmpty() && url.text.trim() != builtUrl()

    /** Поля → URL: пересобираем URL, если он не задан вручную с параметрами. */
    private fun fieldsChanged() {
        val p = io.opendata.db.model.JdbcUrls.parse(url.text)
        if (url.text.isBlank() || p.params == null) sync { url.text = builtUrl() }
    }

    /** URL → поля: тип СУБД по префиксу, хост, порт и база из URL. */
    private fun urlChanged() {
        val p = io.opendata.db.model.JdbcUrls.parse(url.text)
        sync {
            val current = kindBox.selectedItem as DbKind
            if (p.kind != null && !io.opendata.db.model.JdbcUrls.matches(current, url.text)) {
                kindBox.selectedItem = p.kind
                config.kind = p.kind
                refreshDriverStatus()
            }
            p.host?.let { host.text = it }
            p.port?.let { port.text = it.toString() }
            p.database?.let { database.text = it }
        }
    }

    private inline fun sync(f: () -> Unit) {
        syncing = true
        try { f() } finally { syncing = false }
    }

    private fun refreshUrlHint() = fieldsChanged()

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
        row("JDBC URL:") { cell(url).align(AlignX.FILL).comment("Синхронизируется с полями выше; можно вставить готовый URL с параметрами") }
        row("Свойства:") { cell(properties).align(AlignX.FILL).comment("Свойства драйвера: ключ=значение через ';' (SSL и др.)") }
        row("JAR драйвера:") {
            cell(driverJar).align(AlignX.FILL).comment("Пусто — драйвер из менеджера драйверов")
        }
        row("Драйвер:") {
            cell(driverStatus)
            link("Менеджер драйверов…") {
                io.opendata.db.drivers.DriversConfigurable.show(project)
                refreshDriverStatus()
                refreshUrlHint()
            }
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
        // URL, совпадающий с собранным из полей, не храним: при смене полей он пересоберётся сам.
        target.url = if (isCustomUrl) url.text.trim() else ""
        target.driverJar = driverJar.text.trim()
        target.properties = properties.text.split(';').mapNotNull { p ->
            val i = p.indexOf('=')
            if (i <= 0) null else p.substring(0, i).trim() to p.substring(i + 1).trim()
        }.toMap(LinkedHashMap())
    }

    override fun doValidate(): ValidationInfo? {
        if (port.text.trim().toIntOrNull() == null && url.text.isBlank()) return ValidationInfo("Порт должен быть числом", port)
        if (host.text.isBlank() && url.text.isBlank()) return ValidationInfo("Укажите хост или JDBC URL", host)
        val k = kindBox.selectedItem as DbKind
        if (url.text.isNotBlank() && !url.text.trim().startsWith("jdbc:")) return ValidationInfo("JDBC URL должен начинаться с jdbc:", url)
        if (!io.opendata.db.model.JdbcUrls.matches(k, url.text)) return ValidationInfo("URL не подходит для типа ${k.displayName}", url)
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
        DataSources.setPassword(config.id, String(password.password).ifEmpty { null })
        super.doOKAction()
    }

    override fun getPreferredFocusedComponent(): JComponent = if (name.text.isEmpty()) name else host

    companion object {
        /** Показывает диалог; при OK сохраняет источник и возвращает его. */
        fun edit(project: Project, initial: DataSourceConfig): DataSourceConfig? {
            val d = DataSourceDialog(project, initial)
            if (!d.showAndGet()) return null
            DataSourceStorage.getInstance(project).addOrUpdate(d.config)
            return d.config
        }

        @Suppress("unused")
        fun driverState(cfg: DataSourceConfig): String =
            if (cfg.driverJar.isNotBlank() || DriverService.getInstance().isAvailable(cfg.kind)) "драйвер установлен" else "драйвер будет загружен"
    }
}
