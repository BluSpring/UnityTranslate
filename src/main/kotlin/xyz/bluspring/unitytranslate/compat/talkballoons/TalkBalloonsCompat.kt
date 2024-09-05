package xyz.bluspring.unitytranslate.compat.talkballoons

import com.cerbon.talk_balloons.TalkBalloons
import com.cerbon.talk_balloons.util.mixin.IAbstractClientPlayer
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

object TalkBalloonsCompat {
    private val lastBalloonText = ConcurrentSkipListMap<UUID, Pair<Int, String>>()

    fun init() {
        TranscriptEvents.UPDATE.register { transcript, language ->
            if (language != UnityTranslate.config.client.language)
                return@register

            if (transcript.player !is IAbstractClientPlayer)
                return@register

            val uuid = transcript.player.uuid

            if (transcript.player.balloonMessages?.isNotEmpty() != true) {
                lastBalloonText.remove(uuid)
            }

            val text = transcript.text

            if (lastBalloonText.containsKey(uuid)) {
                val (index, lastText) = lastBalloonText[uuid]!!

                if (lastText == text)
                    return@register

                if (index == transcript.index) {
                    transcript.player.balloonMessages.removeIf { it == lastText }
                }
            }

            transcript.player.createBalloonMessage(text, TalkBalloons.config.balloonAge * 20)
            lastBalloonText[uuid] = transcript.index to text
        }
    }
}