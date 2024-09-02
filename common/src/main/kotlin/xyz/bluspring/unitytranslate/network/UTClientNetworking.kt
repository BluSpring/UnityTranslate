package xyz.bluspring.unitytranslate.network

import dev.architectury.event.events.client.ClientPlayerEvent
import dev.architectury.networking.NetworkManager
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.gui.OpenBrowserScreen
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import xyz.bluspring.unitytranslate.network.payloads.SetCurrentLanguagePayload
import xyz.bluspring.unitytranslate.network.payloads.SetUsedLanguagesPayload
import xyz.bluspring.unitytranslate.transcript.Transcript
import java.util.*

object UTClientNetworking {
    fun init() {
        val proxy = UnityTranslate.instance.proxy

        registerReceiver(PacketIds.SERVER_SUPPORT) { buf, ctx ->
            UnityTranslateClient.connectedServerHasSupport = true
        }

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register { player ->
            Minecraft.getInstance().execute {
                proxy.sendPacketClient(SetUsedLanguagesPayload(UnityTranslateClient.languageBoxes.map { it.language }))
            }

            Minecraft.getInstance().execute {
                UnityTranslate.instance.proxy.sendPacketClient(SetCurrentLanguagePayload(UnityTranslate.config.client.language))
            }
        }

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register { _ ->
            UnityTranslateClient.connectedServerHasSupport = false
        }

        registerReceiver(PacketIds.SEND_TRANSCRIPT_TO_CLIENT) { buf, ctx ->
            val sourceId = buf.uuid
            val source = ctx.player.level().getPlayerByUUID(sourceId) ?: return@registerReceiver

            val sourceLanguage = buf.language
            val index = buf.index
            val updateTime = buf.updateTime

            val boxes = UnityTranslateClient.languageBoxes

            for ((language, text) in buf.toSend) {
                if (language == UnityTranslateClient.transcriber.language && sourceId == ctx.player?.uuid)
                    continue

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)

                if (box == null) {
                    TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, source, text, language, updateTime, false), language)
                }
            }
        }

        registerReceiver(PacketIds.MARK_INCOMPLETE) { buf, ctx ->
            val from = buf.from
            val to = buf.to
            val uuid = buf.uuid
            val index = buf.index
            val isIncomplete = buf.isIncomplete

            val box = UnityTranslateClient.languageBoxes.firstOrNull { it.language == to } ?: return@registerReceiver
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }
    }

    private fun <T : CustomPacketPayload> registerReceiver(type: TypeAndCodec<RegistryFriendlyByteBuf, T>, receiver: NetworkManager.NetworkReceiver<T>) {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, type.type, type.codec, receiver)
    }
}