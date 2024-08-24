package xyz.bluspring.unitytranslate.client.transcribers

import xyz.bluspring.unitytranslate.Language
import java.util.function.BiConsumer

abstract class SpeechTranscriber(var language: Language) {
    var lastIndex = 0
    var currentOffset = 0

    lateinit var updater: BiConsumer<Int, String>

    abstract fun stop()

    open fun changeLanguage(language: Language) {
        this.language = language
    }
}