package xyz.bluspring.unitytranslate.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.Util
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import xyz.bluspring.unitytranslate.UnityTranslate

class OpenBrowserScreen(val address: String) : Screen(Component.empty()) {
    override fun init() {
        super.init()

        addRenderableWidget(
            Button(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - Button.DEFAULT_HEIGHT - 5 - Button.DEFAULT_HEIGHT - 15,
                Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
                Component.translatable("unitytranslate.do_not_show_again")
            ) {
                Util.getPlatform().openUri(address)
                UnityTranslate.config.client.openBrowserWithoutPrompt = true
                UnityTranslate.saveConfig()
                this.onClose()
            }
        )

        addRenderableWidget(
            Button(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - Button.DEFAULT_HEIGHT - 15,
                Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
                Component.translatable("unitytranslate.open_browser.open_in_browser")
            ) {
                Util.getPlatform().openUri(address)
                this.onClose()
            }
        )
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(poseStack)
        super.render(poseStack, mouseX, mouseY, partialTick)

        val split = this.font.split(FormattedText.of(I18n.get("unitytranslate.open_browser.prompt")), this.width / 2)

        val start = (this.height / 2 - (10 * split.size))
        for ((index, text) in split.withIndex()) {
            font.draw(poseStack, text, this.width / 2f, start + (this.font.lineHeight * index).toFloat(), 16777215)
        }
    }
}