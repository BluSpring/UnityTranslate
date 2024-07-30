package xyz.bluspring.unitytranslate.client.gui

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import xyz.bluspring.unitytranslate.Language

class TranscriptBox(
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
    var opacity: Int,

    var language: Language
) {
    val transcripts = Int2ObjectLinkedOpenHashMap<String>()

    fun render(guiGraphics: GuiGraphics) {
        guiGraphics.pose().pushPose()

        guiGraphics.pose().translate(0.0, 0.0, -255.0)
        guiGraphics.enableScissor(x, y, x + width, y + height)

        guiGraphics.fill(x, y, x + width, y + height, FastColor.ARGB32.color(opacity, 0, 0, 0))
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.literal("Transcript (${language.code.uppercase()})")
            .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD), x + (width / 2), y + 5, 16777215)

        guiGraphics.enableScissor(x, y + 15, x + width, y + height)

        val lines = transcripts.toList().sortedByDescending { it.first }.map {
            Component.empty()
                .append("<")
                .append(Minecraft.getInstance().player!!.displayName)
                .append(
                    Component.literal(" (English)")
                        .withStyle(ChatFormatting.GREEN)
                )
                .append("> ")
                .append(it.second)
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
}