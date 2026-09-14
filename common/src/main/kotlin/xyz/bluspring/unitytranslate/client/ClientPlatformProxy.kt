package xyz.bluspring.unitytranslate.client

import com.mojang.blaze3d.pipeline.RenderTarget
import icyllis.arc3d.engine.ImmediateContext
import xyz.bluspring.unitytranslate.api.v2.client.ClientAccess
import java.util.*

interface ClientPlatformProxy : ClientAccess {
    val framebuffer: RenderTarget
    val shouldRenderGui: Boolean
    val renderThread: Thread

    val windowHandle: Long

    fun createArcContext(): ImmediateContext

    companion object {
        val instance: ClientPlatformProxy = ServiceLoader.load(ClientPlatformProxy::class.java).first()
    }
}
