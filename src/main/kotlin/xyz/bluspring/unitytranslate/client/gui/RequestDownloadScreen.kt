package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.translator.TranslatorManager

class RequestDownloadScreen : Screen(Component.empty()) {
    var parent: Screen? = null

    override fun init() {
        super.init()

        this.addRenderableWidget(Button.builder(Component.translatable("unitytranslate.request_download.allow")) {
            TranslatorManager.installLibreTranslate()
            Minecraft.getInstance().setScreen(parent)
        }
            .pos(this.width / 2 - Button.SMALL_WIDTH - 20, this.height - 35)
            .width(Button.SMALL_WIDTH)
            .build())

        this.addRenderableWidget(Button.builder(Component.translatable("unitytranslate.request_download.deny")) {
            Minecraft.getInstance().setScreen(parent)
            UnityTranslate.config.server.shouldRunTranslationServer = false
            UnityTranslate.saveConfig()
        }
            .pos(this.width / 2 + 20, this.height - 35)
            .width(Button.SMALL_WIDTH)
            .tooltip(Tooltip.create(Component.translatable("unitytranslate.request_download.deny.desc")))
            .build())
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        //#if MC >= 1.20.4
        //$$ this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        //#else
        this.renderBackground(guiGraphics)
        //#endif
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        val lines = font.split(Component.translatable("unitytranslate.request_download"), this.width - 50)
        for ((index, line) in lines.withIndex()) {
            guiGraphics.drawCenteredString(font, line, this.width / 2, (this.height / 2 - (lines.size * (font.lineHeight + 2))).coerceAtLeast(13) + (index * (font.lineHeight + 2)), 16777215)
        }
    }

    override fun onClose() {
        super.onClose()

        Minecraft.getInstance().setScreen(parent)
    }
}