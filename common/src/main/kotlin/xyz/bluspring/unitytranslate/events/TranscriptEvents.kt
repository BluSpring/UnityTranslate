package xyz.bluspring.unitytranslate.events

import dev.architectury.event.Event
import dev.architectury.event.EventFactory
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.transcript.Transcript

interface TranscriptEvents {
    fun interface Update {
        fun onTranscriptUpdate(transcript: Transcript, language: Language)
    }

    companion object {
        val UPDATE: Event<Update> = EventFactory.createLoop(Update::class.java)
    }
}