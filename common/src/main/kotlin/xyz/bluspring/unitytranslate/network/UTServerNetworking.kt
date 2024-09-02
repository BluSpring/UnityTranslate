package xyz.bluspring.unitytranslate.network

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.networking.NetworkManager
import net.minecraft.ChatFormatting
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.SignBlock
import net.minecraft.world.level.block.entity.SignBlockEntity
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.UnityTranslate.Companion.hasVoiceChat
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.network.payloads.SendTranscriptToClientPayload
import xyz.bluspring.unitytranslate.network.payloads.ServerSupportPayload
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

object UTServerNetworking {
    val proxy = UnityTranslate.instance.proxy
    val playerLanguages = ConcurrentHashMap<UUID, Language>()

    fun init() {
        PacketIds.init()

        val usedLanguages = ConcurrentHashMap<UUID, EnumSet<Language>>()

        registerReceiver(PacketIds.SET_USED_LANGUAGES) { buf, ctx ->
            val languages = EnumSet.copyOf(buf.languages)
            usedLanguages[ctx.player.uuid] = languages
        }

        registerReceiver(PacketIds.SEND_TRANSCRIPT_TO_SERVER) { buf, ctx ->
            val sourceLanguage = buf.sourceLanguage
            val text = buf.text
            val index = buf.index
            val updateTime = buf.updateTime

            val translations = ConcurrentHashMap<Language, String>()
            val translationsToSend = ConcurrentLinkedDeque<Language>()

            Language.entries.filter { usedLanguages.values.any { b -> b.contains(it) } }.map {
                Pair(it, if (sourceLanguage == it)
                    CompletableFuture.supplyAsync {
                        text
                    }
                else
                    TranslatorManager.queueTranslation(text, sourceLanguage, it, ctx.player, index))
            }
                .forEach { (language, future) ->
                    future.whenCompleteAsync { translated, e ->
                        if (e != null) {
                            //e.printStackTrace()
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

        registerReceiver(PacketIds.SET_CURRENT_LANGUAGE) { buf, ctx ->
            val language = buf.language
            val player = ctx.player

            playerLanguages[player.uuid] = language
        }

        registerReceiver(PacketIds.TRANSLATE_SIGN) { buf, ctx ->
            val pos = buf.pos

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
            proxy.sendPacketServer(player, ServerSupportPayload.EMPTY)
        }

        PlayerEvent.PLAYER_QUIT.register { player ->
            usedLanguages.remove(player.uuid)
        }
    }

    private fun <T : CustomPacketPayload> registerReceiver(type: TypeAndCodec<RegistryFriendlyByteBuf, T>, receiver: NetworkManager.NetworkReceiver<T>) {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, type.type, type.codec, receiver)
    }

    private fun broadcastTranslations(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, translationsToSend: ConcurrentLinkedDeque<Language>, translations: ConcurrentHashMap<Language, String>) {
        val toSend = translations.filter { a -> translationsToSend.contains(a.key) }

        if (hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                proxy.sendPacketServer(player, SendTranscriptToClientPayload(source.uuid, sourceLanguage, index, updateTime, toSend))
            }
        } else {
            proxy.sendPacketServer(source, SendTranscriptToClientPayload(source.uuid, sourceLanguage, index, updateTime, toSend))
        }
    }
}