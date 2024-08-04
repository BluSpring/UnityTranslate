package xyz.bluspring.unitytranslate

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType

enum class Language(
    val code: String,
    val supportedTranscribers: Map<TranscriberType, String>
) {
    // Any languages that don't have translation support in LibreTranslate should not be supported here.
    // Use this for reference for the Browser Transcriber: https://r12a.github.io/app-subtags/
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
        TranscriberType.BROWSER to "he"
    )),
    ARABIC("ar", mapOf(
        TranscriberType.BROWSER to "ar"
    )),
    GERMAN("de", mapOf(
        TranscriberType.BROWSER to "de"
    )),
    RUSSIAN("ru", mapOf(
        TranscriberType.BROWSER to "ru"
    )),
    JAPANESE("ja", mapOf(
        TranscriberType.BROWSER to "ja"
    ));

    override fun toString(): String {
        return "$name ($code)"
    }

    val text = Component.translatable("unitytranslate.language.$code")

    companion object {
        fun findLibreLang(code: String): Language? {
            return Language.entries.firstOrNull { it.code == code }
        }
    }
}
