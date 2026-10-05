package io.opendata.db.bulk

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_TINY
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import io.opendata.db.drivers.DriverService
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSources
import io.opendata.db.session.DbError
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

private fun notify(project: Project?, title: String, text: String, type: NotificationType = NotificationType.INFORMATION) =
    NotificationGroupManager.getInstance().getNotificationGroup("OpenData").createNotification(title, text, type).notify(project)

/** Параметры bulk-загрузки/выгрузки (общие поля). */
class BulkDialog(project: Project, private val ds: DataSourceConfig, private val table: DbObject, private val unload: Boolean) : DialogWrapper(project) {
    private val file = TextFieldWithBrowseButton()
    private val method = ComboBox(DefaultComboBoxModel(BulkMethod.entries.toTypedArray())).apply {
        selectedItem = if (ds.kind.isMpp) BulkMethod.GPFDIST else BulkMethod.COPY
        isEnabled = ds.kind.isMpp && !unload
    }
    private val format = ComboBox(DefaultComboBoxModel(BulkFormat.entries.toTypedArray()))
    private val delimiters = linkedMapOf("Запятая ," to ',', "Точка с запятой ;" to ';', "Табуляция" to '\t', "Вертикальная черта |" to '|')
    private val delimiter = ComboBox(DefaultComboBoxModel(delimiters.keys.toTypedArray()))
    private val header = JBCheckBox("Первая строка — заголовок (имена колонок)", true)
    private val nullString = JBTextField().apply { emptyText.text = "пусто" }
    private val encoding = ComboBox(DefaultComboBoxModel(arrayOf("UTF8", "WIN1251", "LATIN1", "KOI8R"))).apply { isEditable = true }
    private val rejectLimit = JBTextField("0")
    private val truncate = JBCheckBox("Очистить таблицу перед загрузкой (TRUNCATE)")
    private val host = JBTextField().apply { emptyText.text = "авто — адрес этого компьютера, видимый кластером" }
    private val port = JBTextField("0")

    init {
        title = (if (unload) "Bulk-выгрузка: " else "Bulk-загрузка: ") + table.name
        val base = project.basePath ?: System.getProperty("user.home")
        if (unload) {
            file.text = Path.of(base, "${table.name}.csv").toString()
            file.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Каталог выгрузки"))
        } else {
            file.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFileDescriptor().withExtensionFilter("CSV/TSV/TXT", "csv", "tsv", "txt").withTitle("Файл для загрузки"))
        }
        format.addActionListener {
            val csv = format.item == BulkFormat.CSV
            nullString.emptyText.text = if (csv) "пусто" else "\\N"
            if (!csv && delimiter.item == "Запятая ,") delimiter.item = "Табуляция"
        }
        init()
    }

    val path: Path get() = Path.of(file.text.trim()).let { if (unload && Files.isDirectory(it)) it.resolve("${table.name}.csv") else it }
    val selectedMethod: BulkMethod get() = method.item
    val gpfdistHost: String get() = host.text.trim()
    val gpfdistPort: Int get() = port.text.trim().toIntOrNull() ?: 0
    val options: BulkOptions
        get() = BulkOptions(
            format = format.item,
            delimiter = delimiters.getValue(delimiter.item),
            header = header.isSelected,
            nullString = nullString.text.ifEmpty { if (format.item == BulkFormat.CSV) "" else "\\N" },
            encoding = (encoding.selectedItem as? String ?: "UTF8").trim(),
            rejectLimit = rejectLimit.text.trim().toIntOrNull() ?: 0,
            truncate = truncate.isSelected,
        )

    override fun createCenterPanel(): JComponent = panel {
        row(if (unload) "Файл:" else "Файл:") { cell(file).align(AlignX.FILL) }
        if (!unload) row("Способ:") { cell(method) }
        row("Формат:") { cell(format); label("Разделитель:"); cell(delimiter) }
        row { cell(header) }
        row("NULL:") { cell(nullString).columns(COLUMNS_TINY); label("Кодировка файла:"); cell(encoding) }
        if (!unload && ds.kind.isMpp) row("Допустимо ошибочных строк:") {
            cell(rejectLimit).columns(COLUMNS_TINY).comment("0 — любая ошибка отменяет загрузку; иначе LOG ERRORS SEGMENT REJECT LIMIT")
        }
        if (!unload) row { cell(truncate) }
        if (ds.kind.isMpp) {
            group("gpfdist (встроенный, без установки gpfdist.exe)") {
                row("Адрес для сегментов:") { cell(host).align(AlignX.FILL) }
                row("Порт:") { cell(port).columns(COLUMNS_TINY).comment("0 — свободный порт. Порт должен быть доступен с сегментов (брандмауэр Windows)") }
            }
        }
    }

    override fun doValidate(): ValidationInfo? = when {
        file.text.isBlank() -> ValidationInfo("Укажите файл", file.textField)
        !unload && !Files.isRegularFile(runCatching { path }.getOrNull() ?: Path.of("")) -> ValidationInfo("Файл не найден", file.textField)
        port.text.trim().toIntOrNull()?.let { it !in 0..65535 } ?: true -> ValidationInfo("Порт 0–65535", port)
        rejectLimit.text.trim().toIntOrNull()?.let { it < 0 || it == 1 } ?: true -> ValidationInfo("0 или число ≥ 2 (Greenplum требует не меньше 2)", rejectLimit)
        else -> null
    }
}

