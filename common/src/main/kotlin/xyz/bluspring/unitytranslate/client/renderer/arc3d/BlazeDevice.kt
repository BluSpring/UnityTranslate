package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.mojang.blaze3d.systems.GpuDevice
import icyllis.arc3d.core.Rect2i
import icyllis.arc3d.core.SharedPtr
import icyllis.arc3d.engine.*

class BlazeDevice(val device: GpuDevice, options: ContextOptions) : Device(0xB1A2E3D, options, BlazeCaps(options)) {
    override fun makeResourceProvider(
        context: Context,
        maxResourceBudget: Long
    ): ResourceProvider {
        return BlazeResourceProvider(this, context, maxResourceBudget)
    }

    override fun onCreateRenderTarget(
        width: Int,
        height: Int,
        sampleCount: Int,
        numColorTargets: Int,
        colorTargets: Array<out Image?>?,
        resolveTargets: Array<out Image?>?,
        mipLevels: IntArray?,
        depthStencilTarget: Image?,
        surfaceFlags: Int
    ): @SharedPtr GpuRenderTarget? {
        return null
    }

    override fun onWrapRenderableBackendTexture(
        texture: BackendImage?,
        sampleCount: Int,
        ownership: Boolean
    ): @SharedPtr GpuRenderTarget? {
        return null
    }

    override fun onWrapBackendRenderTarget(backendRenderTarget: BackendRenderTarget?): @SharedPtr GpuRenderTarget? {
        return null
    }

    override fun onGetOpsRenderPass(
        writeView: ImageProxyView?,
        contentBounds: Rect2i?,
        colorOps: Byte,
        stencilOps: Byte,
        clearColor: FloatArray?,
        sampledTextures: Set<SurfaceProxy?>?,
        pipelineFlags: Int
    ): OpsRenderPass? {
        return null
    }

    override fun onResolveRenderTarget(
        renderTarget: GpuRenderTarget?,
        resolveLeft: Int,
        resolveTop: Int,
        resolveRight: Int,
        resolveBottom: Int
    ) {
    }

    override fun insertFence(): Long {
        return 0
    }

    override fun checkFence(fence: Long): Boolean {
        return false
    }

    override fun deleteFence(fence: Long) {
    }

    override fun addFinishedCallback(callback: FlushInfo.FinishedCallback?) {
    }

    override fun checkFinishedCallbacks() {
    }

    override fun waitForQueue() {
    }
}
