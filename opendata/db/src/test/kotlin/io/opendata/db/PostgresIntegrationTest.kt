package io.opendata.db

import com.intellij.database.datagrid.GridRequestSource
import com.intellij.database.datagrid.ModelIndexSet
import com.intellij.database.run.ReservedCellValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.DbTestSupport.use
import io.opendata.db.grid.ResultGrids
import io.opendata.db.grid.TableDataController
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.DdlGenerator
import io.opendata.db.meta.MetadataCache
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.session.DbSession
import io.opendata.db.session.StatementResult
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Сквозные тесты OpenData DB-слоя на PostgreSQL (ТЗ 36) и Cloudberry-режиме (тот же протокол/каталог).
 * Требуют OPENDATA_PG_URL и тестовую схему docker/postgres/init; без них пропускаются.
 */
class PostgresIntegrationTest : BasePlatformTestCase() {
    override fun runInDispatchThread(): Boolean = false

    private lateinit var ds: DataSourceConfig
    private var skip = false

    override fun setUp() {
        super.setUp()
        val cfg = DbTestSupport.postgres(project)
        if (cfg == null) skip = true else ds = cfg
    }

    private fun session() = DbSession(ds.id)

    fun testIntrospectionTree() {
        if (skip) return
        session().use { s ->
            val m = MetadataLoader(s.connection(), ds.kind)
            val public = m.containers().first { it.name == "public" }
            val folders = m.children(public).associateBy { it.kind }
            fun names(k: DbObjectKind) = m.children(folders.getValue(k)).map { it.name }
            assertTrue(names(DbObjectKind.TABLES_FOLDER).containsAll(listOf("opendata_test", "users", "orders", "big_table")))
            assertTrue(names(DbObjectKind.VIEWS_FOLDER).contains("user_order_totals"))
            assertTrue(names(DbObjectKind.FUNCTIONS_FOLDER).contains("touch_created_at"))
            assertTrue(names(DbObjectKind.SEQUENCES_FOLDER).contains("opendata_seq"))
            assertTrue(names(DbObjectKind.TYPES_FOLDER).contains("order_status"))

            val orders = DbObject(DbObjectKind.TABLE, "orders", schema = "public", table = "orders")
            val parts = m.children(orders).associateBy { it.kind }
            assertEquals(listOf("id", "user_id", "amount", "note", "attachment", "created_at"), m.children(parts.getValue(DbObjectKind.COLUMNS_FOLDER)).map { it.name })
            assertEquals("id", m.children(parts.getValue(DbObjectKind.KEYS_FOLDER)).single().detail)
            assertTrue(m.children(parts.getValue(DbObjectKind.FOREIGN_KEYS_FOLDER)).single().detail!!.contains("users(id)"))
            assertTrue(m.children(parts.getValue(DbObjectKind.INDEXES_FOLDER)).any { it.name == "orders_user_idx" })
            assertTrue(m.children(parts.getValue(DbObjectKind.TRIGGERS_FOLDER)).any { it.name == "orders_touch" })
            assertTrue(m.children(parts.getValue(DbObjectKind.CONSTRAINTS_FOLDER)).any { it.detail!!.startsWith("CHECK") })
            assertEquals(listOf("id"), m.keyColumns(DbObject(DbObjectKind.TABLE, "opendata_test", schema = "public", table = "opendata_test")))
        }
    }

    fun testDdl() {
        if (skip) return
        session().use { s ->
            val g = DdlGenerator(s.connection(), ds.kind)
            val table = g.ddl(DbObject(DbObjectKind.TABLE, "orders", schema = "public", table = "orders"))
            assertTrue(table, table.startsWith("CREATE TABLE public.orders ("))
            assertTrue(table, table.contains("FOREIGN KEY (user_id) REFERENCES users(id)"))
            assertTrue(table, table.contains("CREATE INDEX orders_user_idx"))
            assertTrue(table, table.contains("CREATE TRIGGER orders_touch"))
            assertTrue(g.ddl(DbObject(DbObjectKind.VIEW, "user_order_totals", schema = "public", table = "user_order_totals")).startsWith("CREATE OR REPLACE VIEW"))
            assertTrue(g.ddl(DbObject(DbObjectKind.FUNCTION, "touch_created_at", schema = "public")).contains("CREATE OR REPLACE FUNCTION public.touch_created_at()"))
            assertTrue(g.ddl(DbObject(DbObjectKind.SEQUENCE, "opendata_seq", schema = "public")).startsWith("CREATE SEQUENCE public.opendata_seq"))
            assertEquals("CREATE TYPE public.order_status AS ENUM ('new', 'paid', 'shipped');",
                g.ddl(DbObject(DbObjectKind.TYPE, "order_status", schema = "public", detail = "enum")))
            assertEquals("CREATE SCHEMA public;", g.ddl(DbObject(DbObjectKind.SCHEMA, "public")))
        }
    }

