package xyz.bluspring.unitytranslate.fabric

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.UnityTranslate
import java.nio.file.Path

class FabricProxy : PlatformProxy {
    override val isDev: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override val gameDir: Path
        get() = FabricLoader.getInstance().gameDir

    override val configDir: Path
        get() = FabricLoader.getInstance().configDir

    override val modVersion: String
        get() = FabricLoader.getInstance().getModContainer(UnityTranslate.MOD_ID).orElseThrow().metadata.version.friendlyString

    override fun isModLoaded(id: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(id)
    }

    override fun createByteBuf(): FriendlyByteBuf {
        return PacketByteBufs.create()
    }

    override fun isClient(): Boolean {
        return FabricLoader.getInstance().environmentType == EnvType.CLIENT
    }
}