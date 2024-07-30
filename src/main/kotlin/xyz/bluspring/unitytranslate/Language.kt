package xyz.bluspring.unitytranslate

import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType

enum class Language(
    val code: String,
    val supportedTranscribers: Map<TranscriberType, String>
) {
    // Any languages that don't have translation support in LibreTranslate should not be supported here.
    ENGLISH("en", mapOf(
        TranscriberType.SPHINX to "en-us",
        TranscriberType.BROWSER to "en-US"
    )),
    SPANISH("es", mapOf(
        TranscriberType.SPHINX to "es-mx",
        TranscriberType.BROWSER to "es-013"
    )),
    PORTUGUESE("pt", mapOf(
        TranscriberType.SPHINX to "br-pt",
        TranscriberType.BROWSER to "pt-BR"
    )),
    FRENCH("fr", mapOf(
        TranscriberType.SPHINX to "fr-fr",
        TranscriberType.BROWSER to "fr"
    )),
    SWEDISH("sv", mapOf(
        TranscriberType.BROWSER to "sv"
    )),
    MALAY("ms", mapOf(
        TranscriberType.BROWSER to "ms"
    )),
    HEBREW("he", mapOf(
    )),
    ARABIC("ar", mapOf(
    ))
}
