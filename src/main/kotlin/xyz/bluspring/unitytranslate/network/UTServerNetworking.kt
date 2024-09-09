package xyz.bluspring.unitytranslate.network

//#if MC >= 1.20.6
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
//$$ import xyz.bluspring.unitytranslate.network.payloads.MarkIncompletePayload
//$$ import xyz.bluspring.unitytranslate.network.payloads.SendTranscriptToClientPayload
//$$ import xyz.bluspring.unitytranslate.network.payloads.ServerSupportPayload
//#endif
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.networking.NetworkManager
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
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
        //#if MC >= 1.20.6
        //$$ PacketIds.init()
        //#endif

        val usedLanguages = ConcurrentHashMap<UUID, EnumSet<Language>>()

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.SET_USED_LANGUAGES) { buf, ctx ->
        //$$    val languages = EnumSet.copyOf(buf.languages)
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.SET_USED_LANGUAGES) { buf, ctx ->
            val languages = buf.readEnumSet(Language::class.java)
        //#endif
            usedLanguages[ctx.player.uuid] = languages
        }

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.SEND_TRANSCRIPT_TO_SERVER) { buf, ctx ->
        //$$    val sourceLanguage = buf.sourceLanguage
        //$$    val text = buf.text
        //$$    val index = buf.index
        //$$    val updateTime = buf.updateTime
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.SEND_TRANSCRIPT) { buf, ctx ->
            val sourceLanguage = buf.readEnum(Language::class.java)
            val text = buf.readUtf()
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()
        //#endif

            if (!canPlayerRequestTranslations(ctx.player))
                return@registerReceiver

            // TODO: probably make this better
            if (text.length > 1500) {
                ctx.player.displayClientMessage(Component.literal("Transcription too long! Current transcript discarded.").withStyle(ChatFormatting.RED), true)
                //#if MC >= 1.20.6
                //$$ proxy.sendPacketServer(ctx.player as ServerPlayer, MarkIncompletePayload(sourceLanguage, sourceLanguage, ctx.player.uuid, index, true))
                //#else
                val markBuf = proxy.createByteBuf()
                markBuf.writeEnum(sourceLanguage)
                markBuf.writeEnum(sourceLanguage)
                markBuf.writeUUID(ctx.player.uuid)
                markBuf.writeVarInt(index)
                markBuf.writeBoolean(true)

                proxy.sendPacketServer(ctx.player as ServerPlayer, PacketIds.MARK_INCOMPLETE, markBuf)
                //#endif
                return@registerReceiver
            }

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

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.SET_CURRENT_LANGUAGE) { buf, ctx ->
        //$$    val language = buf.language
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.SET_CURRENT_LANGUAGE) { buf, ctx ->
            val language = buf.readEnum(Language::class.java)
        //#endif
            val player = ctx.player

            playerLanguages[player.uuid] = language
        }

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.TRANSLATE_SIGN) { buf, ctx ->
        //$$    val pos = buf.pos
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketIds.TRANSLATE_SIGN) { buf, ctx ->
            val pos = buf.readBlockPos()
        //#endif

            if (!canPlayerRequestTranslations(ctx.player))
                return@registerReceiver

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
            proxy.sendPacketServer(player,
                //#if MC >= 1.20.6
                //$$ ServerSupportPayload.EMPTY
                //#else
                PacketIds.SERVER_SUPPORT, proxy.createByteBuf()
                //#endif
            )
        }

        PlayerEvent.PLAYER_QUIT.register { player ->
            usedLanguages.remove(player.uuid)
        }
    }

    //#if MC <= 1.20.4
    // turns out, Forge requires us to rebuild the buffer every time we send it to a player,
    // so unfortunately, we cannot reuse the buffer.
    private fun buildBroadcastPacket(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, toSend: Map<Language, String>): FriendlyByteBuf {
        val buf = proxy.createByteBuf()
        buf.writeUUID(source.uuid)
        buf.writeEnum(sourceLanguage)
        buf.writeVarInt(index)
        buf.writeVarLong(updateTime)

        buf.writeVarInt(toSend.size)

        for ((language, translated) in toSend) {
            buf.writeEnum(language)
            buf.writeUtf(translated)
        }

        return buf
    }
    //#endif

    private fun broadcastTranslations(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, translationsToSend: ConcurrentLinkedDeque<Language>, translations: ConcurrentHashMap<Language, String>) {
        val toSend = translations.filter { a -> translationsToSend.contains(a.key) }

        if (hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                //#if MC >= 1.20.6
                //$$ proxy.sendPacketServer(player, SendTranscriptToClientPayload(source.uuid, sourceLanguage, index, updateTime, toSend))
                //#else
                val buf = buildBroadcastPacket(source, sourceLanguage, index, updateTime, toSend)
                proxy.sendPacketServer(player, PacketIds.SEND_TRANSCRIPT, buf)
                //#endif
            }
        } else {
            //#if MC >= 1.20.6
            //$$ proxy.sendPacketServer(source, SendTranscriptToClientPayload(source.uuid, sourceLanguage, index, updateTime, toSend))
            //#else
            val buf = buildBroadcastPacket(source, sourceLanguage, index, updateTime, toSend)
            proxy.sendPacketServer(source, PacketIds.SEND_TRANSCRIPT, buf)
            //#endif
        }
    }

    fun canPlayerRequestTranslations(player: Player): Boolean {
        return Permissions.check(player, "unitytranslate.request_translations", true)
    }

    //#if MC >= 1.20.6
    //$$ private fun <T : CustomPacketPayload> registerReceiver(type: TypeAndCodec<RegistryFriendlyByteBuf, T>, receiver: NetworkManager.NetworkReceiver<T>) {
    //$$    NetworkManager.registerReceiver(NetworkManager.Side.C2S, type.type, type.codec, receiver)
    //$$ }
    //#endif
}