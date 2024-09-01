package xyz.bluspring.unitytranslate.client.transcribers.browser

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.architectury.event.events.client.ClientPlayerEvent
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.HttpUtil
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.gui.OpenBrowserScreen
import xyz.bluspring.unitytranslate.client.gui.RequestDownloadScreen
import xyz.bluspring.unitytranslate.client.transcribers.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType
import java.net.InetSocketAddress

class BrowserSpeechTranscriber(language: Language) : SpeechTranscriber(language) {
    val socketPort = HttpUtil.getAvailablePort()
    val server: ApplicationEngine
    val socket = BrowserSocket()
    val serverPort = if (!HttpUtil.isPortAvailable(25117))
        HttpUtil.getAvailablePort()
    else
        25117

    init {
        server = embeddedServer(Netty, port = serverPort, host = "0.0.0.0", module = Application::module)
            .start(wait = false)
            .apply {
                wsPort = socketPort
            }

        socket.isDaemon = true
        socket.start()

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register { _ ->
            openWebsite()
        }
    }

    fun openWebsite() {
        val mc = Minecraft.getInstance()

        if (socket.totalConnections <= 0 && UnityTranslate.config.client.enabled) {
            if (UnityTranslate.config.client.openBrowserWithoutPrompt) {
                Util.getPlatform().openUri("http://127.0.0.1:$serverPort")
            } else {
                Minecraft.getInstance().execute {
                    if (mc.screen is RequestDownloadScreen) {
                        (mc.screen as RequestDownloadScreen).parent = OpenBrowserScreen("http://127.0.0.1:$serverPort")
                    } else {
                        mc.setScreen(OpenBrowserScreen("http://127.0.0.1:$serverPort"))
                    }
                }
            }
        }
    }

    override fun stop() {
        server.stop(1000L, 1000L)
        socket.stop(1000)
    }

    override fun changeLanguage(language: Language) {
        super.changeLanguage(language)
        this.socket.broadcast("set_language", JsonObject().apply {
            addProperty("language", language.supportedTranscribers[TranscriberType.BROWSER])
        })
    }

    inner class BrowserSocket : WebSocketServer(InetSocketAddress("localhost", socketPort)) {
        var totalConnections = 0

        override fun onOpen(ws: WebSocket, handshake: ClientHandshake) {
            ws.sendData("set_language", JsonObject().apply {
                addProperty("language", language.supportedTranscribers[TranscriberType.BROWSER])
            })
            totalConnections++

            UnityTranslateClient.displayMessage(Component.translatable("unitytranslate.transcriber.connected"))
        }

        override fun onClose(ws: WebSocket, code: Int, reason: String, remote: Boolean) {
            totalConnections--

            UnityTranslateClient.displayMessage(Component.translatable("unitytranslate.transcriber.disconnected")
                .withStyle {
                    it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "http://127.0.0.1:${serverPort}"))
                })
        }

        override fun onMessage(ws: WebSocket, message: String) {
            val msg = JsonParser.parseString(message).asJsonObject
            val data = if (msg.has("d")) msg.getAsJsonObject("d") else JsonObject()

            when (msg.get("op").asString) {
                "transcript" -> {
                    val results = data.getAsJsonArray("results")
                    val index = data.get("index").asInt

                    val deserialized = mutableListOf<Pair<String, Double>>()
                    for (result in results) {
                        val d = result.asJsonObject
                        deserialized.add(d.get("text").asString to d.get("confidence").asDouble)
                    }

                    if (deserialized.isEmpty()) {
                        lastIndex = currentOffset + index
                        return
                    }

                    val selected = deserialized.sortedByDescending { it.second }[0].first

                    if (selected.isNotBlank()) {
                        updater.accept(currentOffset + index, selected.trim())
                    }

                    lastIndex = currentOffset + index
                }

                "reset" -> {
                    currentOffset = lastIndex + 1
                }

                "error" -> {
                    val type = data.get("type").asString

                    UnityTranslateClient.displayMessage(Component.translatable("unitytranslate.transcriber.error")
                        .append(Component.translatable("unitytranslate.transcriber.error.$type")), true)
                }
            }
        }

        override fun onError(ws: WebSocket, ex: Exception) {
            ex.printStackTrace()
        }

        override fun onStart() {
            UnityTranslate.logger.info("Started WebSocket server for Browser Transcriber mode at ${this.address}")
        }

        fun broadcast(op: String, data: JsonObject = JsonObject()) {
            super.broadcast(JsonObject().apply {
                this.addProperty("op", op)
                this.add("d", data)
            }.toString())
        }

        fun WebSocket.sendData(op: String, data: JsonObject = JsonObject()) {
            this.send(JsonObject().apply {
                this.addProperty("op", op)
                this.add("d", data)
            }.toString())
        }
    }
}