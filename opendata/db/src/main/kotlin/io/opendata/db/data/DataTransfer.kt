package io.opendata.db.data

import com.intellij.openapi.progress.ProgressIndicator
import io.opendata.db.model.DbKind
import io.opendata.db.session.ColumnInfo
import java.io.BufferedReader
import java.io.Reader
import java.io.Writer
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

/** Форматы экспорта данных. */
enum class ExportFormat(val title: String, val extension: String) {
    CSV("CSV", "csv"),
    TSV("TSV (табуляция)", "tsv"),
    JSON("JSON", "json"),
    SQL_INSERT("SQL INSERT", "sql"),
    MARKDOWN("Markdown", "md");

    override fun toString() = title
}

data class ExportOptions(
    val format: ExportFormat,
    val header: Boolean = true,
    /** Имя таблицы в SQL INSERT (уже с кавычками, если нужны). */
    val tableName: String = "table_name",
    val kind: DbKind? = null,
)

/** Построчная запись результата в файл выбранного формата. */
abstract class RowWriter(protected val out: Writer) {
    protected lateinit var columns: List<ColumnInfo>
    open fun begin(columns: List<ColumnInfo>) {
        this.columns = columns
    }
    abstract fun row(values: Array<Any?>)
    open fun end() {}

    companion object {
        fun create(out: Writer, o: ExportOptions): RowWriter = when (o.format) {
            ExportFormat.CSV -> DelimitedWriter(out, ',', o.header)
            ExportFormat.TSV -> DelimitedWriter(out, '\t', o.header)
            ExportFormat.JSON -> JsonWriter(out)
            ExportFormat.SQL_INSERT -> SqlInsertWriter(out, o.tableName, o.kind)
            ExportFormat.MARKDOWN -> MarkdownWriter(out)
        }
    }
}

/** Текстовое представление значения JDBC для файлов. */
object Values {
    fun text(v: Any?): String? = when (v) {
        null -> null
        is ByteArray -> hex(v)
        is java.sql.Clob -> v.characterStream.use { it.readText() }
        is java.sql.Blob -> hex(v.binaryStream.use { it.readBytes() })
        is java.sql.Array -> runCatching { arrayText(v.array) }.getOrElse { v.toString() }
        is BigDecimal -> v.toPlainString()
        is Array<*> -> arrayText(v)
        else -> v.toString()
    }

    private fun arrayText(a: Any?): String = when (a) {
        is Array<*> -> a.joinToString(",", "{", "}") { text(it) ?: "NULL" }
        else -> a.toString()
    }

    fun hex(b: ByteArray): String = b.joinToString("") { "%02x".format(it) }
}

class DelimitedWriter(out: Writer, private val delimiter: Char, private val header: Boolean) : RowWriter(out) {
    override fun begin(columns: List<ColumnInfo>) {
        super.begin(columns)
        if (header) line(columns.map { it.name })
    }

    override fun row(values: Array<Any?>) = line(values.map { Values.text(it) })

    private fun line(cells: List<String?>) {
        cells.forEachIndexed { i, c ->
            if (i > 0) out.write(delimiter.code)
            if (c != null) out.write(escape(c))
        }
        out.write("\n")
    }

