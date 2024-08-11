package xyz.bluspring.unitytranslate.translator

import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language
import java.util.concurrent.CompletableFuture

data class Translation(
    val id: String, // follows "playerID-transcriptIndex"
    val text: String,
    val fromLang: Language,
    val toLang: Language,
    val queueTime: Long,
    val future: CompletableFuture<String>,
    val player: Player,
    val index: Int
)
