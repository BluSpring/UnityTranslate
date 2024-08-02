package xyz.bluspring.unitytranslate.transcript

import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language

data class Transcript(
    val player: Player,
    val text: String,
    val language: Language
)
