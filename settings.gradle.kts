pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.firstdark.dev/releases")
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.deftu.dev/snapshots")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://server.bbkr.space/artifactory/libs-release/")

        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm") version("2.0.20")
        kotlin("plugin.serialization") version("2.0.20")
        id("dev.deftu.gradle.multiversion-root") version("2.8.1")
    }
}

val projectName: String = extra["mod.name"]?.toString()!!
rootProject.name = projectName
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.20.1-fabric",
    "1.20.1-forge",

    "1.20.4-fabric",
    "1.20.4-neoforge",

    "1.20.6-fabric",
    "1.20.6-neoforge",

    "1.21.1-fabric",
    "1.21.1-neoforge",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}