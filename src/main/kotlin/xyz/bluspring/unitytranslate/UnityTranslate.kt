package xyz.bluspring.unitytranslate

import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class UnityTranslate : ModInitializer {
    private fun broadcastTranslations(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, sentTranslations: ConcurrentLinkedDeque<Language>, translations: ConcurrentHashMap<Language, String>) {
        val buf = PacketByteBufs.create()
        buf.writeUUID(source.uuid)
        buf.writeEnum(sourceLanguage)
        buf.writeVarInt(index)
        buf.writeVarLong(updateTime)

        val toSend = translations.filter { a -> !sentTranslations.contains(a.key) }

        buf.writeVarInt(toSend.size)

        for ((language, translated) in toSend) {
            buf.writeEnum(language)
            buf.writeUtf(translated)
            sentTranslations.add(language)
        }

        if (hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                ServerPlayNetworking.send(player, PacketIds.SEND_TRANSCRIPT, buf)
            }
        } else {
            ServerPlayNetworking.send(source, PacketIds.SEND_TRANSCRIPT, buf)
        }
    }

    override fun onInitialize() {
        TranslatorManager.init()
        loadConfig()

        val usedLanguages = ConcurrentHashMap<UUID, EnumSet<Language>>()

        ServerPlayNetworking.registerGlobalReceiver(PacketIds.SET_USED_LANGUAGES) { server, player, handler, buf, sender ->
            val languages = buf.readEnumSet(Language::class.java)
            usedLanguages[player.uuid] = languages
        }

        ServerPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { server, player, handler, buf, sender ->
            val sourceLanguage = buf.readEnum(Language::class.java)
            val text = buf.readUtf()
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()

            val translations = ConcurrentHashMap<Language, String>()
            val sentTranslations = ConcurrentLinkedDeque<Language>()

            Language.entries.filter { usedLanguages.values.any { b -> b.contains(it) } }.map {
                if (sourceLanguage == it)
                    CompletableFuture.completedFuture(text)
                        .thenApplyAsync {
                            translations[sourceLanguage] = text

                            server.execute {
                                broadcastTranslations(player, sourceLanguage, index, updateTime, sentTranslations, translations)
                            }
                        }
                else
                    TranslatorManager.queueTranslation(text, sourceLanguage, it)
                        .thenApplyAsync { translated ->
                            translations[it] = translated

                            server.execute {
                                broadcastTranslations(player, sourceLanguage, index, updateTime, sentTranslations, translations)
                            }
                        }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            ServerPlayNetworking.send(handler.player, PacketIds.SERVER_SUPPORT, PacketByteBufs.empty())
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            usedLanguages.remove(handler.player.uuid)
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