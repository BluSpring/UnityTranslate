package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds

data class SendTranscriptToServerPayload(
    val sourceLanguage: Language,
    val text: String,
    val index: Int,
    val updateTime: Long
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.SEND_TRANSCRIPT_TO_SERVER.type
    }

    companion object {
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SendTranscriptToServerPayload> = StreamCodec.composite(
            Language.STREAM_CODEC, SendTranscriptToServerPayload::sourceLanguage,
            ByteBufCodecs.STRING_UTF8, SendTranscriptToServerPayload::text,
            ByteBufCodecs.VAR_INT, SendTranscriptToServerPayload::index,
            ByteBufCodecs.VAR_LONG, SendTranscriptToServerPayload::updateTime,

            ::SendTranscriptToServerPayload
        )
    }
}