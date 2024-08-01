package xyz.bluspring.unitytranslate

import java.util.*

data class UnityTranslateConfig(
    val client: ClientConfig = ClientConfig(),
    val server: CommonConfig = CommonConfig()
) {
    data class ClientConfig(
        var enabled: Boolean = true,
        var openBrowserWithoutPrompt: Boolean = false,
    )

    data class CommonConfig(
        var translatePriority: EnumSet<TranslationPriority> = EnumSet.of(
            TranslationPriority.CLIENT_GPU, // highest priority, prioritize using CUDA on the client-side.
            TranslationPriority.SERVER_GPU, // if supported, use CUDA on the server-side.
            TranslationPriority.SERVER_CPU, // otherwise, translate on the CPU.
            TranslationPriority.CLIENT_CPU, // worst case scenario, use client CPU.
        ),

    )

    data class OffloadedLibreTranslateServer(
        var url: String, // follows http://127.0.0.1:5000 - the /translate endpoint will be appended at the end automatically.
        var authKey: String? = null,
        var weight: Int = 100
    )

    enum class TranslationPriority(val usesCuda: Boolean) {
        SERVER_GPU(true),
        SERVER_CPU(false),
        CLIENT_GPU(true),
        CLIENT_CPU(false),
    }
}
