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

    /** Greenplum 6/7 (и форки: Greengage, WarehousePG, open-gpdb): протокол PostgreSQL, драйвер PostgreSQL JDBC. */
    GREENPLUM(
        "Greenplum", 5432, "postgres",
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

    val isPostgresFamily: Boolean get() = this == POSTGRESQL || this == GREENPLUM || this == CLOUDBERRY

    /** MPP на ядре PostgreSQL: распределение данных, внешние таблицы, gpfdist, COPY … LOG ERRORS. */
    val isMpp: Boolean get() = this == GREENPLUM || this == CLOUDBERRY

    /**
     * Свойства драйвера по умолчанию (пользовательские имеют приоритет):
     * PostgreSQL/Cloudberry — строковые параметры приводятся сервером к типу колонки (редактирование в grid);
     * ClickHouse — без LZ4-сжатия ответов (совместимость clickhouse-jdbc 0.10 с серверами 25.x/26.x).
     */
    val defaultProperties: Map<String, String>
        get() = when (this) {
            POSTGRESQL, GREENPLUM, CLOUDBERRY -> mapOf("stringtype" to "unspecified", "ApplicationName" to "OpenData IDE")
            CLICKHOUSE -> mapOf("compress" to "0")
            DREMIO -> emptyMap()
        }

    fun buildUrl(host: String, port: Int, database: String): String =
        urlTemplate.replace("{host}", host).replace("{port}", port.toString()).replace("{database}", database)
}

/**
 * JAR из Maven Central. [sha256] закреплён в коде: файлы `.sha256` есть в Maven Central не у всех артефактов
 * (например, у slf4j и Arrow их нет), а закреплённая сумма ещё и защищает от подмены зеркала.
 */
data class MavenJar(val group: String, val artifact: String, val version: String, val classifier: String? = null, val sha256: String) {
    val fileName: String get() = "$artifact-$version${classifier?.let { "-$it" } ?: ""}.jar"

    fun url(base: String = "https://repo.maven.apache.org/maven2"): String =
        "$base/${group.replace('.', '/')}/$artifact/$version/$fileName"
}

/**
 * JDBC-драйвер: основной JAR и его зависимости из Maven Central. Скачивается при первом подключении
 * (облегчённый дистрибутив), JAR PostgreSQL входит в комплект IDE.
 */
enum class DriverArtifact(vararg jars: MavenJar) {
    POSTGRESQL(MavenJar("org.postgresql", "postgresql", "42.7.13", sha256 = "6e0e4cc2d8cae902084f8a2b18728b073a6fd9d1f87c9d8bff8f298c18185b93")),
    CLICKHOUSE(
        MavenJar("com.clickhouse", "clickhouse-jdbc", "0.10.0", "all", sha256 = "d83736e24306e11929b0cf81b3306ae903c6f51c735c509ad5680c88640d99ce"),
        // В "-all" не входит SLF4J API; nop-реализация глушит логирование драйвера.
        MavenJar("org.slf4j", "slf4j-api", "2.0.17", sha256 = "7b751d952061954d5abfed7181c1f645d336091b679891591d63329c622eb832"),
        MavenJar("org.slf4j", "slf4j-nop", "2.0.17", sha256 = "3716f83649ec66161a2edefd4f49df34d1dd1c51cdcf941996c6987260f0a829"),
    ),
    ARROW_FLIGHT_SQL(MavenJar("org.apache.arrow", "flight-sql-jdbc-driver", "19.0.0", sha256 = "d3beee43c613c457789825343368f652d570d76c08799dad38a43a10e569b57f"));

    val jars: List<MavenJar> = jars.toList()
    val main: MavenJar get() = jars.first()
    val fileName: String get() = main.fileName
}
