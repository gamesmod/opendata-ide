pluginManagement {
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val intellijPlatformPluginVersion = providers.gradleProperty("intellijPlatformPluginVersion").get()
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.intellij.platform") version intellijPlatformPluginVersion
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "opendata-ide"

// OpenData IDE (ТЗ, раздел 28):
//   opendata/db      — DB-слой продукта (источники данных, Explorer, SQL-консоль, DataGrid, редактирование)
//   tools/product    — сборщик продукта на базе открытой IntelliJ Platform (см. assembleProduct)
//   tools/research   — инструмент этапа 0 (анализ установки DataGrip/IDEA)
include(":opendata:db")
