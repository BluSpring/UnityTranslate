package xyz.bluspring.unitytranslate.commands

import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import xyz.bluspring.unitytranslate.network.PacketIds
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.translator.LocalLibreTranslateInstance
import xyz.bluspring.unitytranslate.translator.TranslatorManager

object UnityTranslateCommands {
    val INFO = Commands.literal("info")
        .executes { ctx ->
            ctx.source.sendSystemMessage(ComponentUtils.formatList(listOf(
                Component.literal("UnityTranslate v${UnityTranslate.instance.proxy.modVersion}"),
                Component.literal("- Mod Enabled: ${UnityTranslate.config.server.enabled}"),
                Component.literal("- Total instances loaded: ${TranslatorManager.instances.size}"),
                Component.literal("- Queued translations: ${TranslatorManager.queuedTranslations.size}"),
                Component.empty(),
                Component.literal("- Supports local translation server: ${LocalLibreTranslateInstance.canRunLibreTranslate()}"),
                Component.literal("- Is local translation server running: ${LocalLibreTranslateInstance.hasStarted}"),
                Component.literal("- Supports CUDA: ${TranslatorManager.supportsCuda}"),
            ), Component.literal("\n")))

            1
        }

    val CLEAR_QUEUE = Commands.literal("clearqueue")
        .executes { ctx ->
            TranslatorManager.queuedTranslations.clear()
            ctx.source.sendSystemMessage(Component.literal("Forcefully cleared translation queue."))

            1
        }

    val DEBUG_RESTART_TIMER = Commands.literal("debugreload")
        .executes { ctx ->
            TranslatorManager.loadFromConfig()
            ctx.source.sendSystemMessage(Component.literal("Restarted timer!"))

            1
        }

    // Unity MP exclusive
    val TOGGLE = Commands.literal("toggle")
        .requires { UnityTranslate.IS_UNITY_SERVER }
        .executes { ctx ->
            UnityTranslate.config.server.enabled = !UnityTranslate.config.server.enabled
            UnityTranslate.saveConfig()

            val buf = UnityTranslate.instance.proxy.createByteBuf()
            buf.writeBoolean(UnityTranslate.config.server.enabled)

            for (player in ctx.source.server.playerList.players) {
                UnityTranslate.instance.proxy.sendPacketServer(player, PacketIds.TOGGLE_MOD, buf)
            }

            ctx.source.sendSystemMessage(Component.literal("Toggled UnityTranslate for all players: ${UnityTranslate.config.server.enabled}"))

            1
        }

    val ROOT = Commands.literal("unitytranslate")
        .requires { it.hasPermission(3) }
        .then(INFO)
        .then(CLEAR_QUEUE)
        .then(TOGGLE)
        .then(DEBUG_RESTART_TIMER)
}