package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.mojang.blaze3d.buffers.GpuFence
import com.mojang.blaze3d.systems.CommandEncoder
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderPassDescriptor
import icyllis.arc3d.core.RawPtr
import icyllis.arc3d.core.Rect2ic
import icyllis.arc3d.engine.*
import icyllis.arc3d.vulkan.VKUtil
import org.joml.Vector4f
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle
import xyz.bluspring.unitytranslate.client.renderer.arc3d.noop.NoopGpuTextureView
import java.util.*
import java.util.function.Function

class BlazeCommandBuffer(val encoder: CommandEncoder) : CommandBuffer() {
    private var currentFence: GpuFence? = null
    private var currentRenderPassDesc: RenderPassDescriptor? = null
    private var currentScissor: ScreenRectangle? = null

    override fun <T : Any?> setupForShaderRead(
        textures: List<@RawPtr T?>,
        toTexture: Function<in T, @RawPtr Image?>
    ) {
        TODO("Not yet implemented")
    }

    override fun beginRenderPass(
        renderPassDesc: RenderPassDesc,
        framebufferDesc: FramebufferDesc,
        renderPassBounds: Rect2ic?,
        clearColors: FloatArray?,
        clearDepth: Float,
        clearStencil: Int
    ): Boolean {
        this.currentRenderPassDesc = RenderPassDescriptor.create { "UnityTranslate Arc3D Render Pass" }
            .apply {
                if (renderPassDesc.mColorAttachments.isNotEmpty()) {
                    for (desc in renderPassDesc.mColorAttachments) {
                        if (!desc.isUsed)
                            withUnusedColorAttachment()
                        else {
                            withColorAttachment(NoopGpuTextureView(desc),
                                if (desc.mLoadOp == Engine.LoadOp.kClear)
                                    Optional.ofNullable(clearColors)
                                        .map { Vector4f(it[0], it[1], it[2], it[3]) }
                                else Optional.empty()
                            )
                        }
                    }
                }

                if (renderPassDesc.mDepthStencilAttachment.isUsed && Engine.ImageFormat.depthBits(renderPassDesc.mDepthStencilAttachment.mFormat) > 0) {
                    val depthAttach = renderPassDesc.mDepthStencilAttachment
                    withDepthAttachment(NoopGpuTextureView(depthAttach), if (depthAttach.mLoadOp == Engine.LoadOp.kClear)
                            OptionalDouble.of(clearDepth.toDouble())
                        else OptionalDouble.empty()
                    )
                }

                if (renderPassBounds != null) {
                    withRenderArea(RenderPass.RenderArea(renderPassBounds.x(), renderPassBounds.y(), renderPassBounds.width(), renderPassBounds.height()))
                } else {
                    withRenderArea(RenderPass.RenderArea(0, 0, framebufferDesc.mWidth, framebufferDesc.mHeight))
                }
            }
        return true
    }

    override fun setViewport(x: Int, y: Int, width: Int, height: Int) {
    }

    override fun setScissor(x: Int, y: Int, width: Int, height: Int) {
        this.currentScissor = ScreenRectangle(x, y, width, height)
    }

    override fun bindGraphicsPipeline(graphicsPipeline: @RawPtr GraphicsPipeline?): Boolean {
        TODO("Not yet implemented")
    }

    override fun bindIndexBuffer(
        indexType: Int,
        buffer: @RawPtr Buffer?,
        offset: Long
    ) {

    }

    override fun bindVertexBuffer(
        binding: Int,
        buffer: @RawPtr Buffer?,
        offset: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun bindUniformBuffer(
        set: Int,
        binding: Int,
        buffer: @RawPtr Buffer?,
        offset: Int,
        size: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun bindTextureSampler(
        set: Int,
        binding: Int,
        texture: @RawPtr Image?,
        swizzle: Short,
        sampler: @RawPtr Sampler?
    ) {
        TODO("Not yet implemented")
    }

    override fun draw(vertexCount: Int, baseVertex: Int) {
        TODO("Not yet implemented")
    }

    override fun drawIndexed(indexCount: Int, baseIndex: Int, baseVertex: Int) {
        TODO("Not yet implemented")
    }

    override fun drawInstanced(
        instanceCount: Int,
        baseInstance: Int,
        vertexCount: Int,
        baseVertex: Int
    ) {

    }

    override fun drawIndexedInstanced(
        indexCount: Int,
        baseIndex: Int,
        instanceCount: Int,
        baseInstance: Int,
        baseVertex: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun endRenderPass() {
        this.currentRenderPassDesc?.use
    }

    override fun onCopyBuffer(
        srcBuffer: @RawPtr Buffer?,
        dstBuffer: @RawPtr Buffer?,
        srcOffset: Long,
        dstOffset: Long,
        size: Long
    ): Boolean {
        if (srcBuffer !is BlazeBuffer || dstBuffer !is BlazeBuffer)
            return false

        this.encoder.copyToBuffer(srcBuffer.source.slice(srcOffset, size), dstBuffer.source.slice(dstOffset, size))
        return true
    }

    override fun onCopyBufferToImage(
        srcBuffer: @RawPtr Buffer?,
        dstImage: @RawPtr Image?,
        copyData: List<BufferImageCopyData>
    ): Boolean {
        if (srcBuffer !is BlazeBuffer || dstImage !is BlazeImage)
            return false

        for ((index, data) in copyData.withIndex()) {
            this.encoder.copyBufferToTexture(srcBuffer.source.slice(data.mBufferOffset, data.mBufferRowBytes), 0, 0, data.mWidth, data.mHeight, dstImage.source,
                data.mX, data.mY, data.mWidth, data.mHeight, index, data.mArraySlice)
        }

        return true
    }

    override fun onCopyImage(
        srcImage: @RawPtr Image,
        srcL: Int, srcT: Int, srcR: Int, srcB: Int,
        dstImage: @RawPtr Image,
        dstX: Int, dstY: Int,
        mipLevel: Int
    ): Boolean {
        if (srcImage !is BlazeImage || dstImage !is BlazeImage)
            return false

        this.encoder.copyTextureToTexture(srcImage.source, dstImage.source, mipLevel, dstX, dstY, srcL, srcT, srcR - srcL, srcB - srcT)
        return true
    }

    override fun begin() {
    }

    override fun submit(queueManager: QueueManager?): Boolean {
        this.encoder.submit()
        return true
    }

    override fun checkFinishedAndReset(): Boolean {
        val fence = this.currentFence
        if (fence != null) {
            if (fence.awaitCompletion(1)) {
                fence.close()
                this.currentFence = null
                return true
            } else {
                return false
            }
        }

        return true
    }

    override fun waitUntilFinished() {
        if (this.currentFence?.awaitCompletion(VKUtil.UINT64_MAX) == true) {
            this.currentFence = null
        }
    }

    override fun destroy() {
        this.currentFence?.close()
        this.currentFence = null
    }
}
