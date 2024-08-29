package xyz.bluspring.unitytranslate.client

import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.client.ClientPlayerEvent
import dev.architectury.event.events.client.ClientTickEvent
import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.network.PacketIds
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.gui.*
import xyz.bluspring.unitytranslate.client.transcribers.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.windows.sapi5.WindowsSpeechApiTranscriber
import xyz.bluspring.unitytranslate.compat.talkballoons.TalkBalloonsCompat
import xyz.bluspring.unitytranslate.network.UTClientNetworking
import xyz.bluspring.unitytranslate.translator.LocalLibreTranslateInstance
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.BiConsumer
import java.util.function.Consumer

class UnityTranslateClient {
    init {
        WindowsSpeechApiTranscriber.isSupported() // runs a check to load Windows Speech API. why write the code again anyway?
        setupCompat()

        transcriber = UnityTranslate.config.client.transcriber.creator.invoke(UnityTranslate.config.client.language)
        setupTranscriber(transcriber)

        ClientGuiEvent.RENDER_HUD.register { guiGraphics, delta ->
            if (shouldRenderBoxes && UnityTranslate.config.client.enabled) {
                for (languageBox in languageBoxes) {
                    languageBox.render(guiGraphics, delta)
                }
            }
        }

        ClientTickEvent.CLIENT_POST.register { mc ->
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
            val currentTime = System.currentTimeMillis()
            for (box in languageBoxes) {
                if (box.transcripts.size > 50) {
                    for (i in 0..(box.transcripts.size - 50)) {
                        box.transcripts.remove()
                    }
                }

                val clientConfig = UnityTranslate.config.client

                if (clientConfig.disappearingText) {
                    for (transcript in box.transcripts) {
                        if (currentTime >= (transcript.arrivalTime + (clientConfig.disappearingTextDelay * 1000L).toLong() + (clientConfig.disappearingTextFade * 1000L).toLong())) {
                            box.transcripts.remove(transcript)
                        }
                    }
                }
            }
        }

        ClientLifecycleEvent.CLIENT_STOPPING.register {
            LocalLibreTranslateInstance.killOpenInstances()
        }

        UTClientNetworking.init()
    }

    fun setupTranscriber(transcriber: SpeechTranscriber) {
        transcriber.updater = BiConsumer { index, text ->
            if (!shouldTranscribe)
                return@BiConsumer

            val updateTime = System.currentTimeMillis()

            if (connectedServerHasSupport) {
                val buf = UnityTranslate.instance.proxy.createByteBuf()
                buf.writeEnum(transcriber.language)
                buf.writeUtf(text)
                buf.writeVarInt(index)
                buf.writeVarLong(updateTime)

                UnityTranslate.instance.proxy.sendPacketClient(PacketIds.SEND_TRANSCRIPT, buf)
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

    private fun setupCompat() {
        if (isTalkBalloonsInstalled) {
            TalkBalloonsCompat.init()
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

        val CONFIGURE_BOXES = (KeyMapping("unitytranslate.configure_boxes", -1, "UnityTranslate"))
        val TOGGLE_TRANSCRIPTION = (KeyMapping("unitytranslate.toggle_transcription", -1, "UnityTranslate"))
        val TOGGLE_BOXES = (KeyMapping("unitytranslate.toggle_boxes", -1, "UnityTranslate"))
        val SET_SPOKEN_LANGUAGE = (KeyMapping("unitytranslate.set_spoken_language", -1, "UnityTranslate"))
        val CLEAR_TRANSCRIPTS = (KeyMapping("unitytranslate.clear_transcripts", -1, "UnityTranslate"))

        @JvmStatic
        fun registerKeys() {
            KeyMappingRegistry.register(CONFIGURE_BOXES)
            KeyMappingRegistry.register(TOGGLE_TRANSCRIPTION)
            KeyMappingRegistry.register(TOGGLE_BOXES)
            KeyMappingRegistry.register(SET_SPOKEN_LANGUAGE)
            KeyMappingRegistry.register(CLEAR_TRANSCRIPTS)
        }

        val isTalkBalloonsInstalled = UnityTranslate.instance.proxy.isModLoaded("talk_balloons")

        fun displayMessage(component: Component, isError: Boolean = false) {
            val full = Component.empty()
                .append(Component.literal("[UnityTranslate]: ")
                    .withStyle(if (isError) ChatFormatting.RED else ChatFormatting.YELLOW, ChatFormatting.BOLD)
                )
                .append(component)

            Minecraft.getInstance().gui.chat.addMessage(full)
        }

        fun renderCreditText(guiGraphics: GuiGraphics) {
            val version = UnityTranslate.instance.proxy.modVersion
            val font = Minecraft.getInstance().font

            guiGraphics.drawString(font, "UnityTranslate v$version", 2, Minecraft.getInstance().window.guiScaledHeight - (font.lineHeight * 2) - 4, 0xAAAAAA)
            guiGraphics.drawString(font, Component.translatable("unitytranslate.credit.author"), 2, Minecraft.getInstance().window.guiScaledHeight - font.lineHeight - 2, 0xAAAAAA)
        }

        private val queuedForJoin = ConcurrentLinkedQueue<Consumer<Minecraft>>()

        init {
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register { _ ->
                for (consumer in queuedForJoin) {
                    consumer.accept(Minecraft.getInstance())
                }
                queuedForJoin.clear()
            }
        }

        fun openDownloadRequest() {
            queuedForJoin.add { mc ->
                if (mc.screen is OpenBrowserScreen) {
                    mc.execute {
                        mc.setScreen(RequestDownloadScreen().apply {
                            parent = mc.screen
                        })
                    }
                } else {
                    mc.execute {
                        mc.setScreen(RequestDownloadScreen())
                    }
                }
            }
        }
    }
}