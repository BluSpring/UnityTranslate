package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds
import java.util.EnumSet
import java.util.UUID

data class MarkIncompletePayload(
    val from: Language,
    val to: Language,
    val uuid: UUID,
    val index: Int,
    var isIncomplete: Boolean
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.MARK_INCOMPLETE.type
    }

    companion object {
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, MarkIncompletePayload> = StreamCodec.composite(
            Language.STREAM_CODEC, MarkIncompletePayload::from,
            Language.STREAM_CODEC, MarkIncompletePayload::to,
            UUIDUtil.STREAM_CODEC, MarkIncompletePayload::uuid,
            ByteBufCodecs.VAR_INT, MarkIncompletePayload::index,
            ByteBufCodecs.BOOL, MarkIncompletePayload::isIncomplete,

            ::MarkIncompletePayload
        )
    }
}