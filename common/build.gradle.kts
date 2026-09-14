import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension

plugins {
    alias(libs.plugins.fletching.table)
    `maven-publish`
}

val mcVersion = stonecutter.current.version
val common = stonecutter.node.sibling("")!!

if (stonecutter.eval(mcVersion, "<=1.20.1")) {
    apply(plugin = "net.neoforged.moddev.legacyforge")

    project.extensions.configure<LegacyForgeExtension> {
        mcpVersion = mcVersion

        accessTransformers {
            from(rootProject.file("common/src/main/resources/META-INF/accesstransformer.cfg"))
        }

        configureModDev(this, "common")
    }
} else {
    apply(plugin = "net.neoforged.moddev")

    project.extensions.configure<NeoForgeExtension> {
        neoFormVersion = tryFindNeoFormVersion(mcVersion)!!

        accessTransformers {
            from(rootProject.file("common/src/main/resources/META-INF/accesstransformer.cfg"))
        }

        configureModDev(this, "common")
    }
}

setupCommon("common")
setupCommonModDev("common")

dependencies {
    api(project(":api")) {
        isTransitive = false
    }

    api(project(":shared")) {
        isTransitive = false
    }

    api(libs.mixin) // Mixin
    api(libs.fabric.kotlin) // Provides all the Kotlin stuff we'd ever need
//    annotationProcessor(libs.mixinextras.common) // MixinExtras
    api(libs.mixinextras.common)
    api(libs.unitytranslatelib) {
        exclude(group = "org.slf4j") // because NeoForge panics otherwise
    }
    api(libs.voicechat.api)
    api(libs.sunset)
    api(libs.plasmo.api.server)
    api(libs.plasmo.api.client)
    api(libs.bundles.arc3d.api)
    api(libs.bundles.arc3d.impl)

    api("maven.modrinth:talk-balloons:${libs.versions.talk.balloons.get()}+${mcVersion}-neoforge")
}
