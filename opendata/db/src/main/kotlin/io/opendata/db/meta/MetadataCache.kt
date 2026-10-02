package io.opendata.db.meta

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import io.opendata.db.drivers.DriverService
import io.opendata.db.model.DataSourceStorage
import java.util.concurrent.ConcurrentHashMap

/** Снимок метаданных для completion: таблицы и колонки. Заполняется в фоне, completion только читает. */
@Service(Service.Level.APP)
class MetadataCache {
    data class TableInfo(val schema: String?, val name: String, val columns: List<String>)

    private val tables = ConcurrentHashMap<String, List<TableInfo>>()
    private val loading = ConcurrentHashMap.newKeySet<String>()

    fun tables(dataSourceId: String): List<TableInfo> = tables[dataSourceId].orEmpty()

    fun invalidate(dataSourceId: String) {
        tables.remove(dataSourceId)
    }

    /** Загружает таблицы и колонки несистемных схем (до [limit] таблиц) отдельным соединением. */
    fun prefetch(dataSourceId: String, limit: Int = 3000) {
        if (tables.containsKey(dataSourceId) || !loading.add(dataSourceId)) return
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val cfg = DataSourceStorage.getInstance().find(dataSourceId) ?: return@executeOnPooledThread
                DriverService.getInstance().connect(cfg, DataSourceStorage.getInstance().getPassword(cfg.id), null).use { c ->
                    val loader = MetadataLoader(c, cfg.kind)
                    val result = ArrayList<TableInfo>()
                    for (container in loader.containers()) {
                        if (result.size >= limit) break
                        val folder = loader.folders(loader.scope(container)).first()
                        val objs = loader.tables(folder, views = false) + loader.tables(folder, views = true)
                        val cols = runCatching { columnsBySchema(c, folder.catalog, folder.schema) }.getOrDefault(emptyMap())
                        objs.take(limit - result.size).forEach { t -> result += TableInfo(t.schema ?: t.catalog, t.name, cols[t.name].orEmpty()) }
                    }
                    tables[dataSourceId] = result
                }
            } catch (e: Throwable) {
                thisLogger().info("Metadata prefetch failed for $dataSourceId: ${e.message}")
            } finally {
                loading.remove(dataSourceId)
            }
        }
    }

    private fun columnsBySchema(c: java.sql.Connection, catalog: String?, schema: String?): Map<String, List<String>> {
        val out = LinkedHashMap<String, MutableList<String>>()
        c.metaData.getColumns(catalog, schema, "%", "%").use { rs ->
            while (rs.next()) out.getOrPut(rs.getString("TABLE_NAME")) { ArrayList() } += rs.getString("COLUMN_NAME")
        }
        return out
    }

    companion object {
        fun getInstance(): MetadataCache = service()
    }
}
