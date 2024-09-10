package xyz.bluspring.unitytranslate.neoforge

//#if NEOFORGE
//$$ import net.neoforged.bus.api.SubscribeEvent
//$$ import net.neoforged.neoforge.common.NeoForge
//$$ import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
//#elseif FORGE
//$$ import net.minecraftforge.common.MinecraftForge
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent
//$$ import net.minecraftforge.server.permission.events.PermissionGatherEvent
//#endif

//#if FORGE-LIKE
//$$ import xyz.bluspring.unitytranslate.UnityTranslate

//$$ object NeoForgeEvents {
//$$     fun init() {
            //#if FORGE
            //$$ MinecraftForge.EVENT_BUS.register(this)
            //#elseif NEOFORGE
            //$$ NeoForge.EVENT_BUS.register(this)
            //#endif
//$$     }
//$$
//$$     @SubscribeEvent
//$$     fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
//$$         UnityTranslate.instance.proxy.registerPermissions(event)
//$$     }
//$$ }
//#endif