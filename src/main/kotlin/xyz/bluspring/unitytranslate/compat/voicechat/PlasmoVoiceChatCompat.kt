package xyz.bluspring.unitytranslate.compat.voicechat

import com.google.inject.Inject
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.event.connection.VoicePlayerUpdateEvent
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.capture.ProximityServerActivationHelper
import su.plo.voice.api.server.event.mute.PlayerVoiceMutedEvent
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

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

    @Inject
    lateinit var voiceClient: PlasmoVoiceClient

    private var proximityHelper: ProximityServerActivationHelper? = null

    override fun onAddonInitialize() {
        instance = this
    }

    @EventSubscribe
    fun onVoiceUpdateEvent(event: VoicePlayerUpdateEvent) {
        if (event.player.playerId != Minecraft.getInstance().player?.uuid)
            return

        if (UnityTranslate.config.client.muteTranscriptWhenVoiceChatMuted) {
            if (event.player.isVoiceDisabled) {
                UnityTranslateClient.shouldTranscribe = false
            } else if (!event.player.isVoiceDisabled && !(event.player.isMuted || event.player.isMicrophoneMuted)) {
                UnityTranslateClient.shouldTranscribe = true
            } else if (!event.player.isVoiceDisabled && (event.player.isMuted || event.player.isMicrophoneMuted)) {
                UnityTranslateClient.shouldTranscribe = false
            }
        }
    }

    companion object {
        lateinit var instance: PlasmoVoiceChatCompat

        fun init() {
            val compat = PlasmoVoiceChatCompat()
            PlasmoVoiceServer.getAddonsLoader().load(compat)
            PlasmoVoiceClient.getAddonsLoader().load(compat)
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

        fun isPlayerAudible(source: Player): Boolean {
            val connection = instance.voiceClient.serverConnection.orElse(null) ?: return false
            val vcPlayer = connection.getPlayerById(source.uuid).orElse(null) ?: return false

            return !vcPlayer.isMuted && !vcPlayer.isMicrophoneMuted && !vcPlayer.isVoiceDisabled
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