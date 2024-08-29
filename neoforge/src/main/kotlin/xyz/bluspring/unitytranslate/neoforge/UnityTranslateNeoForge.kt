package xyz.bluspring.unitytranslate.neoforge

import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen

@Mod(UnityTranslate.MOD_ID)
class UnityTranslateNeoForge {
    init {
        UnityTranslate(NeoForgeProxy())
        FMLJavaModLoadingContext.get().modEventBus.register(this)
    }

    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        UnityTranslateClient()

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { mc, prev ->
                UTConfigScreen(prev)
            }
        }
    }

    @SubscribeEvent
    fun onClientKeybinds(ev: RegisterKeyMappingsEvent) {
        UnityTranslateClient.registerKeys()
    }
}