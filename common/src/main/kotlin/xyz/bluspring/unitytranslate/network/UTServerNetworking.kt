package xyz.bluspring.unitytranslate.network

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.networking.NetworkManager
import net.minecraft.ChatFormatting
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.SignBlock
import net.minecraft.world.level.block.entity.SignBlockEntity
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
    val playerLanguages = ConcurrentHashMap<UUID, Language>()

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

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.SET_CURRENT_LANGUAGE) { buf, ctx ->
            val language = buf.readEnum(Language::class.java)
            val player = ctx.player

            playerLanguages[player.uuid] = language
        }

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.TRANSLATE_SIGN) { buf, ctx ->
            val pos = buf.readBlockPos()

            val player = ctx.player
            val level = player.level()

            val state = level.getBlockState(pos)

            if (state.block !is SignBlock)
                return@registerReceiver

            ctx.queue {
                val entity = level.getBlockEntity(pos)

                if (entity !is SignBlockEntity)
                    return@queue

                val text = (if (entity.isFacingFrontText(player)) entity.frontText else entity.backText)
                    .getMessages(false)
                    .joinToString("\n") { it.string }

                val language = TranslatorManager.detectLanguage(text) ?: Language.ENGLISH
                val toLang = this.playerLanguages.getOrElse(player.uuid) { Language.ENGLISH }

                player.displayClientMessage(Component.empty()
                        .append(Component.literal("[UnityTranslate]: ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.translatable("unitytranslate.transcribe_sign", language.text, toLang.text, TranslatorManager.translateLine(text, language, toLang))),
                    false
                )
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