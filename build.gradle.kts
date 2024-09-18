import com.modrinth.minotaur.dependencies.DependencyType
import com.modrinth.minotaur.dependencies.ModDependency
import dev.deftu.gradle.tools.minecraft.CurseRelation
import dev.deftu.gradle.tools.minecraft.CurseRelationType
import dev.deftu.gradle.utils.*

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

toolkitLoomHelper {
    if (!mcData.isNeoForge) {
        useMixinRefMap("unitytranslate")
    }

    if (mcData.isForge) {
        useTweaker("org.spongepowered.asm.launch.MixinTweaker")
        useForgeMixin("unitytranslate.mixins.json", true)
    }

    if (mcData.isForgeLike) {
        useKotlinForForge()
    }
}

version = "${project.property("mod.version")}+mc${mcData.version}-${mcData.loader.friendlyString}"

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://repo.clojars.org")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.maxhenkel.de/repository/public")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.nucleoid.xyz/")
    maven("https://maven.minecraftforge.net")

    maven("https://repo.plo.su")
    maven("https://repo.plasmoverse.com/releases")
    maven("https://repo.plasmoverse.com/snapshots")
}

val architecturyVersion = when (mcData.version.rawVersion) {
    1_20_01 -> "9.2.14"
    1_20_04 -> "11.1.17"
    1_20_06 -> "12.1.4"
    1_21_01 -> "13.0.6"

    else -> throw IllegalStateException()
}

dependencies {
    implementation("de.maxhenkel.voicechat:voicechat-api:${project.property("voicechat_api_version")}")
    compileOnly("su.plo.voice.api:server:${project.property("plasmo_api_version")}")
    compileOnly("su.plo.voice.api:client:${project.property("plasmo_api_version")}")

    modApi("dev.architectury:architectury-${mcData.loader.friendlyString}:$architecturyVersion")

    if (mcData.isFabric) {
        val modMenuVersion = when (mcData.version.rawVersion) {
            1_20_01 -> "7.2.2"
            1_20_04 -> "9.2.0"
            1_20_06 -> "10.0.0"
            1_21_01 -> "11.0.2"

            else -> throw IllegalStateException()
        }

        modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    }

    if (mcData.isFabric) {
        includeOrShade(modImplementation("me.lucko:fabric-permissions-api:0.3.1")!!)
    }

    val useSVC = true

    if (useSVC)
        modRuntimeOnly("maven.modrinth:simple-voice-chat:${mcData.loader.friendlyString}-${if (mcData.version != MinecraftVersion.VERSION_1_21_1) mcData.version else "1.21"}-${project.property("voicechat_version")}")
    else if (!mcData.isNeoForge) {
        modRuntimeOnly("maven.modrinth:plasmo-voice:${mcData.loader.friendlyString}-${if (mcData.version != MinecraftVersion.VERSION_1_21_1) mcData.version else "1.21"}-${project.property("plasmo_version")}")
        runtimeOnly("su.plo.voice.api:server:${project.property("plasmo_api_version")}")
        runtimeOnly("su.plo.voice.api:client:${project.property("plasmo_api_version")}")
    }

    val clothConfigVersion = when(mcData.version.rawVersion) {
        1_20_01 -> "11.1.118"
        1_20_04 -> "13.0.121"
        1_20_06 -> "14.0.126"
        1_21_01 -> "15.0.128"

        else -> throw IllegalStateException()
    }

    modCompileOnly("maven.modrinth:cloth-config:${clothConfigVersion}+${mcData.loader.friendlyString}")

    val cerbonsApiVersion = if (mcData.isForgeLike) "XWZQbKsr" else "1.1.0"
    modCompileOnly("maven.modrinth:cerbons-api:$cerbonsApiVersion")

    val talkBalloonsVersion = if (mcData.isForgeLike) "kN8kdQ22" else "1.0.0"
    modCompileOnly("maven.modrinth:talk-balloons:$talkBalloonsVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${project.property("kotlin_serialization_version")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${project.property("kotlin_serialization_version")}")

    implementation("org.jetbrains.kotlin:kotlin-reflect:${project.property("kotlin_version")}")

    if (mcData.isFabric) {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
    }

    val jws = includeOrShade("org.java-websocket:Java-WebSocket:1.5.7")!!

    implementation(jws)
    if (mcData.isForgeLike) {
        minecraftRuntimeLibraries(jws)
    }

    if (!mcData.isForgeLike) {// fuck you Forge
        includeOrShade("commons-logging:commons-logging:1.3.4")
        includeOrShade("org.apache.httpcomponents:httpcore:4.4.16")
        includeOrShade("org.apache.httpcomponents:httpclient:4.5.13")
    }
}

