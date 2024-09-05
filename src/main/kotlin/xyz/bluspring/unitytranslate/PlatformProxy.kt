package xyz.bluspring.unitytranslate

import dev.architectury.networking.NetworkManager
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
//#if MC >= 1.20.6
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//#endif
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path

interface PlatformProxy {
    fun isModLoaded(id: String): Boolean
    fun isClient(): Boolean

    val modVersion: String
    val configDir: Path
    val gameDir: Path
    val isDev: Boolean

    fun createByteBuf(): FriendlyByteBuf {
        return FriendlyByteBuf(Unpooled.buffer())
    }

    //#if MC >= 1.20.6
    //$$ fun sendPacketClient(payload: CustomPacketPayload) {
    //$$    NetworkManager.sendToServer(payload)
    //#else
    fun sendPacketClient(id: ResourceLocation, buf: FriendlyByteBuf) {
        NetworkManager.sendToServer(id, buf)
    //#endif
    }

    //#if MC >= 1.20.6
    //$$ fun sendPacketServer(player: ServerPlayer, payload: CustomPacketPayload) {
    //$$    NetworkManager.sendToPlayer(player, payload)
    //#else
    fun sendPacketServer(player: ServerPlayer, id: ResourceLocation, buf: FriendlyByteBuf) {
        NetworkManager.sendToPlayer(player, id, buf)
    //#endif
    }
}