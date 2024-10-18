package xyz.bluspring.unitytranslate.config

import kotlinx.serialization.Serializable
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.client.gui.TranscriptBox
import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType

@Serializable
data class UnityTranslateConfig(
    val client: ClientConfig = ClientConfig(),
    val server: CommonConfig = CommonConfig()
) {
    @Serializable
    data class ClientConfig(
        var enabled: Boolean = true,
        var openBrowserWithoutPrompt: TriState = TriState.DEFAULT,
        var muteTranscriptWhenVoiceChatMuted: Boolean = true,

        @get:IntRange(from = 10, to = 300, increment = 10)
        var textScale: Int = 100,

        val transcriptBoxes: MutableList<TranscriptBox> = mutableListOf(),

        @get:Hidden
        val transcriber: TranscriberType = TranscriberType.BROWSER,

        @get:Hidden
        var language: Language = Language.ENGLISH,

        var disappearingText: Boolean = true,
        @get:DependsOn("disappearingText")
        @get:FloatRange(from = 0.2f, to = 60.0f, increment = 0.1f)
        var disappearingTextDelay: Float = 20.0f,
        @get:DependsOn("disappearingText")
        @get:FloatRange(from = 0.0f, to = 5.0f, increment = 0.1f)
        var disappearingTextFade: Float = 0.5f
    )

    @Serializable
    data class CommonConfig(
        var translatePriority: MutableList<TranslationPriority> = mutableListOf(
            TranslationPriority.CLIENT_GPU, // highest priority, prioritize using CUDA on the client-side.
            //TranslationPriority.SERVER_GPU, // if supported, use CUDA on the server-side. // TODO: make this not fucking require LWJGL
            TranslationPriority.SERVER_CPU, // otherwise, translate on the CPU.
            TranslationPriority.OFFLOADED,  // use alternative servers if available
            TranslationPriority.CLIENT_CPU, // worst case scenario, use client CPU.
        ),

        @get:DependsOn("shouldRunTranslationServer")
        var shouldUseCuda: Boolean = true,

        var shouldRunTranslationServer: Boolean = true,
        @get:DependsOn("shouldRunTranslationServer")
        @get:IntRange(from = 1, to = 128, increment = 1)
        var libreTranslateThreads: Int = 4,

        var offloadServers: MutableList<OffloadedLibreTranslateServer> = mutableListOf(
            OffloadedLibreTranslateServer("https://libretranslate.devos.gay"),
            OffloadedLibreTranslateServer("https://trans.zillyhuhn.com"),
        ),

        // Interval for when the batch translations will be sent.
        // This is done so redundant translations don't go through,
        // which puts unnecessary stress on the translation instances.
        @get:FloatRange(from = 0.5f, to = 5.0f, increment = 0.1f)
        var batchTranslateInterval: Float = 0.5f, // 500ms
    )

    @Serializable
    data class OffloadedLibreTranslateServer(
        var url: String, // follows http://127.0.0.1:5000 - the /translate endpoint will be appended at the end automatically.
        var authKey: String? = null,
        var weight: Int = 100,
        var maxConcurrentTranslations: Int = 20
    )

    enum class TriState {
        TRUE, FALSE, DEFAULT
    }

    enum class TranslationPriority(val usesCuda: TriState) {
        SERVER_GPU(TriState.TRUE),
        SERVER_CPU(TriState.FALSE),
        CLIENT_GPU(TriState.TRUE),
        CLIENT_CPU(TriState.FALSE),
        OFFLOADED(TriState.DEFAULT)
    }
}
