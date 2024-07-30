package xyz.bluspring.unitytranslate

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

class UnityTranslate : ModInitializer {
    override fun onInitialize() {

    }

    companion object {
        val config = UnityTranslateConfig()
        val logger = LoggerFactory.getLogger("UnityTranslate")
    }
}