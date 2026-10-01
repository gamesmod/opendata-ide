package io.opendata.integration

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Smoke-тесты ТЗ 36: plugin loading, Database Tools availability, SQL parsing.
 * Работают в headless-окружении платформы без БД.
 */
class DatabaseToolsAvailabilityTest : BasePlatformTestCase() {

    fun testOpenDataPluginLoaded() {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId("io.opendata.integration"))
        assertNotNull("OpenData integration plugin is not loaded", plugin)
    }

    fun testDatabaseToolsLoaded() {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(OpenDataDiagnostics.DATABASE_PLUGIN_ID))
        assertNotNull("Database Tools and SQL is not loaded", plugin)
        assertFalse("Database Tools and SQL is disabled", PluginManagerCore.isDisabled(plugin!!.pluginId))
    }

    fun testSqlLanguageRegistered() {
        val ids = OpenDataDiagnostics.sqlLanguages().map { it.id }
        assertTrue("SQL language not registered, found: $ids", ids.any { it.equals("SQL", ignoreCase = true) })
    }

    fun testPostgresDialectParsesValidSql() {
        val pg = OpenDataDiagnostics.postgresDialect()
        assertNotNull("PostgreSQL dialect not registered; SQL languages: ${OpenDataDiagnostics.sqlLanguages().map { it.id }}", pg)
        val sql = """
            WITH s AS (SELECT generate_series AS id, md5(generate_series::text) AS value FROM generate_series(1, 100))
            SELECT s.id, s.value FROM s WHERE s.id > 10 ORDER BY s.id;
        """.trimIndent()
        val file = PsiFileFactory.getInstance(project).createFileFromText("q.sql", pg!!, sql)
        assertFalse("PostgreSQL parser reported errors:\n${file.text}", PsiTreeUtil.hasErrorElements(file))
    }

    fun testPostgresDialectReportsSyntaxError() {
        val pg = OpenDataDiagnostics.postgresDialect() ?: return fail("PostgreSQL dialect not registered")
        val file = PsiFileFactory.getInstance(project).createFileFromText("bad.sql", pg, "SELEC 1 FROM;")
        assertTrue("Syntax error was not detected", PsiTreeUtil.hasErrorElements(file))
    }

    fun testDiagnosticsWithoutToolWindows() {
        val report = OpenDataDiagnostics.collect(project, includeToolWindows = false)
        assertTrue(report.render(), report.ok)
    }
}
