package io.opendata.db.model

/**
 * Поддерживаемые СУБД. Всё специфичное для СУБД (URL, драйвер, особенности метаданных) собрано здесь,
 * остальной код работает через стандартный JDBC.
 */
enum class DbKind(
    val displayName: String,
    val defaultPort: Int,
    val defaultDatabase: String,
    /** Шаблон JDBC URL: {host}, {port}, {database}. */
    val urlTemplate: String,
    val driverClass: String,
    val driver: DriverArtifact,
    val supportsTransactions: Boolean,
    /** Объекты верхнего уровня в дереве: схемы (PostgreSQL) или базы данных/каталоги (ClickHouse, Dremio). */
    val defaultSchema: String?,
) {
    POSTGRESQL(
        "PostgreSQL", 5432, "postgres",
        "jdbc:postgresql://{host}:{port}/{database}",
        "org.postgresql.Driver", DriverArtifact.POSTGRESQL,
        supportsTransactions = true, defaultSchema = "public",
    ),

    /** Apache Cloudberry (форк Greenplum) совместим с PostgreSQL по протоколу — используется драйвер PostgreSQL. */
    CLOUDBERRY(
        "Apache Cloudberry", 5432, "postgres",
        "jdbc:postgresql://{host}:{port}/{database}",
        "org.postgresql.Driver", DriverArtifact.POSTGRESQL,
        supportsTransactions = true, defaultSchema = "public",
    ),

    CLICKHOUSE(
        "ClickHouse", 8123, "default",
        "jdbc:clickhouse://{host}:{port}/{database}",
        "com.clickhouse.jdbc.Driver", DriverArtifact.CLICKHOUSE,
        supportsTransactions = false, defaultSchema = null,
    ),

    /** Dremio через Arrow Flight SQL (порт 32010). */
    DREMIO(
        "Dremio", 32010, "",
        "jdbc:arrow-flight-sql://{host}:{port}/?useEncryption=false",
        "org.apache.arrow.driver.jdbc.ArrowFlightJdbcDriver", DriverArtifact.ARROW_FLIGHT_SQL,
        supportsTransactions = false, defaultSchema = null,
    );

    val isPostgresFamily: Boolean get() = this == POSTGRESQL || this == CLOUDBERRY

    /**
     * Свойства драйвера по умолчанию (пользовательские имеют приоритет):
     * PostgreSQL/Cloudberry — строковые параметры приводятся сервером к типу колонки (редактирование в grid);
     * ClickHouse — без LZ4-сжатия ответов (совместимость clickhouse-jdbc 0.10 с серверами 25.x/26.x).
     */
    val defaultProperties: Map<String, String>
        get() = when (this) {
            POSTGRESQL, CLOUDBERRY -> mapOf("stringtype" to "unspecified", "ApplicationName" to "OpenData IDE")
            CLICKHOUSE -> mapOf("compress" to "0")
            DREMIO -> emptyMap()
        }

    fun buildUrl(host: String, port: Int, database: String): String =
        urlTemplate.replace("{host}", host).replace("{port}", port.toString()).replace("{database}", database)
}

/** JAR из Maven Central. */
data class MavenJar(val group: String, val artifact: String, val version: String, val classifier: String? = null) {
    val fileName: String get() = "$artifact-$version${classifier?.let { "-$it" } ?: ""}.jar"

    fun url(base: String = "https://repo.maven.apache.org/maven2"): String =
        "$base/${group.replace('.', '/')}/$artifact/$version/$fileName"
}

/**
 * JDBC-драйвер: основной JAR и его зависимости из Maven Central. Скачивается при первом подключении
 * (облегчённый дистрибутив), JAR PostgreSQL входит в комплект IDE.
 */
enum class DriverArtifact(vararg jars: MavenJar) {
    POSTGRESQL(MavenJar("org.postgresql", "postgresql", "42.7.13")),
    CLICKHOUSE(
        MavenJar("com.clickhouse", "clickhouse-jdbc", "0.10.0", "all"),
        // В "-all" не входит SLF4J API; nop-реализация глушит логирование драйвера.
        MavenJar("org.slf4j", "slf4j-api", "2.0.17"),
        MavenJar("org.slf4j", "slf4j-nop", "2.0.17"),
    ),
    ARROW_FLIGHT_SQL(MavenJar("org.apache.arrow", "flight-sql-jdbc-driver", "19.0.0"));

    val jars: List<MavenJar> = jars.toList()
    val main: MavenJar get() = jars.first()
    val fileName: String get() = main.fileName
}
