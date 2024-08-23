package xyz.bluspring.unitytranslate

import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.network.UTServerNetworking
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.io.File

class UnityTranslate : ModInitializer {
    override fun onInitialize() {
        TranslatorManager.init()
        loadConfig()

        UTServerNetworking.init()
    }

    companion object {
        const val MOD_ID = "unitytranslate"

        val modContainer: ModContainer
            get() = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()

        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "unitytranslate.json")
        var config = UnityTranslateConfig()
        val logger = LoggerFactory.getLogger("UnityTranslate")

        val hasVoiceChat = FabricLoader.getInstance().isModLoaded("voicechat")

        @JvmStatic
        fun id(path: String): ResourceLocation {
            return ResourceLocation(MOD_ID, path)
        }

        fun saveConfig() {
            try {
                if (!configFile.exists())
                    configFile.createNewFile()

                val serialized = Json.encodeToString(UnityTranslateConfig.serializer(), config)
                configFile.writeText(serialized)
            } catch (e: Exception) {
                logger.error("Failed to save UnityTranslate config!")
                e.printStackTrace()
            }
        }

        fun loadConfig() {
            if (!configFile.exists())
                return

            try {
                config = Json.decodeFromString(UnityTranslateConfig.serializer(), configFile.readText())
            } catch (e: Exception) {
                logger.error("Failed to load UnityTranslate config, reverting to defaults.")
                e.printStackTrace()
            }
        }
    }
}