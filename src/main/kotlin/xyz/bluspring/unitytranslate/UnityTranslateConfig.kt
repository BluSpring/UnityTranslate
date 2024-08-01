package xyz.bluspring.unitytranslate

import net.fabricmc.fabric.api.util.TriState
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
            TranslationPriority.OFFLOADED,  // use alternative servers if available
            TranslationPriority.SERVER_CPU, // otherwise, translate on the CPU.
            TranslationPriority.CLIENT_CPU, // worst case scenario, use client CPU.
        ),
        var shouldUseCuda: Boolean = true,
        var shouldRunTranslationServer: Boolean = true,
        var offloadServers: MutableList<OffloadedLibreTranslateServer> = mutableListOf(
            OffloadedLibreTranslateServer("https://trans.zillyhuhn.com"),
            OffloadedLibreTranslateServer("https://translate.fedilab.app", weight = 5), // this server is pretty slow, use with doubt
            OffloadedLibreTranslateServer("https://devos.one")
        )
    )

    data class OffloadedLibreTranslateServer(
        var url: String, // follows http://127.0.0.1:5000 - the /translate endpoint will be appended at the end automatically.
        var authKey: String? = null,
        var weight: Int = 100
    )

    enum class TranslationPriority(val usesCuda: TriState) {
        SERVER_GPU(TriState.TRUE),
        SERVER_CPU(TriState.FALSE),
        CLIENT_GPU(TriState.TRUE),
        CLIENT_CPU(TriState.FALSE),
        OFFLOADED(TriState.DEFAULT)
    }
}
