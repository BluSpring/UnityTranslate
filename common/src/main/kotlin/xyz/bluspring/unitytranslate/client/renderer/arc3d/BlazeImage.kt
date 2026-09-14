package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.mojang.blaze3d.textures.GpuTexture
import icyllis.arc3d.engine.Image
import icyllis.arc3d.engine.ImageDesc

class BlazeImage(val source: GpuTexture, device: BlazeDevice, wrapped: Boolean, desc: ImageDesc, state: BlazeImageMutableState) : Image(device, wrapped, desc, state) {
}
