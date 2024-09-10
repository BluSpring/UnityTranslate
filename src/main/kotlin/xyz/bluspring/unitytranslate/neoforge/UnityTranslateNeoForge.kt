package xyz.bluspring.unitytranslate.neoforge

//#if FORGE-LIKE

//#if FORGE
//$$ import net.minecraftforge.api.distmarker.Dist
//$$ import net.minecraftforge.api.distmarker.OnlyIn
//$$ import net.minecraftforge.common.MinecraftForge
//$$ import net.minecraftforge.client.event.RegisterKeyMappingsEvent
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent
//$$ import net.minecraftforge.fml.ModLoadingContext
//$$ import net.minecraftforge.fml.common.Mod
//$$ import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
//$$ import net.minecraftforge.server.permission.events.PermissionGatherEvent
//#elseif NEOFORGE
//$$ import net.neoforged.api.distmarker.Dist
//$$ import net.neoforged.api.distmarker.OnlyIn
//$$ import net.neoforged.bus.api.SubscribeEvent
//$$ import net.neoforged.fml.ModLoadingContext
//$$ import net.neoforged.fml.common.Mod
//$$ import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent

//$$ import net.neoforged.neoforge.common.NeoForge
//$$ import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
//$$ import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
//#endif

//#if MC >= 1.20.4
//$$ import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
//#endif

//$$ import xyz.bluspring.unitytranslate.UnityTranslate
//$$ import xyz.bluspring.unitytranslate.client.UnityTranslateClient
//$$ 
//$$ @Mod(UnityTranslate.MOD_ID)
//$$ class UnityTranslateNeoForge {
//$$     init {
//$$         UnityTranslate()
//#if MC >= 1.20.4
//$$         MOD_BUS.register(this)
//$$         NeoForge.EVENT_BUS.register(this)
//#else
//$$         FMLJavaModLoadingContext.get().modEventBus.register(this)
//$$         MinecraftForge.EVENT_BUS.register(this)
//#endif
//$$     }
//$$
//$$     @OnlyIn(Dist.CLIENT)
//$$     @SubscribeEvent
//$$     fun onClientLoading(ev: FMLClientSetupEvent) {
//$$         UnityTranslateClient()
//$$
//$$         ConfigScreenHelper.createConfigScreen()
//$$     }
//$$
//$$     fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
//$$         UnityTranslate.instance.proxy.registerPermissions(event)
//$$     }
//$$
//$$     @OnlyIn(Dist.CLIENT)
//$$     @SubscribeEvent
//$$     fun onClientKeybinds(ev: RegisterKeyMappingsEvent) {
//$$         UnityTranslateClient.registerKeys()
//$$     }
//$$ }

//#endif