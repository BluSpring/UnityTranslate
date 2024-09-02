package xyz.bluspring.unitytranslate.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import xyz.bluspring.unitytranslate.transcript.Transcript
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Serializable
data class TranscriptBox(
    var offsetX: Int,
    var offsetY: Int,
    var width: Int,
    var height: Int,
    var opacity: Int,

    var language: Language,

    var offsetXEdge: Boolean = false,
    var offsetYEdge: Boolean = false
) {
    var x: Int
        get() {
            return if (offsetXEdge)
                Minecraft.getInstance().window.guiScaledWidth - offsetX
            else
                offsetX
        }
        set(value) {
            if (value > Minecraft.getInstance().window.guiScaledWidth / 2 - (width / 2)) {
                offsetXEdge = true
                offsetX = Minecraft.getInstance().window.guiScaledWidth - value
            } else {
                offsetXEdge = false
                offsetX = value
            }
        }

    var y: Int
        get() {
            return if (offsetYEdge)
                Minecraft.getInstance().window.guiScaledHeight - offsetY
            else
                offsetY
        }
        set(value) {
            if (value > Minecraft.getInstance().window.guiScaledHeight / 2 - (height / 2)) {
                offsetYEdge = true
                offsetY = Minecraft.getInstance().window.guiScaledHeight - value
            } else {
                offsetYEdge = false
                offsetY = value
            }
        }

    @Transient
    val transcripts = ConcurrentLinkedQueue<Transcript>()

    fun render(poseStack: PoseStack, partialTick: Float) {
        poseStack.pushPose()

        poseStack.translate(0.0, 0.0, -255.0)
        RenderSystem.enableScissor(x, y, x + width, y + height)
        
        Screen.fill(poseStack, x, y, x + width, y + height, FastColor.ARGB32.color(opacity, 0, 0, 0))
        Minecraft.getInstance().font.draw(poseStack, Component.translatable("unitytranslate.transcript").append(" (${language.code.uppercase()})")
            .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD), x + (width / 2).toFloat(), y + 5f, 16777215)

        if (!UnityTranslateClient.shouldTranscribe) {
            RenderSystem.setShaderTexture(0, TRANSCRIPT_MUTED)
            Screen.blit(poseStack, x + width - 20, y + 2, 0f, 0f, 16, 16, 16, 16)
        }

        RenderSystem.enableScissor(x, y + 15, x + width, y + height)

        val lines = transcripts.sortedByDescending { it.arrivalTime }

        val font = Minecraft.getInstance().font
        val scale = UnityTranslate.config.client.textScale / 100f
        val invScale = if (scale == 0f) 0f else 1f / scale

        var currentY = y + height - font.lineHeight

        for (transcript in lines) {
            val component = Component.empty()
                .append("<")
                .append(transcript.player.displayName)
                .append(
                    Component.literal(" (${transcript.language.code.uppercase(Locale.ENGLISH)})")
                        .withStyle(ChatFormatting.GREEN)
                )
                .append("> ")
                .append(
                    Component.literal(transcript.text)
                        .apply {
                            if (transcript.incomplete) {
                                this.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                            }
                        }
                )

            val currentTime = System.currentTimeMillis()
            val delay = (UnityTranslate.config.client.disappearingTextDelay * 1000L).toLong()
            if (UnityTranslate.config.client.disappearingText && currentTime >= transcript.arrivalTime + delay) {
                val fadeTime = (UnityTranslate.config.client.disappearingTextFade * 1000L).toLong()

                val fadeStart = transcript.arrivalTime + delay
                val fadeEnd = fadeStart + fadeTime
                val fadeAmount = ((fadeEnd - currentTime).toFloat() / fadeTime.toFloat())

                val alpha = Mth.clamp(fadeAmount, 0f, 1f)
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha)
            }

            val split = font.split(component, ((width - 5) * invScale).toInt()).reversed()

            for (line in split) {
                poseStack.pushPose()
                poseStack.translate(x.toDouble(), currentY.toDouble(), 0.0)
                poseStack.scale(scale, scale, scale)
                font.draw(poseStack, line, 4f, 0f, 16777215)
                currentY -= (font.lineHeight * scale).toInt()
                poseStack.popPose()
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

            currentY -= 4
        }

        RenderSystem.disableScissor()
        RenderSystem.disableScissor()

        renderOutline(poseStack, x, y, width, height, FastColor.ARGB32.color(100, 0, 0, 0))

        poseStack.popPose()
    }

    private fun renderOutline(poseStack: PoseStack, x: Int, y: Int, width: Int, height: Int, color: Int) {
        Screen.fill(poseStack, x, y, x + width, y + 1, color)
        Screen.fill(poseStack, x, y + height - 1, x + width, y + height, color)
        Screen.fill(poseStack, x, y + 1, x + 1, y + height - 1, color)
        Screen.fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color)
    }

    fun updateTranscript(source: Player, text: String, language: Language, index: Int, updateTime: Long, incomplete: Boolean) {
        if (this.transcripts.any { it.player.uuid == source.uuid && it.index == index }) {
            val transcript = this.transcripts.first { it.player.uuid == source.uuid && it.index == index }

            // it's possible for this to go out of order, let's avoid that
            if (transcript.lastUpdateTime > updateTime)
                return

            transcript.lastUpdateTime = updateTime
            transcript.text = text
            transcript.incomplete = incomplete
            transcript.arrivalTime = System.currentTimeMillis()

            TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(transcript, this@TranscriptBox.language)

            return
        }

        this.transcripts.add(Transcript(index, source, text, language, updateTime, incomplete).apply {
            TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(this, this@TranscriptBox.language)
        })
    }

    companion object {
        val TRANSCRIPT_MUTED = UnityTranslate.id("textures/gui/transcription_muted.png")
    }
}