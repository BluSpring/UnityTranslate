package xyz.bluspring.unitytranslate.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.translator.TranslatorManager

class RequestDownloadScreen : Screen(Component.empty()) {
    var parent: Screen? = null

    override fun init() {
        super.init()

        this.addRenderableWidget(Button(this.width / 2 - Button.SMALL_WIDTH - 20, this.height - 35,
            Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT,
            Component.translatable("unitytranslate.request_download.allow")
        ) {
            TranslatorManager.installLibreTranslate()
            Minecraft.getInstance().setScreen(parent)
        })

        this.addRenderableWidget(Button(this.width / 2 + 20, this.height - 35,
            Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT,
            Component.translatable("unitytranslate.request_download.deny"),
        {
            Minecraft.getInstance().setScreen(parent)
            UnityTranslate.config.server.shouldRunTranslationServer = false
            UnityTranslate.saveConfig()
        }) { btn, poseStack, x, y ->
            renderTooltip(poseStack, Component.translatable("unitytranslate.request_download.deny.desc"), x, y)
        })
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(poseStack)
        super.render(poseStack, mouseX, mouseY, partialTick)

        val lines = font.split(Component.translatable("unitytranslate.request_download"), this.width - 50)
        for ((index, line) in lines.withIndex()) {
            font.draw(poseStack, line, this.width / 2f, ((this.height / 2 - (lines.size * (font.lineHeight + 2))).coerceAtLeast(13) + (index * (font.lineHeight + 2))).toFloat(), 16777215)
        }
    }

    override fun onClose() {
        super.onClose()

        Minecraft.getInstance().setScreen(parent)
    }
}