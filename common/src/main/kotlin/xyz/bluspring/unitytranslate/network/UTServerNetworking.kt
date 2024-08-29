package xyz.bluspring.unitytranslate.network

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.UnityTranslate.Companion.hasVoiceChat
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

object UTServerNetworking {
    fun init() {
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
            val translationsToSend = ConcurrentLinkedDeque<Language>()

            Language.entries.filter { usedLanguages.values.any { b -> b.contains(it) } }.map {
                Pair(it, if (sourceLanguage == it)
                    CompletableFuture.completedFuture(text)
                else
                    TranslatorManager.queueTranslation(text, sourceLanguage, it, player, index))
            }
                .forEach { (language, future) ->
                    future.whenCompleteAsync { translated, e ->
                        if (e != null) {
                            return@whenCompleteAsync
                        }

                        translations[language] = translated
                        translationsToSend.add(language)

                        server.execute {
                            if (translationsToSend.isNotEmpty()) {
                                broadcastTranslations(player, sourceLanguage, index, updateTime, translationsToSend, translations)
                                translationsToSend.clear()
                            }
                        }
                    }
                }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            ServerPlayNetworking.send(handler.player, PacketIds.SERVER_SUPPORT, PacketByteBufs.empty())

            if (UnityTranslate.IS_UNITY_SERVER) {
                val buf = PacketByteBufs.create()
                buf.writeBoolean(UnityTranslate.config.server.enabled)
                ServerPlayNetworking.send(handler.player, PacketIds.TOGGLE_MOD, buf)
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            usedLanguages.remove(handler.player.uuid)
        }
    }

    private fun broadcastTranslations(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, translationsToSend: ConcurrentLinkedDeque<Language>, translations: ConcurrentHashMap<Language, String>) {
        val buf = PacketByteBufs.create()
        buf.writeUUID(source.uuid)
        buf.writeEnum(sourceLanguage)
        buf.writeVarInt(index)
        buf.writeVarLong(updateTime)

        val toSend = translations.filter { a -> translationsToSend.contains(a.key) }

        buf.writeVarInt(toSend.size)

        for ((language, translated) in toSend) {
            buf.writeEnum(language)
            buf.writeUtf(translated)
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
}