package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class OpenBrowserScreen(val address: String) : Screen(Component.empty()) {
    override fun init() {
        super.init()

        addRenderableWidget(
            StringWidget(this.width / 2, this.height / 2, this.width / 3, this.height / 3,
                Component.literal(""),
                this.font
            )
        )
    }
}