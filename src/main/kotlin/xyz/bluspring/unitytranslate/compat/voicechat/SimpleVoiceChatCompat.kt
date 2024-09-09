package xyz.bluspring.unitytranslate.compat.voicechat

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.Group
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent
import de.maxhenkel.voicechat.api.events.VoicechatDisableEvent
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent
import net.minecraft.server.level.ServerPlayer
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

@ForgeVoicechatPlugin
class SimpleVoiceChatCompat : VoicechatPlugin {
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

        registration.registerEvent(VoicechatDisableEvent::class.java) {
            if (UnityTranslate.config.client.muteTranscriptWhenVoiceChatMuted) {
                if (it.isDisabled) {
                    UnityTranslateClient.shouldTranscribe = false
                } else if (!it.isDisabled && !it.voicechat.isMuted) {
                    UnityTranslateClient.shouldTranscribe = true
                } else if (!it.isDisabled && it.voicechat.isMuted) {
                    UnityTranslateClient.shouldTranscribe = false
                }
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

            return source.serverLevel().getPlayers {
                (!isPlayerDeafened(it) &&
                        ((it.distanceToSqr(source) <= voiceChatServer.voiceChatDistance * voiceChatServer.voiceChatDistance && UTVoiceChatCompat.areBothSpectator(source, it)) ||
                                playerSharesGroup(source, it))
                        )
                        || it == source
            }
        }

        fun playerSharesGroup(player: ServerPlayer, other: ServerPlayer): Boolean {
            val firstGroup = voiceChatServer.getConnectionOf(player.uuid)?.group ?: return false
            if (firstGroup.type == Group.Type.OPEN)
                return true

            val secondGroup = voiceChatServer.getConnectionOf(other.uuid)?.group ?: return false
            if (secondGroup.type == Group.Type.ISOLATED && firstGroup.id != secondGroup.id)
                return false

            return firstGroup.id == secondGroup.id
        }

        fun isPlayerDeafened(player: ServerPlayer): Boolean {
            return voiceChatServer.getConnectionOf(player.uuid)?.isDisabled == true
        }
    }
}