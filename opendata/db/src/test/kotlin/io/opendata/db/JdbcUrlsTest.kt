package io.opendata.db

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.drivers.DriverService
import io.opendata.db.model.DataSourceConfig
import io.opendata.db.model.DbKind
import io.opendata.db.model.JdbcUrls

/** JDBC URL подключения: тип СУБД по префиксу, разбор адреса, проверка соответствия типу. */
class JdbcUrlsTest : BasePlatformTestCase() {

    fun testKindByPrefix() {
        assertEquals(DbKind.POSTGRESQL, JdbcUrls.kindOf("jdbc:postgresql://h/db"))
        assertEquals(DbKind.CLICKHOUSE, JdbcUrls.kindOf(" JDBC:ClickHouse://h:8123/db"))
        assertEquals(DbKind.CLICKHOUSE, JdbcUrls.kindOf("jdbc:ch://h:8123"))
        assertEquals(DbKind.DREMIO, JdbcUrls.kindOf("jdbc:arrow-flight-sql://h:32010/?useEncryption=false"))
        assertNull(JdbcUrls.kindOf("jdbc:mysql://h/db"))
    }

    fun testParse() {
        val p = JdbcUrls.parse("jdbc:postgresql://gp-master.local:6543/sales?sslmode=require&user=etl")
        assertEquals("gp-master.local", p.host)
        assertEquals(6543, p.port)
        assertEquals("sales", p.database)
        assertEquals("sslmode=require&user=etl", p.params)

        val multi = JdbcUrls.parse("jdbc:postgresql://h1:5432,h2:5433/db")
        assertEquals("h1", multi.host); assertEquals(5432, multi.port)

        val v6 = JdbcUrls.parse("jdbc:postgresql://[::1]:5432/db")
        assertEquals("::1", v6.host); assertEquals(5432, v6.port)

        val dremio = JdbcUrls.parse("jdbc:arrow-flight-sql://dremio:32010/?useEncryption=false")
        assertEquals("dremio", dremio.host); assertEquals(32010, dremio.port); assertNull(dremio.database)

        val noPort = JdbcUrls.parse("jdbc:clickhouse://ch/analytics")
        assertEquals("ch", noPort.host); assertNull(noPort.port); assertEquals("analytics", noPort.database)
    }

    fun testMatchesAndAddress() {
        assertTrue(JdbcUrls.matches(DbKind.GREENPLUM, "jdbc:postgresql://h/db"))
        assertTrue(JdbcUrls.matches(DbKind.CLOUDBERRY, "jdbc:postgresql://h/db"))
        assertFalse(JdbcUrls.matches(DbKind.POSTGRESQL, "jdbc:clickhouse://h/db"))
        assertTrue("неизвестный префикс не блокируется", JdbcUrls.matches(DbKind.POSTGRESQL, "jdbc:custom://h"))

        val cfg = DataSourceConfig().apply { kind = DbKind.DREMIO; host = "localhost"; port = 5432; url = "jdbc:arrow-flight-sql://dremio.corp:32010/?useEncryption=false" }
        assertEquals("адрес берётся из URL, а не из полей", "dremio.corp:32010", JdbcUrls.address(cfg))
        assertEquals("jdbc:arrow-flight-sql://dremio.corp:32010/?useEncryption=false", cfg.effectiveUrl())
        val fields = DataSourceConfig().apply { kind = DbKind.GREENPLUM; host = "gp"; port = 5432; database = "dwh" }
        assertEquals("jdbc:postgresql://gp:5432/dwh", fields.effectiveUrl())
        assertEquals("gp:5432", JdbcUrls.address(fields))
    }

    /** URL другого типа СУБД — понятная ошибка, а не «драйвер не принял URL». */
    fun testMismatchedUrlGivesClearError() {
        val cfg = DataSourceConfig().apply { kind = DbKind.POSTGRESQL; url = "jdbc:clickhouse://localhost:8123/default" }
        val e = runCatching { DriverService.getInstance().connect(cfg, null, null) }.exceptionOrNull()
        assertNotNull(e)
        assertTrue(e!!.message, e.message!!.contains("не подходит для типа подключения PostgreSQL"))
    }

    /** Все драйверы входят в поставку: каталог drivers плагина или OPENDATA_DRIVERS_DIR. */
    fun testAllDriversAvailableWithoutDownload() {
        if (DbTestSupport.env("OPENDATA_DRIVERS_DIR") == null) return
        for (k in DbKind.entries) assertTrue(k.displayName, DriverService.getInstance().isAvailable(k))
    }
}

/** Диалог подключения: вставленный URL заполняет тип, хост, порт и базу; URL сохраняется. */
class DataSourceDialogUrlTest : BasePlatformTestCase() {
    private fun field(d: Any, name: String) = d.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(d)

    fun testPastedUrlFillsFieldsAndIsSaved() {
        val d = io.opendata.db.ui.DataSourceDialog(project, DataSourceConfig())
        try {
            (field(d, "url") as javax.swing.text.JTextComponent).text = "jdbc:clickhouse://ch.corp:8443/analytics?ssl=true"
            assertEquals(DbKind.CLICKHOUSE, (field(d, "kindBox") as javax.swing.JComboBox<*>).selectedItem)
            assertEquals("ch.corp", (field(d, "host") as javax.swing.text.JTextComponent).text)
            assertEquals("8443", (field(d, "port") as javax.swing.text.JTextComponent).text)
            assertEquals("analytics", (field(d, "database") as javax.swing.text.JTextComponent).text)
            val apply = d.javaClass.getDeclaredMethod("apply", DataSourceConfig::class.java).apply { isAccessible = true }
            val saved = DataSourceConfig()
            apply.invoke(d, saved)
            assertEquals(DbKind.CLICKHOUSE, saved.kind)
            assertEquals("URL с параметрами сохраняется как есть", "jdbc:clickhouse://ch.corp:8443/analytics?ssl=true", saved.url)
            assertEquals("jdbc:clickhouse://ch.corp:8443/analytics?ssl=true", saved.effectiveUrl())

            // Изменение полей пересобирает URL без параметров; совпадающий с полями URL не хранится.
            (field(d, "url") as javax.swing.text.JTextComponent).text = "jdbc:postgresql://gp:5432/dwh"
            (field(d, "host") as javax.swing.text.JTextComponent).text = "gp2"
            assertEquals("jdbc:postgresql://gp2:5432/dwh", (field(d, "url") as javax.swing.text.JTextComponent).text)
            val saved2 = DataSourceConfig()
            apply.invoke(d, saved2)
            assertEquals("", saved2.url)
            assertEquals("jdbc:postgresql://gp2:5432/dwh", saved2.effectiveUrl())
        } finally {
            com.intellij.openapi.util.Disposer.dispose(d.disposable)
        }
    }
}
