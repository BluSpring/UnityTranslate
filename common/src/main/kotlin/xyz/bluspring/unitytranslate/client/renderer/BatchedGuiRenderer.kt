package xyz.bluspring.unitytranslate.client.renderer

import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuSampler
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.VertexConsumer
import icyllis.arc3d.granite.GraniteSurface
import icyllis.arc3d.granite.RecordingContext
import net.minecraft.client.renderer.Projection
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.rendertype.RenderType
import org.joml.Matrix4f
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import java.nio.ByteBuffer
import java.util.*

object BatchedGuiRenderer {
    private val bufferBuilders = WeakHashMap<QueuedDraw, ByteBufferBuilder>()
    private val gameBuffers = mutableMapOf<QueuedDraw, BufferBuilder>()
    private val screenBuffers = mutableMapOf<QueuedDraw, BufferBuilder>()
    private val projection = Projection()
    private val projectionBuffer = ProjectionMatrixBuffer("unitytranslate_gui")

    val immediateContext = ClientPlatformProxy.instance.createArcContext()
    val recordingContext = RecordingContext.makeRecordingContext(immediateContext, RecordingContext.Options())
    val surface = GraniteSurface.makeRenderTarget(Core)

    @JvmStatic @JvmOverloads
    fun getBuffer(type: RenderType, scissor: ScreenRectangle? = null, layer: DrawLayer = DrawLayer.IN_GAME): VertexConsumer {
        val prepared = type.prepare()
        val draw = QueuedDraw(RenderSetupInfo(prepared.pipeline, prepared.textures.map {
            Texture(it.name, it.textureView, it.sampler)
        }), scissor, layer)
        return layer.buffers.computeIfAbsent(draw) {
            BufferBuilder(this.getBufferBuilder(draw), type.primitiveTopology(), type.format())
        }
    }

    @JvmStatic @JvmOverloads
    fun getBuffer(pipeline: RenderPipeline,
                  textures: List<Texture> = listOf(),
                  scissor: ScreenRectangle? = null,
                  uniforms: Map<String, ByteBuffer> = mapOf(),
                  layer: DrawLayer = DrawLayer.IN_GAME
    ): VertexConsumer {
        val draw = QueuedDraw(RenderSetupInfo(pipeline, textures, uniforms), scissor, layer)

        return layer.buffers.computeIfAbsent(draw) {
            BufferBuilder(this.getBufferBuilder(draw), pipeline.primitiveTopology, pipeline.getVertexFormatBinding(0)!!)
        }
    }

    private fun getBufferBuilder(draw: QueuedDraw): ByteBufferBuilder {
        return this.bufferBuilders.computeIfAbsent(draw) {
            ByteBufferBuilder(8 * 1024) // 8 KiB
        }
    }

    private object BufferType {
        const val VERTICES = GpuBuffer.USAGE_VERTEX
        const val INDICES = GpuBuffer.USAGE_INDEX
        const val UNIFORM = GpuBuffer.USAGE_UNIFORM
    }

    fun render(layer: DrawLayer = DrawLayer.IN_GAME) {
        val buffers = layer.buffers
        if (buffers.isEmpty())
            return

        val draws = buffers.mapValues { it.value.build() }

        val framebuffer = ClientPlatformProxy.instance.framebuffer
        this.projection.setupOrtho(1000f, 11000f, framebuffer.width.toFloat() / ClientPlatformProxy.instance.guiScale.toFloat(), framebuffer.height.toFloat() / ClientPlatformProxy.instance.guiScale.toFloat(), true)
        RenderSystem.setProjectionMatrix(this.projectionBuffer.getBuffer(this.projection), ProjectionType.ORTHOGRAPHIC)

        val dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(Matrix4f().setTranslation(0f, 0f, -11000f))

        RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass({ "UnityTranslate GUI Renderer" }, framebuffer.colorTextureView!!, Optional.empty(), if (framebuffer.useDepth) framebuffer.depthTextureView!! else null,
                OptionalDouble.empty())
            .use { renderPass ->
                RenderSystem.bindDefaultUniforms(renderPass)
                renderPass.setUniform("DynamicTransforms", dynamicTransforms)

                var current = 0
                for ((draw, buffer) in draws) {
                    if (buffer == null)
                        continue

                    val index = current++

                    RenderSystem.getDevice().createBuffer({ "UnityTranslate Vertex Buffer $index" }, BufferType.VERTICES, buffer.vertexBuffer())
                        .use { vertexBuffer ->
                            RenderSystem.getDevice().createBuffer({ "UnityTranslate Index Buffer $index" }, BufferType.INDICES, buffer.indexBuffer() ?: run {
                                buffer.sortQuads(this.getBufferBuilder(draw), RenderSystem.getProjectionType().vertexSorting())
                                buffer.indexBuffer()!!
                            }).use { indexBuffer ->
                                renderPass.setPipeline(draw.setup.pipeline)
                                renderPass.setVertexBuffer(0, vertexBuffer.slice())
                                renderPass.setIndexBuffer(indexBuffer, buffer.drawState().indexType)

                                val scissorArea = draw.scissorArea
                                if (scissorArea != null) {
                                    renderPass.enableScissor(scissorArea.left, framebuffer.height - scissorArea.bottom, 0.coerceAtLeast((scissorArea.right - scissorArea.left)), 0.coerceAtLeast(scissorArea.bottom - scissorArea.top))
                                } else {
                                    renderPass.disableScissor()
                                }

                                for (texture in draw.setup.textures) {
                                    renderPass.bindTexture(texture.name, texture.textureView, texture.sampler ?: RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST))
                                }

                                val uniforms = draw.setup.uniforms.mapValues { (name, uniformBuffer) ->
                                    RenderSystem.getDevice().createBuffer({ "UnityTranslate Uniform Buffer for $name" }, BufferType.UNIFORM, uniformBuffer)
                                }

                                for ((name, uniformBuffer) in uniforms) {
                                    renderPass.setUniform(name, uniformBuffer)
                                }

                                renderPass.drawIndexed(buffer.drawState().indexCount, 1, 0, 0, 0)

                                for ((_, uniformBuffer) in uniforms) {
                                    uniformBuffer.close()
                                }
                            }
                        }

                    buffer.close()
                }
            }

        buffers.clear()
    }

    @JvmRecord
    data class RenderSetupInfo(
        val pipeline: RenderPipeline,
        val textures: List<Texture>,
        val uniforms: Map<String, ByteBuffer> = mapOf()
    )

    @JvmRecord
    data class Texture @JvmOverloads constructor(
        val name: String,
        val textureView: GpuTextureView,
        val sampler: GpuSampler? = null,
    )

    @JvmRecord
    internal data class QueuedDraw(val setup: RenderSetupInfo, val scissorArea: ScreenRectangle?, val layer: DrawLayer)

    enum class DrawLayer(internal val buffers: MutableMap<QueuedDraw, BufferBuilder>) {
        IN_GAME(gameBuffers),
        SCREEN(screenBuffers),
    }
}
