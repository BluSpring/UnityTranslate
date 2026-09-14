package xyz.bluspring.unitytranslate.client.renderer.arc3d.noop

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.textures.GpuTexture

object NoopGpuTexture : GpuTexture(0, "UT NoOp Arc3D GPU texture", GpuFormat.R16_FLOAT, 0, 0, 0, 0) {
    override fun close() {
        throw IllegalStateException()
    }

    override fun isClosed(): Boolean {
        throw IllegalStateException()
    }
}
