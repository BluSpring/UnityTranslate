package xyz.bluspring.unitytranslate.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StringWidget(x: Int, y: Int, width: Int, height: Int, message: Component, val font: Font) : AbstractWidget(x, y, width, height, message) {
    private var alignX = 0f
    var color = 0xFFFFFF
    var tooltip: Component? = null

    constructor(message: Component, font: Font) : this(0, 0, font.width(message.visualOrderText), 9, message, font)

    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
    }

    fun alignLeft(): StringWidget {
        this.alignX = 0f
        return this
    }

    fun alignCenter(): StringWidget {
        this.alignX = 0.5f
        return this
    }

    fun alignRight(): StringWidget {
        this.alignX = 1f
        return this
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(poseStack, mouseX, mouseY, partialTick)
        val i = x + Math.round(this.alignX * (this.getWidth() - font.width(message)).toFloat())
        val n2 = this.getHeight()
        val j = y + (n2 - 9) / 2
        Screen.drawString(poseStack, font, message, i, j, color)

        if (this.isHoveredOrFocused && tooltip != null) {
            Minecraft.getInstance().screen?.renderTooltip(poseStack, tooltip!!, mouseX, mouseY)
        }
    }
}