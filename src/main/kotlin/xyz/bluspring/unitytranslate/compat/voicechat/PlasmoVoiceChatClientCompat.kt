package xyz.bluspring.unitytranslate.compat.voicechat

import com.google.inject.Inject
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.client.PlasmoVoiceClient

@Addon(
    id = "pv-unitytranslate-compat-client",
    name = "UnityTranslate",
    version = "1.0.0",
    authors = [ "BluSpring" ],
    scope = AddonLoaderScope.CLIENT
)
class PlasmoVoiceChatClientCompat : AddonInitializer {
    @Inject
    lateinit var voiceClient: PlasmoVoiceClient

    override fun onAddonInitialize() {
        instance = this
    }

    companion object {
        lateinit var instance: PlasmoVoiceChatClientCompat
    }
}