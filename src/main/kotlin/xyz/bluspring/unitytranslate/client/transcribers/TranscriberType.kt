package xyz.bluspring.unitytranslate.client.transcribers

import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.sphinx.SphinxSpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.windows.sapi5.WindowsSpeechApiTranscriber

enum class TranscriberType(val creator: (Language) -> SpeechTranscriber, val enabled: Boolean = true) {
    SPHINX(::SphinxSpeechTranscriber, false),
    BROWSER(::BrowserSpeechTranscriber),
    WINDOWS_SAPI(::WindowsSpeechApiTranscriber, WindowsSpeechApiTranscriber.isSupported())
}