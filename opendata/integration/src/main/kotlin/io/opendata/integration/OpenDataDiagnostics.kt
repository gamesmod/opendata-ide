package io.opendata.integration

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Диагностика интеграции с Database Tools and SQL.
 *
 * Использует только стабильный публичный API платформы (PluginManagerCore, Language,
 * ToolWindowManager). Ничего не предполагает о внутренних классах com.intellij.database.*:
 * ID tool window и SQL-языков не хардкодятся как обязательные, а обнаруживаются.
 */
object OpenDataDiagnostics {
    const val DATABASE_PLUGIN_ID: String = "com.intellij.database"

    /** Ожидаемый ID окна Database Explorer; фактический список окон выводится в отчёт. */
    const val EXPECTED_DATABASE_TOOL_WINDOW_ID: String = "Database"

    data class Check(val name: String, val ok: Boolean, val details: String)

    data class Report(val checks: List<Check>) {
        val ok: Boolean get() = checks.all { it.ok }

        fun render(): String = buildString {
            for (c in checks) appendLine("[${if (c.ok) "OK" else "FAIL"}] ${c.name}: ${c.details}")
            appendLine("RESULT: ${if (ok) "OK" else "FAIL"}")
        }
    }

    fun collect(project: Project?, includeToolWindows: Boolean): Report {
        val checks = mutableListOf<Check>()

        val app = ApplicationInfo.getInstance()
        checks += Check(
            "IDE", true,
            "${app.fullApplicationName}, build ${app.build.asString()}, runtime ${System.getProperty("java.runtime.version")}",
        )

        val dbPlugin = PluginManagerCore.getPlugin(PluginId.getId(DATABASE_PLUGIN_ID))
        val dbEnabled = dbPlugin != null && !PluginManagerCore.isDisabled(dbPlugin.pluginId)
        checks += Check(
            "Database Tools and SQL", dbEnabled,
            if (dbPlugin == null) "плагин $DATABASE_PLUGIN_ID не найден"
            else "${dbPlugin.name} ${dbPlugin.version}${if (dbEnabled) "" else " (отключён)"}",
        )

        val sqlLanguages = sqlLanguages()
        checks += Check("SQL language", sqlLanguages.any { it.id.equals("SQL", ignoreCase = true) },
            sqlLanguages.joinToString { it.id }.ifEmpty { "SQL-языки не зарегистрированы" })
        val pg = postgresDialect()
        checks += Check("PostgreSQL dialect", pg != null, pg?.let { "${it.id} (${it.displayName})" } ?: "не найден")

        if (includeToolWindows && project != null) {
            val ids = ToolWindowManager.getInstance(project).toolWindowIds.toList()
            val found = ids.any { it.equals(EXPECTED_DATABASE_TOOL_WINDOW_ID, ignoreCase = true) }
            checks += Check("Database Tool Window", found, "tool windows: ${ids.sorted().joinToString()}")
        }
        return Report(checks)
    }

    fun sqlLanguages(): List<Language> =
        Language.getRegisteredLanguages().filter { lang ->
            lang.id.equals("SQL", ignoreCase = true) || generateSequence(lang.baseLanguage) { it.baseLanguage }.any { it.id.equals("SQL", true) }
        }.sortedBy { it.id }

    /** Диалект PostgreSQL ищется среди зарегистрированных языков, а не по хардкоду класса. */
    fun postgresDialect(): Language? =
        Language.getRegisteredLanguages().sortedBy { it.id }
            .firstOrNull { it.id.contains("postgres", ignoreCase = true) || it.displayName.contains("PostgreSQL", ignoreCase = true) }
}
