package io.opendata.db.bulk

import com.intellij.openapi.progress.ProgressIndicator
import io.opendata.db.data.CsvReader
import io.opendata.db.model.DbKind
import java.io.FilterInputStream
import java.io.InputStream
import java.net.InetAddress
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException

enum class BulkFormat(val title: String) { CSV("CSV"), TEXT("TEXT (разделитель, \\N = NULL)") ; override fun toString() = title }

enum class BulkMethod(val title: String) {
    /** COPY … FROM STDIN через координатор (PgJDBC CopyManager): PostgreSQL, Greenplum, Cloudberry. */
    COPY("COPY через координатор"),
    /** Внешняя таблица gpfdist: сегменты читают файл параллельно со встроенного gpfdist (Greenplum, Cloudberry). */
    GPFDIST("gpfdist — параллельно сегментами");

    override fun toString() = title
}

data class BulkOptions(
    val format: BulkFormat = BulkFormat.CSV,
    val delimiter: Char = ',',
    val header: Boolean = true,
    /** Строка NULL: в CSV по умолчанию пустая, в TEXT — \N. */
    val nullString: String = "",
    val quote: Char = '"',
    /** Кодировка файла (имя PostgreSQL: UTF8, WIN1251, …). */
    val encoding: String = "UTF8",
    /** Greenplum/Cloudberry: LOG ERRORS SEGMENT REJECT LIMIT n ROWS; 0 — без пропуска ошибочных строк. */
    val rejectLimit: Int = 0,
    val truncate: Boolean = false,
)

data class BulkResult(val method: BulkMethod, val rows: Long, val rejected: Long, val millis: Long, val detail: String = "")

/**
 * Bulk-загрузка для PostgreSQL / Greenplum / Cloudberry без нативных утилит (работает и на Windows):
 *  - [copy]: COPY FROM STDIN, поток файла идёт через координатор;
 *  - [gpfdistLoad]: как gpload — встроенный [GpfdistServer] отдаёт файл, сегменты читают его параллельно через
 *    временную READABLE EXTERNAL TABLE, затем INSERT … SELECT;
 *  - [gpfdistUnload]: выгрузка таблицы через WRITABLE EXTERNAL TABLE — сегменты пишут в файл параллельно.
 * Ошибочные строки в Greenplum/Cloudberry пропускаются и журналируются (LOG ERRORS SEGMENT REJECT LIMIT).
 */
object BulkLoader {

    fun literal(s: String): String =
        if (s.any { it == '\\' || it < ' ' }) "E'" + s.replace("\\", "\\\\").replace("'", "''").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r") + "'"
        else "'" + s.replace("'", "''") + "'"

    private fun sreh(kind: DbKind, o: BulkOptions) =
        if (kind.isMpp && o.rejectLimit > 0) " LOG ERRORS SEGMENT REJECT LIMIT ${o.rejectLimit} ROWS" else ""

    /** Колонки таблицы с типами (format_type) — для внешней таблицы и сопоставления с заголовком файла. */
    fun tableColumns(c: Connection, qualifiedTable: String): List<Pair<String, String>> =
        c.prepareStatement(
            "SELECT attname, format_type(atttypid, atttypmod) FROM pg_attribute " +
                "WHERE attrelid = ?::regclass AND attnum > 0 AND NOT attisdropped ORDER BY attnum",
        ).use { ps ->
            ps.setString(1, qualifiedTable)
            ps.executeQuery().use { rs -> buildList { while (rs.next()) add(rs.getString(1) to rs.getString(2)) } }
        }

    /** Заголовок файла (CSV/TEXT) в нужной кодировке. */
    fun fileHeader(file: Path, o: BulkOptions): List<String> =
        Files.newBufferedReader(file, javaCharset(o.encoding)).use { r ->
            val first = if (o.format == BulkFormat.CSV) CsvReader(r, o.delimiter).next().orEmpty() else r.readLine().orEmpty().split(o.delimiter)
            first.map { it?.trim()?.removePrefix("\uFEFF").orEmpty() }
        }

