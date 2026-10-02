import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

// Открытая сборка IntelliJ (Apache 2.0) — база продукта OpenData IDE.
val ossIde: Provider<String> = providers.gradleProperty("ossIdePath")
    .orElse(providers.environmentVariable("OPENDATA_PLATFORM_HOME"))

repositories {
    intellijPlatform {
        defaultRepositories()
    }
    mavenCentral {
        content { excludeGroupByRegex("bundled.*|localIde.*|idea.*|com\\.jetbrains\\.intellij\\..*") }
    }
}

/** JDBC-драйверы: PostgreSQL входит в дистрибутив, остальные IDE скачивает по требованию (тестам — из Gradle). */
val bundledDrivers by configurations.creating { isTransitive = false }
val testDrivers by configurations.creating { isTransitive = false }

dependencies {
    bundledDrivers("org.postgresql:postgresql:42.7.13")
    testDrivers("org.postgresql:postgresql:42.7.13")
    testDrivers("com.clickhouse:clickhouse-jdbc:0.10.0:all")
    testDrivers("org.slf4j:slf4j-api:2.0.17")
    testDrivers("org.slf4j:slf4j-nop:2.0.17")
    testDrivers("org.apache.arrow:flight-sql-jdbc-driver:19.0.0")
    intellijPlatform {
        local(ossIde.orElse(provider { throw GradleException("Укажите -PossIdePath или OPENDATA_PLATFORM_HOME (scripts/fetch-platform.sh)") }).get())
        // Открытый grid (DataGrid) из intellij-community.
        bundledPlugin("intellij.grid.core.plugin")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    projectName = "opendata-db"
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }
    buildSearchableOptions = false
    instrumentCode = false
}

val prepareDrivers by tasks.registering(Sync::class) {
    group = "opendata"
    description = "JDBC-драйверы, входящие в дистрибутив (build/drivers)"
    from(bundledDrivers)
    into(layout.buildDirectory.dir("drivers"))
}

val prepareTestDrivers by tasks.registering(Sync::class) {
    from(testDrivers)
    into(layout.buildDirectory.dir("test-drivers"))
}

tasks.test {
    dependsOn(prepareTestDrivers)
    // Тестовые СУБД (docker/docker-compose.yml): без переменной окружения тесты соответствующей СУБД пропускаются.
    val dbEnv = listOf(
        "OPENDATA_PG_URL", "OPENDATA_PG_USER", "OPENDATA_PG_PASSWORD",
        "OPENDATA_CH_URL", "OPENDATA_CH_USER", "OPENDATA_CH_PASSWORD",
        "OPENDATA_CB_URL", "OPENDATA_CB_USER", "OPENDATA_CB_PASSWORD",
        "OPENDATA_DREMIO_URL", "OPENDATA_DREMIO_USER", "OPENDATA_DREMIO_PASSWORD",
        "OPENDATA_TEST_DOWNLOAD",
    )
    dbEnv.forEach { name -> System.getenv(name)?.let { environment(name, it) } }
    environment(
        "OPENDATA_DRIVERS_DIR",
        System.getenv("OPENDATA_DRIVERS_DIR") ?: layout.buildDirectory.dir("test-drivers").get().asFile.absolutePath,
    )
}
