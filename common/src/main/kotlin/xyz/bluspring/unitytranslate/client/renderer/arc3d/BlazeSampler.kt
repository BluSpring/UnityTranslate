package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.mojang.blaze3d.textures.GpuSampler
import icyllis.arc3d.engine.Sampler
import icyllis.arc3d.engine.SamplerDesc

class BlazeSampler(val source: GpuSampler, device: BlazeDevice, desc: SamplerDesc) : Sampler(device, desc) {
    override fun destroy() {
        this.source.close()
    }
}
