package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds

data class SetCurrentLanguagePayload(
    val language: Language
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.SET_CURRENT_LANGUAGE.type
    }

    companion object {
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SetCurrentLanguagePayload> = StreamCodec.composite(
            Language.STREAM_CODEC, SetCurrentLanguagePayload::language,
            ::SetCurrentLanguagePayload
        )
    }
}