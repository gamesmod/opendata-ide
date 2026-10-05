package io.opendata.db

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.DbTestSupport.use
import io.opendata.db.bulk.BulkFormat
import io.opendata.db.bulk.BulkLoader
import io.opendata.db.bulk.BulkMethod
import io.opendata.db.bulk.BulkOptions
import io.opendata.db.data.CsvReader
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.DdlGenerator
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.session.DbSession
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

/**
 * Bulk-загрузка: COPY (PostgreSQL, Greenplum, Cloudberry) и встроенный gpfdist (Greenplum 6/7, Cloudberry) на
 * настоящих кластерах. Без OPENDATA_GP_URL / OPENDATA_GP6_URL / OPENDATA_CB_URL MPP-часть пропускается.
 */
class BulkLoadTest : BasePlatformTestCase() {
    override fun runInDispatchThread(): Boolean = false

    private lateinit var dir: Path

    override fun setUp() {
        super.setUp()
        dir = Files.createTempDirectory("bulk")
    }

    override fun tearDown() {
        try { dir.toFile().deleteRecursively() } finally { super.tearDown() }
    }

    private val rows = 5000

    /** CSV с заголовком в другом регистре и порядке, запятыми и переводами строк в кавычках и пустыми значениями (NULL). */
    private fun csv(badRow: Boolean): Path = dir.resolve("load data.csv").also { f ->
        Files.writeString(f, buildString {
            append("NOTE,Id,name\n")
            for (i in 1..rows) {
                append(if (i % 11 == 0) "\"multi\nline, \"\"quoted\"\"\"" else if (i % 17 == 0) "" else "n$i").append(',')
                append(i).append(',').append("name $i").append('\n')
                if (badRow && i == rows / 2) append("x,not-a-number,bad\n")
            }
        })
    }

    private fun count(c: Connection, sql: String): Long = c.createStatement().use { st -> st.executeQuery(sql).use { it.next(); it.getLong(1) } }

    private fun recreate(c: Connection, mpp: Boolean) = c.createStatement().use {
        it.execute("DROP TABLE IF EXISTS opendata_bulk")
        it.execute("CREATE TABLE opendata_bulk (id int, name text, note text)" + if (mpp) " DISTRIBUTED BY (id)" else "")
    }

    private fun checkContent(c: Connection) {
        assertEquals(rows.toLong(), count(c, "SELECT count(*) FROM opendata_bulk"))
        assertEquals((rows / 11).toLong(), count(c, "SELECT count(*) FROM opendata_bulk WHERE note = E'multi\\nline, \"quoted\"'"))
        assertEquals("пустое поле без кавычек — NULL", (rows / 17 - rows / (11 * 17)).toLong(), count(c, "SELECT count(*) FROM opendata_bulk WHERE note IS NULL"))
        assertEquals((rows.toLong() * (rows + 1)) / 2, count(c, "SELECT sum(id) FROM opendata_bulk"))
    }

    /** PostgreSQL: COPY по заголовку, TRUNCATE, откат при ошибке. */
    fun testPostgresCopy() {
        val ds = DbTestSupport.postgres(project) ?: return
        DbSession(ds.id).use { s ->
            val c = s.connection()
            val q = MetadataLoader(c, ds.kind)::quote
            recreate(c, mpp = false)
            val r = BulkLoader.copy(c, ds.kind, "opendata_bulk", q, csv(badRow = false), BulkOptions(), null)
            assertEquals(BulkMethod.COPY, r.method)
            assertEquals(rows.toLong(), r.rows)
            checkContent(c)
            // Повтор с TRUNCATE не удваивает данные.
            BulkLoader.copy(c, ds.kind, "opendata_bulk", q, csv(badRow = false), BulkOptions(truncate = true), null)
            assertEquals(rows.toLong(), count(c, "SELECT count(*) FROM opendata_bulk"))
            // Ошибочная строка (PostgreSQL без SREH) — откат всего COPY и TRUNCATE.
            val e = runCatching { BulkLoader.copy(c, ds.kind, "opendata_bulk", q, csv(badRow = true), BulkOptions(truncate = true), null) }.exceptionOrNull()
            assertNotNull(e)
            assertEquals(rows.toLong(), count(c, "SELECT count(*) FROM opendata_bulk"))
            // TEXT с табуляцией и \N.
            val txt = dir.resolve("t.tsv").also { Files.writeString(it, "1\tone\t\\N\n2\ttwo\tx\n") }
            BulkLoader.copy(c, ds.kind, "opendata_bulk", q, txt, BulkOptions(BulkFormat.TEXT, '\t', header = false, nullString = "\\N", truncate = true), null)
            assertEquals(1L, count(c, "SELECT count(*) FROM opendata_bulk WHERE note IS NULL"))
            c.createStatement().use { it.execute("DROP TABLE opendata_bulk") }
        }
    }

