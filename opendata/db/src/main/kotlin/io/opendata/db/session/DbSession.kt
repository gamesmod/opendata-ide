package io.opendata.db.session

import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.ProgressIndicator
import io.opendata.db.drivers.DriverService
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSourceStorage
import java.sql.Connection
import java.sql.Statement
import java.util.concurrent.atomic.AtomicReference

/**
 * Сессия консоли или редактора таблицы: одно JDBC-соединение, режим транзакций, текущий Statement для отмены.
 * Все методы, обращающиеся к БД, вызываются вне EDT.
 */
class DbSession(dataSourceId: String) : Disposable {
    @Volatile
    var dataSourceId: String = dataSourceId
        private set

    private var connection: Connection? = null
    private val running = AtomicReference<Statement?>()

    val config: DataSourceConfig
        get() = DataSourceStorage.getInstance().find(dataSourceId)
            ?: error("Источник данных не найден: $dataSourceId")

    /** true — autocommit, false — ручной режим (Commit/Rollback). */
    @Volatile
    var autoCommit: Boolean = true
        set(value) {
            field = value
            synchronized(this) {
                connection?.takeIf { !it.isClosed }?.let { c -> if (c.autoCommit != value) runCatching { c.autoCommit = value } }
            }
        }

    val isConnected: Boolean @Synchronized get() = connection?.let { !it.isClosed } == true

    val isRunning: Boolean get() = running.get() != null

    fun switchDataSource(id: String) {
        if (id == dataSourceId) return
        close()
        dataSourceId = id
    }

    @Synchronized
    fun connection(indicator: ProgressIndicator? = null): Connection {
        connection?.takeIf { !it.isClosed }?.let { return it }
        val cfg = config
        val c = DriverService.getInstance().connect(cfg, DataSourceStorage.getInstance().getPassword(cfg.id), indicator)
        if (cfg.kind.supportsTransactions) runCatching { c.autoCommit = autoCommit }
        connection = c
        return c
    }

    fun execute(sql: String, fetchLimit: Int = QueryExecutor.DEFAULT_FETCH_LIMIT, indicator: ProgressIndicator? = null): List<StatementResult> {
        val c = try {
            connection(indicator)
        } catch (e: Throwable) {
            return listOf(StatementResult.Failure(sql, DbError.from(e), 0))
        }
        return QueryExecutor.execute(c, sql, fetchLimit, onStatement = { running.set(it) })
    }

    /** Отмена выполняющегося запроса штатным JDBC Statement.cancel() (ТЗ 25). */
    fun cancel() {
        running.get()?.let { runCatching { it.cancel() } }
    }

    @Synchronized
    fun commit() {
        connection?.takeIf { !it.isClosed && !it.autoCommit }?.commit()
    }

    @Synchronized
    fun rollback() {
        connection?.takeIf { !it.isClosed && !it.autoCommit }?.rollback()
    }

    @Synchronized
    fun close() {
        cancel()
        connection?.let { c -> runCatching { if (!c.autoCommit) c.rollback() }; runCatching { c.close() } }
        connection = null
    }

    override fun dispose() = close()

    companion object {
        /** Test Connection: отдельное соединение, SELECT 1 и информация о сервере. */
        fun testConnection(config: DataSourceConfig, password: String?, indicator: ProgressIndicator?): String {
            DriverService.getInstance().connect(config, password, indicator).use { c ->
                val md = c.metaData
                c.createStatement().use { st -> st.execute("SELECT 1") }
                return "${md.databaseProductName} ${md.databaseProductVersion}\nДрайвер: ${md.driverName} ${md.driverVersion}"
            }
        }
    }
}