    fun testSelectIntoDataGrid() {
        if (skip) return
        session().use { s ->
            val r = s.execute("SELECT generate_series AS id, md5(generate_series::text) AS value FROM generate_series(1,100)").single()
            r as StatementResult.Rows
            assertEquals(listOf("id", "value"), r.columns.map { it.name })
            assertEquals(100, r.rows.size)
            ApplicationManager.getApplication().invokeAndWait {
                val holder = com.intellij.openapi.util.Disposer.newDisposable()
                try {
                    val grid = ResultGrids.createReadOnlyGrid(project, r, holder)
                    assertEquals(100, grid.getDataModel(com.intellij.database.run.ui.DataAccessType.DATA_WITH_MUTATIONS).rowCount)
                    assertEquals(2, grid.getDataModel(com.intellij.database.run.ui.DataAccessType.DATA_WITH_MUTATIONS).columnCount)
                } finally {
                    com.intellij.openapi.util.Disposer.dispose(holder)
                }
            }
        }
    }

    fun testErrorWithPositionAndScriptResults() {
        if (skip) return
        session().use { s ->
            val f = s.execute("SELECT * FROM no_such_table_xyz").single() as StatementResult.Failure
            assertEquals("42P01", f.error.sqlState)
            assertEquals(15, f.error.position)
            assertEquals("ERROR", f.error.severity)
            val c = s.execute("INSERT INTO orders(user_id, amount) VALUES (999, -1)").single() as StatementResult.Failure
            assertNotNull(c.error.constraint)
            val u = s.execute("UPDATE opendata_test SET name = name WHERE id < 0").single() as StatementResult.Update
            assertEquals(0L, u.count)
        }
    }

    fun testCancelLongQuery() {
        if (skip) return
        session().use { s ->
            val pool = Executors.newSingleThreadExecutor()
            try {
                val future = pool.submit<List<StatementResult>> { s.execute("SELECT pg_sleep(30)") }
                Thread.sleep(700)
                s.cancel()
                val r = future.get(15, TimeUnit.SECONDS).single() as StatementResult.Failure
                assertEquals("57014", r.error.sqlState)
                assertEquals(1, (s.execute("SELECT 1").single() as StatementResult.Rows).rows.size)
            } finally {
                pool.shutdownNow()
            }
        }
    }

    fun testManualTransactionCommitRollback() {
        if (skip) return
        val id = DbTestSupport.scalar(ds, "INSERT INTO opendata_test(name) VALUES ('tx-before') RETURNING id")!!
        try {
            session().use { s ->
                s.autoCommit = false
                s.execute("UPDATE opendata_test SET name = 'tx-rolled-back' WHERE id = $id")
                s.rollback()
                assertEquals("tx-before", DbTestSupport.scalar(ds, "SELECT name FROM opendata_test WHERE id = $id"))
                s.execute("UPDATE opendata_test SET name = 'tx-committed' WHERE id = $id")
                assertEquals("tx-before", DbTestSupport.scalar(ds, "SELECT name FROM opendata_test WHERE id = $id"))
                s.commit()
                assertEquals("tx-committed", DbTestSupport.scalar(ds, "SELECT name FROM opendata_test WHERE id = $id"))
            }
        } finally {
            DbTestSupport.exec(ds, "DELETE FROM opendata_test WHERE id = $id")
        }
    }

