package xyz.bluspring.unitytranslate.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.PacketIds
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.gui.EditTranscriptBoxesScreen
import xyz.bluspring.unitytranslate.client.gui.LanguageSelectScreen
import xyz.bluspring.unitytranslate.client.gui.TranscriptBox
import xyz.bluspring.unitytranslate.client.transcribers.SpeechTranscriber
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.function.BiConsumer

class UnityTranslateClient : ClientModInitializer {
    override fun onInitializeClient() {
        transcriber = UnityTranslate.config.client.transcriber.creator.invoke(UnityTranslate.config.client.language)
        setupTranscriber(transcriber)

        HudRenderCallback.EVENT.register { guiGraphics, delta ->
            if (shouldRenderBoxes) {
                for (languageBox in languageBoxes) {
                    languageBox.render(guiGraphics)
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            if (CONFIGURE_BOXES.consumeClick()) {
                it.setScreen(EditTranscriptBoxesScreen(languageBoxes))
            }

            if (TOGGLE_TRANSCRIPTION.consumeClick()) {
                shouldTranscribe = !shouldTranscribe
                it.player?.displayClientMessage(Component.translatable("unitytranslate.transcript")
                    .append(": ")
                    .append(if (shouldTranscribe) CommonComponents.OPTION_ON else CommonComponents.OPTION_OFF), true
                )
            }

            if (TOGGLE_BOXES.consumeClick() && it.screen !is EditTranscriptBoxesScreen) {
                shouldRenderBoxes = !shouldRenderBoxes
                it.player?.displayClientMessage(Component.translatable("unitytranslate.transcript_boxes")
                    .append(": ")
                    .append(if (shouldRenderBoxes) CommonComponents.OPTION_ON else CommonComponents.OPTION_OFF), true
                )
            }

            if (SET_SPOKEN_LANGUAGE.consumeClick() && it.screen == null) {
                it.setScreen(LanguageSelectScreen(null, false))
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SERVER_SUPPORT) { client, listener, buf, sender ->
            connectedServerHasSupport = true
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { client, listener, buf, sender ->
            val sourceId = buf.readUUID()
            val source = client.level!!.getPlayerByUUID(sourceId) ?: return@registerGlobalReceiver

            val sourceLanguage = buf.readEnum(Language::class.java)
            val updateLast = buf.readBoolean()
            val totalLanguages = buf.readVarInt()

            val boxes = languageBoxes

            for (i in 0 until totalLanguages) {
                val language = buf.readEnum(Language::class.java)
                val text = buf.readUtf()

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, updateLast)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            connectedServerHasSupport = false
        }
    }

    fun setupTranscriber(transcriber: SpeechTranscriber) {
        transcriber.updater = BiConsumer { updateLast, text ->
            if (!shouldTranscribe)
                return@BiConsumer

            if (connectedServerHasSupport) {
                val buf = PacketByteBufs.create()
                buf.writeEnum(transcriber.language)
                buf.writeUtf(text)
                buf.writeBoolean(updateLast)

                ClientPlayNetworking.send(PacketIds.SEND_TRANSCRIPT, buf)
            } else {
                if (Minecraft.getInstance().player == null)
                    return@BiConsumer

                for (box in languageBoxes) {
                    if (box.language == transcriber.language) {
                        box.updateTranscript(Minecraft.getInstance().player!!, text, transcriber.language, updateLast)

                        continue
                    }

                    TranslatorManager.queueTranslation(text, transcriber.language, box.language)
                        .thenApplyAsync {
                            box.updateTranscript(Minecraft.getInstance().player!!, it, transcriber.language, updateLast)
                        }
                }
            }
        }
    }

    companion object {
        lateinit var transcriber: SpeechTranscriber

        var connectedServerHasSupport = false

        var shouldTranscribe = true
        var shouldRenderBoxes = true

        val languageBoxes: MutableList<TranscriptBox>
            get() {
                return UnityTranslate.config.client.transcriptBoxes
            }

        val CONFIGURE_BOXES = KeyBindingHelper.registerKeyBinding(KeyMapping("unitytranslate.configure_boxes", GLFW.GLFW_KEY_KP_7, "UnityTranslate"))
        val TOGGLE_TRANSCRIPTION = KeyBindingHelper.registerKeyBinding(KeyMapping("unitytranslate.toggle_transcription", GLFW.GLFW_KEY_KP_8, "UnityTranslate"))
        val TOGGLE_BOXES = KeyBindingHelper.registerKeyBinding(KeyMapping("unitytranslate.toggle_boxes", GLFW.GLFW_KEY_KP_9, "UnityTranslate"))
        val SET_SPOKEN_LANGUAGE = KeyBindingHelper.registerKeyBinding(KeyMapping("unitytranslate.set_spoken_language", GLFW.GLFW_KEY_KP_6, "UnityTranslate"))

        fun displayMessage(component: Component, isError: Boolean = false) {
            val full = Component.empty()
                .append(Component.literal("[UnityTranslate]: ")
                    .withStyle(if (isError) ChatFormatting.RED else ChatFormatting.YELLOW, ChatFormatting.BOLD)
                )
                .append(component)

            Minecraft.getInstance().gui.chat.addMessage(full)
        }
    }
}