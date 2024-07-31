package xyz.bluspring.unitytranslate.translator

import net.suuft.libretranslate.Translator
import xyz.bluspring.unitytranslate.Language
import java.util.concurrent.CompletableFuture

object TranslatorManager {
    init {
        //Translator.setUrlApi("http://127.0.0.1:5000/translate")
    }

    fun queueTranslation(line: String, from: Language, to: Language): CompletableFuture<String> {
        return CompletableFuture.supplyAsync { translateLine(line, from, to) }
    }

    fun translateLine(line: String, from: Language, to: Language): String {
        return Translator.translate(from.code, to.code, line)
    }

    fun init() {}
}