object BulkUi {
    fun load(project: Project, ds: DataSourceConfig, table: DbObject) = run(project, ds, table, unload = false)
    fun unload(project: Project, ds: DataSourceConfig, table: DbObject) = run(project, ds, table, unload = true)

    private fun run(project: Project, ds: DataSourceConfig, table: DbObject, unload: Boolean) {
        val d = BulkDialog(project, ds, table, unload)
        if (!d.showAndGet()) return
        val file = d.path
        val o = d.options
        val title = (if (unload) "Bulk-выгрузка " else "Bulk-загрузка ") + table.name
        object : Task.Backgroundable(project, title, true) {
            private var result: BulkResult? = null
            override fun run(indicator: ProgressIndicator) {
                DriverService.getInstance().connect(ds, DataSources.getPassword(ds.id), indicator).use { c ->
                    val loader = MetadataLoader(c, ds.kind)
                    val qualified = loader.qualified(table)
                    val host = d.gpfdistHost.ifEmpty { BulkLoader.clientAddress(c) }
                    result = when {
                        unload -> BulkLoader.gpfdistUnload(c, ds.kind, qualified, loader::quote, file, o, host, d.gpfdistPort, indicator)
                        d.selectedMethod == BulkMethod.GPFDIST -> BulkLoader.gpfdistLoad(c, ds.kind, qualified, loader::quote, file, o, host, d.gpfdistPort, indicator = indicator)
                        else -> BulkLoader.copy(c, ds.kind, qualified, loader::quote, file, o, indicator)
                    }
                }
            }

            override fun onSuccess() {
                val r = result ?: return
                val rejected = if (r.rejected > 0) "\nОтклонено строк: ${r.rejected} (журнал: SELECT * FROM gp_read_error_log(…))" else ""
                notify(project, title, "${r.method.title}: ${r.rows} строк за ${r.millis / 1000.0} с$rejected" + (if (unload) "\n$file" else ""))
            }

            override fun onThrowable(error: Throwable) = notify(project, title, DbError.from(error).render(), NotificationType.ERROR)
        }.queue()
    }
}

/** Отдельный gpfdist-сервер каталога: для своих внешних таблиц LOCATION ('gpfdist://адрес:порт/файл'). */
@Service(Service.Level.APP)
class GpfdistService : Disposable {
    @Volatile var server: GpfdistServer? = null
        private set

    fun start(root: Path, port: Int): GpfdistServer {
        stop()
        return GpfdistServer(root, port).also { server = it }
    }

    fun stop() {
        server?.close()
        server = null
    }

    override fun dispose() = stop()

    companion object {
        fun getInstance(): GpfdistService = service()
    }
}

class GpfdistServerDialog(project: Project?) : DialogWrapper(project) {
    private val svc = GpfdistService.getInstance()
    private val dir = TextFieldWithBrowseButton().apply {
        text = svc.server?.root?.toString() ?: project?.basePath ?: System.getProperty("user.home")
        addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Каталог с файлами"))
    }
    private val port = JBTextField((svc.server?.port ?: 8080).toString())
    private val status = JBLabel()

    init {
        title = "gpfdist-сервер"
        setOKButtonText("Запустить")
        setCancelButtonText("Закрыть")
        refresh()
        init()
    }

    private fun refresh() {
        val s = svc.server
        status.text = if (s == null) "Остановлен" else
            "<html>Работает: порт ${s.port}, каталог ${s.root}<br>Пример: LOCATION ('gpfdist://${runCatching { java.net.InetAddress.getLocalHost().hostAddress }.getOrDefault("host")}:${s.port}/file.csv')" +
                "<br>Отдано ${s.bytesSent.get() / 1024} КБ, принято ${s.bytesReceived.get() / 1024} КБ, запросов ${s.requests.get()}</html>"
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Каталог:") { cell(dir).align(AlignX.FILL) }
        row("Порт:") { cell(port).columns(COLUMNS_TINY) }
        row { cell(status) }
        row {
            button("Остановить") { svc.stop(); refresh() }
            comment("Сервер работает, пока открыта IDE. Сегменты должны иметь доступ к порту (брандмауэр).")
        }
    }

    override fun doValidate(): ValidationInfo? = when {
        !Files.isDirectory(runCatching { Path.of(dir.text.trim()) }.getOrNull() ?: Path.of("-")) -> ValidationInfo("Каталог не найден", dir.textField)
        port.text.trim().toIntOrNull()?.let { it !in 0..65535 } ?: true -> ValidationInfo("Порт 0–65535", port)
        else -> null
    }

    override fun doOKAction() {
        try {
            val s = svc.start(Path.of(dir.text.trim()), port.text.trim().toInt())
            refresh()
            notify(null, "gpfdist-сервер", "Запущен на порту ${s.port}: ${s.root}")
        } catch (e: Exception) {
            setErrorText("Не удалось запустить: ${e.message}")
        }
    }
}

class GpfdistServerAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) {
        GpfdistServerDialog(e.project).show()
    }
}
