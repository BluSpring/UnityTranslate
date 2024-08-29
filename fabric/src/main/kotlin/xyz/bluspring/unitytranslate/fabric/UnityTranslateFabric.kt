package xyz.bluspring.unitytranslate.fabric

import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.FriendlyByteBuf
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.UnityTranslate
import java.nio.file.Path

class UnityTranslateFabric : ModInitializer {
    override fun onInitialize() {
        UnityTranslate(proxy)
    }

    companion object {
        val proxy = FabricProxy()
    }
}