package xyz.bluspring.unitytranslate.network

//#if MC >= 1.20.6
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.codec.StreamCodec
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
//$$ import xyz.bluspring.unitytranslate.network.payloads.*
//#endif
import xyz.bluspring.unitytranslate.UnityTranslate

object PacketIds {
    //#if MC >= 1.20.6
    //$$  val SERVER_SUPPORT = create("server_support", ServerSupportPayload.CODEC)
    //$$  val SEND_TRANSCRIPT_TO_CLIENT = create("send_transcript_client", SendTranscriptToClientPayload.CODEC)
    //$$  val SEND_TRANSCRIPT_TO_SERVER = create("send_transcript_server", SendTranscriptToServerPayload.CODEC)
    //$$  val SET_USED_LANGUAGES = create("set_used_languages", SetUsedLanguagesPayload.CODEC)
    //$$  val MARK_INCOMPLETE = create("mark_incomplete", MarkIncompletePayload.CODEC)
    //$$  val TRANSLATE_SIGN = create("translate_sign", TranslateSignPayload.CODEC)
    //$$  val SET_CURRENT_LANGUAGE = create("set_current_language", SetCurrentLanguagePayload.CODEC)
    //$$
    //$$  fun init() {
    //$$  }
    //$$
    //$$  private fun <T : CustomPacketPayload> create(id: String, codec: StreamCodec<RegistryFriendlyByteBuf, T>): TypeAndCodec<RegistryFriendlyByteBuf, T> {
    //$$      return TypeAndCodec(CustomPacketPayload.Type(UnityTranslate.id(id)), codec)
    //$$  }
    //#else
    val SERVER_SUPPORT = UnityTranslate.id("server_support")
    val SEND_TRANSCRIPT = UnityTranslate.id("send_transcript")
    val SET_USED_LANGUAGES = UnityTranslate.id("set_used_languages")
    val MARK_INCOMPLETE = UnityTranslate.id("mark_incomplete")
    val TRANSLATE_SIGN = UnityTranslate.id("translate_sign")
    val SET_CURRENT_LANGUAGE = UnityTranslate.id("set_current_language")
    //#endif
}