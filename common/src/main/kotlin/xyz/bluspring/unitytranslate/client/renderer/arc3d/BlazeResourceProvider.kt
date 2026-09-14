package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.textures.AddressMode
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTexture
import icyllis.arc3d.core.SharedPtr
import icyllis.arc3d.engine.*
import org.lwjgl.opengl.GL32C
import java.util.*

class BlazeResourceProvider(private val device: BlazeDevice, context: Context, maxResourceBudget: Long) : ResourceProvider(device, context, maxResourceBudget) {
    override fun onCreateNewImage(desc: ImageDesc): @SharedPtr Image {
        return BlazeImage(this.device.device.createTexture(null as String?,
            GpuTexture.USAGE_COPY_SRC or GpuTexture.USAGE_COPY_DST or GpuTexture.USAGE_TEXTURE_BINDING or GpuTexture.USAGE_RENDER_ATTACHMENT,
            when (desc.glFormat) {
                GL32C.GL_R8 -> GpuFormat.R8_UNORM
                GL32C.GL_R8_SNORM -> GpuFormat.R8_SNORM
                GL32C.GL_RG8 -> GpuFormat.RG8_UNORM
                GL32C.GL_RG8_SNORM -> GpuFormat.RG8_SNORM
                GL32C.GL_RGB8 -> GpuFormat.RGB8_UNORM
                GL32C.GL_RGB8_SNORM -> GpuFormat.RGB8_SNORM
                GL32C.GL_RGBA8 -> GpuFormat.RGBA8_UNORM
                GL32C.GL_RGBA8_SNORM -> GpuFormat.RGBA8_SNORM
                GL32C.GL_R16 -> GpuFormat.R16_UNORM
                GL32C.GL_R16_SNORM -> GpuFormat.R16_SNORM
                GL32C.GL_RG16 -> GpuFormat.RG16_UNORM
                GL32C.GL_RG16_SNORM -> GpuFormat.RG16_SNORM
                GL32C.GL_RGB16 -> GpuFormat.RGB16_UNORM
                GL32C.GL_RGB16_SNORM -> GpuFormat.RGB16_SNORM
                GL32C.GL_RGBA16 -> GpuFormat.RGBA16_UNORM
                GL32C.GL_RGBA16_SNORM -> GpuFormat.RGBA16_SNORM
                GL32C.GL_R8UI -> GpuFormat.R8_UINT
                GL32C.GL_R8I -> GpuFormat.R8_SINT
                GL32C.GL_RG8UI -> GpuFormat.RG8_UINT
                GL32C.GL_RG8I -> GpuFormat.RG8_SINT
                GL32C.GL_RGB8UI -> GpuFormat.RGB8_UINT
                GL32C.GL_RGB8I -> GpuFormat.RGB8_SINT
                GL32C.GL_RGBA8UI -> GpuFormat.RGBA8_UINT
                GL32C.GL_RGBA8I -> GpuFormat.RGBA8_SINT
                GL32C.GL_R16UI -> GpuFormat.R16_UINT
                GL32C.GL_R16I -> GpuFormat.R16_SINT
                GL32C.GL_RG16UI -> GpuFormat.RG16_UINT
                GL32C.GL_RG16I -> GpuFormat.RG16_SINT
                GL32C.GL_RGB16UI -> GpuFormat.RGB16_UINT
                GL32C.GL_RGB16I -> GpuFormat.RGB16_SINT
                GL32C.GL_RGBA16UI -> GpuFormat.RGBA16_UINT
                GL32C.GL_RGBA16I -> GpuFormat.RGBA16_SINT
                GL32C.GL_R32UI -> GpuFormat.R32_UINT
                GL32C.GL_R32I -> GpuFormat.R32_SINT
                GL32C.GL_RG32UI -> GpuFormat.RG32_UINT
                GL32C.GL_RG32I -> GpuFormat.RG32_SINT
                GL32C.GL_RGB32UI -> GpuFormat.RGB32_UINT
                GL32C.GL_RGB32I -> GpuFormat.RGB32_SINT
                GL32C.GL_RGBA32UI -> GpuFormat.RGBA32_UINT
                GL32C.GL_RGBA32I -> GpuFormat.RGBA32_SINT
                GL32C.GL_R16F -> GpuFormat.R16_FLOAT
                GL32C.GL_RG16F -> GpuFormat.RG16_FLOAT
                GL32C.GL_RGB16F -> GpuFormat.RGB16_FLOAT
                GL32C.GL_RGBA16F -> GpuFormat.RGBA16_FLOAT
                GL32C.GL_R32F -> GpuFormat.R32_FLOAT
                GL32C.GL_RG32F -> GpuFormat.RG32_FLOAT
                GL32C.GL_RGB32F -> GpuFormat.RGB32_FLOAT
                GL32C.GL_RGBA32F -> GpuFormat.RGBA32_FLOAT
                GL32C.GL_RGB10_A2 -> GpuFormat.RGB10A2_UNORM
                GL32C.GL_UNSIGNED_INT_10_10_10_2 -> GpuFormat.RGB10A2_UINT
                GL32C.GL_R11F_G11F_B10F -> GpuFormat.RG11B10_FLOAT
                GL32C.GL_DEPTH_COMPONENT32F -> GpuFormat.D32_FLOAT
                GL32C.GL_DEPTH32F_STENCIL8 -> GpuFormat.D32_FLOAT_S8_UINT
                GL32C.GL_UNSIGNED_INT_24_8 -> GpuFormat.D24_UNORM_S8_UINT
                GL32C.GL_DEPTH_COMPONENT16 -> GpuFormat.D16_UNORM
                GL32C.GL_STENCIL_INDEX8 -> GpuFormat.S8_UINT
                else -> throw IllegalArgumentException("Unknown texture type (GL: ${desc.glFormat}, VK: ${desc.vkFormat})")
            },
            desc.width, desc.height, desc.depth, desc.mipLevelCount),
            this.device, false, desc, BlazeImageMutableState()
        )
    }

