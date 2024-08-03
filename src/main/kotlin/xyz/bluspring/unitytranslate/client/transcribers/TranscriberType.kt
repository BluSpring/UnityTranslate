package xyz.bluspring.unitytranslate.client.transcribers

import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.sphinx.SphinxSpeechTranscriber

enum class TranscriberType(val creator: (Language) -> SpeechTranscriber) {
    SPHINX(::SphinxSpeechTranscriber),
    BROWSER(::BrowserSpeechTranscriber)
}