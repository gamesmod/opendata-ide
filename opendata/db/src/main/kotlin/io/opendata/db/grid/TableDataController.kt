package io.opendata.db.grid

import com.intellij.database.connection.throwable.info.SimpleErrorInfo
import com.intellij.database.datagrid.DataGridListModel
import com.intellij.database.datagrid.GridRequestSource
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DbKind
import io.opendata.db.session.DbError
import io.opendata.db.session.DbSession
import io.opendata.db.session.QueryExecutor
import io.opendata.db.session.StatementResult

/**
 * Данные одной таблицы/представления для DataGrid (ТЗ 19–20): страница данных, фильтр WHERE, сортировка ORDER BY,
 * подсчёт строк, применение изменений (DML в транзакции). Все обращения к БД — вне EDT.
 */
class TableDataController(
    val project: Project,
    val session: DbSession,
    val obj: DbObject,
) : TableTarget, Disposable {

    val kind: DbKind get() = session.config.kind

    @Volatile var where: String = ""
    @Volatile var orderBy: String = ""
    @Volatile var lastError: DbError? = null
        private set
    /** Последний выполненный SELECT — для вывода в редакторе. */
    @Volatile var lastQuery: String = ""
        private set

    private val loader: MetadataLoader by lazy { MetadataLoader(session.connection(), kind) }

    override val qualifiedName: String by lazy { loader.qualified(obj) }
    override val keyColumns: List<String> by lazy { if (obj.kind == DbObjectKind.TABLE) loader.keyColumns(obj) else emptyList() }
    override fun quote(identifier: String): String = loader.quote(identifier)

    /** Редактирование: только таблицы с ключом; Dremio — только чтение. */
    val isEditable: Boolean get() = obj.kind == DbObjectKind.TABLE && kind != DbKind.DREMIO && keyColumns.isNotEmpty()

    val model: DataGridListModel = ResultGrids.newModel()

    val pager = TablePager(
        model = { model },
        loadPage = { source, offset, limit -> loadAsync(source, offset, limit) },
        countRows = { source -> countAsync(source) },
    )

    val hookUp: TableHookUp by lazy {
        TableHookUp(project, model, readOnly = !isEditable, pager = pager) { TableMutator(it, this) }
    }

    fun selectSql(offset: Long, limit: Int): String = buildString {
        append("SELECT * FROM ").append(qualifiedName)
        if (where.isNotBlank()) append(" WHERE ").append(where.trim())
        if (orderBy.isNotBlank()) append(" ORDER BY ").append(orderBy.trim())
        if (limit > 0) append(" LIMIT ").append(limit)
        if (offset > 0) append(" OFFSET ").append(offset)
    }

    /** Синхронная загрузка страницы (вне EDT). */
    fun fetch(offset: Long, limit: Int): StatementResult {
        val sql = selectSql(offset, limit)
        lastQuery = sql
        return QueryExecutor.execute(session.connection(), sql, fetchLimit = 0).first()
    }

    fun count(): Long {
        val sql = "SELECT count(*) FROM $qualifiedName" + (if (where.isNotBlank()) " WHERE ${where.trim()}" else "")
        session.connection().createStatement().use { st -> st.executeQuery(sql).use { rs -> rs.next(); return rs.getLong(1) } }
    }

    private fun onEdt(r: () -> Unit) = ApplicationManager.getApplication().invokeLater(r, ModalityState.any())

    fun loadAsync(source: GridRequestSource, offset: Long, limit: Int) {
        hookUp.notifyRequestStarted(source)
        ApplicationManager.getApplication().executeOnPooledThread {
            // В обработчике ошибки нельзя звать selectSql(): он сам обращается к соединению (qualifiedName).
            val result = runCatching { fetch(offset, limit) }.getOrElse { StatementResult.Failure(lastQuery, DbError.from(it), 0) }
            onEdt { applyResult(source, result, offset) }
        }
    }

    /** Применяет результат загрузки к модели (EDT). */
    fun applyResult(source: GridRequestSource, result: StatementResult, offset: Long) {
        when (result) {
            is StatementResult.Rows -> {
                lastError = null
                ResultGrids.fill(hookUp, result)
                pager.pageLoaded(offset, result.rows.size)
                hookUp.notifyRequestFinished(source, true)
            }
            is StatementResult.Failure -> {
                lastError = result.error
                hookUp.notifyRequestError(source, SimpleErrorInfo.create(result.error.render()))
                hookUp.notifyRequestFinished(source, false)
            }
            else -> hookUp.notifyRequestFinished(source, true)
        }
    }

    fun countAsync(source: GridRequestSource) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val n = runCatching { count() }.getOrNull() ?: return@executeOnPooledThread
            onEdt {
                pager.totalCountReceived(source, n)
                hookUp.notifyRequestFinished(source, true)
            }
        }
    }

    // --- TableTarget (используется TableMutator.submit) ---

    override fun apply(statements: List<Dml>): Throwable? = runCatching {
        val c = session.connection()
        val tx = kind.supportsTransactions
        val wasAuto = c.autoCommit
        if (tx && wasAuto) c.autoCommit = false
        try {
            for (dml in statements) {
                val sql = if (kind == DbKind.CLICKHOUSE) clickHouseDml(dml.sql) else dml.sql
                c.prepareStatement(sql).use { ps ->
                    dml.params.forEachIndexed { i, v -> ps.setObject(i + 1, v) }
                    ps.executeUpdate()
                }
            }
            if (tx && wasAuto) c.commit()
        } catch (e: Throwable) {
            if (tx && wasAuto) runCatching { c.rollback() }
            throw e
        } finally {
            if (tx && wasAuto) runCatching { c.autoCommit = true }
        }
    }.exceptionOrNull()

    override fun reload(): StatementResult.Rows {
        val r = fetch(pager.offset, pager.limit)
        if (r is StatementResult.Failure) throw java.sql.SQLException(r.error.message)
        return r as StatementResult.Rows
    }

    override fun dispose() = session.close()

    companion object {
        private val UPDATE_RE = Regex("""(?s)^UPDATE (.+?) SET (.+) WHERE (.+)$""")

        /** ClickHouse: UPDATE → ALTER TABLE … UPDATE (мутация, синхронно); DELETE FROM — lightweight delete. */
        fun clickHouseDml(sql: String): String {
            UPDATE_RE.matchEntire(sql)?.let { m ->
                val (table, set, where) = m.destructured
                return "ALTER TABLE $table UPDATE $set WHERE $where SETTINGS mutations_sync = 2"
            }
            return sql
        }
    }
}
