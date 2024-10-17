package xyz.bluspring.unitytranslate

import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.LifecycleEvent
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.commands.UnityTranslateCommands
import xyz.bluspring.unitytranslate.compat.voicechat.PlasmoVoiceChatCompat
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.network.UTServerNetworking
import xyz.bluspring.unitytranslate.translator.LocalLibreTranslateInstance
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.io.File

class UnityTranslate(val proxy: PlatformProxy = PlatformProxyImpl()) {
    init {
        instance = this
        configFile = File(proxy.configDir.toFile(), "unitytranslate.json")
        version = proxy.modVersion

        TranslatorManager.init()
        loadConfig()

        LifecycleEvent.SERVER_STOPPING.register {
            LocalLibreTranslateInstance.killOpenInstances()
        }

        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(UnityTranslateCommands.ROOT)
        }

        UTServerNetworking.init()

        if (proxy.isModLoaded("plasmovoice")) {
            PlasmoVoiceChatCompat.init()
        }
    }

    companion object {
        const val MOD_ID = "unitytranslate"

        lateinit var instance: UnityTranslate
        lateinit var configFile: File
        lateinit var version: String
        val hasVoiceChat: Boolean
            get() {
                return UTVoiceChatCompat.hasVoiceChat
            }

        var config = UnityTranslateConfig()
        val logger = LoggerFactory.getLogger("UnityTranslate")

        private val json = Json {
            this.ignoreUnknownKeys = true
            this.prettyPrint = true
            this.encodeDefaults = true
        }

        @JvmStatic
        fun id(path: String): ResourceLocation {
            //#if MC >= 1.21
            //$$ return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
            //#else
            return ResourceLocation(MOD_ID, path)
            //#endif
        }

        fun saveConfig() {
            try {
                if (!configFile.exists())
                    configFile.createNewFile()

                val serialized = json.encodeToString(UnityTranslateConfig.serializer(), config)
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
                config = json.decodeFromString(UnityTranslateConfig.serializer(), configFile.readText())
            } catch (e: Exception) {
                logger.error("Failed to load UnityTranslate config, reverting to defaults.")
                e.printStackTrace()
            }
        }
    }
}