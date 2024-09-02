package xyz.bluspring.unitytranslate.client.transcribers.browser

import com.sun.net.httpserver.HttpServer
import net.minecraft.client.resources.language.I18n
import xyz.bluspring.unitytranslate.UnityTranslate

object BrowserApplication {
    var socketPort: Int = 0

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
                .replace("\$GITHUB$", "<a href=\"https://github.com/BluSpring/UnityTranslate/issues\">GitHub</a>")
                .replace("\$BR$", "<br>")
        }

        return current
    }

    fun addHandler(server: HttpServer) {
        server.createContext("/") { ctx ->
            when (ctx.requestURI.path) {
                "/" -> {
                    ctx.responseHeaders.set("Content-Type", "text/html; charset=utf-8")

                    val byteArray = applyTranslations(this::class.java.getResource("/website/speech_recognition.html")!!.readText(Charsets.UTF_8)).toByteArray(Charsets.UTF_8)
                    ctx.sendResponseHeaders(200, byteArray.size.toLong())
                    ctx.responseBody.write(byteArray)
                    ctx.responseBody.close()
                }

                "/index.js" -> {
                    ctx.responseHeaders.set("Content-Type", "text/javascript; charset=utf-8")

                    val byteArray = applyTranslations(this::class.java.getResource("/website/speech.js")!!.readText(Charsets.UTF_8))
                        .replace("%SOCKET_PORT%", socketPort.toString())
                        .toByteArray(Charsets.UTF_8)
                    ctx.sendResponseHeaders(200, byteArray.size.toLong())
                    ctx.responseBody.write(byteArray)
                    ctx.responseBody.close()
                }

                "/speech.css" -> {
                    ctx.responseHeaders.set("Content-Type", "text/css; charset=utf-8")

                    val byteArray = applyTranslations(this::class.java.getResource("/website/speech.css")!!.readText(Charsets.UTF_8))
                        .toByteArray(Charsets.UTF_8)
                    ctx.sendResponseHeaders(200, byteArray.size.toLong())
                    ctx.responseBody.write(byteArray)
                    ctx.responseBody.close()
                }
            }
        }
    }
}
