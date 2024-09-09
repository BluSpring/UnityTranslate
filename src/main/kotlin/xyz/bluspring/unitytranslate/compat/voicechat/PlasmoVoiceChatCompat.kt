package xyz.bluspring.unitytranslate.compat.voicechat

import com.google.inject.Inject
import net.minecraft.server.level.ServerPlayer
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.capture.ProximityServerActivationHelper

@Addon(
    id = "pv-unitytranslate-compat",
    name = "UnityTranslate",
    version = "1.0.0",
    authors = [ "BluSpring" ],
    scope = AddonLoaderScope.ANY
)
class PlasmoVoiceChatCompat : AddonInitializer {
    @Inject
    lateinit var voiceServer: PlasmoVoiceServer

    private var proximityHelper: ProximityServerActivationHelper? = null

    override fun onAddonInitialize() {
        instance = this
    }

    companion object {
        lateinit var instance: PlasmoVoiceChatCompat

        fun init() {
            PlasmoVoiceServer.getAddonsLoader().load(PlasmoVoiceChatCompat())
        }

        fun getNearbyPlayers(source: ServerPlayer): List<ServerPlayer> {
            if (isPlayerMutedOrDeafened(source))
                return listOf(source)

            val distance = instance.voiceServer.config?.voice()?.proximity()?.defaultDistance() ?: 5

            return source.serverLevel().getPlayers {
                (!isPlayerDeafened(it) &&
                        ((it.distanceToSqr(source) <= distance * distance && UTVoiceChatCompat.areBothSpectator(source, it)) ||
                                playerSharesGroup(it, source))
                        )
                        || it == source
            }
        }

        fun playerSharesGroup(player: ServerPlayer, other: ServerPlayer): Boolean {
            // TODO: Support pv-addon-groups
            return false
        }

        fun isPlayerMutedOrDeafened(player: ServerPlayer): Boolean {
            val vcPlayer = instance.voiceServer.playerManager.getPlayerById(player.uuid).orElse(null) ?: return true

            if (!vcPlayer.hasVoiceChat())
                return true

            return vcPlayer.isVoiceDisabled || vcPlayer.isMicrophoneMuted
        }

        fun isPlayerDeafened(player: ServerPlayer): Boolean {
            val vcPlayer = instance.voiceServer.playerManager.getPlayerById(player.uuid).orElse(null) ?: return true

            if (!vcPlayer.hasVoiceChat())
                return true

            return vcPlayer.isVoiceDisabled
        }
    }
}