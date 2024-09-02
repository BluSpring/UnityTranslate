package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.network.PacketIds

class ServerSupportPayload : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.SERVER_SUPPORT.type
    }

    companion object {
        val EMPTY = ServerSupportPayload()
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ServerSupportPayload> = StreamCodec.unit(EMPTY)
    }
}
