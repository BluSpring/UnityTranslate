package xyz.bluspring.unitytranslate.client.transcribers.browser

import io.ktor.http.*
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
            call.respondText(contentType = ContentType.Text.Html, text = this::class.java.getResource("/website/speech_recognition.html")!!.readText())
        }

        get("/index.js") {
            call.respondText(contentType = ContentType.Text.Html, text = this::class.java.getResource("/website/speech.js")!!.readText()
                .replace("%SOCKET_PORT%", socketPort.toString())
            )
        }

        get("/speech.css") {
            call.respondText(contentType = ContentType.Text.CSS, text = this::class.java.getResource("/website/speech.css")!!.readText())
        }
    }
}