package io.opendata.db.data

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import io.opendata.db.drivers.DriverService
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSources
import io.opendata.db.model.DbKind
import io.opendata.db.session.DbError
import io.opendata.db.session.StatementResult
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

/** Экспорт и импорт данных из UI: диалоги, фоновые задачи, уведомления. */
object DataTransferUi {

    private fun notify(project: Project, title: String, text: String, type: NotificationType = NotificationType.INFORMATION, file: Path? = null) {
        val n = NotificationGroupManager.getInstance().getNotificationGroup("OpenData").createNotification(title, text, type)
        if (file != null) {
            n.addAction(NotificationAction.createSimpleExpiring("Открыть") {
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file)?.let { FileEditorManager.getInstance(project).openFile(it, true) }
            })
            n.addAction(NotificationAction.createSimple("Показать в папке") { RevealFileAction.openFile(file) })
        }
        n.notify(project)
    }

    private fun defaultDir(project: Project): Path =
        project.basePath?.let { Path.of(it) } ?: Path.of(System.getProperty("user.home"))

    /** Выгрузка всей таблицы или представления (потоково, без ограничения строк). */
    fun exportTable(project: Project, ds: DataSourceConfig, obj: DbObject) {
        val d = ExportDialog(project, defaultDir(project).resolve(obj.name), ds.kind, obj.name)
        if (!d.showAndGet()) return
        val file = d.file
        runExport(project, "Экспорт ${obj.name}", file) { indicator ->
            DriverService.getInstance().connect(ds, DataSources.getPassword(ds.id), indicator).use { c ->
                val loader = MetadataLoader(c, ds.kind)
                val opts = d.options.copy(tableName = d.options.tableName.ifBlank { loader.qualified(obj) })
                DataExporter.exportQuery(c, "SELECT * FROM ${loader.qualified(obj)}", file, opts, indicator)
            }
        }
    }

    /**
     * Экспорт результата консоли. Если результат был обрезан лимитом выборки и это запрос на чтение,
     * он выполняется заново и выгружается полностью; иначе выгружаются загруженные строки.
     */
    fun exportResult(project: Project, dataSourceId: String?, result: StatementResult.Rows) {
        val ds = dataSourceId?.let { DataSources.find(it) }
        val d = ExportDialog(project, defaultDir(project).resolve("result"), ds?.kind, "result")
        if (!d.showAndGet()) return
        val file = d.file
        val rerun = result.truncated && ds != null && isReadQuery(result.sql)
        runExport(project, "Экспорт результата", file) { indicator ->
            if (rerun) DriverService.getInstance().connect(ds!!, DataSources.getPassword(ds.id), indicator).use { c ->
                DataExporter.exportQuery(c, result.sql, file, d.options, indicator)
            } else DataExporter.exportRows(result.columns, result.rows, file, d.options)
        }
    }

    fun isReadQuery(sql: String): Boolean =
        Regex("^\\s*(--[^\\n]*\\n\\s*|/\\*.*?\\*/\\s*)*(select|with|show|values|table|explain|describe)\\b", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .containsMatchIn(sql)

    private fun runExport(project: Project, title: String, file: Path, work: (ProgressIndicator) -> Long) {
        object : Task.Backgroundable(project, title, true) {
            private var rows = 0L
            override fun run(indicator: ProgressIndicator) {
                rows = work(indicator)
            }
            override fun onSuccess() = notify(project, title, "Строк: $rows → $file", file = file)
            override fun onThrowable(error: Throwable) =
                notify(project, title, DbError.from(error).render(), NotificationType.ERROR)
        }.queue()
    }

    /** Импорт CSV/TSV в существующую таблицу или в новую таблицу контейнера (схемы/базы данных). */
    fun import(project: Project, ds: DataSourceConfig, table: DbObject?, container: DbObject?, onSuccess: () -> Unit = {}) {
        if (ds.kind == DbKind.DREMIO) {
            Messages.showInfoMessage(project, "Dremio подключён только для чтения: импорт не поддерживается.", "Импорт данных")
            return
        }
        val d = ImportDialog(project, table, defaultDir(project))
        if (!d.showAndGet()) return
        val file = d.file
        val opts = d.options
        object : Task.Backgroundable(project, "Импорт ${file.fileName}", true) {
            private var result: ImportResult? = null
            private var target = ""
            override fun run(indicator: ProgressIndicator) {
                DriverService.getInstance().connect(ds, DataSources.getPassword(ds.id), indicator).use { c ->
                    val loader = MetadataLoader(c, ds.kind)
                    target = if (table != null) loader.qualified(table) else {
                        val scope = loader.scope(container!!)
                        val newTable = DbObject(io.opendata.db.meta.DbObjectKind.TABLE, d.newTableName, catalog = scope.catalog, schema = scope.schema, table = d.newTableName)
                        loader.qualified(newTable).also { q ->
                            c.createStatement().use { it.execute(DataImporter.createTableSql(ds.kind, q, DataImporter.fileColumns(file, opts), loader::quote)) }
                            if (ds.kind.supportsTransactions && !c.autoCommit) c.commit()
                        }
                    }
                    result = DataImporter.import(c, ds.kind, target, loader::quote, file, opts, indicator)
                }
            }
            override fun onSuccess() {
                val r = result ?: return
                val skipped = if (r.skippedColumns.isEmpty()) "" else "\nПропущены колонки файла: ${r.skippedColumns.joinToString()}"
                notify(project, "Импорт данных", "Загружено строк: ${r.rows} → $target$skipped")
                onSuccess()
            }
            override fun onThrowable(error: Throwable) =
                notify(project, "Импорт данных", DbError.from(error).render(), NotificationType.ERROR)
        }.queue()
    }
}

