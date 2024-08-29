package xyz.bluspring.unitytranslate.network

import xyz.bluspring.unitytranslate.UnityTranslate

object PacketIds {
    val SERVER_SUPPORT = UnityTranslate.id("server_support")
    val SEND_TRANSCRIPT = UnityTranslate.id("send_transcript")
    val SET_USED_LANGUAGES = UnityTranslate.id("set_used_languages")
    val MARK_INCOMPLETE = UnityTranslate.id("mark_incomplete")
    val TOGGLE_MOD = UnityTranslate.id("toggle")
}