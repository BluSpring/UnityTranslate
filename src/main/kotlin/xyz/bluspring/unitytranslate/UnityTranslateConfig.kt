package xyz.bluspring.unitytranslate

import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.util.TriState
import xyz.bluspring.unitytranslate.client.gui.TranscriptBox
import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType
import java.util.*

@Serializable
data class UnityTranslateConfig(
    val client: ClientConfig = ClientConfig(),
    val server: CommonConfig = CommonConfig()
) {
    @Serializable
    data class ClientConfig(
        var enabled: Boolean = true,
        var openBrowserWithoutPrompt: Boolean = false,
        var muteTranscriptWhenVoiceChatMuted: Boolean = false,

        var transcriptBoxes: MutableList<TranscriptBox> = mutableListOf(),
        var transcriber: TranscriberType = TranscriberType.BROWSER,
        var language: Language = Language.ENGLISH
    )

    @Serializable
    data class CommonConfig(
        var translatePriority: Set<TranslationPriority> = EnumSet.of(
            TranslationPriority.CLIENT_GPU, // highest priority, prioritize using CUDA on the client-side.
            TranslationPriority.SERVER_GPU, // if supported, use CUDA on the server-side.
            TranslationPriority.OFFLOADED,  // use alternative servers if available
            TranslationPriority.SERVER_CPU, // otherwise, translate on the CPU.
            TranslationPriority.CLIENT_CPU, // worst case scenario, use client CPU.
        ),
        var shouldUseCuda: Boolean = true,
        var shouldRunTranslationServer: Boolean = true,
        var offloadServers: MutableList<OffloadedLibreTranslateServer> = mutableListOf(
            OffloadedLibreTranslateServer("https://trans.zillyhuhn.com"),
            OffloadedLibreTranslateServer("https://translate.fedilab.app", weight = 5, maxConcurrentTranslations = 3), // this server is pretty slow, use with doubt
            OffloadedLibreTranslateServer("https://libretranslate.devos.one", weight = 105)
        )
    )

    @Serializable
    data class OffloadedLibreTranslateServer(
        var url: String, // follows http://127.0.0.1:5000 - the /translate endpoint will be appended at the end automatically.
        var authKey: String? = null,
        var weight: Int = 100,
        var maxConcurrentTranslations: Int = 8
    )

    enum class TranslationPriority(val usesCuda: TriState) {
        SERVER_GPU(TriState.TRUE),
        SERVER_CPU(TriState.FALSE),
        CLIENT_GPU(TriState.TRUE),
        CLIENT_CPU(TriState.FALSE),
        OFFLOADED(TriState.DEFAULT)
    }
}
