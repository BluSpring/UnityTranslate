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

class UnityTranslateClient : ClientModInitializer {
    override fun onInitializeClient() {
        val transcriber = BrowserSpeechTranscriber(Language.ENGLISH)

        val languageBoxes = mutableListOf<TranscriptBox>()
        languageBoxes.add(
            TranscriptBox(
                5, 150, 150, 170, 120, Language.ENGLISH, transcriber
            )
        )

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