toolkitReleases {
    detectVersionType.set(true)
    releaseName.set("[${mcData.version}] UnityTranslate ${modData.version} (${mcData.loader.friendlyName})")

    modrinth {
        projectId.set("yA7uge1H")

        if (mcData.loader == ModLoader.FABRIC) {
            dependencies.add(ModDependency("Ha28R6CL", DependencyType.REQUIRED)) // Fabric Language Kotlin
        } else if (mcData.isForgeLike) {
            dependencies.add(ModDependency("ordsPcFz", DependencyType.REQUIRED)) // Kotlin for Forge
        }

        dependencies.addAll(listOf(
            ModDependency("lhGA9TYQ", DependencyType.REQUIRED), // Architectury API
            ModDependency("l3tS9WUS", DependencyType.OPTIONAL), // Talk Balloons
            ModDependency("9eGKb6K1", DependencyType.OPTIONAL), // Simple Voice Chat
        ))
    }

    curseforge {
        projectId.set("1093604")

        if (mcData.loader == ModLoader.FABRIC) {
            relations.add(CurseRelation("fabric-language-kotlin", CurseRelationType.REQUIRED)) // Fabric Language Kotlin
        } else if (mcData.isForgeLike) {
            relations.add(CurseRelation("kotlin-for-forge", CurseRelationType.REQUIRED)) // Kotlin for Forge
        }

        relations.addAll(listOf(
            CurseRelation("architectury-api", CurseRelationType.REQUIRED), // Architectury API
            CurseRelation("talk-balloons", CurseRelationType.OPTIONAL), // Talk Balloons
            CurseRelation("simple-voice-chat", CurseRelationType.OPTIONAL), // Simple Voice Chat
        ))
    }

    changelogFile.set(File(project.rootDir, "CHANGELOG.md"))
}

tasks {
    processResources {
        val properties = mutableMapOf<String, String>()

        properties.putAll(mapOf(
            "mod_version" to modData.version,
            "mc_version" to mcData.version.toString(),
            "architectury_version" to architecturyVersion,
        ))

        val forgeLoaderVersion: String? = run {
            if (!mcData.isPresent) {
                return@run null
            }

            if (!mcData.isForgeLike) {
                return@run null
            }

            if (mcData.isLegacyForge) {
                return@run null
            }

            val version = MinecraftInfo.ForgeLike.getKotlinForForgeVersion(mcData.version)
            val majorVersion = version.split(".")[0]
            "[$majorVersion,)"
        }

        if (mcData.isForgeLike) {
            properties["forge_kotlin_version"] = mcData.dependencies.forgeLike.kotlinForForgeVersion
            properties["forge_loader_version"] = forgeLoaderVersion!!
            properties["mod_loader_name"] = mcData.loader.friendlyString

            if (mcData.isForge && mcData.version.rawVersion <= 1_20_01) {
                properties["forge_loader"] = "javafml"
            } else {
                properties["forge_loader"] = "kotlinforforge"
            }

            if (mcData.isForge) {
                properties["FUCKING_REQUIRED"] = "mandatory=true"
            } else {
                properties["FUCKING_REQUIRED"] = "required=true"
            }
        }

        if (mcData.isFabric) {
            properties["fabric_kotlin_version"] = mcData.dependencies.fabric.fabricLanguageKotlinVersion
            properties["loader_version"] = mcData.dependencies.fabric.fabricLoaderVersion

            exclude("META-INF/mods.toml")
            exclude("META-INF/neoforge.mods.toml")
        }

        for ((key, value) in properties) {
            inputs.property(key, value)
        }

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(properties)
        }

        filesMatching("META-INF/mods.toml") {
            expand(properties)
        }

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }
}
