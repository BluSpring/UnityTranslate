package xyz.bluspring.unitytranslate.translator

import net.suuft.libretranslate.Translator
import xyz.bluspring.unitytranslate.Language

object TranslatorManager {
    init {
        Translator.setUrlApi("http://127.0.0.1:5000/translate")
    }

    fun translateLine(line: String, from: Language, to: Language): String {
        return Translator.translate(from.code, to.code, line)
    }

    fun init() {}
}