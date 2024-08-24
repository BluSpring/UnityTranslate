package xyz.bluspring.unitytranslate.client.transcribers.browser

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.minecraft.client.resources.language.I18n
import xyz.bluspring.unitytranslate.UnityTranslate

var socketPort: Int = 0

var wsPort: Int
    get() = socketPort
    set(value) {
        socketPort = value
    }

val translationMap = mapOf(
    "TRANSCRIPT" to "unitytranslate.transcript",
    "INFO" to "unitytranslate.transcriber.browser.info",
    "TITLE" to "unitytranslate.transcriber.browser.title",
    "PAUSED" to "unitytranslate.transcriber.browser.paused",
    "PAUSED_DESC" to "unitytranslate.transcriber.browser.paused.desc",
)

private fun applyTranslations(text: String): String {
    var current = text

    for ((id, key) in translationMap) {
        val translated = I18n.get(key)

        // Protection against translation keys
        if (translated.contains('<') || translated.contains('>')) {
            UnityTranslate.logger.error("UnityTranslate has detected HTML tag characters in translation key $key! To protect your system, UnityTranslate has forcefully crashed the transcriber (and potentially the game) to avoid any malicious actors.")
            UnityTranslate.logger.error("This may have occurred for one of the following reasons:")
            UnityTranslate.logger.error(" - A server resource pack added a malicious translation for $key")
            UnityTranslate.logger.error(" - Your currently applied resource pack contains a malicious translation for $key")
            UnityTranslate.logger.error(" - A world you have joined has a resource pack containing a malicious translation for $key")
            UnityTranslate.logger.error("")
            UnityTranslate.logger.error("Translated line: \"$translated\"")

            throw IllegalStateException("HTML tags detected in $key!")
        }

        current = current.replace("%I18N_$id%", translated)
            .replace("%GITHUB%", "<a href=\"https://github.com/BluSpring/UnityTranslate/issues\">GitHub</a>")
            .replace("%BR%", "<br>")
    }

    return current
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText(contentType = ContentType.Text.Html, text = applyTranslations(this::class.java.getResource("/website/speech_recognition.html")!!.readText()))
        }

        get("/index.js") {
            call.respondText(contentType = ContentType.Text.Html, text = applyTranslations(this::class.java.getResource("/website/speech.js")!!.readText())
                .replace("%SOCKET_PORT%", socketPort.toString())
            )
        }

        get("/speech.css") {
            call.respondText(contentType = ContentType.Text.CSS, text = applyTranslations(this::class.java.getResource("/website/speech.css")!!.readText()))
        }
    }
}