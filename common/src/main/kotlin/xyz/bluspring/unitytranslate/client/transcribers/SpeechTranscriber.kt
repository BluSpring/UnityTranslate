package xyz.bluspring.unitytranslate.client.transcribers

import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.network.PacketIds
import java.util.function.BiConsumer

abstract class SpeechTranscriber(var language: Language) {
    var lastIndex = 0
    var currentOffset = 0

    lateinit var updater: BiConsumer<Int, String>

    abstract fun stop()
    open fun setMuted(muted: Boolean) {}

    open fun changeLanguage(language: Language) {
        this.language = language

        if (Minecraft.getInstance().player != null) {
            val buf = UnityTranslate.instance.proxy.createByteBuf()
            buf.writeEnum(language)

            UnityTranslate.instance.proxy.sendPacketClient(PacketIds.SET_CURRENT_LANGUAGE, buf)
        }
    }
}