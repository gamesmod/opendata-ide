package io.opendata.db

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.DbTestSupport.use
import io.opendata.db.grid.TableDataController
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.DdlGenerator
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.session.DbSession
import io.opendata.db.session.StatementResult

/**
 * Dremio через Arrow Flight SQL JDBC: introspection, запросы, DDL представлений, режим только чтения.
 * Требует OPENDATA_DREMIO_URL (jdbc:arrow-flight-sql://localhost:32010/?useEncryption=false), пространство opendata
 * с представлением events_v (см. docker/dremio/init.sh).
 */
class DremioIntegrationTest : BasePlatformTestCase() {
    override fun runInDispatchThread(): Boolean = false

    private lateinit var ds: DataSourceConfig
    private var skip = false

    override fun setUp() {
        super.setUp()
        val cfg = DbTestSupport.dremio()
        if (cfg == null) skip = true else ds = cfg
    }

    private fun view() = DbObject(DbObjectKind.VIEW, "events_v", schema = "opendata", table = "events_v")

    fun testIntrospection() {
        if (skip) return
        DbSession(ds.id).use { s ->
            val m = MetadataLoader(s.connection(), ds.kind)
            val space = m.containers().first { it.name == "opendata" }
            val folders = m.children(space).associateBy { it.kind }
            assertTrue(m.children(folders.getValue(DbObjectKind.VIEWS_FOLDER)).any { it.name == "events_v" })
            assertEquals(listOf("id", "name"), m.columns(view()).map { it.name })
        }
    }

    fun testQueryAndViewDdl() {
        if (skip) return
        DbSession(ds.id).use { s ->
            val r = s.execute("SELECT * FROM opendata.events_v ORDER BY id").single() as StatementResult.Rows
            assertEquals(3, r.rows.size)
            assertEquals(listOf("id", "name"), r.columns.map { it.name })
            val f = s.execute("SELECT * FROM opendata.no_such_view").single() as StatementResult.Failure
            assertTrue(f.error.message, f.error.message.isNotBlank())
            val ddl = DdlGenerator(s.connection(), ds.kind).ddl(view())
            assertTrue(ddl, ddl.startsWith("CREATE OR REPLACE VIEW"))
            assertTrue(ddl, ddl.contains("VALUES", ignoreCase = true))
        }
    }

    fun testTableEditorIsReadOnlyWithPaging() {
        if (skip) return
        val c = TableDataController(project, DbSession(ds.id), view())
        try {
            assertFalse(c.isEditable)
            c.orderBy = "id"
            val rows = DbTestSupport.loadFirstPage(c)
            assertEquals(3, rows.rows.size)
            assertEquals(3L, c.count())
        } finally {
            Disposer.dispose(c)
        }
    }
}
