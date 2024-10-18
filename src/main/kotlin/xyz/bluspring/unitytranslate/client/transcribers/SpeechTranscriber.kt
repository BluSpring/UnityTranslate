package xyz.bluspring.unitytranslate.client.transcribers

import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.network.PacketIds
//#if MC >= 1.20.6
//$$ import xyz.bluspring.unitytranslate.network.payloads.SetCurrentLanguagePayload
//#endif
import java.util.function.BiConsumer

abstract class SpeechTranscriber(var language: Language) {
    var lastIndex = 0
    var currentOffset = 0

    // JSGF Grammar strings
    // See: https://www.w3.org/TR/jsgf/
    val grammars = mutableListOf<String>()

    init {
        run {
            grammars.add(UnityTranslate::class.java.getResource("/default_grammar.jsgf")?.readText(Charsets.UTF_8) ?: return@run)
        }
    }

    lateinit var updater: BiConsumer<Int, String>

    abstract fun stop()
    open fun setMuted(muted: Boolean) {}
    open fun updateGrammars() {}

    open fun changeLanguage(language: Language) {
        this.language = language

        if (Minecraft.getInstance().player != null) {
            //#if MC >= 1.20.6
            //$$ UnityTranslate.instance.proxy.sendPacketClient(SetCurrentLanguagePayload(language))
            //#else
            val buf = UnityTranslate.instance.proxy.createByteBuf()
            buf.writeEnum(language)

            UnityTranslate.instance.proxy.sendPacketClient(PacketIds.SET_CURRENT_LANGUAGE, buf)
            //#endif
        }
    }
}