    private fun escape(s: String): String =
        if (s.any { it == delimiter || it == '"' || it == '\n' || it == '\r' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
}

class JsonWriter(out: Writer) : RowWriter(out) {
    private var first = true

    override fun begin(columns: List<ColumnInfo>) {
        super.begin(columns)
        out.write("[")
    }

    override fun row(values: Array<Any?>) {
        out.write(if (first) "\n  {" else ",\n  {")
        first = false
        values.forEachIndexed { i, v ->
            if (i > 0) out.write(", ")
            out.write(string(columns[i].name)); out.write(": "); out.write(value(v))
        }
        out.write("}")
    }

    override fun end() = out.write(if (first) "]\n" else "\n]\n")

    private fun value(v: Any?): String = when (v) {
        null -> "null"
        is Boolean -> v.toString()
        is Double -> if (v.isFinite()) v.toString() else string(v.toString())
        is Float -> if (v.isFinite()) v.toString() else string(v.toString())
        is BigDecimal -> v.toPlainString()
        is Number -> v.toString()
        else -> string(Values.text(v)!!)
    }

    companion object {
        fun string(s: String): String = buildString {
            append('"')
            for (c in s) when {
                c == '"' -> append("\\\"")
                c == '\\' -> append("\\\\")
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c < ' ' -> append("\\u%04x".format(c.code))
                else -> append(c)
            }
            append('"')
        }
    }
}

class SqlInsertWriter(out: Writer, private val table: String, private val kind: DbKind?) : RowWriter(out) {
    private lateinit var prefix: String

    override fun begin(columns: List<ColumnInfo>) {
        super.begin(columns)
        prefix = "INSERT INTO $table (" + columns.joinToString(", ") { quote(it.name) } + ") VALUES ("
    }

    override fun row(values: Array<Any?>) {
        out.write(prefix)
        out.write(values.joinToString(", ") { literal(it) })
        out.write(");\n")
    }

    private fun quote(id: String): String = when {
        kind == DbKind.CLICKHOUSE -> "`" + id.replace("`", "``") + "`"
        id.matches(Regex("[a-z_][a-z0-9_]*")) -> id
        else -> "\"" + id.replace("\"", "\"\"") + "\""
    }

    private fun literal(v: Any?): String = when (v) {
        null -> "NULL"
        is Boolean -> if (v) "TRUE" else "FALSE"
        is BigDecimal -> v.toPlainString()
        is Double, is Float -> if ((v as Number).toDouble().isFinite()) v.toString() else str(v.toString())
        is Number -> v.toString()
        is ByteArray -> when (kind) {
            DbKind.POSTGRESQL, DbKind.CLOUDBERRY -> "'\\x${Values.hex(v)}'"
            DbKind.CLICKHOUSE -> "unhex('${Values.hex(v)}')"
            else -> "X'${Values.hex(v)}'"
        }
        else -> str(Values.text(v)!!)
    }

    private fun str(s: String) = "'" + s.replace("'", "''").let { if (kind == DbKind.CLICKHOUSE) it.replace("\\", "\\\\") else it } + "'"
}

class MarkdownWriter(out: Writer) : RowWriter(out) {
    override fun begin(columns: List<ColumnInfo>) {
        super.begin(columns)
        out.write("| " + columns.joinToString(" | ") { cell(it.name) } + " |\n")
        out.write("|" + columns.joinToString("|") { "---" } + "|\n")
    }

    override fun row(values: Array<Any?>) {
        out.write("| " + values.joinToString(" | ") { cell(Values.text(it) ?: "NULL") } + " |\n")
    }

    private fun cell(s: String) = s.replace("|", "\\|").replace("\r", "").replace("\n", "<br>")
}

/** Экспорт данных: результат запроса (потоково, без ограничения строк) или уже загруженные строки. */
object DataExporter {
    const val FETCH_SIZE = 1000

    fun columnsOf(rs: ResultSet): List<ColumnInfo> {
        val md = rs.metaData
        return (1..md.columnCount).map { i ->
            ColumnInfo(md.getColumnLabel(i), md.getColumnType(i), md.getColumnTypeName(i), runCatching { md.getColumnClassName(i) }.getOrNull(), null, null)
        }
    }

    /**
     * Выполняет [sql] на [connection] и пишет все строки в [file]. Для PostgreSQL курсор работает только
     * вне auto-commit, поэтому на время выгрузки auto-commit выключается (данные не меняются).
     */
    fun exportQuery(connection: Connection, sql: String, file: Path, options: ExportOptions, indicator: ProgressIndicator?): Long {
        val wasAuto = connection.autoCommit
        val cursor = options.kind == DbKind.POSTGRESQL || options.kind == DbKind.CLOUDBERRY
        if (cursor && wasAuto) connection.autoCommit = false
        try {
            connection.createStatement().use { st ->
                st.fetchSize = FETCH_SIZE
                st.executeQuery(sql).use { rs ->
                    val columns = columnsOf(rs)
                    return write(file, options, columns, indicator) { emit ->
                        while (rs.next()) emit(Array(columns.size) { rs.getObject(it + 1) })
                    }
                }
            }
        } finally {
            if (cursor && wasAuto) runCatching { connection.rollback(); connection.autoCommit = true }
        }
    }

    fun exportRows(columns: List<ColumnInfo>, rows: List<Array<Any?>>, file: Path, options: ExportOptions): Long =
        write(file, options, columns, null) { emit -> rows.forEach(emit) }

    private fun write(file: Path, options: ExportOptions, columns: List<ColumnInfo>, indicator: ProgressIndicator?,
                      source: ((Array<Any?>) -> Unit) -> Unit): Long {
        file.parent?.let { Files.createDirectories(it) }
        var count = 0L
        Files.newBufferedWriter(file, StandardCharsets.UTF_8).use { out ->
            val w = RowWriter.create(out, options)
            w.begin(columns)
            source { values ->
                w.row(values)
                count++
                if (count % FETCH_SIZE == 0L) {
                    indicator?.checkCanceled()
                    indicator?.text2 = "Строк: $count"
                }
            }
            w.end()
        }
        return count
    }
}

/** Разбор CSV/TSV по RFC 4180: кавычки, удвоенные кавычки, переводы строк внутри значения. */
class CsvReader(reader: Reader, private val delimiter: Char) : AutoCloseable {
    private val r = if (reader is BufferedReader) reader else BufferedReader(reader)

    /** Следующая строка: значения (null — пустое поле без кавычек) или null в конце файла. */
    fun next(): List<String?>? {
        var c = r.read()
        if (c == -1) return null
        val cells = ArrayList<String?>()
        val sb = StringBuilder()
        var quoted = false
        var wasQuoted = false
        while (true) {
            if (quoted) {
                when {
                    c == -1 -> throw IllegalArgumentException("Незакрытая кавычка в CSV")
                    c == '"'.code -> {
                        r.mark(1)
                        val n = r.read()
                        if (n == '"'.code) sb.append('"') else { quoted = false; if (n != -1) r.reset() }
                    }
                    else -> sb.append(c.toChar())
                }
            } else {
                when {
                    c == -1 || c == '\n'.code -> {
                        cells += cell(sb, wasQuoted)
                        return cells
                    }
                    c == '\r'.code -> {}
                    c == delimiter.code -> {
                        cells += cell(sb, wasQuoted); sb.setLength(0); wasQuoted = false
                    }
                    c == '"'.code && sb.isEmpty() && !wasQuoted -> { quoted = true; wasQuoted = true }
                    else -> sb.append(c.toChar())
                }
            }
            c = r.read()
        }
    }

    private fun cell(sb: StringBuilder, quoted: Boolean): String? = if (sb.isEmpty() && !quoted) null else sb.toString()

    override fun close() = r.close()

    companion object {
        fun delimiterFor(file: Path): Char = if (file.fileName.toString().lowercase().endsWith(".tsv")) '\t' else ','
    }
}

data class ImportOptions(
    val delimiter: Char = ',',
    val header: Boolean = true,
    /** Пустое поле без кавычек — NULL (иначе пустая строка). */
    val emptyAsNull: Boolean = true,
    val batchSize: Int = 1000,
)

data class ImportResult(val rows: Long, val skippedColumns: List<String>)

/** Импорт CSV/TSV в таблицу (ТЗ: импорт данных). */
object DataImporter {

    /** Колонки файла: заголовок или c1..cN по первой строке. */
    fun fileColumns(file: Path, options: ImportOptions): List<String> =
        Files.newBufferedReader(file, StandardCharsets.UTF_8).use { r ->
            val first = CsvReader(r, options.delimiter).next().orEmpty()
            if (options.header) first.mapIndexed { i, s -> s?.trim()?.removePrefix("\uFEFF")?.ifEmpty { null } ?: "c${i + 1}" }
            else first.indices.map { "c${it + 1}" }
        }

    /** CREATE TABLE с текстовыми колонками по заголовку файла. */
    fun createTableSql(kind: DbKind, qualifiedTable: String, columns: List<String>, quote: (String) -> String): String = when (kind) {
        DbKind.CLICKHOUSE -> "CREATE TABLE $qualifiedTable (" + columns.joinToString(", ") { "${quote(it)} Nullable(String)" } +
            ") ENGINE = MergeTree ORDER BY tuple()"
        else -> "CREATE TABLE $qualifiedTable (" + columns.joinToString(", ") { "${quote(it)} TEXT" } + ")"
    }

    /**
     * Загружает [file] в [qualifiedTable]. Колонки сопоставляются по заголовку (без учёта регистра), без заголовка —
     * по порядку. В СУБД с транзакциями импорт атомарный: при ошибке всё откатывается.
     */
    fun import(connection: Connection, kind: DbKind, qualifiedTable: String, quote: (String) -> String, file: Path,
               options: ImportOptions, indicator: ProgressIndicator?): ImportResult {
        data class Target(val name: String, val sqlType: Int)

        val tableColumns = connection.createStatement().use { st ->
            st.executeQuery("SELECT * FROM $qualifiedTable WHERE 1 = 0").use { rs ->
                val md = rs.metaData
                (1..md.columnCount).map { Target(md.getColumnName(it), md.getColumnType(it)) }
            }
        }
        Files.newBufferedReader(file, StandardCharsets.UTF_8).use { reader ->
            val csv = CsvReader(reader, options.delimiter)
            val first = csv.next() ?: return ImportResult(0, emptyList())
            // fileIndex → колонка таблицы
            val mapping: List<Pair<Int, Target>>
            val skipped = ArrayList<String>()
            var pending: List<String?>? = null
            if (options.header) {
                val byName = tableColumns.associateBy { it.name.lowercase() }
                mapping = first.mapIndexedNotNull { i, h ->
                    val name = h?.trim()?.removePrefix("\uFEFF").orEmpty()
                    val t = byName[name.lowercase()]
                    if (t == null) { skipped += name; null } else i to t
                }
            } else {
                mapping = first.indices.filter { it < tableColumns.size }.map { it to tableColumns[it] }
                if (first.size > tableColumns.size) skipped += (tableColumns.size until first.size).map { "c${it + 1}" }
                pending = first
            }
            require(mapping.isNotEmpty()) { "Ни одна колонка файла не совпала с колонками таблицы $qualifiedTable" }

            val sql = "INSERT INTO $qualifiedTable (" + mapping.joinToString(", ") { quote(it.second.name) } + ") VALUES (" +
                mapping.joinToString(", ") { "?" } + ")"
            val tx = kind.supportsTransactions
            val wasAuto = connection.autoCommit
            if (tx && wasAuto) connection.autoCommit = false
            var count = 0L
            try {
                connection.prepareStatement(sql).use { ps ->
                    var inBatch = 0
                    var line = if (options.header) 1L else 0L
                    fun add(values: List<String?>) {
                        line++
                        mapping.forEachIndexed { p, (fi, t) ->
                            val raw = values.getOrNull(fi)
                            val v = if (raw == null) (if (options.emptyAsNull) null else "") else raw
                            try {
                                ps.setObject(p + 1, convert(v, t.sqlType))
                            } catch (e: Exception) {
                                throw IllegalArgumentException("Строка $line, колонка ${t.name}: «$raw» — ${e.message}", e)
                            }
                        }
                        ps.addBatch(); inBatch++; count++
                        if (inBatch >= options.batchSize) {
                            ps.executeBatch(); inBatch = 0
                            indicator?.checkCanceled()
                            indicator?.text2 = "Строк: $count"
                        }
                    }
                    pending?.let { add(it) }
                    while (true) add(csv.next() ?: break)
                    if (inBatch > 0) ps.executeBatch()
                }
                if (tx) connection.commit()
            } catch (e: Throwable) {
                if (tx) runCatching { connection.rollback() }
                throw e
            } finally {
                if (tx && wasAuto) runCatching { connection.autoCommit = true }
            }
            return ImportResult(count, skipped)
        }
    }

    /** Строка из файла → значение для JDBC по типу колонки (числа и логические — типизированно, остальное строкой). */
    fun convert(v: String?, sqlType: Int): Any? {
        if (v == null) return null
        val s = v.trim()
        return when (sqlType) {
            Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT -> if (s.isEmpty()) null else BigDecimal(s).toBigIntegerExact().let {
                if (it.bitLength() < 64) it.toLong() else it
            }
            Types.DECIMAL, Types.NUMERIC, Types.REAL, Types.FLOAT, Types.DOUBLE -> if (s.isEmpty()) null else BigDecimal(s)
            Types.BOOLEAN, Types.BIT -> when (s.lowercase()) {
                "" -> null
                "true", "t", "1", "yes", "y", "да" -> true
                "false", "f", "0", "no", "n", "нет" -> false
                else -> throw IllegalArgumentException("не логическое значение")
            }
            else -> v
        }
    }
}
