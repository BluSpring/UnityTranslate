package xyz.bluspring.unitytranslate.translator

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.util.random.Weight
import net.minecraft.util.random.WeightedEntry
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.util.Flags
import xyz.bluspring.unitytranslate.util.HttpHelper
import java.net.URL

open class LibreTranslateInstance(val url: String, private var weight: Int, val authKey: String? = null) : WeightedEntry {
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

    fun batchTranslate(texts: List<String>, from: Language, to: Language): List<String>? {
        if (!supportsLanguage(from, to))
            return null

        return try {
            batchTranslate(from.code, to.code, texts)
        } catch (e: Exception) {
            if (SHOULD_PRINT_ERRORS)
                e.printStackTrace()

            null
        }
    }

    open fun detectLanguage(text: String): Language? {
        val detected = HttpHelper.post("$url/detect", JsonObject().apply {
            addProperty("q", text)

            if (authKey?.isNotBlank() == true)
                addProperty("api_key", authKey)
        }).asJsonArray.sortedByDescending { it.asJsonObject.get("confidence").asDouble }

        val langCode = detected.firstOrNull()?.asJsonObject?.get("language")?.asString ?: return null
        val lang = Language.findLibreLang(langCode)

        if (lang == null) {
            UnityTranslate.logger.error("Failed to find language for LibreTranslate code $langCode!")
        }

        return lang
    }

    open fun batchTranslate(from: String, to: String, request: List<String>): List<String> {
        val translated = HttpHelper.post("$url/translate", JsonObject().apply {
            addProperty("source", from)
            addProperty("target", to)
            add("q", JsonArray().apply {
                for (s in request) {
                    this.add(s)
                }
            })

            if (authKey?.isNotBlank() == true)
                addProperty("api_key", authKey)
        }).asJsonObject.get("translatedText").asJsonArray

        return translated.map { it.asString }
    }

    fun translate(text: String, from: Language, to: Language): String? {
        if (!supportsLanguage(from, to))
            return null

        return try {
            translate(from.code, to.code, text)
        } catch (e: Exception) {
            if (SHOULD_PRINT_ERRORS)
                e.printStackTrace()

            null
        }
    }

    open fun translate(from: String, to: String, request: String): String {
        return HttpHelper.post("$url/translate", JsonObject().apply {
            addProperty("source", from)
            addProperty("target", to)
            addProperty("q", request)
            addProperty("format", "text")

            if (authKey?.isNotBlank() == true)
                addProperty("api_key", authKey)
        })
            .asJsonObject.get("translatedText").asString
    }

    override fun getWeight(): Weight {
        return Weight.of(weight)
    }

    companion object {
        const val MAX_CONCURRENT_TRANSLATIONS = 15
        val SHOULD_PRINT_ERRORS = Flags.PRINT_HTTP_ERRORS || UnityTranslate.instance.proxy.isDev
    }
}