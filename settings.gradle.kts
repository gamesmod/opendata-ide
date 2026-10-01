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

// OpenData-модули (ТЗ, раздел 28). Модули появляются по мере необходимости:
//   integration — плагин интеграции с Database Tools (этапы 1–4)
//   product     — конфигурация продукта OpenData IDE (этап 5, см. opendata/product/README.md)
include(":opendata:integration")
