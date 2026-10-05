plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.intellij.platform") apply false
}

// Stage 0: исследование установки IDE. Эквивалент scripts/research.ps1 для запуска из Gradle.
// gradle research -PlocalIdePath=<IDE_HOME> [-PcommunityPath=<intellij-community>]
tasks.register<Exec>("research") {
    group = "opendata"
    description = "Сканирует локальную JetBrains IDE/DataGrip и генерирует docs/*-analysis.md"
    val ide = providers.gradleProperty("localIdePath")
        .orElse(providers.environmentVariable("JETBRAINS_IDE_HOME"))
        .orElse(providers.environmentVariable("DATAGRIP_HOME"))
    val community = providers.gradleProperty("communityPath")
        .orElse(providers.environmentVariable("INTELLIJ_COMMUNITY_HOME"))
    val javaHome = System.getProperty("java.home")
    val gradleVersion = gradle.gradleVersion
    doFirst {
        if (!ide.isPresent) throw GradleException("Укажите -PlocalIdePath или переменную JETBRAINS_IDE_HOME / DATAGRIP_HOME")
        val args = mutableListOf(
            "$javaHome/bin/java", "tools/research/Research.java",
            "--ide", ide.get(), "--out", "docs", "--state", "build/research", "--gradle", gradleVersion,
        )
        if (community.isPresent) args += listOf("--community", community.get())
        commandLine(args)
    }
    workingDir = rootDir
}

// Этап 5: сборка продукта OpenData IDE из открытой платформы и плагина opendata-db.
// gradlew assembleProduct -PossIdePath=<распакованная открытая сборка> [-PproductOs=linux|windows]
tasks.register<Exec>("assembleProduct") {
    group = "opendata"
    description = "Собирает дистрибутив OpenData IDE в build/product/<os>/OpenData-IDE"
    dependsOn(":opendata:db:buildPlugin", ":opendata:db:prepareDrivers")
    val base = providers.gradleProperty("ossIdePath").orElse(providers.environmentVariable("OPENDATA_PLATFORM_HOME"))
    val os = providers.gradleProperty("productOs").orElse("linux")
    val version = project.version.toString()
    val javaHome = System.getProperty("java.home")
    doFirst {
        val plugin = file("opendata/db/build/distributions").listFiles { f -> f.name.endsWith(".zip") }!!.maxBy { it.lastModified() }
        val args = mutableListOf(
            "$javaHome/bin/java", "-Djava.awt.headless=true", "tools/product/AssembleProduct.java",
            "--base", base.get(), "--plugin", plugin.absolutePath,
            "--drivers", file("opendata/db/build/drivers").absolutePath,
            "--out", file("build/product/${os.get()}/OpenData-IDE").absolutePath, "--version", version,
        )
        // Значок opendata64.exe: путь к rcedit-x64.exe (scripts/build.ps1 загружает его с проверкой SHA-256).
        providers.gradleProperty("rcedit").orNull?.let { args += listOf("--rcedit", it) }
        if (os.get() == "windows") {
            args += listOf("--zip", file("build/distributions/OpenData-IDE-$version-windows-x64.zip").absolutePath)
        }
        commandLine(args)
    }
    workingDir = rootDir
}
