package io.opendata.db

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.DbTestSupport.use
import io.opendata.db.data.CsvReader
import io.opendata.db.data.DataExporter
import io.opendata.db.data.DataImporter
import io.opendata.db.data.DataTransferUi
import io.opendata.db.data.ExportFormat
import io.opendata.db.data.ExportOptions
import io.opendata.db.data.ImportOptions
import io.opendata.db.drivers.DriverOverride
import io.opendata.db.drivers.DriverSettings
import io.opendata.db.meta.DbObject
import io.opendata.db.meta.DbObjectKind
import io.opendata.db.meta.MetadataLoader
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DataSourceStorage
import io.opendata.db.model.DataSources
import io.opendata.db.model.DbKind
import io.opendata.db.session.ColumnInfo
import io.opendata.db.session.DbSession
import io.opendata.db.ui.ConnectionsTransfer
import io.opendata.db.ui.ConsoleFiles
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Types

/** Подключения в проекте, менеджер драйверов, импорт и экспорт данных. */
class DataTransferTest : BasePlatformTestCase() {
    override fun runInDispatchThread(): Boolean = false

    private lateinit var tmp: Path

    override fun setUp() {
        super.setUp()
        tmp = Files.createTempDirectory("opendata-transfer")
    }

    override fun tearDown() {
        try {
            tmp.toFile().deleteRecursively()
            DriverSettings.getInstance().loadState(DriverSettings.State())
        } finally {
            super.tearDown()
        }
    }

    private fun col(name: String, type: Int = Types.VARCHAR) = ColumnInfo(name, type, null, null, null, null)

    // ---------------------------------------------------------------- подключения в проекте

    fun testDataSourcesBelongToProject() {
        val storage = DataSourceStorage.getInstance(project)
        val ds = DataSourceConfig().apply { name = "project-scoped"; host = "db.local" }
        storage.addOrUpdate(ds)
        try {
            assertSame(ds, DataSources.find(ds.id))
            // Состояние сервиса проекта — тот же XML, что пишется в .idea/opendata-datasources.xml.
            val xml = ConnectionsTransfer.toXml(storage.dataSources)
            assertTrue(xml.contains("db.local"))
            assertFalse("пароли не экспортируются", xml.contains("password"))
            val before = storage.dataSources.size
            assertEquals(before, ConnectionsTransfer.importInto(storage, xml))
            assertEquals("при совпадении id добавляется копия", before * 2, storage.dataSources.size)
        } finally {
            storage.dataSources.filter { it.name == "project-scoped" }.forEach { storage.remove(it.id) }
        }
        assertNull(DataSources.find(ds.id))
    }

    fun testConsoleFilesLiveInProject() {
        // Путь консоли: <проект>/.idea/opendata/consoles/<id источника>/console.sql
        assertTrue(ConsoleFiles.consolesDir(project).endsWith(Path.of("opendata", "consoles")))
        val f = tmp.resolve(".idea/opendata/consoles/abc-123/console.sql")
        Files.createDirectories(f.parent)
        Files.writeString(f, "select 1;")
        val vf = com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshAndFindFileByNioFile(f)!!
        assertEquals("abc-123", ConsoleFiles.dataSourceIdOf(vf))
        assertNull(ConsoleFiles.dataSourceIdOf(com.intellij.testFramework.LightVirtualFile("x.sql")))
    }

    // ---------------------------------------------------------------- менеджер драйверов

    fun testDriverSettingsOverrideDefaults() {
        val s = DriverSettings.getInstance()
        assertEquals("jdbc:postgresql://h:1/d", s.buildUrl(DbKind.POSTGRESQL, "h", 1, "d"))
        assertEquals(DbKind.CLICKHOUSE.driver.jars, s.artifacts(DbKind.CLICKHOUSE))
        s.set(DriverOverride().apply {
            kind = DbKind.POSTGRESQL
            urlTemplate = "jdbc:postgresql://{host}:{port}/{database}?ssl=true"
            version = "42.7.4"
            properties = linkedMapOf("connectTimeout" to "5")
            jars = "/a/one.jar${java.io.File.pathSeparator}/b/two.jar"
        })
        assertEquals("jdbc:postgresql://h:1/d?ssl=true", s.buildUrl(DbKind.POSTGRESQL, "h", 1, "d"))
        val main = s.artifacts(DbKind.POSTGRESQL).first()
        assertEquals("42.7.4", main.version)
        assertEquals("у выбранной версии нет закреплённой суммы — проверка по SHA-1 Maven", "", main.sha256)
        assertEquals("5", s.defaultProperties(DbKind.POSTGRESQL)["connectTimeout"])
        assertEquals("unspecified", s.defaultProperties(DbKind.POSTGRESQL)["stringtype"])
        assertEquals(listOf(Path.of("/a/one.jar"), Path.of("/b/two.jar")), s.customJars(DbKind.POSTGRESQL))
        // Cloudberry использует тот же драйвер, но свои настройки.
        assertEquals("jdbc:postgresql://h:1/d", s.buildUrl(DbKind.CLOUDBERRY, "h", 1, "d"))
        // Пустые настройки удаляются.
        s.set(DriverOverride().apply { kind = DbKind.POSTGRESQL })
        assertEquals(DbKind.POSTGRESQL.urlTemplate, s.urlTemplate(DbKind.POSTGRESQL))
        assertTrue(s.state.drivers.isEmpty())
    }

