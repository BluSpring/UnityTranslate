package xyz.bluspring.unitytranslate.transcript

import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language

data class Transcript(
    val index: Int,
    val player: Player,
    var text: String,
    val language: Language,
    var lastUpdateTime: Long
)
