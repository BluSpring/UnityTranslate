package xyz.bluspring.unitytranslate.client.renderer.arc3d

import icyllis.arc3d.engine.*

class BlazeCaps(options: ContextOptions) : Caps(options) {
    override fun isFormatTexturable(format: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMaxRenderTargetSampleCount(format: Int, sampled: Boolean): Int {
        TODO("Not yet implemented")
    }

    override fun isRenderableFormat(
        format: Int,
        sampleCount: Int,
        sampled: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRenderTargetSampleCount(
        sampleCount: Int,
        format: Int,
        sampled: Boolean
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getSupportedWriteColorType(
        surfaceColorType: Int,
        dstDesc: ImageDesc?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun onSupportedReadColorType(
        srcColorType: Int,
        srcFormat: BackendFormat?,
        dstColorType: Int
    ): Long {
        TODO("Not yet implemented")
    }

    override fun onGetDefaultBackendFormat(colorType: Int): BackendFormat? {
        TODO("Not yet implemented")
    }

    override fun getCompressedBackendFormat(compressionType: Int): BackendFormat? {
        TODO("Not yet implemented")
    }

    override fun getColorTypeInfo(
        colorType: Int,
        desc: ImageDesc
    ): ColorTypeInfo? {
        TODO("Not yet implemented")
    }

    override fun getColorTypeInfo(colorType: Int, format: Int): ColorTypeInfo? {
        TODO("Not yet implemented")
    }

    override fun makeGraphicsPipelineKey(
        old: PipelineKey?,
        pipelineDesc: PipelineDesc?,
        renderPassDesc: RenderPassDesc?
    ): PipelineKey {
        TODO("Not yet implemented")
    }

    override fun computeImageKey(
        desc: ImageDesc?,
        recycle: IResourceKey?
    ): IResourceKey? {
        TODO("Not yet implemented")
    }
}
