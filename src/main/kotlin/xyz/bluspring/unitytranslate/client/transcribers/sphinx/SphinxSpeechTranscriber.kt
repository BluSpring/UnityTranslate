package xyz.bluspring.unitytranslate.client.transcribers.sphinx

import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.client.transcribers.SpeechTranscriber

class SphinxSpeechTranscriber(language: Language) : SpeechTranscriber(language) {
    init {
        throw IllegalStateException("PocketSphinx transcription is currently not supported!")
    }

    override fun stop() {
    }
}