    fun javaCharset(pgEncoding: String): Charset = when (pgEncoding.uppercase().replace("-", "").replace("_", "")) {
        "UTF8", "UNICODE" -> Charsets.UTF_8
        "WIN1251", "CP1251", "WINDOWS1251" -> Charset.forName("windows-1251")
        "LATIN1" -> Charsets.ISO_8859_1
        "KOI8R", "KOI8" -> Charset.forName("KOI8-R")
        else -> runCatching { Charset.forName(pgEncoding) }.getOrDefault(Charsets.UTF_8)
    }

    /** Колонки файла, сопоставленные с таблицей по заголовку (без учёта регистра); без заголовка — null (по порядку). */
    private fun mappedColumns(c: Connection, table: String, file: Path, o: BulkOptions, quote: (String) -> String): List<String>? {
        if (!o.header) return null
        val tableCols = tableColumns(c, table).map { it.first }
        val byLower = tableCols.associateBy { it.lowercase() }
        val header = fileHeader(file, o)
        val unknown = header.filter { byLower[it.lowercase()] == null }
        if (unknown.isNotEmpty()) throw SQLException("В таблице $table нет колонок из заголовка файла: ${unknown.joinToString()}")
        return header.map { quote(byLower.getValue(it.lowercase())) }
    }

    // ------------------------------------------------------------------ COPY

    fun copySql(kind: DbKind, table: String, columns: List<String>?, o: BulkOptions): String = buildString {
        append("COPY ").append(table)
        columns?.let { append(" (").append(it.joinToString(", ")).append(")") }
        append(" FROM STDIN WITH DELIMITER ").append(literal(o.delimiter.toString()))
        append(" NULL ").append(literal(o.nullString))
        if (o.format == BulkFormat.CSV) {
            append(" CSV")
            if (o.header) append(" HEADER")
            append(" QUOTE ").append(literal(o.quote.toString()))
        }
        append(" ENCODING ").append(literal(o.encoding))
        append(sreh(kind, o))
    }

    fun copy(c: Connection, kind: DbKind, table: String, quote: (String) -> String, file: Path, o: BulkOptions, indicator: ProgressIndicator?): BulkResult {
        require(kind.isPostgresFamily) { "COPY поддерживается для PostgreSQL, Greenplum и Cloudberry" }
        val started = System.currentTimeMillis()
        val sql = copySql(kind, table, mappedColumns(c, table, file, o, quote), o)
        val wasAuto = c.autoCommit
        c.autoCommit = false
        try {
            truncateErrorLog(c, kind, table)
            if (o.truncate) c.createStatement().use { it.execute("TRUNCATE $table") }
            val rows = progressStream(Files.newInputStream(file), Files.size(file), indicator).use { input -> copyIn(c, sql, input) }
            val rejected = rejectedRows(c, kind, table)
            c.commit()
            return BulkResult(BulkMethod.COPY, rows, rejected, System.currentTimeMillis() - started, sql)
        } catch (e: Throwable) {
            runCatching { c.rollback() }
            throw e
        } finally {
            runCatching { c.autoCommit = wasAuto }
        }
    }

    /** PgJDBC CopyManager через отражение: драйвер загружен в отдельный classloader. */
    private fun copyIn(c: Connection, sql: String, input: InputStream): Long {
        val api = Class.forName("org.postgresql.PGConnection", true, c.javaClass.classLoader)
        val pg = c.unwrap(api)
        val cm = api.getMethod("getCopyAPI").invoke(pg)
        return try {
            cm.javaClass.getMethod("copyIn", String::class.java, InputStream::class.java, Int::class.javaPrimitiveType)
                .invoke(cm, sql, input, 1 shl 16) as Long
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }

    // ------------------------------------------------------------------ gpfdist

    /** Адрес рабочей станции, видимый кластером (inet_client_addr координатора); иначе — локальный адрес. */
    fun clientAddress(c: Connection): String =
        runCatching { c.createStatement().use { st -> st.executeQuery("SELECT host(inet_client_addr())").use { rs -> if (rs.next()) rs.getString(1) else null } } }
            .getOrNull()?.takeIf { it.isNotBlank() && it != "127.0.0.1" && it != "::1" }
            ?: InetAddress.getLocalHost().hostAddress

    private fun formatClause(o: BulkOptions, forWrite: Boolean): String = buildString {
        append(if (o.format == BulkFormat.CSV) "FORMAT 'CSV' (" else "FORMAT 'TEXT' (")
        if (o.header && !forWrite) append("HEADER ")
        append("DELIMITER ").append(literal(o.delimiter.toString()))
        append(" NULL ").append(literal(o.nullString))
        if (o.format == BulkFormat.CSV) append(" QUOTE ").append(literal(o.quote.toString()))
        append(") ENCODING ").append(literal(o.encoding))
    }

    private fun extName() = "opendata_ext_" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)

