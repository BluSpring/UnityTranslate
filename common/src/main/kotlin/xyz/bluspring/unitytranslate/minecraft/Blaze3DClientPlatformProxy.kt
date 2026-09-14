package xyz.bluspring.unitytranslate.minecraft

import com.mojang.blaze3d.opengl.GlBackend
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.GpuDevice
import com.mojang.blaze3d.systems.GpuDeviceBackend
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vulkan.VulkanBackend
import com.mojang.blaze3d.vulkan.VulkanDevice
import icyllis.arc3d.engine.ContextOptions
import icyllis.arc3d.engine.ImmediateContext
import icyllis.arc3d.opengl.GLUtil
import icyllis.arc3d.vulkan.VKUtil
import icyllis.arc3d.vulkan.VulkanBackendContext
import icyllis.arc3d.vulkan.VulkanMemoryAllocator
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL
import xyz.bluspring.unitytranslate.api.v2.client.InputValue
import xyz.bluspring.unitytranslate.api.v2.util.reverse
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.renderer.arc3d.BlazeDevice
import xyz.bluspring.unitytranslate.client.renderer.arc3d.BlazeQueueManager

abstract class Blaze3DClientPlatformProxy : ClientPlatformProxy {
    private val lookup: Map<InputValue, Int> = mapOf(
        InputValue.KEY_0 to InputConstants.KEY_0,
        InputValue.KEY_1 to InputConstants.KEY_1,
        InputValue.KEY_2 to InputConstants.KEY_2,
        InputValue.KEY_3 to InputConstants.KEY_3,
        InputValue.KEY_4 to InputConstants.KEY_4,
        InputValue.KEY_5 to InputConstants.KEY_5,
        InputValue.KEY_6 to InputConstants.KEY_6,
        InputValue.KEY_7 to InputConstants.KEY_7,
        InputValue.KEY_8 to InputConstants.KEY_8,
        InputValue.KEY_9 to InputConstants.KEY_9,
        InputValue.KEY_A to InputConstants.KEY_A,
        InputValue.KEY_B to InputConstants.KEY_B,
        InputValue.KEY_C to InputConstants.KEY_C,
        InputValue.KEY_D to InputConstants.KEY_D,
        InputValue.KEY_E to InputConstants.KEY_E,
        InputValue.KEY_F to InputConstants.KEY_F,
        InputValue.KEY_G to InputConstants.KEY_G,
        InputValue.KEY_H to InputConstants.KEY_H,
        InputValue.KEY_I to InputConstants.KEY_I,
        InputValue.KEY_J to InputConstants.KEY_J,
        InputValue.KEY_K to InputConstants.KEY_K,
        InputValue.KEY_L to InputConstants.KEY_L,
        InputValue.KEY_M to InputConstants.KEY_M,
        InputValue.KEY_N to InputConstants.KEY_N,
        InputValue.KEY_O to InputConstants.KEY_O,
        InputValue.KEY_P to InputConstants.KEY_P,
        InputValue.KEY_Q to InputConstants.KEY_Q,
        InputValue.KEY_R to InputConstants.KEY_R,
        InputValue.KEY_S to InputConstants.KEY_S,
        InputValue.KEY_T to InputConstants.KEY_T,
        InputValue.KEY_U to InputConstants.KEY_U,
        InputValue.KEY_V to InputConstants.KEY_V,
        InputValue.KEY_W to InputConstants.KEY_W,
        InputValue.KEY_X to InputConstants.KEY_X,
        InputValue.KEY_Y to InputConstants.KEY_Y,
        InputValue.KEY_Z to InputConstants.KEY_Z,
        InputValue.KEY_F1 to InputConstants.KEY_F1,
        InputValue.KEY_F2 to InputConstants.KEY_F2,
        InputValue.KEY_F3 to InputConstants.KEY_F3,
        InputValue.KEY_F4 to InputConstants.KEY_F4,
        InputValue.KEY_F5 to InputConstants.KEY_F5,
        InputValue.KEY_F6 to InputConstants.KEY_F6,
        InputValue.KEY_F7 to InputConstants.KEY_F7,
        InputValue.KEY_F8 to InputConstants.KEY_F8,
        InputValue.KEY_F9 to InputConstants.KEY_F9,
        InputValue.KEY_F10 to InputConstants.KEY_F10,
        InputValue.KEY_F11 to InputConstants.KEY_F11,
        InputValue.KEY_F12 to InputConstants.KEY_F12,
        InputValue.KEY_F13 to InputConstants.KEY_F13,
        InputValue.KEY_F14 to InputConstants.KEY_F14,
        InputValue.KEY_F15 to InputConstants.KEY_F15,
        InputValue.KEY_F16 to InputConstants.KEY_F16,
        InputValue.KEY_F17 to InputConstants.KEY_F17,
        InputValue.KEY_F18 to InputConstants.KEY_F18,
        InputValue.KEY_F19 to InputConstants.KEY_F19,
        InputValue.KEY_F20 to InputConstants.KEY_F20,
        InputValue.KEY_F21 to InputConstants.KEY_F21,
        InputValue.KEY_F22 to InputConstants.KEY_F22,
        InputValue.KEY_F23 to InputConstants.KEY_F23,
        InputValue.KEY_F24 to InputConstants.KEY_F24,
        InputValue.KEY_F25 to InputConstants.KEY_F25,
        InputValue.KEY_NUMLOCK to InputConstants.KEY_NUMLOCK,
        InputValue.KEY_NUMPAD0 to InputConstants.KEY_NUMPAD0,
        InputValue.KEY_NUMPAD1 to InputConstants.KEY_NUMPAD1,
        InputValue.KEY_NUMPAD2 to InputConstants.KEY_NUMPAD2,
        InputValue.KEY_NUMPAD3 to InputConstants.KEY_NUMPAD3,
        InputValue.KEY_NUMPAD4 to InputConstants.KEY_NUMPAD4,
        InputValue.KEY_NUMPAD5 to InputConstants.KEY_NUMPAD5,
        InputValue.KEY_NUMPAD6 to InputConstants.KEY_NUMPAD6,
        InputValue.KEY_NUMPAD7 to InputConstants.KEY_NUMPAD7,
        InputValue.KEY_NUMPAD8 to InputConstants.KEY_NUMPAD8,
        InputValue.KEY_NUMPAD9 to InputConstants.KEY_NUMPAD9,
        InputValue.KEY_NUMPADCOMMA to InputConstants.KEY_NUMPADCOMMA,
        InputValue.KEY_NUMPADENTER to InputConstants.KEY_NUMPADENTER,
        InputValue.KEY_NUMPADEQUALS to InputConstants.KEY_NUMPADEQUALS,
        InputValue.KEY_DOWN to InputConstants.KEY_DOWN,
        InputValue.KEY_LEFT to InputConstants.KEY_LEFT,
        InputValue.KEY_RIGHT to InputConstants.KEY_RIGHT,
        InputValue.KEY_UP to InputConstants.KEY_UP,
        InputValue.KEY_ADD to InputConstants.KEY_ADD,
        InputValue.KEY_APOSTROPHE to InputConstants.KEY_APOSTROPHE,
        InputValue.KEY_BACKSLASH to InputConstants.KEY_BACKSLASH,
        InputValue.KEY_COMMA to InputConstants.KEY_COMMA,
        InputValue.KEY_EQUALS to InputConstants.KEY_EQUALS,
        InputValue.KEY_GRAVE to InputConstants.KEY_GRAVE,
        InputValue.KEY_LBRACKET to InputConstants.KEY_LBRACKET,
        InputValue.KEY_MINUS to InputConstants.KEY_MINUS,
        InputValue.KEY_MULTIPLY to InputConstants.KEY_MULTIPLY,
        InputValue.KEY_PERIOD to InputConstants.KEY_PERIOD,
        InputValue.KEY_RBRACKET to InputConstants.KEY_RBRACKET,
        InputValue.KEY_SEMICOLON to InputConstants.KEY_SEMICOLON,
        InputValue.KEY_SLASH to InputConstants.KEY_SLASH,
        InputValue.KEY_SPACE to InputConstants.KEY_SPACE,
        InputValue.KEY_TAB to InputConstants.KEY_TAB,
        InputValue.KEY_LALT to InputConstants.KEY_LALT,
        InputValue.KEY_LCONTROL to InputConstants.KEY_LCONTROL,
        InputValue.KEY_LSHIFT to InputConstants.KEY_LSHIFT,
        InputValue.KEY_LSUPER to InputConstants.KEY_LSUPER,
        InputValue.KEY_RALT to InputConstants.KEY_RALT,
        InputValue.KEY_RCONTROL to InputConstants.KEY_RCONTROL,
        InputValue.KEY_RSHIFT to InputConstants.KEY_RSHIFT,
        InputValue.KEY_RSUPER to InputConstants.KEY_RSUPER,
        InputValue.KEY_RETURN to InputConstants.KEY_RETURN,
        InputValue.KEY_ESCAPE to InputConstants.KEY_ESCAPE,
        InputValue.KEY_BACKSPACE to InputConstants.KEY_BACKSPACE,
        InputValue.KEY_DELETE to InputConstants.KEY_DELETE,
        InputValue.KEY_END to InputConstants.KEY_END,
        InputValue.KEY_HOME to InputConstants.KEY_HOME,
        InputValue.KEY_INSERT to InputConstants.KEY_INSERT,
        InputValue.KEY_PAGEDOWN to InputConstants.KEY_PAGEDOWN,
        InputValue.KEY_PAGEUP to InputConstants.KEY_PAGEUP,
        InputValue.KEY_CAPSLOCK to InputConstants.KEY_CAPSLOCK,
        InputValue.KEY_PAUSE to InputConstants.KEY_PAUSE,
        InputValue.KEY_SCROLLLOCK to InputConstants.KEY_SCROLLLOCK,
        InputValue.KEY_PRINTSCREEN to InputConstants.KEY_PRINTSCREEN,
        InputValue.PRESS to InputConstants.PRESS,
        InputValue.RELEASE to InputConstants.RELEASE,
        InputValue.REPEAT to InputConstants.REPEAT,
        InputValue.MOUSE_BUTTON_LEFT to InputConstants.MOUSE_BUTTON_LEFT,
        InputValue.MOUSE_BUTTON_RIGHT to InputConstants.MOUSE_BUTTON_RIGHT,
        InputValue.MOUSE_BUTTON_MIDDLE to InputConstants.MOUSE_BUTTON_MIDDLE,
        InputValue.MOUSE_BUTTON_4 to InputConstants.MOUSE_BUTTON_4,
        InputValue.MOUSE_BUTTON_5 to InputConstants.MOUSE_BUTTON_5,
        InputValue.MOUSE_BUTTON_6 to InputConstants.MOUSE_BUTTON_6,
        InputValue.MOUSE_BUTTON_7 to InputConstants.MOUSE_BUTTON_7,
        InputValue.MOUSE_BUTTON_8 to InputConstants.MOUSE_BUTTON_8,
        InputValue.MOD_SHIFT to InputConstants.MOD_SHIFT,
        InputValue.MOD_CONTROL to InputConstants.MOD_CONTROL,
        InputValue.MOD_ALT to InputConstants.MOD_ALT,
        InputValue.MOD_SUPER to InputConstants.MOD_SUPER,
        InputValue.MOD_CAPS_LOCK to InputConstants.MOD_CAPS_LOCK,
        InputValue.MOD_NUM_LOCK to InputConstants.MOD_NUM_LOCK,
    )

