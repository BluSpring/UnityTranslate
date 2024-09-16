package xyz.bluspring.unitytranslate.commands

import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.translator.LocalLibreTranslateInstance
import xyz.bluspring.unitytranslate.translator.TranslatorManager

object UnityTranslateClientCommands {
    val TRANSCRIBER = ClientCommandRegistrationEvent.literal("checktranscriber")
        .executes {
            if (UnityTranslateClient.transcriber is BrowserSpeechTranscriber) {
                (UnityTranslateClient.transcriber as BrowserSpeechTranscriber).openWebsite()
            }

            it.source.`arch$sendSuccess`({ Component.literal("Reopening browser transcriber if not opened") }, false)

            1
        }

    val INFO = ClientCommandRegistrationEvent.literal("info")
        .executes {
            it.source.`arch$sendSuccess`({
                ComponentUtils.formatList(
                    listOf(
                        Component.literal("UnityTranslate v${UnityTranslate.instance.proxy.modVersion}"),
                        Component.literal("- Enabled: ${UnityTranslate.config.client.enabled}"),
                        Component.literal("- Current transcriber: ${UnityTranslate.config.client.transcriber}"),
                        Component.literal("- Spoken language: ${UnityTranslate.config.client.language}"),
                        Component.empty(),
                        Component.literal("- Server supports UnityTranslate: ${UnityTranslateClient.connectedServerHasSupport}"),
                        Component.literal("- Supports local translation server: ${LocalLibreTranslateInstance.canRunLibreTranslate()}"),
                        Component.literal("- Is local translation server running: ${LocalLibreTranslateInstance.hasStarted}"),
                        Component.literal("- Supports CUDA: ${TranslatorManager.supportsCuda}"),
                    ), Component.literal("\n")
                )
            }, false)

            1
        }

    val OPEN_BROWSER = ClientCommandRegistrationEvent.literal("openbrowser")
        .executes {
            if (UnityTranslateClient.transcriber is BrowserSpeechTranscriber) {
                (UnityTranslateClient.transcriber as BrowserSpeechTranscriber).openWebsite()
            }

            1
        }

    val ROOT = ClientCommandRegistrationEvent.literal("unitytranslateclient")
        .then(TRANSCRIBER)
        .then(INFO)
        .then(OPEN_BROWSER)

    fun init() {
        ClientCommandRegistrationEvent.EVENT.register { dispatcher, ctx ->
            dispatcher.register(ROOT)
        }
    }
}