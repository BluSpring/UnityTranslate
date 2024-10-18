package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.config.UnityTranslateConfig

class OpenBrowserScreen(val address: String) : Screen(Component.empty()) {
    override fun init() {
        super.init()

        addRenderableWidget(
            Button.builder(Component.translatable("unitytranslate.do_not_show_again")) {
                UnityTranslate.config.client.openBrowserWithoutPromptV2 = UnityTranslateConfig.TriState.FALSE
                UnityTranslate.saveConfig()

                UnityTranslateClient.displayMessage(Component.translatable("unitytranslate.disabled_browser_transcription", Component.keybind("unitytranslate.open_config")))

                this.onClose()
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - Button.DEFAULT_HEIGHT - 5 - Button.DEFAULT_HEIGHT - 15)
                .tooltip(Tooltip.create(Component.translatable("unitytranslate.do_not_show_again.desc")))
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.translatable("gui.copy_link_to_clipboard")) {
                this.minecraft!!.keyboardHandler.clipboard = address
                this.onClose()
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - Button.DEFAULT_HEIGHT - 5 - Button.DEFAULT_HEIGHT - 5 - Button.DEFAULT_HEIGHT - 15)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.translatable("unitytranslate.open_browser.open_in_browser")) {
                UnityTranslate.config.client.openBrowserWithoutPromptV2 = UnityTranslateConfig.TriState.TRUE
                UnityTranslate.saveConfig()
                Util.getPlatform().openUri(address)
                this.onClose()
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - Button.DEFAULT_HEIGHT - 15)
                .tooltip(Tooltip.create(Component.translatable("unitytranslate.open_browser.open_in_browser.desc")))
                .build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        //#if MC >= 1.20.4
        //$$ this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        //#else
        this.renderBackground(guiGraphics)
        //#endif
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        val split = this.font.split(FormattedText.of(I18n.get("unitytranslate.open_browser.prompt")), this.width / 2)

        val start = (this.height / 2 - (10 * split.size))
        for ((index, text) in split.withIndex()) {
            guiGraphics.drawCenteredString(this.font, text, this.width / 2, start + (this.font.lineHeight * index), 16777215)
        }
    }
}