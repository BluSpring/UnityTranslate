package xyz.bluspring.unitytranslate.fabric.compat.modmenu

//#if FABRIC
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen

class UTModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory {
            UTConfigScreen(it)
        }
    }
}
//#endif