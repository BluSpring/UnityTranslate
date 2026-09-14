package xyz.bluspring.unitytranslate.client.renderer.ui.font

import net.minecraft.client.gui.font.providers.FreeTypeUtil
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.freetype.FT_Face
import org.lwjgl.util.freetype.FreeType
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class FreeTypeFontReference(val stream: InputStream, val fontSize: Float) : FontReference {
    val face: FT_Face

    init {
        synchronized(FreeTypeUtil.LIBRARY_LOCK) {
            MemoryStack.stackPush().use { stack ->
                val fontFacePtr = stack.mallocPointer(1)
                FreeTypeUtil.assertError(FreeType.FT_New_Memory_Face(
                    FreeTypeUtil.getLibrary(),
                    ByteBuffer.wrap(stream.readAllBytes()),
                    0L, fontFacePtr
                ), "Initializing font face for UnityTranslate")

                this.face = FT_Face.create(fontFacePtr.get())
            }

            FreeTypeUtil.assertError(FreeType.FT_Select_Charmap(this.face, FreeType.FT_ENCODING_UNICODE), "Find unicode charmap for UnityTranslate")
        }

        FreeTypeUtil.assertError(FreeType.FT_Set_Pixel_Sizes(this.face, this.fontSize.roundToInt(), this.fontSize.roundToInt()),
            "Set pixel size for UnityTranslate")

    }

    override val lineHeight: Int
        get() = (this.fontSize + 2f).roundToInt()

    override fun width(text: TextComponent): Int {
        return FreeType.FT_Load_Char(this.face, 'L', FreeType.FT_LOAD_COLOR)
    }

    override fun width(text: String): Int {
        TODO("Not yet implemented")
    }

    override fun split(
        text: TextComponent,
        maxWidth: Int
    ): List<TextComponent> {
        TODO("Not yet implemented")
    }

    override fun substr(
        text: TextComponent,
        maxWidth: Int
    ): TextComponent {
        TODO("Not yet implemented")
    }
}
