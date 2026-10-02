package io.opendata.db.session

import java.sql.Clob
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLWarning
import java.sql.Statement
import java.sql.Types

data class ColumnInfo(
    val name: String,
    val sqlType: Int,
    val typeName: String?,
    val className: String?,
    val table: String?,
    val schema: String?,
)

/** Нормализованная ошибка СУБД (ТЗ 26). position — 1-based смещение в тексте выражения. */
data class DbError(
    val message: String,
    val sqlState: String?,
    val position: Int?,
    val detail: String? = null,
    val hint: String? = null,
    val constraint: String? = null,
    val severity: String? = null,
) {
    fun render(): String = buildString {
        append(severity ?: "ERROR").append(": ").append(message)
        sqlState?.let { append("\n  SQLSTATE: ").append(it) }
        position?.let { append("\n  Position: ").append(it) }
        detail?.let { append("\n  Detail: ").append(it) }
        hint?.let { append("\n  Hint: ").append(it) }
        constraint?.let { append("\n  Constraint: ").append(it) }
    }

    companion object {
        private val POSITION_RE = Regex("""(?i)\bposition[:\s]+(\d+)""")

        fun from(e: Throwable): DbError {
            val sql = e as? SQLException ?: (e.cause as? SQLException)
            // PostgreSQL / Cloudberry: org.postgresql.util.ServerErrorMessage — через reflection,
            // так как драйвер загружен в отдельном classloader.
            val server = runCatching { e.javaClass.getMethod("getServerErrorMessage").invoke(e) }.getOrNull()
            if (server != null) {
                fun str(m: String) = runCatching { server.javaClass.getMethod(m).invoke(server) as String? }.getOrNull()
                val pos = runCatching { server.javaClass.getMethod("getPosition").invoke(server) as Int }.getOrNull()?.takeIf { it > 0 }
                return DbError(
                    message = str("getMessage") ?: e.message.orEmpty(),
                    sqlState = str("getSQLState") ?: sql?.sqlState,
                    position = pos, detail = str("getDetail"), hint = str("getHint"),
                    constraint = str("getConstraint"), severity = str("getSeverity"),
                )
            }
            val message = e.message ?: e.javaClass.simpleName
            return DbError(
                message = message,
                sqlState = sql?.sqlState,
                position = POSITION_RE.find(message)?.groupValues?.get(1)?.toIntOrNull(),
            )
        }
    }
}

sealed class StatementResult {
    abstract val sql: String
    abstract val durationMs: Long

    data class Rows(
        override val sql: String,
        val columns: List<ColumnInfo>,
        val rows: List<Array<Any?>>,
        val truncated: Boolean,
        override val durationMs: Long,
    ) : StatementResult()

    data class Update(override val sql: String, val count: Long, override val durationMs: Long) : StatementResult()

    data class Failure(override val sql: String, val error: DbError, override val durationMs: Long) : StatementResult()
}

/** Выполнение SQL поверх обычного JDBC-соединения. Вызывается только вне EDT (ТЗ 24). */
object QueryExecutor {
    const val DEFAULT_FETCH_LIMIT = 500
    private const val MAX_LOB_CHARS = 1_000_000

    /**
     * Выполняет одно выражение. Может вернуть несколько результатов (multiple result sets / update counts).
     * [onStatement] получает Statement до выполнения — для отмены через Statement.cancel() (ТЗ 25).
     */
    fun execute(
        connection: Connection,
        sql: String,
        fetchLimit: Int = DEFAULT_FETCH_LIMIT,
        onStatement: (Statement?) -> Unit = {},
        warnings: (SQLWarning) -> Unit = {},
    ): List<StatementResult> {
        val started = System.nanoTime()
        fun elapsed() = (System.nanoTime() - started) / 1_000_000
        val results = ArrayList<StatementResult>()
        try {
            connection.createStatement().use { st ->
                onStatement(st)
                if (fetchLimit > 0) runCatching { st.maxRows = fetchLimit + 1 }
                var hasResultSet = st.execute(sql)
                var guard = 0
                while (guard++ < 100) {
                    if (hasResultSet) {
                        st.resultSet.use { rs -> results += readRows(sql, rs, fetchLimit, elapsed()) }
                    } else {
                        val count = runCatching { st.largeUpdateCount }.getOrElse { st.updateCount.toLong() }
                        if (count < 0) break
                        results += StatementResult.Update(sql, count, elapsed())
                    }
                    hasResultSet = st.moreResults
                }
                generateSequence(runCatching { st.warnings }.getOrNull()) { it.nextWarning }.forEach(warnings)
            }
        } catch (e: Throwable) {
            if (e is Error && e !is NoClassDefFoundError && e !is LinkageError) throw e
            results += StatementResult.Failure(sql, DbError.from(e), elapsed())
        } finally {
            onStatement(null)
        }
        if (results.isEmpty()) results += StatementResult.Update(sql, 0, elapsed())
        return results
    }

    fun readRows(sql: String, rs: ResultSet, fetchLimit: Int, durationMs: Long): StatementResult.Rows {
        val md = rs.metaData
        val columns = (1..md.columnCount).map { i ->
            ColumnInfo(
                name = md.getColumnLabel(i).ifBlank { md.getColumnName(i) },
                sqlType = md.getColumnType(i),
                typeName = runCatching { md.getColumnTypeName(i) }.getOrNull(),
                className = runCatching { md.getColumnClassName(i) }.getOrNull(),
                table = runCatching { md.getTableName(i) }.getOrNull()?.ifBlank { null },
                schema = runCatching { md.getSchemaName(i) }.getOrNull()?.ifBlank { null },
            )
        }
        val rows = ArrayList<Array<Any?>>()
        var truncated = false
        while (rs.next()) {
            if (fetchLimit > 0 && rows.size >= fetchLimit) { truncated = true; break }
            rows += Array(columns.size) { i -> normalize(rs, i + 1, columns[i].sqlType) }
        }
        return StatementResult.Rows(sql, columns, rows, truncated, durationMs)
    }

    /** Приводит значения драйвера к типам JDK, понятным DataGrid (объекты драйверов живут в чужом classloader). */
    fun normalize(rs: ResultSet, index: Int, sqlType: Int): Any? {
        val value: Any? = when (sqlType) {
            Types.CLOB, Types.NCLOB -> (rs.getObject(index) as? Clob)?.let { it.getSubString(1, minOf(it.length(), MAX_LOB_CHARS.toLong()).toInt()) }
            Types.BLOB, Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> runCatching { rs.getBytes(index) }.getOrElse { rs.getObject(index) }
            else -> rs.getObject(index)
        }
        if (value == null || rs.wasNull() && value !is String) return null
        return when (value) {
            is String, is Number, is Boolean, is java.util.Date, is java.time.temporal.Temporal, is ByteArray, is java.util.UUID -> value
            is java.sql.Array -> runCatching { (value.array as? Array<*>)?.joinToString(", ", "{", "}") }.getOrNull() ?: value.toString()
            is java.sql.SQLXML -> value.string
            is Clob -> value.getSubString(1, minOf(value.length(), MAX_LOB_CHARS.toLong()).toInt())
            else -> value.toString()
        }
    }
}
