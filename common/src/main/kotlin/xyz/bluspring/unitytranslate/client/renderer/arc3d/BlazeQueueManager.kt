package xyz.bluspring.unitytranslate.client.renderer.arc3d

import icyllis.arc3d.engine.CommandBuffer
import icyllis.arc3d.engine.ContextOptions
import icyllis.arc3d.engine.QueueManager
import icyllis.arc3d.engine.ResourceProvider

class BlazeQueueManager(val device: BlazeDevice, options: ContextOptions) : QueueManager(device, options) {
    override fun createNewCommandBuffer(resourceProvider: ResourceProvider?): CommandBuffer {
        return BlazeCommandBuffer(this.device)
    }
}
