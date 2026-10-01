package io.opendata.integration

import com.intellij.credentialStore.OneTimeString
import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.dataSource.DatabaseConnectionManager
import com.intellij.database.dataSource.DatabaseDriver
import com.intellij.database.dataSource.DatabaseDriverManager
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.util.DasUtil
import com.intellij.database.util.DataSourceUtil
import com.intellij.database.util.LoaderContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.classpath.SimpleClasspathElementFactory
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * Этап 1 (ТЗ 31) и часть этапов 2/4 через ШТАТНЫЙ API Database Tools and SQL — без собственного DB-слоя:
 * PostgreSQL Data Source → драйвер из Driver Manager → Test Connection → introspection → запрос, commit/rollback
 * через соединение Database Tools.
 *
 * Все используемые классы/методы найдены research'ем в DataGrip 2026.2.6 (262.10968.148), см.
 * docs/jetbrains-db-analysis.md и реестр internal API в docs/architecture.md.
 *
 * Требует OPENDATA_PG_URL (scripts/test.ps1 -WithPostgres); без него тесты пропускаются.
 */
class DatabaseToolsPostgresScenarioTest : BasePlatformTestCase() {

    // DB-операции Database Tools запрещено выполнять в EDT (ТЗ 24) — тест работает в фоновом потоке.
    override fun runInDispatchThread(): Boolean = false

    private val url: String? = System.getenv("OPENDATA_PG_URL")?.takeIf { it.isNotBlank() }
    private val user: String = System.getenv("OPENDATA_PG_USER")?.takeIf { it.isNotBlank() } ?: "opendata"
    private val password: String = System.getenv("OPENDATA_PG_PASSWORD")?.takeIf { it.isNotBlank() } ?: "opendata"

    override fun setUp() {
        super.setUp()
        // Database Tools запускает JDBC-драйвер во внешнем процессе с рабочим каталогом = basePath проекта.
        // Лёгкий тестовый проект переиспользуется между классами, и его временный каталог может быть уже удалён.
        project.basePath?.let { Files.createDirectories(Path.of(it)) }
    }

    private fun skipWithoutDb(): Boolean {
        if (url == null) println("SKIPPED: OPENDATA_PG_URL not set")
        return url == null
    }

    /** Штатный драйвер PostgreSQL из Driver Manager (ТЗ 13). */
    private fun postgresDriver(): DatabaseDriver {
        val drivers = DatabaseDriverManager.getInstance().drivers
        return drivers.firstOrNull { it.id == "postgresql" }
            ?: drivers.firstOrNull { it.id.contains("postgres", ignoreCase = true) }
            ?: error("PostgreSQL driver not found; drivers: ${drivers.map { it.id }}")
    }

    /**
     * PostgreSQL Data Source штатными средствами (ТЗ 11). Файл JDBC-драйвера берётся из тестового classpath,
     * чтобы тест не зависел от загрузки драйвера из сети (в UI это делает Driver Manager → Download).
     */
    private fun createDataSource(): LocalDataSource {
        val driver = postgresDriver()
        val jar = Path.of(System.getProperty("opendata.pg.driver.jar") ?: error("opendata.pg.driver.jar is not set (see build.gradle.kts)"))
        driver.additionalClasspathElements = SimpleClasspathElementFactory.createElements(VfsUtilCore.pathToUrl(jar.toString()))
        DatabaseDriverManager.getInstance().updateDriver(driver)

        val ds = LocalDataSource.fromDriver(driver, url!!, false)
        ds.name = "PostgreSQL Local"
        ds.username = user
        ds.passwordStorage = LocalDataSource.Storage.MEMORY
        DatabaseCredentials.getInstance().storePassword(ds, OneTimeString(password))
        ApplicationManager.getApplication().invokeAndWait {
            LocalDataSourceManager.getInstance(project).addDataSource(ds)
        }
        return ds
    }

    private fun removeDataSource(ds: LocalDataSource) {
        ApplicationManager.getApplication().invokeAndWait {
            LocalDataSourceManager.getInstance(project).removeDataSource(ds)
        }
    }

