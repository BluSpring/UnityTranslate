package xyz.bluspring.unitytranslate.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.client.gui.EditTranscriptBoxesScreen
import xyz.bluspring.unitytranslate.client.gui.TranscriptBox
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.function.BiConsumer

class UnityTranslateClient : ClientModInitializer {
    override fun onInitializeClient() {
        val transcriber = BrowserSpeechTranscriber(Language.ENGLISH)

        val languageBoxes = mutableListOf<TranscriptBox>()
        languageBoxes.add(
            TranscriptBox(
                5, 150, 150, 170, 120, Language.ENGLISH
            )
        )

        languageBoxes.add(
            TranscriptBox(
                35, 150, 150, 170, 120, Language.SPANISH
            )
        )

        languageBoxes.add(
            TranscriptBox(
                75, 150, 150, 170, 120, Language.MALAY
            )
        )

        languageBoxes.add(
            TranscriptBox(
                105, 150, 150, 170, 120, Language.FRENCH
            )
        )

        languageBoxes.add(
            TranscriptBox(
                125, 150, 150, 170, 120, Language.SWEDISH
            )
        )

        languageBoxes.add(
            TranscriptBox(
                155, 150, 150, 170, 120, Language.PORTUGUESE
            )
        )

        languageBoxes.add(
            TranscriptBox(
                175, 150, 150, 170, 120, Language.ARABIC
            )
        )

        languageBoxes.add(
            TranscriptBox(
                195, 150, 150, 170, 120, Language.HEBREW
            )
        )

        transcriber.updater = BiConsumer { i, text ->
            for (box in languageBoxes) {
                if (box.language == transcriber.language) {
                    box.transcripts[i] = text
                    continue
                }

                box.transcripts[i] = TranslatorManager.translateLine(text, transcriber.language, box.language)
            }
        }

        HudRenderCallback.EVENT.register { guiGraphics, delta ->
            for (languageBox in languageBoxes) {
                languageBox.render(guiGraphics)
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            if (CONFIGURE_BOXES.consumeClick()) {
                it.setScreen(EditTranscriptBoxesScreen(languageBoxes))
            }
        }
    }

    companion object {
        val CONFIGURE_BOXES = KeyBindingHelper.registerKeyBinding(KeyMapping("unitytranslate.configure_boxes", GLFW.GLFW_KEY_KP_7, "UnityTranslate"))
    }
}