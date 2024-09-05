package xyz.bluspring.unitytranslate.network.payloads

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds

data class SetUsedLanguagesPayload(
    val languages: List<Language>
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return PacketIds.SET_USED_LANGUAGES.type
    }

    companion object {
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SetUsedLanguagesPayload> = StreamCodec.composite(
            ByteBufCodecs.list<RegistryFriendlyByteBuf, Language>()
                .apply(Language.STREAM_CODEC), SetUsedLanguagesPayload::languages,

            ::SetUsedLanguagesPayload
        )
    }
}