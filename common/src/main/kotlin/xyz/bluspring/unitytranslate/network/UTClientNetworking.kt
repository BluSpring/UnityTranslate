package xyz.bluspring.unitytranslate.network

import dev.architectury.event.events.client.ClientPlayerEvent
import dev.architectury.networking.NetworkManager
import net.minecraft.Util
import net.minecraft.client.Minecraft
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
        val proxy = UnityTranslate.instance.proxy

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.SERVER_SUPPORT) { buf, ctx ->
            UnityTranslateClient.connectedServerHasSupport = true
        }

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register { player ->
            Minecraft.getInstance().execute {
                val buf = proxy.createByteBuf()
                buf.writeEnumSet(EnumSet.copyOf(UnityTranslateClient.languageBoxes.map { it.language }), Language::class.java)

                proxy.sendPacketClient(PacketIds.SET_USED_LANGUAGES, buf)
            }

            Minecraft.getInstance().execute {
                val buf = UnityTranslate.instance.proxy.createByteBuf()
                buf.writeEnum(UnityTranslate.config.client.language)

                UnityTranslate.instance.proxy.sendPacketClient(PacketIds.SET_CURRENT_LANGUAGE, buf)
            }
        }

        if (UnityTranslate.IS_UNITY_SERVER && Minecraft.getInstance().level?.server == null) {
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.TOGGLE_MOD) { buf, ctx ->
                val isEnabled = buf.readBoolean()
                UnityTranslate.config.client.enabled = isEnabled

                if (isEnabled) {
                    val transcriber = UnityTranslateClient.transcriber

                    if (transcriber is BrowserSpeechTranscriber && transcriber.socket.totalConnections <= 0) {
                        val serverPort = transcriber.serverPort

                        ctx.queue {
                            if (UnityTranslate.config.client.openBrowserWithoutPrompt) {
                                Util.getPlatform().openUri("http://127.0.0.1:$serverPort")
                            } else {
                                Minecraft.getInstance().setScreen(OpenBrowserScreen("http://127.0.0.1:$serverPort"))
                            }
                        }
                    }
                }
            }
        }

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register { _ ->
            UnityTranslateClient.connectedServerHasSupport = false
        }

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.SEND_TRANSCRIPT) { buf, ctx ->
            val sourceId = buf.readUUID()
            val source = ctx.player.level().getPlayerByUUID(sourceId) ?: return@registerReceiver

            val sourceLanguage = buf.readEnum(Language::class.java)
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()

            val totalLanguages = buf.readVarInt()

            val boxes = UnityTranslateClient.languageBoxes

            for (i in 0 until totalLanguages) {
                val language = buf.readEnum(Language::class.java)
                val text = buf.readUtf()

                if (language == UnityTranslateClient.transcriber.language && sourceId == ctx.player?.uuid)
                    continue

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)

                if (box == null) {
                    TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, source, text, language, updateTime, false), language)
                }
            }
        }

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.MARK_INCOMPLETE) { buf, ctx ->
            val from = buf.readEnum(Language::class.java)
            val to = buf.readEnum(Language::class.java)
            val uuid = buf.readUUID()
            val index = buf.readVarInt()
            val isIncomplete = buf.readBoolean()

            val box = UnityTranslateClient.languageBoxes.firstOrNull { it.language == to } ?: return@registerReceiver
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }
    }
}