    private val reverseLookup = this.lookup.reverse()

    override fun translate(value: InputValue): Int {
        return this.lookup[value]!!
    }

    override fun translate(value: Int): InputValue? {
        return this.reverseLookup[value]
    }

    override fun createArcContext(): ImmediateContext {
        val context = when (Minecraft.getInstance().window.backend()) {
            is GlBackend -> {
                GLUtil.makeOpenGL(GL.getCapabilities(), ContextOptions())
                    ?: throw RuntimeException("UnityTranslate failed to create an Arc3D backend in OpenGL!")
            }

            //? if >= 26.2 {
            is VulkanBackend -> {
                val device = RenderSystem.getDevice()
                val deviceBackend = device.backend

                if (deviceBackend !is VulkanDevice)
                    throw IllegalStateException("")

                VKUtil.makeVulkan(VulkanBackendContext().apply {
                    this.mInstance = deviceBackend.instance().vkInstance()
                    this.mDevice = deviceBackend.vkDevice()
                    this.mPhysicalDevice = deviceBackend.vkDevice().physicalDevice
                    this.mQueue = deviceBackend.graphicsQueue().vkQueue
                    this.mGraphicsQueueIndex = deviceBackend.graphicsQueue().queueFamilyIndex
                    this.mMemoryAllocator = VulkanMemoryAllocator(deviceBackend.vma(), false)
                }, ContextOptions())
            }
            //? }

            else -> null
        }

        if (context == null) {
            // We don't want to hard crash, let's try to defer to Blaze3D directly.
            val options = ContextOptions()
            val device = BlazeDevice(RenderSystem.getDevice(), options)
            return ImmediateContext(device, BlazeQueueManager(device, options))
        }

        return context
    }

    private val GpuDevice.backend: GpuDeviceBackend
        get() {
            return GpuDevice::class.java.getDeclaredField("backend")
                .apply {
                    this.isAccessible = true
                }
                .get(this) as GpuDeviceBackend
        }
}
