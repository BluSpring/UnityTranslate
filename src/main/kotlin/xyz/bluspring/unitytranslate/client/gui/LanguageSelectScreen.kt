package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType

class LanguageSelectScreen(val parent: Screen?, val isAddingBox: Boolean) : Screen(Component.empty()) {
    private lateinit var list: LanguageSelectionList

    override fun init() {
        super.init()

        list = LanguageSelectionList()
        this.addRenderableWidget(list)
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                this.onDone()
            }
                .bounds(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 38, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT)
                .build()
        )
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    private fun onDone() {
        val language = list.selected?.language ?: return

        if (isAddingBox) {
            Minecraft.getInstance().execute {
                UnityTranslate.config.client.transcriptBoxes.add(TranscriptBox(0, 0, 150, 170, 120, language))
                UnityTranslate.saveConfig()
            }
        } else {
            UnityTranslate.config.client.language = language
            UnityTranslateClient.transcriber.changeLanguage(language)
            UnityTranslate.saveConfig()
        }

        onClose()
    }

    private inner class LanguageSelectionList : ObjectSelectionList<LanguageSelectionList.Entry>(Minecraft.getInstance(), this@LanguageSelectScreen.width, this@LanguageSelectScreen.height, 32, this@LanguageSelectScreen.height - 65 + 4, 18) {
        init {
            for (language in Language.entries.sortedBy { it.name }) {
                this.addEntry(Entry(language))
            }
        }

        inner class Entry(val language: Language) : ObjectSelectionList.Entry<Entry>() {
            private val shouldBeDeactivated = isAddingBox && UnityTranslate.config.client.transcriptBoxes.any { it.language == language }
            private var lastClickTime: Long = 0L

            override fun render(
                guiGraphics: GuiGraphics,
                index: Int, top: Int, left: Int,
                width: Int, height: Int,
                mouseX: Int, mouseY: Int,
                hovering: Boolean, partialTick: Float
            ) {
                val color = if (shouldBeDeactivated) {
                    0x656565
                } else 0xFFFFFF

                guiGraphics.drawCenteredString(font, language.text, this@LanguageSelectScreen.width / 2, top + 1, color)

                var x = this@LanguageSelectScreen.width / 2 + (font.width(language.text) / 2) + 4
                for (type in language.supportedTranscribers.keys) {
                    guiGraphics.blit(when (type) {
                        TranscriberType.BROWSER -> BROWSER_ICON
                        TranscriberType.SPHINX -> SPHINX_ICON
                    },
                        x, top - 1, 0f, 0f, 16, 16, 16, 16
                    )

                    x += 20
                }
            }

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                if (shouldBeDeactivated)
                    return false

                if (button == 0) {
                    this@LanguageSelectionList.selected = this
                    if (Util.getMillis() - this.lastClickTime < 250L) {
                        this@LanguageSelectScreen.onDone()
                    }

                    this.lastClickTime = Util.getMillis()
                    return true
                } else {
                    this.lastClickTime = Util.getMillis()
                    return false
                }
            }

            override fun getNarration(): Component {
                return Component.translatable("narrator.select", this.language.text)
            }
        }
    }

    companion object {
        val BROWSER_ICON = UnityTranslate.id("textures/gui/transcriber/browser.png")
        val SPHINX_ICON = UnityTranslate.id("textures/gui/transcriber/sphinx.png")
    }
}