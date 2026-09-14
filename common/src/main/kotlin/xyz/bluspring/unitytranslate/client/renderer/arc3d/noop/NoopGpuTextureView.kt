package xyz.bluspring.unitytranslate.client.renderer.arc3d.noop

import com.mojang.blaze3d.textures.GpuTextureView
import icyllis.arc3d.engine.RenderPassDesc

class NoopGpuTextureView(val holding: RenderPassDesc.AttachmentDesc) : GpuTextureView(NoopGpuTexture, 0, 0) {
    override fun close() {
        throw IllegalStateException()
    }

    override fun isClosed(): Boolean {
        throw IllegalStateException()
    }
}
