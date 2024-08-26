package xyz.bluspring.unitytranslate.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.Util
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.gui.OpenBrowserScreen
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import xyz.bluspring.unitytranslate.transcript.Transcript
import java.util.*

object UTClientNetworking {
    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SERVER_SUPPORT) { client, listener, buf, sender ->
            UnityTranslateClient.connectedServerHasSupport = true
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            client.execute {
                val buf = PacketByteBufs.create()
                buf.writeEnumSet(EnumSet.copyOf(UnityTranslateClient.languageBoxes.map { it.language }), Language::class.java)

                ClientPlayNetworking.send(PacketIds.SET_USED_LANGUAGES, buf)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.TOGGLE_MOD) { client, listener, buf, sender ->
            val isEnabled = buf.readBoolean()
            UnityTranslate.config.client.enabled = isEnabled

            if (isEnabled) {
                val transcriber = UnityTranslateClient.transcriber

                if (transcriber is BrowserSpeechTranscriber && transcriber.socket.totalConnections <= 0) {
                    val serverPort = transcriber.serverPort

                    client.execute {
                        if (UnityTranslate.config.client.openBrowserWithoutPrompt) {
                            Util.getPlatform().openUri("http://127.0.0.1:$serverPort")
                        } else {
                            client.setScreen(OpenBrowserScreen("http://127.0.0.1:$serverPort"))
                        }
                    }
                }
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            UnityTranslateClient.connectedServerHasSupport = false
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { client, listener, buf, sender ->
            val sourceId = buf.readUUID()
            val source = client.level!!.getPlayerByUUID(sourceId) ?: return@registerGlobalReceiver

            val sourceLanguage = buf.readEnum(Language::class.java)
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()

            val totalLanguages = buf.readVarInt()

            val boxes = UnityTranslateClient.languageBoxes

            for (i in 0 until totalLanguages) {
                val language = buf.readEnum(Language::class.java)
                val text = buf.readUtf()

                if (language == UnityTranslateClient.transcriber.language && sourceId == client.player?.uuid)
                    continue

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)

                if (box == null) {
                    TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, source, text, language, updateTime, false), language)
                }
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.MARK_INCOMPLETE) { client, listener, buf, sender ->
            val from = buf.readEnum(Language::class.java)
            val to = buf.readEnum(Language::class.java)
            val uuid = buf.readUUID()
            val index = buf.readVarInt()
            val isIncomplete = buf.readBoolean()

            val box = UnityTranslateClient.languageBoxes.firstOrNull { it.language == to } ?: return@registerGlobalReceiver
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }
    }
}