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
    )),
    CHINESE("zh", mapOf(
        TranscriberType.BROWSER to "zh"
    )),
    ITALIAN("it", mapOf(
        TranscriberType.BROWSER to "it"
    )),
    CHINESE_TRADITIONAL("zt", mapOf(
        TranscriberType.BROWSER to "zh" // TODO: ???
    )),
    CZECH("cs", mapOf(
        TranscriberType.BROWSER to "cs"
    )),
    DANISH("da", mapOf(
        TranscriberType.BROWSER to "da"
    )),
    DUTCH("nl", mapOf(
        TranscriberType.BROWSER to "nl"
    )),
    FINNISH("fi", mapOf(
        TranscriberType.BROWSER to "fi"
    )),
    GREEK("el", mapOf(
        TranscriberType.BROWSER to "el"
    )),
    HINDI("hi", mapOf(
        TranscriberType.BROWSER to "hi"
    )),
    HUNGARIAN("hu", mapOf(
        TranscriberType.BROWSER to "hu"
    )),
    INDONESIAN("id", mapOf(
        TranscriberType.BROWSER to "id"
    )),
    KOREAN("ko", mapOf(
        TranscriberType.BROWSER to "ko"
    )),
    NORWEGIAN("nb", mapOf(
        TranscriberType.BROWSER to "nb"
    )),
    POLISH("pl", mapOf(
        TranscriberType.BROWSER to "pl"
    )),
    TAGALOG("tl", mapOf(
        TranscriberType.BROWSER to "tl"
    )),
    THAI("th", mapOf(
        TranscriberType.BROWSER to "th"
    )),
    TURKISH("tr", mapOf(
        TranscriberType.BROWSER to "tr"
    )),
    UKRAINIAN("uk", mapOf(
        TranscriberType.BROWSER to "uk"
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