    private fun <T> withConnection(ds: LocalDataSource, block: (com.intellij.database.dataSource.DatabaseConnection) -> T): T {
        // Штатный suspend-API подключения Database Tools (DatabaseConnectionManager.Builder.create) в корутине.
        val ref = runBlocking { DatabaseConnectionManager.getInstance().build(project, ds).setAskPassword(false).create() }
            ?: error("Database Tools returned no connection for ${ds.name}")
        try {
            return block(ref.get())
        } finally {
            ref.close()
        }
    }

    fun testDataSourceIsRegistered() {
        if (skipWithoutDb()) return
        val ds = createDataSource()
        try {
            assertTrue(LocalDataSourceManager.getInstance(project).dataSources.contains(ds))
            assertEquals("PostgreSQL Local", ds.name)
            assertTrue("dialect: ${ds.dbms}", ds.dbms.name.contains("POSTGRES", ignoreCase = true))
        } finally {
            removeDataSource(ds)
        }
    }

    /** Test Connection + SELECT version() через соединение Database Tools (ТЗ 31, 32). */
    fun testConnectAndSelectVersion() {
        if (skipWithoutDb()) return
        val ds = createDataSource()
        try {
            val version = withConnection(ds) { c ->
                val st = c.remoteConnection.createStatement()
                try {
                    val rs = st.executeQuery("SELECT version()")
                    assertTrue(rs.next())
                    rs.getString(1)
                } finally {
                    st.close()
                }
            }
            println("Database Tools connection: $version")
            assertTrue(version, version.startsWith("PostgreSQL"))
        } finally {
            removeDataSource(ds)
        }
    }

    /** Schema introspection штатным механизмом (ТЗ 23, 31): таблицы и колонки в модели Database Tools. */
    fun testIntrospectionLoadsTables() {
        if (skipWithoutDb()) return
        val ds = createDataSource()
        try {
            DataSourceUtil.performManualSyncTask(LoaderContext.selectGeneralTask(project, ds))
                .toFuture().get(180, TimeUnit.SECONDS)

            val tables = DasUtil.getTables(ds).toList()
            val names = tables.map { it.name }.toSet()
            println("Introspected tables: $names")
            assertTrue("tables: $names", names.containsAll(listOf("opendata_test", "users", "orders")))

            val columns = DasUtil.getColumns(tables.first { it.name == "opendata_test" }).map { it.name }.toList()
            assertEquals(listOf("id", "name", "created_at"), columns)
        } finally {
            removeDataSource(ds)
        }
    }

    /** UPDATE + Commit/Rollback через соединение Database Tools (ТЗ 19, 34). */
    fun testCommitAndRollbackThroughDatabaseTools() {
        if (skipWithoutDb()) return
        val ds = createDataSource()
        try {
            withConnection(ds) { c ->
                val conn = c.remoteConnection
                conn.autoCommit = false
                val st = conn.createStatement()
                try {
                    val rs = st.executeQuery("INSERT INTO opendata_test(name) VALUES ('dt-before') RETURNING id")
                    assertTrue(rs.next())
                    val id = rs.getInt(1)
                    conn.commit()

                    st.execute("UPDATE opendata_test SET name = 'dt-rolled-back' WHERE id = $id")
                    conn.rollback()
                    assertEquals("dt-before", name(st, id))

                    st.execute("UPDATE opendata_test SET name = 'dt-committed' WHERE id = $id")
                    conn.commit()
                    assertEquals("dt-committed", name(st, id))

                    st.execute("DELETE FROM opendata_test WHERE id = $id")
                    conn.commit()
                } finally {
                    st.close()
                }
            }
        } finally {
            removeDataSource(ds)
        }
    }

    private fun name(st: com.intellij.database.remote.jdbc.RemoteStatement, id: Int): String {
        val rs = st.executeQuery("SELECT name FROM opendata_test WHERE id = $id")
        assertTrue(rs.next())
        return rs.getString(1)
    }
}
