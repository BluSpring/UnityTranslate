package xyz.bluspring.unitytranslate

import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class UnityTranslate : ModInitializer {
    override fun onInitialize() {
        TranslatorManager.init()
        loadConfig()

        ServerPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { server, player, handler, buf, sender ->
            val sourceLanguage = buf.readEnum(Language::class.java)
            val text = buf.readUtf()
            val updateLast = buf.readBoolean()

            val translations = ConcurrentHashMap<Language, String>()
            val sentTranslations = ConcurrentLinkedDeque<Language>()

            Language.entries.map {
                if (sourceLanguage == it)
                    CompletableFuture.completedFuture(text)
                        .thenApplyAsync {
                            translations[sourceLanguage] = text
                        }
                else
                    TranslatorManager.queueTranslation(text, sourceLanguage, it)
                        .thenApplyAsync { translated ->
                            translations[it] = translated
                        }
            }.forEach {
                it.thenApplyAsync {
                    val buf2 = PacketByteBufs.create()
                    buf2.writeUUID(player.uuid)
                    buf2.writeEnum(sourceLanguage)
                    buf2.writeBoolean(updateLast)

                    val toSend = translations.filter { a -> !sentTranslations.contains(a.key) }

                    buf2.writeVarInt(toSend.size)

                    for ((language, translated) in translations) {
                        buf2.writeEnum(language)
                        buf2.writeUtf(translated)
                        sentTranslations.add(language)
                    }

                    if (hasVoiceChat) {
                        val nearby = UTVoiceChatCompat.getNearbyPlayers(player)

                        for (p in nearby) {
                            if (UTVoiceChatCompat.isPlayerDeafened(p) && p != player)
                                continue

                            ServerPlayNetworking.send(p, PacketIds.SEND_TRANSCRIPT, buf2)
                        }
                    } else {
                        ServerPlayNetworking.send(player, PacketIds.SEND_TRANSCRIPT, buf2)
                    }
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            ServerPlayNetworking.send(handler.player, PacketIds.SERVER_SUPPORT, PacketByteBufs.empty())
        }
    }

    companion object {
        const val MOD_ID = "unitytranslate"

        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "unitytranslate.json")
        var config = UnityTranslateConfig()
        val logger = LoggerFactory.getLogger("UnityTranslate")

        val hasVoiceChat = FabricLoader.getInstance().isModLoaded("voicechat")

        @JvmStatic
        fun id(path: String): ResourceLocation {
            return ResourceLocation(MOD_ID, path)
        }

        fun saveConfig() {
            if (!configFile.exists())
                configFile.createNewFile()

            val serialized = Json.encodeToString(UnityTranslateConfig.serializer(), config)
            configFile.writeText(serialized)
        }

        fun loadConfig() {
            if (!configFile.exists())
                return

            config = Json.decodeFromString(UnityTranslateConfig.serializer(), configFile.readText())
        }
    }
}