    /** Редактирование через DataGrid: edit cell → Revert; edit → Submit; insert (generated key, DEFAULT); delete. */
    fun testTableEditorSubmitRevertInsertDelete() {
        if (skip) return
        val id = DbTestSupport.scalar(ds, "INSERT INTO opendata_test(name) VALUES ('grid-before') RETURNING id")!!
        val c = TableDataController(project, DbSession(ds.id), DbObject(DbObjectKind.TABLE, "opendata_test", schema = "public", table = "opendata_test"))
        try {
            assertTrue(c.isEditable)
            assertEquals(listOf("id"), c.keyColumns)
            c.where = "id = $id"
            DbTestSupport.loadFirstPage(c)
            val m = c.hookUp.mutator
            val nameCol = c.model.columns.indexOfFirst { it.name == "name" }
            val rows = ModelIndexSet.forRows(c.model, 0)
            val cols = ModelIndexSet.forColumns(c.model, nameCol)

            ApplicationManager.getApplication().invokeAndWait { m.mutate(GridRequestSource(null), rows, cols, "grid-reverted", true) }
            assertTrue(m.hasPendingChanges())
            assertTrue(m.pendingChanges, m.pendingChanges.startsWith("UPDATE public.opendata_test SET name = ? WHERE id = ?"))
            ApplicationManager.getApplication().invokeAndWait { m.revertAll(GridRequestSource(null)) }
            assertFalse(m.hasPendingChanges())

            ApplicationManager.getApplication().invokeAndWait { m.mutate(GridRequestSource(null), rows, cols, "grid-after", true) }
            var src = GridRequestSource(null)
            ApplicationManager.getApplication().invokeAndWait { m.submit(src, true) }
            DbTestSupport.waitFor(src)
            assertEquals("grid-after", DbTestSupport.scalar(ds, "SELECT name FROM opendata_test WHERE id = $id"))
            assertFalse(m.hasPendingChanges())

            // Вставка: id и created_at — DEFAULT (generated key), name — значение.
            c.where = "name LIKE 'grid-%'"
            DbTestSupport.loadFirstPage(c)
            val base = c.model.rowCount
            ApplicationManager.getApplication().invokeAndWait {
                m.insertRows(GridRequestSource(null), 1)
                m.mutate(GridRequestSource(null), ModelIndexSet.forRows(c.model, base), cols, "grid-inserted", true)
            }
            assertTrue(m.pendingChanges, m.pendingChanges.contains("INSERT INTO public.opendata_test (name) VALUES (?)"))
            src = GridRequestSource(null)
            ApplicationManager.getApplication().invokeAndWait { m.submit(src, true) }
            DbTestSupport.waitFor(src)
            val newId = DbTestSupport.scalar(ds, "SELECT id FROM opendata_test WHERE name = 'grid-inserted'")
            assertNotNull(newId)
            assertNotNull(DbTestSupport.scalar(ds, "SELECT created_at FROM opendata_test WHERE id = $newId"))

            // Удаление вставленной строки и NULL.
            c.where = "id = $newId"
            DbTestSupport.loadFirstPage(c)
            ApplicationManager.getApplication().invokeAndWait { m.deleteRows(GridRequestSource(null), ModelIndexSet.forRows(c.model, 0)) }
            src = GridRequestSource(null)
            ApplicationManager.getApplication().invokeAndWait { m.submit(src, true) }
            DbTestSupport.waitFor(src)
            assertEquals("0", DbTestSupport.scalar(ds, "SELECT count(*) FROM opendata_test WHERE id = $newId"))

            c.where = "id = $id"
            DbTestSupport.loadFirstPage(c)
            ApplicationManager.getApplication().invokeAndWait { m.mutate(GridRequestSource(null), rows, cols, ReservedCellValue.NULL, true) }
            src = GridRequestSource(null)
            ApplicationManager.getApplication().invokeAndWait { m.submit(src, true) }
            DbTestSupport.waitFor(src)
            assertNull(DbTestSupport.scalar(ds, "SELECT name FROM opendata_test WHERE id = $id"))
        } finally {
            com.intellij.openapi.util.Disposer.dispose(c)
            DbTestSupport.exec(ds, "DELETE FROM opendata_test WHERE id = $id OR name LIKE 'grid-%'")
        }
    }

    fun testPagingAndCount() {
        if (skip) return
        val c = TableDataController(project, DbSession(ds.id), DbObject(DbObjectKind.TABLE, "big_table", schema = "public", table = "big_table"))
        try {
            c.orderBy = "id"
            val first = DbTestSupport.loadFirstPage(c)
            assertEquals(500, first.rows.size)
            assertEquals(1, (first.rows.first()[0] as Number).toInt())
            assertFalse(c.pager.isLastPage)
            val second = c.fetch(500, 500) as StatementResult.Rows
            assertEquals(501, (second.rows.first()[0] as Number).toInt())
            assertEquals(200_000L, c.count())
            assertFalse(c.isEditable) // нет первичного ключа → только чтение
        } finally {
            com.intellij.openapi.util.Disposer.dispose(c)
        }
    }

    fun testCompletionMetadata() {
        if (skip) return
        val cache = MetadataCache.getInstance()
        cache.invalidate(ds.id)
        cache.prefetch(ds.id)
        val deadline = System.currentTimeMillis() + 30_000
        while (cache.tables(ds.id).isEmpty() && System.currentTimeMillis() < deadline) Thread.sleep(100)
        val users = cache.tables(ds.id).first { it.name == "users" }
        assertEquals("public", users.schema)
        assertTrue(users.columns.containsAll(listOf("id", "email", "full_name")))
    }

    fun testCloudberryModeOnPostgresProtocol() {
        val cb = DbTestSupport.cloudberry(project) ?: return
        DbSession(cb.id).use { s ->
            val m = MetadataLoader(s.connection(), cb.kind)
            assertTrue(m.containers().any { it.name == "public" })
            // На PostgreSQL pg_get_table_distributedby отсутствует — DDL без DISTRIBUTED BY, но без ошибки.
            val ddl = DdlGenerator(s.connection(), cb.kind).ddl(DbObject(DbObjectKind.TABLE, "opendata_test", schema = "public", table = "opendata_test"))
            assertTrue(ddl, ddl.startsWith("CREATE TABLE public.opendata_test"))
        }
    }
}
