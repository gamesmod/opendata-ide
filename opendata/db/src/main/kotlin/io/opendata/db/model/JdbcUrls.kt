package io.opendata.db.model

/**
 * Разбор JDBC URL поддерживаемых СУБД: тип по префиксу и host/port/database для синхронизации с полями
 * диалога и для отображения адреса подключения.
 */
object JdbcUrls {
    data class Parsed(val kind: DbKind?, val host: String?, val port: Int?, val database: String?, val params: String?)

    private val PREFIXES = listOf(
        "jdbc:postgresql:" to DbKind.POSTGRESQL,
        "jdbc:pgsql:" to DbKind.POSTGRESQL,
        "jdbc:clickhouse:" to DbKind.CLICKHOUSE,
        "jdbc:ch:" to DbKind.CLICKHOUSE,
        "jdbc:arrow-flight-sql:" to DbKind.DREMIO,
        "jdbc:arrow-flight:" to DbKind.DREMIO,
    )

    /** Тип СУБД по префиксу URL; для протокола PostgreSQL — семейство PostgreSQL (сам тип уточняет пользователь). */
    fun kindOf(url: String): DbKind? {
        val u = url.trim().lowercase()
        return PREFIXES.firstOrNull { u.startsWith(it.first) }?.second
    }

    /** Совместим ли URL с типом подключения (Greenplum и Cloudberry используют протокол PostgreSQL). */
    fun matches(kind: DbKind, url: String): Boolean {
        val k = kindOf(url) ?: return true
        return k == kind || (k.isPostgresFamily && kind.isPostgresFamily)
    }

    /** jdbc:sub[:sub2]://host[:port][,host2…][/database][?params] */
    fun parse(url: String): Parsed {
        val u = url.trim()
        val kind = kindOf(u)
        val i = u.indexOf("//")
        if (i < 0) return Parsed(kind, null, null, null, null)
        var rest = u.substring(i + 2)
        val q = rest.indexOf('?').let { if (it < 0) rest.length else it }
        val params = rest.substring(q).removePrefix("?").ifEmpty { null }
        rest = rest.substring(0, q)
        val slash = rest.indexOf('/')
        val authority = if (slash < 0) rest else rest.substring(0, slash)
        val database = if (slash < 0) null else java.net.URLDecoder.decode(rest.substring(slash + 1).trimEnd('/'), Charsets.UTF_8).ifEmpty { null }
        val first = authority.substringAfterLast('@').split(',').first()
        val (host, port) = if (first.startsWith("[")) {
            val end = first.indexOf(']')
            first.substring(1, end) to first.substring(end + 1).removePrefix(":").toIntOrNull()
        } else {
            val c = first.lastIndexOf(':')
            if (c < 0) first to null else first.substring(0, c) to first.substring(c + 1).toIntOrNull()
        }
        return Parsed(kind, host.ifEmpty { null }, port, database, params)
    }

    /** Адрес подключения для отображения: host:port из URL, если он задан, иначе из полей. */
    fun address(config: DataSourceConfig): String {
        if (config.url.isBlank()) return "${config.host}:${config.port}"
        val p = parse(config.url)
        return listOfNotNull(p.host, p.port?.toString()).joinToString(":").ifEmpty { config.url }
    }
}
