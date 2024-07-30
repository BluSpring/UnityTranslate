package xyz.bluspring.unitytranslate.client.transcribers

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
import xyz.bluspring.unitytranslate.Language

abstract class SpeechTranscriber(val language: Language) {
    val transcripts = Int2ObjectLinkedOpenHashMap<String>()
    var lastIndex = 0
    var currentOffset = 0

    abstract fun stop()
}