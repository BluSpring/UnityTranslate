package xyz.bluspring.unitytranslate.neoforge

import dev.architectury.platform.Mod.ConfigurationScreenProvider
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_CONTEXT
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen
import java.util.function.Supplier

@Mod(UnityTranslate.MOD_ID)
class UnityTranslateNeoForge {
    init {
        UnityTranslate(NeoForgeProxy())

        MOD_BUS.register(this)
    }

    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        UnityTranslateClient()

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
            IConfigScreenFactory { mc, prev ->
                UTConfigScreen(prev)
            }
        }
    }

    @SubscribeEvent
    fun onClientKeybinds(ev: RegisterKeyMappingsEvent) {
        UnityTranslateClient.registerKeys()
    }
}