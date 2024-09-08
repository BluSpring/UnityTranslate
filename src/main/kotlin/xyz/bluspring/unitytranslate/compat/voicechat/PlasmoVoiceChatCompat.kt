package xyz.bluspring.unitytranslate.compat.voicechat

import net.minecraft.server.level.ServerPlayer
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.server.PlasmoVoiceServer

@Addon(
    id = "pv-unitytranslate-compat",
    name = "UnityTranslate",
    version = "1.0.0",
    authors = [ "BluSpring" ]
)
class PlasmoVoiceChatCompat : AddonInitializer {
    @InjectPlasmoVoice
    lateinit var voiceServer: PlasmoVoiceServer

    override fun onAddonInitialize() {
        instance = this
    }

    companion object {
        lateinit var instance: PlasmoVoiceChatCompat

        fun getNearbyPlayers(source: ServerPlayer): List<ServerPlayer> {
            if (isPlayerDeafened(source))
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

        fun isPlayerDeafened(player: ServerPlayer): Boolean {
            val vcPlayer = instance.voiceServer.playerManager.getPlayerById(player.uuid).orElse(null) ?: return true

            if (!vcPlayer.hasVoiceChat())
                return true

            return vcPlayer.isVoiceDisabled
        }
    }
}