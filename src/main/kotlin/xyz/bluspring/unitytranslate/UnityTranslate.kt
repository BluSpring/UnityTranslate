package xyz.bluspring.unitytranslate

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class UnityTranslate : ModInitializer {
    override fun onInitialize() {
        TranslatorManager.init()

        ServerPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { server, player, handler, buf, sender ->
            val sourceLanguage = buf.readEnum(Language::class.java)
            val text = buf.readUtf()
            val updateLast = buf.readBoolean()

            val translations = ConcurrentHashMap<Language, String>()

            CompletableFuture.allOf(*Language.entries.map {
                TranslatorManager.queueTranslation(text, sourceLanguage, it)
                    .thenApplyAsync { translated ->
                        translations[it] = translated
                    }
            }.toTypedArray())
                .thenApplyAsync {
                    val buf2 = PacketByteBufs.create()
                    buf2.writeUUID(player.uuid)
                    buf2.writeEnum(sourceLanguage)
                    buf2.writeBoolean(updateLast)

                    buf2.writeVarInt(translations.size)

                    for ((language, translated) in translations) {
                        buf2.writeEnum(language)
                        buf2.writeUtf(translated)
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

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            if (server.isDedicatedServer) {
                ServerPlayNetworking.send(handler.player, PacketIds.SERVER_SUPPORT, PacketByteBufs.empty())
            }
        }
    }

    companion object {
        const val MOD_ID = "unitytranslate"

        val config = UnityTranslateConfig()
        val logger = LoggerFactory.getLogger("UnityTranslate")

        val hasVoiceChat = FabricLoader.getInstance().isModLoaded("voicechat")

        @JvmStatic
        fun id(path: String): ResourceLocation {
            return ResourceLocation(MOD_ID, path)
        }
    }
}