    override fun onWrapBackendImage(backendImage: BackendImage): @SharedPtr Image? {
        TODO("Not yet implemented")
    }

    override fun createSampler(desc: SamplerDesc): @SharedPtr Sampler {
        return BlazeSampler(this.device.device.createSampler(
            when (desc.addressModeX) {
                SamplerDesc.ADDRESS_MODE_REPEAT, SamplerDesc.ADDRESS_MODE_MIRRORED_REPEAT -> AddressMode.REPEAT
                else -> AddressMode.CLAMP_TO_EDGE
            },
            when (desc.addressModeY) {
                SamplerDesc.ADDRESS_MODE_REPEAT, SamplerDesc.ADDRESS_MODE_MIRRORED_REPEAT -> AddressMode.REPEAT
                else -> AddressMode.CLAMP_TO_EDGE
            },
            when (desc.minFilter) {
                SamplerDesc.FILTER_LINEAR -> FilterMode.LINEAR
                SamplerDesc.FILTER_NEAREST -> FilterMode.NEAREST
                else -> throw IllegalArgumentException("Unknown filter type ${desc.minFilter}")
            },
            when (desc.magFilter) {
                SamplerDesc.FILTER_LINEAR -> FilterMode.LINEAR
                SamplerDesc.FILTER_NEAREST -> FilterMode.NEAREST
                else -> throw IllegalArgumentException("Unknown filter type ${desc.magFilter}")
            },
            desc.maxAnisotropy,
            OptionalDouble.empty()
        ), this.device, desc)
    }

    override fun onCreateNewBuffer(
        size: Long,
        usage: Int
    ): @SharedPtr Buffer {
        return BlazeBuffer(this.device, this.device.device.createBuffer(null, usage, size))
    }
}
