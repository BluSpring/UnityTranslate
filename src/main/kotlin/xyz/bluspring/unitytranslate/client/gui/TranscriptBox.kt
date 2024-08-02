package xyz.bluspring.unitytranslate.client.gui

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.transcript.Transcript
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Serializable
data class TranscriptBox(
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
    var opacity: Int,

    var language: Language
) {
    @Transient
    val transcripts = ConcurrentLinkedQueue<Transcript>()

    fun render(guiGraphics: GuiGraphics) {
        guiGraphics.pose().pushPose()

        guiGraphics.pose().translate(0.0, 0.0, -255.0)
        guiGraphics.enableScissor(x, y, x + width, y + height)

        guiGraphics.fill(x, y, x + width, y + height, FastColor.ARGB32.color(opacity, 0, 0, 0))
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("unitytranslate.transcript").append(" (${language.code.uppercase()})")
            .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD), x + (width / 2), y + 5, 16777215)

        guiGraphics.enableScissor(x, y + 15, x + width, y + height)

        val lines = transcripts.reversed().map {
            Component.empty()
                .append("<")
                .append(it.player.displayName)
                .append(
                    Component.literal(" (${it.language.code.uppercase(Locale.ENGLISH)})")
                        .withStyle(ChatFormatting.GREEN)
                )
                .append("> ")
                .append(it.text)
        }

        val font = Minecraft.getInstance().font

        var currentY = y + height - font.lineHeight
        for (component in lines) {
            val split = font.split(component, width - 5).reversed()

            for (line in split) {
                guiGraphics.drawString(font, line, x + 4, currentY, 16777215)
                currentY -= font.lineHeight
            }
        }

        guiGraphics.disableScissor()
        guiGraphics.disableScissor()

        guiGraphics.renderOutline(x, y, width, height, FastColor.ARGB32.color(100, 0, 0, 0))

        guiGraphics.pose().popPose()
    }

    fun updateTranscript(source: Player, text: String, language: Language, updateLast: Boolean) {
        if (updateLast) {
            this.transcripts.remove(this.transcripts.last())
        }

        this.transcripts.add(Transcript(source, text, language))
    }
}