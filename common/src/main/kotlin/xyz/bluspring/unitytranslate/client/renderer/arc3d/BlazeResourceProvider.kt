package xyz.bluspring.unitytranslate.client.renderer.arc3d

import icyllis.arc3d.core.SharedPtr
import icyllis.arc3d.engine.*

class BlazeResourceProvider(device: BlazeDevice, context: Context, maxResourceBudget: Long) : ResourceProvider(device, context, maxResourceBudget) {
    override fun onCreateNewImage(desc: ImageDesc?): @SharedPtr Image? {
        TODO("Not yet implemented")
    }

    override fun onWrapBackendImage(backendImage: BackendImage): @SharedPtr Image? {
        TODO("Not yet implemented")
    }

    override fun createSampler(desc: SamplerDesc?): @SharedPtr Sampler? {
        TODO("Not yet implemented")
    }

    override fun onCreateNewBuffer(
        size: Long,
        usage: Int
    ): @SharedPtr Buffer? {
        TODO("Not yet implemented")
    }
}
