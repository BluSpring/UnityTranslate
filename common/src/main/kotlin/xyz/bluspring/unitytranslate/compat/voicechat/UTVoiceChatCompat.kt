package xyz.bluspring.unitytranslate.compat.voicechat

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

@ForgeVoicechatPlugin
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

        registration.registerEvent(VoicechatServerStartedEvent::class.java) {
            voiceChatServer = it.voicechat
        }
    }

    companion object {
        lateinit var voiceChatServer: VoicechatServerApi

        fun getNearbyPlayers(source: ServerPlayer): List<ServerPlayer> {
            if (isPlayerDeafened(source))
                return listOf(source)

            return (source.level as ServerLevel).getPlayers {
                (!isPlayerDeafened(it) &&
                        ((it.distanceToSqr(source) <= voiceChatServer.voiceChatDistance * voiceChatServer.voiceChatDistance && areBothSpectator(it, source)) ||
                                playerSharesGroup(it, source))
                )
                || it == source
            }
        }

        fun playerSharesGroup(player: ServerPlayer, other: ServerPlayer): Boolean {
            val firstGroup = voiceChatServer.getConnectionOf(player.uuid)?.group ?: return false
            val secondGroup = voiceChatServer.getConnectionOf(other.uuid)?.group ?: return false

            return firstGroup.id == secondGroup.id
        }

        fun isPlayerDeafened(player: ServerPlayer): Boolean {
            return voiceChatServer.getConnectionOf(player.uuid)?.isDisabled == true
        }

        fun areBothSpectator(player: ServerPlayer, other: ServerPlayer): Boolean {
            if (player.gameMode.gameModeForPlayer == GameType.SPECTATOR && other.gameMode.gameModeForPlayer == GameType.SPECTATOR)
                return true
            else if (player.gameMode.gameModeForPlayer == GameType.SPECTATOR && other.gameMode.gameModeForPlayer != GameType.SPECTATOR)
                return false

            return true
        }
    }
}