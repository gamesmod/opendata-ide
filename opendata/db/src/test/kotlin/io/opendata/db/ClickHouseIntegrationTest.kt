package io.opendata.db

import com.intellij.database.datagrid.GridRequestSource
import com.intellij.database.datagrid.ModelIndexSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.DbTestSupport.use
import io.opendata.db.grid.TableDataController
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.DdlGenerator
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.session.DbSession
import io.opendata.db.session.StatementResult

/**
 * ClickHouse: introspection, запросы, ошибки, DDL (SHOW CREATE), редактирование (ALTER TABLE … UPDATE, DELETE, INSERT).
 * Требует OPENDATA_CH_URL (например jdbc:clickhouse://localhost:8123/opendata) и базу opendata из docker/clickhouse/init.
 */
class ClickHouseIntegrationTest : BasePlatformTestCase() {
    override fun runInDispatchThread(): Boolean = false

    private lateinit var ds: DataSourceConfig
    private var skip = false

    override fun setUp() {
        super.setUp()
        val cfg = DbTestSupport.clickhouse(project)
        if (cfg == null) skip = true else ds = cfg
    }

    fun testIntrospectionAndDdl() {
        if (skip) return
        DbSession(ds.id).use { s ->
            val m = MetadataLoader(s.connection(), ds.kind)
            val db = m.containers().first { it.name == "opendata" }
            val folders = m.children(db).associateBy { it.kind }
            assertTrue(m.children(folders.getValue(DbObjectKind.TABLES_FOLDER)).any { it.name == "events" })
            assertTrue(m.children(folders.getValue(DbObjectKind.VIEWS_FOLDER)).any { it.name == "events_v" })
            val events = DbTestSupport.table(ds, "opendata", "events")
            assertEquals(listOf("id", "name", "ts", "payload"), m.columns(events).map { it.name })
            assertEquals(listOf("id"), m.keyColumns(events))
            val ddl = DdlGenerator(s.connection(), ds.kind).ddl(events)
            assertTrue(ddl, ddl.contains("CREATE TABLE opendata.events"))
            assertTrue(ddl, ddl.contains("MergeTree"))
        }
    }

    fun testQueryAndError() {
        if (skip) return
        DbSession(ds.id).use { s ->
            val r = s.execute("SELECT number AS id, toString(number) AS s FROM system.numbers LIMIT 100").single() as StatementResult.Rows
            assertEquals(100, r.rows.size)
            val f = s.execute("SELECT * FROMM opendata.events").single() as StatementResult.Failure
            assertNotNull(f.error.position)
            assertTrue(f.error.message, f.error.message.contains("Syntax error"))
        }
    }

    fun testTableEditor() {
        if (skip) return
        DbTestSupport.exec(ds, "DELETE FROM opendata.events WHERE id >= 100")
        DbTestSupport.exec(ds, "INSERT INTO opendata.events (id, name, payload) VALUES (100, 'ch-before', 'p')")
        val c = TableDataController(project, DbSession(ds.id), DbTestSupport.table(ds, "opendata", "events"))
        try {
            assertTrue(c.isEditable)
            c.where = "id = 100"
            DbTestSupport.loadFirstPage(c)
            val m = c.hookUp.mutator
            val nameCol = c.model.columns.indexOfFirst { it.name == "name" }
            ApplicationManager.getApplication().invokeAndWait {
                m.mutate(GridRequestSource(null), ModelIndexSet.forRows(c.model, 0), ModelIndexSet.forColumns(c.model, nameCol), "ch-after", true)
            }
            var src = GridRequestSource(null)
            ApplicationManager.getApplication().invokeAndWait { m.submit(src, true) }
            DbTestSupport.waitFor(src)
            assertEquals("ch-after", DbTestSupport.scalar(ds, "SELECT name FROM opendata.events WHERE id = 100"))

            ApplicationManager.getApplication().invokeAndWait { m.deleteRows(GridRequestSource(null), ModelIndexSet.forRows(c.model, 0)) }
            src = GridRequestSource(null)
            ApplicationManager.getApplication().invokeAndWait { m.submit(src, true) }
            DbTestSupport.waitFor(src)
            assertEquals("0", DbTestSupport.scalar(ds, "SELECT count() FROM opendata.events WHERE id = 100"))
        } finally {
            Disposer.dispose(c)
            DbTestSupport.exec(ds, "DELETE FROM opendata.events WHERE id >= 100")
        }
    }
}