    fun testGreenplum7() = mpp(DbTestSupport.greenplum(project, "OPENDATA_GP_URL"))
    fun testGreenplum6() = mpp(DbTestSupport.greenplum(project, "OPENDATA_GP6_URL"))
    fun testCloudberry() = mpp(DbTestSupport.realCloudberry(project))

    private fun mpp(ds: DataSourceConfig?) {
        ds ?: return
        DbSession(ds.id).use { s ->
            val c = s.connection()
            val m = MetadataLoader(c, ds.kind)
            val q = m::quote
            val host = DbTestSupport.env("OPENDATA_GPFDIST_HOST") ?: BulkLoader.clientAddress(c)
            println("${ds.name}: ${c.metaData.databaseProductVersion}; gpfdist host $host")

            // 1. COPY через координатор с журналом ошибок: плохая строка отклонена, остальные загружены.
            recreate(c, mpp = true)
            val copy = BulkLoader.copy(c, ds.kind, "opendata_bulk", q, csv(badRow = true), BulkOptions(rejectLimit = 10), null)
            assertEquals(rows.toLong(), copy.rows)
            assertEquals(1L, copy.rejected)
            checkContent(c)

            // 2. gpfdist: сегменты читают файл со встроенного сервера параллельно.
            val load = BulkLoader.gpfdistLoad(c, ds.kind, "opendata_bulk", q, csv(badRow = true), BulkOptions(rejectLimit = 10, truncate = true), host)
            assertEquals(BulkMethod.GPFDIST, load.method)
            assertEquals(rows.toLong(), load.rows)
            assertEquals(1L, load.rejected)
            checkContent(c)

            // 3. Без допуска ошибок плохая строка отменяет загрузку целиком.
            val fail = runCatching { BulkLoader.gpfdistLoad(c, ds.kind, "opendata_bulk", q, csv(badRow = true), BulkOptions(truncate = true), host) }.exceptionOrNull()
            assertNotNull(fail)
            assertEquals(rows.toLong(), count(c, "SELECT count(*) FROM opendata_bulk"))

            // 4. Выгрузка через writable external table: сегменты пишут файл, IDE добавляет заголовок.
            val out = dir.resolve("unload/opendata_bulk.csv")
            val unload = BulkLoader.gpfdistUnload(c, ds.kind, "opendata_bulk", q, out, BulkOptions(), host)
            assertEquals(rows.toLong(), unload.rows)
            CsvReader(Files.newBufferedReader(out), ',').use { r ->
                assertEquals(listOf("id", "name", "note"), r.next())
                var n = 0
                while (r.next() != null) n++
                assertEquals(rows, n)
            }

            // 5. Метаданные и DDL MPP: DISTRIBUTED BY, функции (в GP6 нет pg_proc.prokind).
            c.createStatement().use { it.execute("CREATE OR REPLACE FUNCTION opendata_f(x int) RETURNS int AS 'SELECT x + 1' LANGUAGE sql") }
            val ddl = DdlGenerator(c, ds.kind).ddl(DbObject(DbObjectKind.TABLE, "opendata_bulk", schema = "public", table = "opendata_bulk"))
            assertTrue(ddl, ddl.contains("DISTRIBUTED BY (id)"))
            val fn = DdlGenerator(c, ds.kind).ddl(DbObject(DbObjectKind.FUNCTION, "opendata_f", schema = "public"))
            assertTrue(fn, fn.contains("opendata_f"))
            val public = m.containers().first { it.name == "public" }
            val folders = m.children(public).associateBy { it.kind }
            assertTrue(m.children(folders.getValue(DbObjectKind.TABLES_FOLDER)).any { it.name == "opendata_bulk" })
            assertTrue(m.children(folders.getValue(DbObjectKind.FUNCTIONS_FOLDER)).any { it.name == "opendata_f" })

            c.createStatement().use { it.execute("DROP TABLE opendata_bulk"); it.execute("DROP FUNCTION opendata_f(int)") }
        }
    }
}
