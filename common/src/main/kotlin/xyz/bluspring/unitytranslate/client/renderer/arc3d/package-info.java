/**
 * A custom rendering backend for Arc3D to interface with modern Blaze3D APIs.
 * Used as a fallback backend if OpenGL or Vulkan is unavailable for Blaze3D (e.g. MetalRender,
 * Potato3D), so UnityTranslate can continue to render.
 * <p>
 * Unavailable for Minecraft 1.21.5 and earlier.
 */
package xyz.bluspring.unitytranslate.client.renderer.arc3d;
