package xyz.bluspring.unitytranslate.compat.voicechat

import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent
import net.minecraft.server.level.ServerPlayer
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

        registration.registerEvent(VoicechatServerStartedEvent::class.java) {
            voiceChatServer = it.voicechat
        }
    }

    companion object {
        lateinit var voiceChatServer: VoicechatServerApi

        fun getNearbyPlayers(source: ServerPlayer): List<ServerPlayer> {
            return voiceChatServer.getPlayersInRange(voiceChatServer.fromServerLevel(source.serverLevel()), voiceChatServer.createPosition(source.position().x, source.position().y, source.position().z), voiceChatServer.voiceChatDistance)
                .map { it.player as ServerPlayer }
        }

        fun isPlayerDeafened(player: ServerPlayer): Boolean {
            return voiceChatServer.getConnectionOf(player.uuid)?.isDisabled == true
        }
    }
}