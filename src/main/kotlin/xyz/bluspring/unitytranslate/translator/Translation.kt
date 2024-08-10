package xyz.bluspring.unitytranslate.translator

import xyz.bluspring.unitytranslate.Language
import java.util.concurrent.CompletableFuture

data class Translation(
    val id: String, // follows "playerID-transcriptIndex"
    val text: String,
    val fromLang: Language,
    val toLang: Language,
    val queueTime: Long,
    val future: CompletableFuture<String>
)
