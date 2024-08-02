package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class UTConfigScreen(private val parent: Screen?) : Screen(Component.literal("UnityTranslate")) {
    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }
}