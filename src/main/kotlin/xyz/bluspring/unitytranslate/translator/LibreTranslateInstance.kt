package xyz.bluspring.unitytranslate.translator

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.gson.JsonParser
import net.minecraft.util.random.Weight
import net.minecraft.util.random.WeightedEntry
import net.suuft.libretranslate.exception.BadTranslatorResponseException
import net.suuft.libretranslate.type.TranslateResponse
import net.suuft.libretranslate.util.JsonUtil
import xyz.bluspring.unitytranslate.Language
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

open class LibreTranslateInstance(val url: String, private var weight: Int) : WeightedEntry {
    private var cachedSupportedLanguages = HashMultimap.create<Language, Language>()
    var latency: Int = -1
        private set

    var currentlyTranslating = 0

    init {
        val startTime = System.currentTimeMillis()
        if (this.translate("Latency test for UnityTranslate", Language.ENGLISH, Language.SPANISH) == null)
            throw Exception("Failed to run latency test for LibreTranslate instance $url!")
        latency = (System.currentTimeMillis() - startTime).toInt()
    }

    val supportedLanguages: Multimap<Language, Language>
        get() {
            if (cachedSupportedLanguages.isEmpty) {
                val array = JsonParser.parseString(URL("$url/languages").readText()).asJsonArray

                for (element in array) {
                    val langData = element.asJsonObject
                    val srcLang = Language.findLibreLang(langData.get("code").asString) ?: continue

                    val targets = langData.getAsJsonArray("targets")
                    for (target in targets) {
                        val targetLang = Language.findLibreLang(target.asString) ?: continue
                        cachedSupportedLanguages.put(srcLang, targetLang)
                    }
                }
            }

            return cachedSupportedLanguages
        }

    fun supportsLanguage(from: Language, to: Language): Boolean {
        if (!supportedLanguages.containsKey(from)) {
            return false
        }

        val supportedTargets = supportedLanguages.get(from)

        return supportedTargets.contains(to)
    }

    fun translate(text: String, from: Language, to: Language): String? {
        if (!supportsLanguage(from, to))
            return null

        return try {
            translate(from.code, to.code, text)
        } catch (e: Exception) {
            null
        }
    }

    // Copied from LibreTranslate-Java, there were race conditions everywhere.
    fun translate(from: String, to: String, request: String): String {
        try {
            val url = URL("$url/translate")
            val httpConn = url.openConnection() as HttpURLConnection
            httpConn.requestMethod = "POST"
            httpConn.setRequestProperty("accept", "application/json")
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            httpConn.doOutput = true

            val writer = OutputStreamWriter(httpConn.outputStream)
            writer.write(
                "q=" + URLEncoder.encode(
                    request,
                    "UTF-8"
                ) + "&source=" + from + "&target=" + to + "&format=text"
            )

            writer.flush()
            writer.close()
            httpConn.outputStream.close()
            if (httpConn.responseCode / 100 != 2) {
                throw BadTranslatorResponseException(httpConn.responseCode, "${this.url}/translate")
            } else {
                val responseStream = httpConn.inputStream
                val s = Scanner(responseStream).useDelimiter("\\A")
                val response = if (s.hasNext()) s.next() else ""
                return (JsonUtil.from(
                    response,
                    TranslateResponse::class.java
                ) as TranslateResponse).translatedText
            }
        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getWeight(): Weight {
        return Weight.of(weight)
    }

    companion object {
        const val MAX_CONCURRENT_TRANSLATIONS = 5
    }
}