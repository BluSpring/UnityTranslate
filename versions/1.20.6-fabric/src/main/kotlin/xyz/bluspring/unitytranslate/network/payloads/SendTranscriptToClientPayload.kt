package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds
import java.util.*

data class SendTranscriptToClientPayload(
    val uuid: UUID,
    val language: Language,
    val index: Int,
    val updateTime: Long,
    val toSend: Map<Language, String>
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.SEND_TRANSCRIPT_TO_CLIENT.type
    }

    companion object {
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SendTranscriptToClientPayload> = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SendTranscriptToClientPayload::uuid,
            Language.STREAM_CODEC, SendTranscriptToClientPayload::language,
            ByteBufCodecs.VAR_INT, SendTranscriptToClientPayload::index,
            ByteBufCodecs.VAR_LONG, SendTranscriptToClientPayload::updateTime,
            StreamCodec.of({ buf, map ->
                buf.writeVarInt(map.size)

                for ((language, translated) in map) {
                    buf.writeEnum(language)
                    buf.writeUtf(translated)
                }
            }, { buf ->
                val map = mutableMapOf<Language, String>()
                val size = buf.readVarInt()

                for (i in 0 until size) {
                    map[buf.readEnum(Language::class.java)] = buf.readUtf()
                }

                map
            }), SendTranscriptToClientPayload::toSend,

            ::SendTranscriptToClientPayload
        )
    }
}