    /**
     * Загрузка через временную внешнюю таблицу: сегменты читают [file] со встроенного gpfdist ([server] или новый
     * на [port]), строки вставляются INSERT … SELECT в одной транзакции.
     */
    fun gpfdistLoad(
        c: Connection, kind: DbKind, table: String, quote: (String) -> String, file: Path, o: BulkOptions,
        host: String, port: Int = 0, server: GpfdistServer? = null, indicator: ProgressIndicator? = null,
    ): BulkResult {
        require(kind.isMpp) { "gpfdist поддерживается только для Greenplum и Cloudberry" }
        val started = System.currentTimeMillis()
        val own = server == null
        val srv = server ?: GpfdistServer(file.toAbsolutePath().parent, port)
        val ext = extName()
        val wasAuto = c.autoCommit
        try {
            val types = tableColumns(c, table).associate { it.first.lowercase() to (it.first to it.second) }
            val fileCols = if (o.header) fileHeader(file, o) else types.values.map { it.first }
            val extCols = fileCols.mapIndexed { i, name ->
                val t = types[name.lowercase()]
                if (t != null) "${quote(t.first)} ${t.second}" else quote(name.ifBlank { "c${i + 1}" }) + " text"
            }
            val target = fileCols.mapNotNull { types[it.lowercase()]?.first?.let(quote) }
            val unknown = fileCols.filter { types[it.lowercase()] == null }
            if (o.header && unknown.isNotEmpty()) throw SQLException("В таблице $table нет колонок из заголовка файла: ${unknown.joinToString()}")
            val location = GpfdistServer.location(host, srv.port, srv.publish(file))
            val ddl = "CREATE READABLE EXTERNAL TEMP TABLE $ext (${extCols.joinToString(", ")}) LOCATION (${literal(location)}) " +
                formatClause(o, forWrite = false) + sreh(kind, o)
            indicator?.text = "gpfdist: $location"
            c.autoCommit = false
            c.createStatement().use { st ->
                st.execute(ddl)
                if (o.truncate) st.execute("TRUNCATE $table")
                val rows = st.executeUpdate("INSERT INTO $table (${target.joinToString(", ")}) SELECT ${target.joinToString(", ")} FROM $ext").toLong()
                val rejected = rejectedRows(c, kind, ext)
                st.execute("DROP EXTERNAL TABLE IF EXISTS $ext")
                c.commit()
                return BulkResult(BulkMethod.GPFDIST, rows, rejected, System.currentTimeMillis() - started, ddl)
            }
        } catch (e: Throwable) {
            runCatching { c.rollback() }
            throw srv.lastError?.let { SQLException("${e.message}\ngpfdist: $it", e) } ?: e
        } finally {
            runCatching { c.autoCommit = wasAuto }
            if (own) srv.close()
        }
    }