    // ---------------------------------------------------------------- форматы

    fun testWritersAndCsvRoundTrip() {
        val cols = listOf(col("id", Types.INTEGER), col("name"), col("note"))
        val rows = listOf(arrayOf<Any?>(1, "a,b", "line1\nline2"), arrayOf<Any?>(2, "q\"uote", null))
        fun export(f: ExportFormat): String {
            val file = tmp.resolve("out.${f.extension}")
            assertEquals(2L, DataExporter.exportRows(cols, rows, file, ExportOptions(f, tableName = "t", kind = DbKind.POSTGRESQL)))
            return Files.readString(file)
        }
        val csv = export(ExportFormat.CSV)
        assertEquals("id,name,note\n1,\"a,b\",\"line1\nline2\"\n2,\"q\"\"uote\",\n", csv)
        CsvReader(StringReader(csv), ',').use { r ->
            assertEquals(listOf("id", "name", "note"), r.next())
            assertEquals(listOf("1", "a,b", "line1\nline2"), r.next())
            assertEquals(listOf("2", "q\"uote", null), r.next())
            assertNull(r.next())
        }
        assertEquals("id\tname\tnote\n1\ta,b\t\"line1\nline2\"\n2\t\"q\"\"uote\"\t\n", export(ExportFormat.TSV))
        assertEquals("[\n  {\"id\": 1, \"name\": \"a,b\", \"note\": \"line1\\nline2\"},\n  {\"id\": 2, \"name\": \"q\\\"uote\", \"note\": null}\n]\n",
            export(ExportFormat.JSON))
        assertEquals("INSERT INTO t (id, name, note) VALUES (1, 'a,b', 'line1\nline2');\nINSERT INTO t (id, name, note) VALUES (2, 'q\"uote', NULL);\n",
            export(ExportFormat.SQL_INSERT))
        assertTrue(export(ExportFormat.MARKDOWN).startsWith("| id | name | note |\n|---|---|---|\n| 1 | a,b | line1<br>line2 |"))
    }

    fun testConvertByColumnType() {
        assertEquals(42L, DataImporter.convert("42", Types.INTEGER))
        assertEquals(java.math.BigDecimal("1.50"), DataImporter.convert(" 1.50 ", Types.NUMERIC))
        assertEquals(true, DataImporter.convert("да", Types.BOOLEAN))
        assertNull(DataImporter.convert("", Types.BIGINT))
        assertEquals("", DataImporter.convert("", Types.VARCHAR))
        assertTrue(DataTransferUi.isReadQuery("-- c\n  select 1"))
        assertTrue(DataTransferUi.isReadQuery("WITH x AS (SELECT 1) SELECT * FROM x"))
        assertFalse(DataTransferUi.isReadQuery("DELETE FROM t RETURNING *"))
    }

    // ---------------------------------------------------------------- СУБД

