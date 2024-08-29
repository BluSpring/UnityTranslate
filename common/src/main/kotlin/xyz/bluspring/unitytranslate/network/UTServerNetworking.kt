package xyz.bluspring.unitytranslate.network

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.networking.NetworkManager
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
    val proxy = UnityTranslate.instance.proxy

    fun init() {
        val usedLanguages = ConcurrentHashMap<UUID, EnumSet<Language>>()

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.SET_USED_LANGUAGES) { buf, ctx ->
            val languages = buf.readEnumSet(Language::class.java)
            usedLanguages[ctx.player.uuid] = languages
        }

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.SEND_TRANSCRIPT) { buf, ctx ->
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
                    TranslatorManager.queueTranslation(text, sourceLanguage, it, ctx.player, index))
            }
                .forEach { (language, future) ->
                    future.whenCompleteAsync { translated, e ->
                        if (e != null) {
                            return@whenCompleteAsync
                        }

                        translations[language] = translated
                        translationsToSend.add(language)

                        ctx.queue {
                            if (translationsToSend.isNotEmpty()) {
                                broadcastTranslations(ctx.player as ServerPlayer, sourceLanguage, index, updateTime, translationsToSend, translations)
                                translationsToSend.clear()
                            }
                        }
                    }
                }
        }

        PlayerEvent.PLAYER_JOIN.register { player ->
            proxy.sendPacketServer(player, PacketIds.SERVER_SUPPORT, proxy.createByteBuf())

            if (UnityTranslate.IS_UNITY_SERVER) {
                val buf = proxy.createByteBuf()
                buf.writeBoolean(UnityTranslate.config.server.enabled)
                proxy.sendPacketServer(player, PacketIds.TOGGLE_MOD, buf)
            }
        }

        PlayerEvent.PLAYER_QUIT.register { player ->
            usedLanguages.remove(player.uuid)
        }
    }

    private fun broadcastTranslations(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, translationsToSend: ConcurrentLinkedDeque<Language>, translations: ConcurrentHashMap<Language, String>) {
        val buf = proxy.createByteBuf()
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

                proxy.sendPacketServer(player, PacketIds.SEND_TRANSCRIPT, buf)
            }
        } else {
            proxy.sendPacketServer(source, PacketIds.SEND_TRANSCRIPT, buf)
        }
    }
}