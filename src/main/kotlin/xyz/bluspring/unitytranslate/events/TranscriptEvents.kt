package xyz.bluspring.unitytranslate.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import xyz.bluspring.unitytranslate.events.TranscriptEvents.Update
import xyz.bluspring.unitytranslate.transcript.Transcript

interface TranscriptEvents {
    fun interface Update {
        fun onTranscriptUpdate(transcript: Transcript)
    }

    companion object {
        val UPDATE: Event<Update> = EventFactory.createArrayBacked(Update::class.java) { events -> Update { transcript ->
                for (event in events) {
                    event.onTranscriptUpdate(transcript)
                }
            }
        }
    }
}