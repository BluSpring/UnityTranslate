package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.network.PacketIds

data class TranslateSignPayload(
    val pos: BlockPos
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.TRANSLATE_SIGN.type
    }

    companion object {
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, TranslateSignPayload> = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TranslateSignPayload::pos,
            ::TranslateSignPayload
        )
    }
}