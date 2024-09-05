package xyz.bluspring.unitytranslate.neoforge

//#if FORGE-LIKE

//#if FORGE
//$$ import net.minecraftforge.client.ConfigScreenHandler
//$$ import net.minecraftforge.client.event.RegisterKeyMappingsEvent
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent
//$$ import net.minecraftforge.fml.ModLoadingContext
//$$ import net.minecraftforge.fml.common.Mod
//$$ import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
//#elseif NEOFORGE
//$$ import net.neoforged.bus.api.SubscribeEvent
//$$ import net.neoforged.fml.ModLoadingContext
//$$ import net.neoforged.fml.common.Mod
//$$ import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent

//#if MC >= 1.20.6
//$$ import net.neoforged.neoforge.client.gui.IConfigScreenFactory
//#else
//$$ import net.neoforged.neoforge.client.ConfigScreenHandler
//#endif

//$$ import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
//#endif

//#if MC >= 1.20.4
//$$ import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
//#endif

//$$ import xyz.bluspring.unitytranslate.UnityTranslate
//$$ import xyz.bluspring.unitytranslate.client.UnityTranslateClient
//$$ import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen
//$$ 
//$$ @Mod(UnityTranslate.MOD_ID)
//$$ class UnityTranslateNeoForge {
//$$     init {
//$$         UnityTranslate()
//#if MC >= 1.20.4
//$$         MOD_BUS.register(this)
//#else
//$$         FMLJavaModLoadingContext.get().modEventBus.register(this)
//#endif
//$$     }
//$$ 
//$$     @SubscribeEvent
//$$     fun onClientLoading(ev: FMLClientSetupEvent) {
//$$         UnityTranslateClient()
//$$
//#if MC >= 1.20.6
//$$        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
//$$            IConfigScreenFactory { mc, prev ->
//#else
//$$         ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
//$$             ConfigScreenHandler.ConfigScreenFactory { mc, prev ->
//#endif
//$$                 UTConfigScreen(prev)
//$$             }
//$$         }
//$$     }
//$$ 
//$$     @SubscribeEvent
//$$     fun onClientKeybinds(ev: RegisterKeyMappingsEvent) {
//$$         UnityTranslateClient.registerKeys()
//$$     }
//$$ }

//#endif