package xyz.bluspring.unitytranslate

import dev.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.FriendlyByteBuf
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

    fun createByteBuf(): FriendlyByteBuf

    @Environment(EnvType.CLIENT)
    fun sendPacketClient(id: ResourceLocation, buf: FriendlyByteBuf) {
        NetworkManager.sendToServer(id, buf)
    }
    fun sendPacketServer(player: ServerPlayer, id: ResourceLocation, buf: FriendlyByteBuf) {
        NetworkManager.sendToPlayer(player, id, buf)
    }
}