    /** PostgreSQL: экспорт таблицы → импорт в новую таблицу → то же число строк; атомарность импорта. */
    fun testPostgresExportImport() {
        val ds = DbTestSupport.postgres(project) ?: return
        DbSession(ds.id).use { s ->
            val c = s.connection()
            val m = MetadataLoader(c, ds.kind)
            val users = DbObject(DbObjectKind.TABLE, "users", schema = "public", table = "users")
            val expected = c.createStatement().use { st -> st.executeQuery("SELECT count(*) FROM public.users").use { it.next(); it.getLong(1) } }

            val csv = tmp.resolve("users.csv")
            assertEquals(expected, DataExporter.exportQuery(c, "SELECT * FROM ${m.qualified(users)}", csv, ExportOptions(ExportFormat.CSV, kind = ds.kind), null))
            assertTrue(c.autoCommit)

            c.createStatement().use { it.execute("DROP TABLE IF EXISTS public.opendata_import") }
            val opts = ImportOptions(delimiter = ',', header = true)
            c.createStatement().use { it.execute(DataImporter.createTableSql(ds.kind, "public.opendata_import", DataImporter.fileColumns(csv, opts), m::quote)) }
            val r = DataImporter.import(c, ds.kind, "public.opendata_import", m::quote, csv, opts, null)
            assertEquals(expected, r.rows)
            assertTrue(r.skippedColumns.isEmpty())

            // Импорт в типизированную таблицу по заголовку, часть колонок, неизвестная колонка пропускается.
            Files.writeString(tmp.resolve("t.csv"), "name,unknown\nimported-1,x\n\"imported, 2\",y\n")
            val before = count(c, "public.opendata_test")
            val r2 = DataImporter.import(c, ds.kind, "public.opendata_test", m::quote, tmp.resolve("t.csv"), opts, null)
            assertEquals(2L, r2.rows)
            assertEquals(listOf("unknown"), r2.skippedColumns)
            assertEquals(before + 2, count(c, "public.opendata_test"))

            // Ошибка во второй строке — откатывается весь импорт.
            Files.writeString(tmp.resolve("bad.csv"), "id,name\n900001,ok\nnot-a-number,bad\n")
            val e = runCatching { DataImporter.import(c, ds.kind, "public.opendata_test", m::quote, tmp.resolve("bad.csv"), opts, null) }.exceptionOrNull()
            assertNotNull(e)
            assertTrue(e!!.message, e.message!!.contains("Строка 3"))
            assertEquals(before + 2, count(c, "public.opendata_test"))

            c.createStatement().use {
                it.execute("DELETE FROM public.opendata_test WHERE name IN ('imported-1', 'imported, 2')")
                it.execute("DROP TABLE public.opendata_import")
            }
        }
    }

    fun testClickHouseExportImport() {
        val ds = DbTestSupport.clickhouse(project) ?: return
        DbSession(ds.id).use { s ->
            val c = s.connection()
            val m = MetadataLoader(c, ds.kind)
            val json = tmp.resolve("events.json")
            assertEquals(3L, DataExporter.exportQuery(c, "SELECT id, name FROM opendata.events ORDER BY id", json, ExportOptions(ExportFormat.JSON, kind = ds.kind), null))
            assertTrue(Files.readString(json).contains("\"name\": \"alpha\""))

            val csv = tmp.resolve("ch.csv")
            Files.writeString(csv, "id,name\n10,x\n11,\"y, z\"\n")
            c.createStatement().use { it.execute("DROP TABLE IF EXISTS opendata.import_test") }
            c.createStatement().use { it.execute("CREATE TABLE opendata.import_test (id UInt64, name String) ENGINE = MergeTree ORDER BY id") }
            assertEquals(2L, DataImporter.import(c, ds.kind, "opendata.import_test", m::quote, csv, ImportOptions(), null).rows)
            assertEquals(2L, count(c, "opendata.import_test"))

            c.createStatement().use { it.execute("DROP TABLE IF EXISTS opendata.import_new") }
            c.createStatement().use { it.execute(DataImporter.createTableSql(ds.kind, "opendata.import_new", listOf("id", "name"), m::quote)) }
            assertEquals(2L, DataImporter.import(c, ds.kind, "opendata.import_new", m::quote, csv, ImportOptions(), null).rows)
            c.createStatement().use { it.execute("DROP TABLE opendata.import_test"); it.execute("DROP TABLE opendata.import_new") }
        }
    }

    fun testDremioExport() {
        val ds = DbTestSupport.dremio(project) ?: return
        DbSession(ds.id).use { s ->
            val file = tmp.resolve("dremio.csv")
            assertEquals(3L, DataExporter.exportQuery(s.connection(), "SELECT * FROM opendata.events_v ORDER BY id", file, ExportOptions(ExportFormat.CSV, kind = ds.kind), null))
            assertEquals("id,name", Files.readAllLines(file).first())
        }
    }

    private fun count(c: java.sql.Connection, table: String): Long =
        c.createStatement().use { st -> st.executeQuery("SELECT count(*) FROM $table").use { it.next(); it.getLong(1) } }
}
