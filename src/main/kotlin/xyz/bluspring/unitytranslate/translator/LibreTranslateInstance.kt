package xyz.bluspring.unitytranslate.translator

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.gson.JsonParser
import net.minecraft.util.random.Weight
import net.minecraft.util.random.WeightedEntry
import net.suuft.libretranslate.Translator
import xyz.bluspring.unitytranslate.Language
import java.net.URL

open class LibreTranslateInstance(val url: String, private var weight: Int) : WeightedEntry {
    private var cachedSupportedLanguages = HashMultimap.create<Language, Language>()
    var latency: Int = -1
        private set

    init {
        val startTime = System.currentTimeMillis()
        if (this.translate("Latency test for UnityTranslate", Language.ENGLISH, Language.SPANISH) == null)
            throw Exception("Failed to run latency test for LibreTranslate instance $url!")
        latency = (System.currentTimeMillis() - startTime).toInt()
    }

    protected fun setUrl() {
        Translator.setUrlApi("$url/translate")
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

        if (!supportedTargets.contains(to)) {
            return false
        }

        return true
    }

    fun translate(text: String, from: Language, to: Language): String? {
        if (!supportsLanguage(from, to))
            return null

        setUrl()
        return try {
            Translator.translate(from.code, to.code, text)
        } catch (_: Exception) {
            null
        }
    }

    override fun getWeight(): Weight {
        return Weight.of(weight)
    }
}