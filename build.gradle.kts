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
