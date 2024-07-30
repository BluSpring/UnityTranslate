package xyz.bluspring.unitytranslate.client.transcribers.browser

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

var socketPort: Int = 0

var wsPort: Int
    get() = socketPort
    set(value) {
        socketPort = value
    }

fun Application.module() {
    routing {
        get("/") {
            call.respondText(this::class.java.getResource("/website/speech_recognition.html")!!.readText())
        }

        get("/index.js") {
            call.respondText(this::class.java.getResource("/website/speech.js")!!.readText()
                .replace("%SOCKET_PORT%", socketPort.toString())
            )
        }
    }
}