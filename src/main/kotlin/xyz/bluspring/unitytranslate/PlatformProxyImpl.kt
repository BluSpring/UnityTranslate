package xyz.bluspring.unitytranslate

//#if FABRIC
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
//#endif
import java.nio.file.Path
//#if FORGE
//$$ import net.minecraftforge.api.distmarker.Dist
//$$ import net.minecraftforge.fml.ModList
//$$ import net.minecraftforge.fml.loading.FMLLoader
//$$ import net.minecraftforge.fml.loading.FMLPaths
//#elseif NEOFORGE
//$$ import net.neoforged.api.distmarker.Dist
//$$ import net.neoforged.fml.ModList
//$$ import net.neoforged.fml.loading.FMLLoader
//$$ import net.neoforged.fml.loading.FMLPaths
//#endif

class PlatformProxyImpl : PlatformProxy {
    override val isDev: Boolean
        //#if FABRIC
        get() = FabricLoader.getInstance().isDevelopmentEnvironment
        //#elseif FORGE-LIKE
        //$$ get() = !FMLLoader.isProduction()
        //#endif

    override val gameDir: Path
        //#if FABRIC
        get() = FabricLoader.getInstance().gameDir
        //#elseif FORGE-LIKE
        //$$ get() = FMLPaths.GAMEDIR.get()
        //#endif

    override val configDir: Path
        //#if FABRIC
        get() = FabricLoader.getInstance().configDir
        //#elseif FORGE-LIKE
        //$$ get() = FMLPaths.CONFIGDIR.get()
        //#endif

    override val modVersion: String
        //#if FABRIC
        get() = FabricLoader.getInstance().getModContainer(UnityTranslate.MOD_ID).orElseThrow().metadata.version.friendlyString
        //#elseif FORGE-LIKE
        //$$ get() = ModList.get().getModFileById(UnityTranslate.MOD_ID).versionString()
        //#endif

    override fun isModLoaded(id: String): Boolean {
        //#if FABRIC
        return FabricLoader.getInstance().isModLoaded(id)
        //#elseif FORGE-LIKE
        //$$ return ModList.get().isLoaded(id)
        //#endif
    }

    override fun isClient(): Boolean {
        //#if FABRIC
        return FabricLoader.getInstance().environmentType == EnvType.CLIENT
        //#elseif FORGE-LIKE
        //$$ return FMLLoader.getDist() == Dist.CLIENT
        //#endif
    }
}