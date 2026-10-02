package io.opendata.db

import com.intellij.database.datagrid.GridRequestSource
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.opendata.db.grid.TableDataController
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.model.DataSources
import io.opendata.db.model.DbKind
import io.opendata.db.session.DbSession
import io.opendata.db.session.StatementResult
import java.util.concurrent.TimeUnit

/** Общие помощники интеграционных тестов: источники данных из переменных окружения. */
object DbTestSupport {
    fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

    fun postgres(project: Project): DataSourceConfig? = env("OPENDATA_PG_URL")?.let { url ->
        register(project, DataSourceConfig().apply {
            name = "PostgreSQL test"; kind = DbKind.POSTGRESQL; this.url = url
            user = env("OPENDATA_PG_USER") ?: "opendata"
        }, env("OPENDATA_PG_PASSWORD") ?: "opendata")
    }

    fun clickhouse(project: Project): DataSourceConfig? = env("OPENDATA_CH_URL")?.let { url ->
        register(project, DataSourceConfig().apply {
            name = "ClickHouse test"; kind = DbKind.CLICKHOUSE; this.url = url
            user = env("OPENDATA_CH_USER") ?: "default"
        }, env("OPENDATA_CH_PASSWORD") ?: "")
    }

    /** Cloudberry: при отсутствии отдельного стенда проверяется на PostgreSQL (совместимый протокол и каталог). */
    fun cloudberry(project: Project): DataSourceConfig? = (env("OPENDATA_CB_URL") ?: env("OPENDATA_PG_URL"))?.let { url ->
        register(project, DataSourceConfig().apply {
            name = "Cloudberry test"; kind = DbKind.CLOUDBERRY; this.url = url
            user = env("OPENDATA_CB_USER") ?: env("OPENDATA_PG_USER") ?: "opendata"
        }, env("OPENDATA_CB_PASSWORD") ?: env("OPENDATA_PG_PASSWORD") ?: "opendata")
    }

    fun dremio(project: Project): DataSourceConfig? = env("OPENDATA_DREMIO_URL")?.let { url ->
        register(project, DataSourceConfig().apply {
            name = "Dremio test"; kind = DbKind.DREMIO; this.url = url
            user = env("OPENDATA_DREMIO_USER") ?: "dremio"
        }, env("OPENDATA_DREMIO_PASSWORD") ?: "dremio123")
    }

    private fun register(project: Project, cfg: DataSourceConfig, password: String): DataSourceConfig {
        DataSourceStorage.getInstance(project).addOrUpdate(cfg)
        DataSources.setPassword(cfg.id, password.ifEmpty { null })
        return cfg
    }

    fun table(cfg: DataSourceConfig, container: String, name: String, kind: DbObjectKind = DbObjectKind.TABLE): DbObject =
        if (cfg.kind == DbKind.CLICKHOUSE) {
            // В ClickHouse JDBC базы данных — схемы или каталоги (зависит от версии драйвера): определяем по метаданным.
            DbSession(cfg.id).use { s ->
                val c = MetadataLoader(s.connection(), cfg.kind).containers().first { it.name == container }
                DbObject(kind, name, catalog = c.catalog, schema = if (c.kind == DbObjectKind.SCHEMA) c.name else null, table = name)
            }
        } else DbObject(kind, name, schema = container, table = name)

    fun <T> DbSession.use(block: (DbSession) -> T): T = try { block(this) } finally { close() }

    /** Загружает первую страницу в контроллер синхронно (модель заполняется на EDT). */
    fun loadFirstPage(c: TableDataController): StatementResult.Rows {
        val r = c.fetch(0, c.pager.limit)
        check(r is StatementResult.Rows) { (r as StatementResult.Failure).error.render() }
        ApplicationManager.getApplication().invokeAndWait { c.applyResult(GridRequestSource(null), r, 0) }
        return r
    }

    fun waitFor(source: GridRequestSource) {
        check(source.actionCallback.waitFor(TimeUnit.SECONDS.toMillis(60))) { "timeout" }
        check(!source.errorOccurred()) { source.errorMessage ?: "error" }
    }

    fun scalar(cfg: DataSourceConfig, sql: String): String? = DbSession(cfg.id).use { s ->
        s.connection().createStatement().use { st -> st.executeQuery(sql).use { rs -> if (rs.next()) rs.getString(1) else null } }
    }

    fun exec(cfg: DataSourceConfig, sql: String) = DbSession(cfg.id).use { s ->
        s.connection().createStatement().use { st -> st.execute(sql) }
    }
}