class ExportDialog(project: Project, base: Path, private val kind: DbKind?, defaultTable: String) : DialogWrapper(project) {
    private val format = ComboBox(DefaultComboBoxModel(ExportFormat.entries.toTypedArray()))
    private val path = TextFieldWithBrowseButton()
    private val header = JBCheckBox("Заголовок с именами колонок", true)
    private val table = JBTextField(defaultTable)

    init {
        title = "Экспорт данных"
        path.text = "$base.${ExportFormat.CSV.extension}"
        path.addActionListener {
            val fmt = format.item
            val wrapper = FileChooserFactory.getInstance()
                .createSaveFileDialog(FileSaverDescriptor("Экспорт данных", "Файл для выгрузки", fmt.extension), project)
            val current = Path.of(path.text)
            wrapper.save(current.parent, current.fileName.toString())?.let { path.text = it.file.path }
        }
        format.addActionListener {
            val ext = format.item.extension
            path.text = path.text.replace(Regex("\\.[A-Za-z]+$"), "") + ".$ext"
            header.isEnabled = format.item == ExportFormat.CSV || format.item == ExportFormat.TSV
            table.isEnabled = format.item == ExportFormat.SQL_INSERT
        }
        table.isEnabled = false
        init()
    }

    val file: Path get() = Path.of(path.text.trim())
    val options: ExportOptions get() = ExportOptions(format.item, header.isSelected, table.text.trim(), kind)

    override fun createCenterPanel(): JComponent = panel {
        row("Формат:") { cell(format) }
        row("Файл:") { cell(path).align(AlignX.FILL) }
        row { cell(header) }
        row("Таблица в INSERT:") { cell(table).align(AlignX.FILL) }
        row { comment("Кодировка UTF-8. Таблицы и представления выгружаются полностью, без ограничения числа строк.") }
    }

    override fun doValidate(): ValidationInfo? =
        if (path.text.isBlank()) ValidationInfo("Укажите файл", path.textField) else null
}

class ImportDialog(project: Project, private val table: DbObject?, base: Path) : DialogWrapper(project) {
    private val path = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFileDescriptor().withExtensionFilter("CSV/TSV", "csv", "tsv", "txt").withTitle("Файл для импорта"))
        text = ""
    }
    private val delimiters = linkedMapOf("Запятая ," to ',', "Точка с запятой ;" to ';', "Табуляция" to '\t', "Вертикальная черта |" to '|')
    private val delimiter = ComboBox(DefaultComboBoxModel(delimiters.keys.toTypedArray()))
    private val header = JBCheckBox("Первая строка — заголовок", true)
    private val emptyAsNull = JBCheckBox("Пустые значения как NULL", true)
    private val newTable = JBTextField()
    private val preview = JBLabel(" ")
    private val baseDir = base

    init {
        title = if (table != null) "Импорт данных в ${table.name}" else "Импорт данных в новую таблицу"
        path.textField.document.addDocumentListener(object : com.intellij.ui.DocumentAdapter() {
            override fun textChanged(e: javax.swing.event.DocumentEvent) = onFileChanged()
        })
        header.addActionListener { refreshPreview() }
        delimiter.addActionListener { refreshPreview() }
        init()
    }

    private fun onFileChanged() {
        val p = runCatching { Path.of(path.text.trim()) }.getOrNull() ?: return
        if (!Files.isRegularFile(p)) return
        delimiter.selectedItem = delimiters.entries.first { it.value == CsvReader.delimiterFor(p) }.key
        if (table == null && newTable.text.isBlank()) newTable.text = p.fileName.toString().substringBeforeLast('.').lowercase().replace(Regex("[^a-z0-9_]+"), "_")
        refreshPreview()
    }

    private fun refreshPreview() {
        val p = runCatching { Path.of(path.text.trim()) }.getOrNull()?.takeIf { Files.isRegularFile(it) } ?: return
        preview.text = runCatching { "Колонки файла: " + DataImporter.fileColumns(p, options).joinToString(", ") }.getOrElse { "Ошибка чтения: ${it.message}" }
    }

    val file: Path get() = Path.of(path.text.trim())
    val newTableName: String get() = newTable.text.trim()
    val options: ImportOptions
        get() = ImportOptions(delimiters.getValue(delimiter.item), header.isSelected, emptyAsNull.isSelected)

    override fun createCenterPanel(): JComponent = panel {
        row("Файл:") { cell(path).align(AlignX.FILL) }
        row("Разделитель:") { cell(delimiter) }
        row { cell(header) }
        row { cell(emptyAsNull) }
        if (table == null) row("Новая таблица:") { cell(newTable).align(AlignX.FILL).comment("Колонки создаются текстовыми по заголовку файла") }
        row { cell(preview) }
        row {
            comment(if (table != null) "Колонки сопоставляются по заголовку (без учёта регистра), без заголовка — по порядку. " +
                "В PostgreSQL и Cloudberry импорт выполняется одной транзакцией." else "Кодировка файла — UTF-8.")
        }
    }

    override fun doValidate(): ValidationInfo? = when {
        path.text.isBlank() || !Files.isRegularFile(runCatching { file }.getOrNull() ?: baseDir) -> ValidationInfo("Выберите файл", path.textField)
        table == null && newTableName.isEmpty() -> ValidationInfo("Укажите имя новой таблицы", newTable)
        else -> null
    }
}
