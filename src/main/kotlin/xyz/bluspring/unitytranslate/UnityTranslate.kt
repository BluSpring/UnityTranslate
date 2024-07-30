package xyz.bluspring.unitytranslate

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.translator.TranslatorManager

class UnityTranslate : ModInitializer {
    override fun onInitialize() {
        TranslatorManager.init()
    }

    companion object {
        val config = UnityTranslateConfig()
        val logger = LoggerFactory.getLogger("UnityTranslate")
    }
}