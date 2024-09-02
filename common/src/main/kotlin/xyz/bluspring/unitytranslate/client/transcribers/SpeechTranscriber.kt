package xyz.bluspring.unitytranslate.client.transcribers

import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.network.PacketIds
import xyz.bluspring.unitytranslate.network.payloads.SetCurrentLanguagePayload
import java.util.function.BiConsumer

abstract class SpeechTranscriber(var language: Language) {
    var lastIndex = 0
    var currentOffset = 0

    lateinit var updater: BiConsumer<Int, String>

    abstract fun stop()

    open fun changeLanguage(language: Language) {
        this.language = language

        if (Minecraft.getInstance().player != null) {
            UnityTranslate.instance.proxy.sendPacketClient(SetCurrentLanguagePayload(language))
        }
    }
}