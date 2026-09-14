package xyz.bluspring.unitytranslate.client.renderer

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.ClientConfig
import xyz.bluspring.unitytranslate.client.gui.LogoTransitionOverlay
import xyz.bluspring.unitytranslate.client.gui.TranscriptBoxRenderer
import xyz.bluspring.unitytranslate.client.gui.hud.QuickLanguageRadialSelector
import xyz.bluspring.unitytranslate.client.gui.hud.ingame.DownloadProgressHud

object UnityTranslateGui {
    val transcriptRenderer = TranscriptBoxRenderer()
    val quickLanguageSelector = QuickLanguageRadialSelector()
    val downloadProgressHud = DownloadProgressHud()

    fun resize(width: Int, height: Int) {
        BatchedGuiRenderer.resize(width, height)

        for (container in this.transcriptRenderer.containers) {
            container.updateConfig()
        }
    }

    private var lastWidth = 0
    private var lastHeight = 0
    private var lastGui = 0.0

    private fun updateSizes() {
        if (ClientPlatformProxy.instance.windowWidth != lastWidth || ClientPlatformProxy.instance.windowHeight != lastHeight || ClientPlatformProxy.instance.guiScale != lastGui) {
            this.resize(ClientPlatformProxy.instance.windowWidth, ClientPlatformProxy.instance.windowHeight)
            this.lastWidth = ClientPlatformProxy.instance.windowWidth
            this.lastHeight = ClientPlatformProxy.instance.windowHeight
            this.lastGui = ClientPlatformProxy.instance.guiScale
        }
    }

    private var hasInit = false

    fun tick() {
        if (!this.hasInit) {
            this.transcriptRenderer.updateConfig(ClientConfig.transcriptBoxes)
            this.hasInit = true
        }

        this.transcriptRenderer.tick()
        LogoTransitionOverlay.tick()
    }

    fun submit(uiGraphics: UIGraphics, partialTick: Float, mouseX: Double, mouseY: Double) {
        updateSizes()
        this.transcriptRenderer.submit(uiGraphics, partialTick, mouseX.toInt(), mouseY.toInt())
        this.downloadProgressHud.submit(uiGraphics, partialTick, mouseX.toInt(), mouseY.toInt())
//        this.quickLanguageSelector.submit(uiGraphics, partialTick, mouseX, mouseY)
    }

    fun submitLate(graphics: UIGraphics, partialTick: Float) {
        updateSizes()

        val mouseX = ClientPlatformProxy.instance.mouseX
        val mouseY = ClientPlatformProxy.instance.mouseY

        this.downloadProgressHud.submit(graphics, partialTick, mouseX.toInt(), mouseY.toInt())
        LogoTransitionOverlay.submit(graphics, partialTick)
    }
}
