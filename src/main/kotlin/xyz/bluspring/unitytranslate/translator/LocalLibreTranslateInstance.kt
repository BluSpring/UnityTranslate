package xyz.bluspring.unitytranslate.translator

import net.minecraft.util.HttpUtil

class LocalLibreTranslateInstance : LibreTranslateInstance("http://127.0.0.1:${if (HttpUtil.isPortAvailable(5000)) 5000 else HttpUtil.getAvailablePort()}", 100) {
    val port = this.url.replace("http://127.0.0.1", "").toInt()
    // TODO: figure out how to localhost LibreTranslate

    init {
        val supportsCuda = TranslatorManager.supportsCuda


    }
}