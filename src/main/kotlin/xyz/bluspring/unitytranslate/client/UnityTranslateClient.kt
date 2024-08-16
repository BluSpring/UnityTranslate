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
import xyz.bluspring.unitytranslate.client.transcribers.windows.sapi5.WindowsSpeechApiTranscriber
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.*
import java.util.function.BiConsumer

class UnityTranslateClient : ClientModInitializer {
    override fun onInitializeClient() {
        WindowsSpeechApiTranscriber.isSupported() // runs a check to load Windows Speech API. why write the code again anyway?

        transcriber = UnityTranslate.config.client.transcriber.creator.invoke(UnityTranslate.config.client.language)
        setupTranscriber(transcriber)

        HudRenderCallback.EVENT.register { guiGraphics, delta ->
            if (shouldRenderBoxes) {
                for (languageBox in languageBoxes) {
                    languageBox.render(guiGraphics)
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (CONFIGURE_BOXES.consumeClick()) {
                mc.setScreen(EditTranscriptBoxesScreen(languageBoxes))
            }

            if (TOGGLE_TRANSCRIPTION.consumeClick()) {
                shouldTranscribe = !shouldTranscribe
                mc.player?.displayClientMessage(
                    Component.translatable("unitytranslate.transcript")
                        .append(": ")
                        .append(if (shouldTranscribe) CommonComponents.OPTION_ON else CommonComponents.OPTION_OFF), true
                )
            }

            if (TOGGLE_BOXES.consumeClick() && mc.screen !is EditTranscriptBoxesScreen) {
                shouldRenderBoxes = !shouldRenderBoxes
                mc.player?.displayClientMessage(
                    Component.translatable("unitytranslate.transcript_boxes")
                        .append(": ")
                        .append(if (shouldRenderBoxes) CommonComponents.OPTION_ON else CommonComponents.OPTION_OFF),
                    true
                )
            }

            if (SET_SPOKEN_LANGUAGE.consumeClick() && mc.screen == null) {
                mc.setScreen(LanguageSelectScreen(null, false))
            }

            if (CLEAR_TRANSCRIPTS.consumeClick()) {
                for (box in languageBoxes) {
                    box.transcripts.clear()
                }
            }

            // prune transcripts
            for (box in languageBoxes) {
                if (box.transcripts.size > 50) {
                    for (i in 0..(box.transcripts.size - 50)) {
                        box.transcripts.remove()
                    }
                }
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SERVER_SUPPORT) { client, listener, buf, sender ->
            connectedServerHasSupport = true
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            client.execute {
                val buf = PacketByteBufs.create()
                buf.writeEnumSet(EnumSet.copyOf(languageBoxes.map { it.language }), Language::class.java)

                ClientPlayNetworking.send(PacketIds.SET_USED_LANGUAGES, buf)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { client, listener, buf, sender ->
            val sourceId = buf.readUUID()
            val source = client.level!!.getPlayerByUUID(sourceId) ?: return@registerGlobalReceiver

            val sourceLanguage = buf.readEnum(Language::class.java)
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()

            val totalLanguages = buf.readVarInt()

            val boxes = languageBoxes

            for (i in 0 until totalLanguages) {
                val language = buf.readEnum(Language::class.java)
                val text = buf.readUtf()

                if (language == transcriber.language)
                    continue

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.MARK_INCOMPLETE) { client, listener, buf, sender ->
            val from = buf.readEnum(Language::class.java)
            val to = buf.readEnum(Language::class.java)
            val uuid = buf.readUUID()
            val index = buf.readVarInt()
            val isIncomplete = buf.readBoolean()

            val box = languageBoxes.firstOrNull { it.language == to } ?: return@registerGlobalReceiver
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            connectedServerHasSupport = false
        }
    }

    fun setupTranscriber(transcriber: SpeechTranscriber) {
        transcriber.updater = BiConsumer { index, text ->
            if (!shouldTranscribe)
                return@BiConsumer

            val updateTime = System.currentTimeMillis()

            if (connectedServerHasSupport) {
                val buf = PacketByteBufs.create()
                buf.writeEnum(transcriber.language)
                buf.writeUtf(text)
                buf.writeVarInt(index)
                buf.writeVarLong(updateTime)

                ClientPlayNetworking.send(PacketIds.SEND_TRANSCRIPT, buf)
                languageBoxes.firstOrNull { it.language == transcriber.language }?.updateTranscript(Minecraft.getInstance().player!!, text, transcriber.language, index, updateTime, false)
            } else {
                if (Minecraft.getInstance().player == null)
                    return@BiConsumer

                for (box in languageBoxes) {
                    if (box.language == transcriber.language) {
                        box.updateTranscript(Minecraft.getInstance().player!!, text, transcriber.language, index, updateTime, false)

                        continue
                    }

                    TranslatorManager.queueTranslation(text, transcriber.language, box.language, Minecraft.getInstance().player!!, index)
                        .whenCompleteAsync { it, e ->
                            if (e != null)
                                return@whenCompleteAsync

                            box.updateTranscript(Minecraft.getInstance().player!!, it, transcriber.language, index, updateTime, false)
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
        val CLEAR_TRANSCRIPTS = KeyBindingHelper.registerKeyBinding(KeyMapping("unitytranslate.clear_transcripts", GLFW.GLFW_KEY_KP_5, "UnityTranslate"))

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