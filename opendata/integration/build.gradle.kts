import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

// Локальная IDE/DataGrip (ТЗ 8.2): путь не хардкодится, берётся из свойства или окружения.
val localIde: Provider<String> = providers.gradleProperty("localIdePath")
    .orElse(providers.environmentVariable("JETBRAINS_IDE_HOME"))
    .orElse(providers.environmentVariable("DATAGRIP_HOME"))

repositories {
    // Сначала локальные артефакты IntelliJ Platform (bundledPlugin/bundledModule локальной IDE),
    // чтобы виртуальные координаты не запрашивались у Maven Central.
    intellijPlatform {
        defaultRepositories()
    }
    mavenCentral {
        content {
            excludeGroupByRegex("bundled.*|localIde.*|idea.*|com\\.jetbrains\\.intellij\\..*")
        }
    }
}

dependencies {
    intellijPlatform {
        if (localIde.isPresent) {
            local(localIde.get())
        } else {
            create(providers.gradleProperty("platformType").get(), providers.gradleProperty("platformVersion").get())
        }
        // ТЗ 8.1: Database Tools and SQL подключается как bundled plugin той же установки.
        // Его транзитивные модули (SQL, grid и т.д.) резолвятся плагином сборки автоматически.
        bundledPlugin("com.intellij.database")
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    // Только для JDBC smoke-теста тестовой БД (fallback-путь ТЗ 17/25), не для продукта.
    testImplementation("org.postgresql:postgresql:42.7.4")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    // Имя каталога плагина и архива: opendata-integration-<version>.zip
    projectName = "opendata-integration"
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

tasks {
    runIde {
        // POC: IDE пишет отчёт диагностики при старте проекта — это машинно-проверяемое
        // подтверждение «IDE запускается / Database Tools loaded / Database Tool Window visible».
        systemProperty("opendata.diagnostics.file", rootProject.layout.buildDirectory.file("poc/diagnostics.txt").get().asFile.absolutePath)
        jvmArgs("-Xmx2g")
        // scripts/run.ps1 -Poc передаёт каталог проекта, который IDE откроет сразу при старте.
        providers.gradleProperty("openProject").orNull?.let { args(it) }
    }
    test {
        // Путь к JDBC-драйверу PostgreSQL для сценарных тестов Database Tools (подключается как driver file).
        val pgJar = configurations.testRuntimeClasspath.map { c -> c.files.first { it.name.startsWith("postgresql-") } }
        jvmArgumentProviders += CommandLineArgumentProvider { listOf("-Dopendata.pg.driver.jar=${pgJar.get().absolutePath}") }
        // Параметры тестовой PostgreSQL (docker/postgres) пробрасываются из окружения.
        listOf("OPENDATA_PG_URL", "OPENDATA_PG_USER", "OPENDATA_PG_PASSWORD").forEach { name ->
            System.getenv(name)?.let { environment(name, it) }
        }
    }
}
