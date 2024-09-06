package xyz.bluspring.unitytranslate.neoforge

//#if FORGE-LIKE

//#if FORGE
//$$ import net.minecraftforge.fml.ModLoadingContext
//#elseif NEOFORGE
//$$ import net.neoforged.fml.ModLoadingContext
//#endif

//#if MC >= 1.20.6
//$$ import net.neoforged.neoforge.client.gui.IConfigScreenFactory
//#else
    //#if FORGE
    //$$ import net.minecraftforge.client.ConfigScreenHandler
    //#else
    //$$ import net.neoforged.neoforge.client.ConfigScreenHandler
    //#endif
//#endif

//$$ import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen

//#endif

object ConfigScreenHelper {
    fun createConfigScreen() {
//#if FORGE-LIKE
//#if MC >= 1.20.6
//$$    ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
//$$        IConfigScreenFactory { mc, prev ->
//#else
//$$    ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
//$$        ConfigScreenHandler.ConfigScreenFactory { mc, prev ->
//#endif
//$$            UTConfigScreen(prev)
//$$        }
//$$    }
//#endif
    }
}