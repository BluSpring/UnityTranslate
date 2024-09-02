package xyz.bluspring.unitytranslate.neoforge

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.FMLPaths
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.UnityTranslate
import java.nio.file.Path

class NeoForgeProxy : PlatformProxy {
    override val isDev: Boolean
        get() = !FMLLoader.isProduction()

    override val gameDir: Path
        get() = FMLPaths.GAMEDIR.get()

    override val configDir: Path
        get() = FMLPaths.CONFIGDIR.get()

    override val modVersion: String
        get() = ModList.get().getModFileById(UnityTranslate.MOD_ID).versionString()

    override fun isModLoaded(id: String): Boolean {
        return ModList.get().isLoaded(id)
    }

    override fun createByteBuf(): FriendlyByteBuf {
        return FriendlyByteBuf(Unpooled.buffer())
    }

    override fun isClient(): Boolean {
        return FMLLoader.getDist() == Dist.CLIENT
    }
}