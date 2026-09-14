package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.google.common.collect.Queues
import com.mojang.blaze3d.systems.RenderPass

class BlazeCommandQueue {
    private val commands = Queues.newSynchronousQueue<(RenderPass) -> Unit>()
    private val cleanup = Queues.newSynchronousQueue<() -> Unit>()

    fun queue(command: (RenderPass) -> Unit) {
        this.commands.add(command)
    }

    fun cleanup(cleanup: () -> Unit) {
        this.cleanup.add(cleanup)
    }

    fun runAllCommands(pass: RenderPass) {
        while (this.commands.isNotEmpty()) {
            val command = this.commands.poll()
            command(pass)
        }

        while (this.cleanup.isNotEmpty()) {
            val cleanup = this.cleanup.poll()
            cleanup()
        }
    }
}
