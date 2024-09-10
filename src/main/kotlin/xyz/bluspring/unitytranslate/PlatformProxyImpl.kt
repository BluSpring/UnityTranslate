package xyz.bluspring.unitytranslate

//#if FABRIC
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
//#endif
import net.minecraft.world.entity.player.Player
import java.nio.file.Path
//#if FORGE
//$$ import net.minecraftforge.api.distmarker.Dist
//$$ import net.minecraftforge.fml.ModList
//$$ import net.minecraftforge.fml.loading.FMLLoader
//$$ import net.minecraftforge.fml.loading.FMLPaths
//$$ import net.minecraftforge.server.permission.PermissionAPI
//$$ import net.minecraftforge.server.permission.events.PermissionGatherEvent
//$$ import net.minecraftforge.server.permission.nodes.PermissionNode
//$$ import net.minecraftforge.server.permission.nodes.PermissionTypes
//#elseif NEOFORGE
//$$ import net.neoforged.api.distmarker.Dist
//$$ import net.neoforged.fml.ModList
//$$ import net.neoforged.fml.loading.FMLLoader
//$$ import net.neoforged.fml.loading.FMLPaths
//$$ import net.neoforged.neoforge.server.permission.PermissionAPI
//$$ import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
//$$ import net.neoforged.neoforge.server.permission.nodes.PermissionNode
//$$ import net.neoforged.neoforge.server.permission.nodes.PermissionTypes
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

    // how did Forge manage to overcomplicate permissions of all things

    //#if FORGE-LIKE
    //$$ val requestTranslationsNode = PermissionNode(UnityTranslate.MOD_ID, "request_translations", PermissionTypes.BOOLEAN, { _, _, _ -> true })
    //$$ override fun registerPermissions(event: PermissionGatherEvent.Nodes) {
    //$$    event.addNodes(requestTranslationsNode)
    //$$ }
    //#endif

    override fun hasTranscriptPermission(player: Player): Boolean {
        //#if FABRIC
        return Permissions.check(player, "unitytranslate.request_translations", true)
        //#elseif FORGE-LIKE
        //$$ return PermissionAPI.getOfflinePermission(player.uuid, requestTranslationsNode)
        //#endif
    }
}