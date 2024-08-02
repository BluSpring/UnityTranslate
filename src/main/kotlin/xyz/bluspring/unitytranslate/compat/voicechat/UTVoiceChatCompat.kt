package xyz.bluspring.unitytranslate.compat.voicechat

import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

class UTVoiceChatCompat : VoicechatPlugin {
    override fun getPluginId(): String {
        return UnityTranslate.MOD_ID
    }

    override fun registerEvents(registration: EventRegistration) {
        super.registerEvents(registration)

        registration.registerEvent(MicrophoneMuteEvent::class.java) {
            if (UnityTranslate.config.client.muteTranscriptWhenVoiceChatMuted) {
                UnityTranslateClient.shouldTranscribe = !it.isDisabled
            }
        }
    }
}