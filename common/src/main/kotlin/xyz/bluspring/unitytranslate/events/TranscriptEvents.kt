package xyz.bluspring.unitytranslate.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.events.TranscriptEvents.Update
import xyz.bluspring.unitytranslate.transcript.Transcript

interface TranscriptEvents {
    fun interface Update {
        fun onTranscriptUpdate(transcript: Transcript, language: Language)
    }

    companion object {
        val UPDATE: Event<Update> = EventFactory.createArrayBacked(Update::class.java) { events -> Update { transcript, language ->
                for (event in events) {
                    event.onTranscriptUpdate(transcript, language)
                }
            }
        }
    }
}