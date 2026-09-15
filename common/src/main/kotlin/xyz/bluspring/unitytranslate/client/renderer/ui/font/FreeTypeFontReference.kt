package xyz.bluspring.unitytranslate.client.renderer.ui.font

import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import java.awt.Font
import java.awt.Toolkit
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.io.InputStream
import kotlin.math.roundToInt

class FreeTypeFontReference(stream: InputStream, val fontSize: Float) : FontReference {
    val font: Font = Font.createFonts(stream)[0]
        .deriveFont(fontSize)
    private val context = FontRenderContext(AffineTransform(), true, false)

    override val lineHeight: Int
        get() = Toolkit.getDefaultToolkit().getFontMetrics(this.font).height

    override fun width(text: TextComponent): Int {
        var width = 0

        text.visit({ component, style ->
            var fontStyle = Font.PLAIN
            if (style.bold == true)
                fontStyle = fontStyle or Font.BOLD

            if (style.italic == true)
                fontStyle = fontStyle or Font.ITALIC

            val font = this.font.deriveFont(fontStyle, this.fontSize)
            width += font.getStringBounds(component, context).width.roundToInt()
        })

        return width
    }

    override fun width(text: String): Int {
        return font.getStringBounds(text, context).width.roundToInt()
    }

    override fun split(
        text: TextComponent,
        maxWidth: Int
    ): List<TextComponent> {
        var currentWidth = 0
        var lastComponent = TextComponent.empty()
        val currentComponents = mutableListOf<TextComponent>()
        text.visit({ component, style ->
            for (part in component.split(" ")) {
                val combined = TextComponent.literal(part).withStyle(style)
                val width = this.width(combined)

                if (currentWidth + width >= maxWidth) {
                    currentComponents.add(lastComponent)
                    lastComponent = combined
                    currentWidth = width
                } else {
                    lastComponent.append(combined)
                    currentWidth += width
                }
            }
        })

        currentComponents.add(lastComponent)
        return currentComponents
    }

    override fun substr(
        text: TextComponent,
        maxWidth: Int
    ): TextComponent {
        val main = TextComponent.empty()
        var currentWidth = 0

        text.visit({ component, style ->
            for (part in component.split(" ")) {
                val combined = TextComponent.literal(part).withStyle(style)
                val width = this.width(combined)

                if (currentWidth + width < maxWidth) {
                    main.append(combined)
                    currentWidth += width
                }
            }
        })

        return main
    }
}
