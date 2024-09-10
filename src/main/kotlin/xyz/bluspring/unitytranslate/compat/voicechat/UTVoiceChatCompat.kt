package xyz.bluspring.unitytranslate.compat.voicechat

import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import xyz.bluspring.unitytranslate.UnityTranslate

object UTVoiceChatCompat {
    val usesSimpleVoiceChat: Boolean
        get() {
            return UnityTranslate.instance.proxy.isModLoaded("voicechat")
        }

    val usesPlasmoVoice: Boolean
        get() {
            return UnityTranslate.instance.proxy.isModLoaded("plasmovoice")
        }

    val hasVoiceChat: Boolean
        get() {
            return usesSimpleVoiceChat || usesPlasmoVoice
        }

    fun getNearbyPlayers(source: ServerPlayer): List<ServerPlayer> {
        return if (usesSimpleVoiceChat)
            SimpleVoiceChatCompat.getNearbyPlayers(source)
        else if (usesPlasmoVoice)
            PlasmoVoiceChatCompat.getNearbyPlayers(source)
        else
            listOf(source)
    }

    fun isPlayerDeafened(player: ServerPlayer): Boolean {
        return if (usesSimpleVoiceChat)
            SimpleVoiceChatCompat.isPlayerDeafened(player)
        else if (usesPlasmoVoice)
            PlasmoVoiceChatCompat.isPlayerDeafened(player)
        else
            false
    }

    fun isPlayerAudible(player: Player): Boolean {
        if (player == Minecraft.getInstance().player)
            return true

        return if (usesSimpleVoiceChat)
            SimpleVoiceChatCompat.isPlayerAudible(player)
        else if (usesPlasmoVoice)
            PlasmoVoiceChatCompat.isPlayerAudible(player)
        else false
    }

    fun areBothSpectator(player: ServerPlayer, other: ServerPlayer): Boolean {
        if (player.gameMode.gameModeForPlayer == GameType.SPECTATOR && other.gameMode.gameModeForPlayer == GameType.SPECTATOR)
            return true
        else if (player.gameMode.gameModeForPlayer == GameType.SPECTATOR && other.gameMode.gameModeForPlayer != GameType.SPECTATOR)
            return false
        else if (player.gameMode.gameModeForPlayer != GameType.SPECTATOR && other.gameMode.gameModeForPlayer == GameType.SPECTATOR)
            return true

        return true
    }
}