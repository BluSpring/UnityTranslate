package xyz.bluspring.unitytranslate

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.codec.StreamEncoder
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
    )),
    BULGARIAN("bg", mapOf(
        TranscriberType.BROWSER to "bg"
    )),
    ALBANIAN("sq", mapOf(
        TranscriberType.BROWSER to "sq"
    )),
    AZERBAIJANI("az", mapOf(
        TranscriberType.BROWSER to "az"
    )),
    BENGALI("bn", mapOf(
        TranscriberType.BROWSER to "bn"
    )),
    CATALAN("ca", mapOf(
        TranscriberType.BROWSER to "ca"
    )),
    ESPERANTO("eo", mapOf(
        TranscriberType.BROWSER to "eo"
    )),
    ESTONIAN("et", mapOf(
        TranscriberType.BROWSER to "et"
    )),
    IRISH("ga", mapOf(
        TranscriberType.BROWSER to "ga"
    )),
    LATVIAN("lv", mapOf(
        TranscriberType.BROWSER to "lv"
    )),
    LITHUANIAN("lt", mapOf(
        TranscriberType.BROWSER to "lt"
    )),
    PERSIAN("fa", mapOf(
        TranscriberType.BROWSER to "fa"
    )),
    ROMANIAN("ro", mapOf(
        TranscriberType.BROWSER to "ro"
    )),
    SLOVAK("sk", mapOf(
        TranscriberType.BROWSER to "sk"
    )),
    SLOVENIAN("sl", mapOf(
        TranscriberType.BROWSER to "sl"
    )),
    URDU("ur", mapOf(
        TranscriberType.BROWSER to "ur"
    ));

    override fun toString(): String {
        return "$name ($code)"
    }

    val text = Component.translatable("unitytranslate.language.$code")

    companion object {
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Language> = StreamCodec.of({ buf, language ->
            buf.writeEnum(language)
        }, { buf ->
            buf.readEnum(Language::class.java)
        })

        fun findLibreLang(code: String): Language? {
            return Language.entries.firstOrNull { it.code == code }
        }
    }
}
