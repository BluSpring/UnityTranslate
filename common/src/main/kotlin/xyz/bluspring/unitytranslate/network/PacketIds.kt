package xyz.bluspring.unitytranslate.network

import dev.architectury.impl.NetworkAggregator
import dev.architectury.networking.NetworkManager
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.network.payloads.*

object PacketIds {
    val SERVER_SUPPORT = create("server_support", ServerSupportPayload.CODEC)
    val SEND_TRANSCRIPT_TO_CLIENT = create("send_transcript_client", SendTranscriptToClientPayload.CODEC)
    val SEND_TRANSCRIPT_TO_SERVER = create("send_transcript_server", SendTranscriptToServerPayload.CODEC)
    val SET_USED_LANGUAGES = create("set_used_languages", SetUsedLanguagesPayload.CODEC)
    val MARK_INCOMPLETE = create("mark_incomplete", MarkIncompletePayload.CODEC)
    val TRANSLATE_SIGN = create("translate_sign", TranslateSignPayload.CODEC)
    val SET_CURRENT_LANGUAGE = create("set_current_language", SetCurrentLanguagePayload.CODEC)

    fun init() {
    }

    private fun <T : CustomPacketPayload> create(id: String, codec: StreamCodec<RegistryFriendlyByteBuf, T>): TypeAndCodec<RegistryFriendlyByteBuf, T> {
        return TypeAndCodec(CustomPacketPayload.Type(UnityTranslate.id(id)), codec)
    }
}