    /** Выгрузка таблицы в файл через WRITABLE EXTERNAL TABLE: сегменты пишут параллельно; заголовок пишет IDE. */
    fun gpfdistUnload(
        c: Connection, kind: DbKind, table: String, quote: (String) -> String, file: Path, o: BulkOptions,
        host: String, port: Int = 0, indicator: ProgressIndicator? = null,
    ): BulkResult {
        require(kind.isMpp) { "gpfdist поддерживается только для Greenplum и Cloudberry" }
        val started = System.currentTimeMillis()
        file.toAbsolutePath().parent?.let { Files.createDirectories(it) }
        val cols = tableColumns(c, table).map { it.first }
        // Внешняя таблица дописывает файл — начинаем с пустого (или с заголовка).
        Files.write(file, if (o.header) (headerLine(cols, o) + "\n").toByteArray(javaCharset(o.encoding)) else ByteArray(0))
        val ext = extName()
        GpfdistServer(file.toAbsolutePath().parent, port).use { srv ->
            val location = GpfdistServer.location(host, srv.port, srv.publish(file))
            val ddl = "CREATE WRITABLE EXTERNAL TEMP TABLE $ext (LIKE $table) LOCATION (${literal(location)}) " +
                formatClause(o, forWrite = true) + " DISTRIBUTED RANDOMLY"
            indicator?.text = "gpfdist: $location"
            try {
                c.createStatement().use { st ->
                    st.execute(ddl)
                    val rows = st.executeUpdate("INSERT INTO $ext SELECT * FROM $table").toLong()
                    st.execute("DROP EXTERNAL TABLE IF EXISTS $ext")
                    if (!c.autoCommit) c.commit()
                    return BulkResult(BulkMethod.GPFDIST, rows, 0, System.currentTimeMillis() - started, ddl)
                }
            } catch (e: Throwable) {
                if (!c.autoCommit) runCatching { c.rollback() }
                throw srv.lastError?.let { SQLException("${e.message}\ngpfdist: $it", e) } ?: e
            }
        }
    }

    private fun headerLine(cols: List<String>, o: BulkOptions): String = cols.joinToString(o.delimiter.toString()) { name ->
        if (o.format == BulkFormat.CSV && name.any { it == o.delimiter || it == o.quote || it == '\n' || it == '\r' })
            o.quote + name.replace(o.quote.toString(), "${o.quote}${o.quote}") + o.quote
        else name
    }

    // ------------------------------------------------------------------ журнал ошибок Greenplum

    private fun truncateErrorLog(c: Connection, kind: DbKind, table: String) {
        if (!kind.isMpp) return
        runCatching { c.prepareStatement("SELECT gp_truncate_error_log(?)").use { it.setString(1, table); it.execute() } }
            .onFailure { rollbackToSavepointIfNeeded(c) }
    }

    /** Число отклонённых строк из журнала ошибок (gp_read_error_log); 0 вне Greenplum/Cloudberry. */
    fun rejectedRows(c: Connection, kind: DbKind, table: String): Long {
        if (!kind.isMpp) return 0
        val sp = runCatching { c.setSavepoint() }.getOrNull()
        return runCatching {
            c.prepareStatement("SELECT count(*) FROM gp_read_error_log(?)").use { ps ->
                ps.setString(1, table)
                ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else 0L }
            }
        }.getOrElse { sp?.let { s -> runCatching { c.rollback(s) } }; 0L }
            .also { sp?.let { s -> runCatching { c.releaseSavepoint(s) } } }
    }

    private fun rollbackToSavepointIfNeeded(@Suppress("UNUSED_PARAMETER") c: Connection) {
        // gp_truncate_error_log вызывается до начала загрузки: при ошибке транзакция ещё пуста — откатываем её.
        runCatching { c.rollback() }
    }

    /** Поток файла с прогрессом и отменой. */
    private fun progressStream(input: InputStream, total: Long, indicator: ProgressIndicator?): InputStream = object : FilterInputStream(input) {
        private var read = 0L
        private fun tick(n: Int) {
            if (n <= 0 || indicator == null) return
            read += n
            indicator.checkCanceled()
            if (total > 0) { indicator.isIndeterminate = false; indicator.fraction = read.toDouble() / total }
        }
        override fun read(): Int = super.read().also { if (it >= 0) tick(1) }
        override fun read(b: ByteArray, off: Int, len: Int): Int = super.read(b, off, len).also { tick(it) }
    }
}
