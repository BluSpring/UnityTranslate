package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import java.util.*

class EditTranscriptBoxesScreen(val boxes: MutableList<TranscriptBox>) : Screen(Component.empty()) {
    override fun init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                this.onClose()
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 50)
                .build()
        )
    }

    private var boxEditContext: BoxEditContext? = null

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var inAnyBox = false

        for (box in boxes) {
            if (mouseX >= box.x - 1 && mouseY >= box.y - 1 && mouseX <= box.x + box.width + 1 && mouseY <= box.y + box.height + 1) {
                if (mouseX >= box.x - 1 && mouseX <= box.x + 1) {
                    if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                        guiGraphics.fill(box.x, box.y - 1, box.x + box.width, box.y + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR))
                    } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                        guiGraphics.fill(box.x, box.y + box.height - 1, box.x + box.width, box.y + box.height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR))
                    } else {
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR))
                    }

                    guiGraphics.fill(box.x - 1, box.y, box.x + 1, box.y + box.height, FastColor.ARGB32.color(255, 255, 255, 255))
                } else if (mouseX >= box.x + box.width - 1 && mouseX <= box.x + box.width + 1) {
                    if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                        guiGraphics.fill(box.x, box.y - 1, box.x + box.width, box.y + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR))
                    } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                        guiGraphics.fill(box.x, box.y + box.height - 1, box.x + box.width, box.y + box.height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR))
                    } else {
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR))
                    }

                    guiGraphics.fill(box.x + box.width - 1, box.y, box.x + box.width + 1, box.y + box.height, FastColor.ARGB32.color(255, 255, 255, 255))
                } else if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                    guiGraphics.fill(box.x, box.y - 1, box.x + box.width, box.y + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                    GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR))
                } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                    guiGraphics.fill(box.x, box.y + box.height - 1, box.x + box.width, box.y + box.height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                    GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR))
                } else {
                    guiGraphics.renderOutline(box.x, box.y, box.width, box.height, FastColor.ARGB32.color(255, 255, 255, 255))
                    GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR))
                }

                inAnyBox = true
                break
            }
        }

        if (!inAnyBox) {
            GLFW.glfwSetCursor(this.minecraft!!.window.window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR))
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for ((index, box) in boxes.withIndex()) {
            if (mouseX >= box.x - 1 && mouseY >= box.y - 1 && mouseX <= box.x + box.width + 1 && mouseY <= box.y + box.height + 1) {
                boxEditContext = if (mouseX >= box.x - 1 && mouseX <= box.x + 1) {
                    if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.START_X, MoveMode.START_Y), mouseX, mouseY)
                    } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.START_X, MoveMode.END_Y), mouseX, mouseY)
                    } else {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.START_X), mouseX, mouseY)
                    }
                } else if (mouseX >= box.x + box.width - 1 && mouseX <= box.x + box.width + 1) {
                    if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.END_X, MoveMode.START_Y), mouseX, mouseY)
                    } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.END_X, MoveMode.END_Y), mouseX, mouseY)
                    } else {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.END_X), mouseX, mouseY)
                    }
                } else if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                    BoxEditContext(index, box, EnumSet.of(MoveMode.START_Y), mouseX, mouseY)
                } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                    BoxEditContext(index, box, EnumSet.of(MoveMode.END_Y), mouseX, mouseY)
                } else {
                    BoxEditContext(index, box, EnumSet.of(MoveMode.START_X, MoveMode.START_Y, MoveMode.END_X, MoveMode.END_Y), mouseX, mouseY)
                }

                if (boxEditContext!!.box.x <= 0) {
                    boxEditContext!!.box.x = 0
                }

                if (boxEditContext!!.box.y <= 0) {
                    boxEditContext!!.box.y = 0
                }

                if (boxEditContext!!.box.width >= this.minecraft!!.window.guiScaledWidth) {
                    boxEditContext!!.box.width = this.minecraft!!.window.guiScaledWidth
                }

                if (boxEditContext!!.box.height >= this.minecraft!!.window.guiScaledHeight) {
                    boxEditContext!!.box.height = this.minecraft!!.window.guiScaledHeight
                }

                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (boxEditContext != null) {
            boxEditContext = null
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mx: Double, my: Double) {
        val mouseX = Mth.clamp(mx, 0.0, this.minecraft!!.window.guiScaledWidth.toDouble())
        val mouseY = Mth.clamp(my, 0.0, this.minecraft!!.window.guiScaledHeight.toDouble())

        if (boxEditContext != null) {
            val ctx = boxEditContext!!
            val mode = ctx.mode

            if (mode.contains(MoveMode.START_X)) {
                ctx.newX += mouseX - ctx.lastMouseX
                ctx.newX = Mth.clamp(ctx.newX, 0.0, this.minecraft!!.window.guiScaledWidth.toDouble() - ctx.box.width)

                if (!mode.contains(MoveMode.END_X)) {
                    ctx.newWidth -= mouseX - ctx.lastMouseX
                }

                ctx.box.x = ctx.newX.toInt()
                ctx.box.width = ctx.newWidth.toInt()
            }

            if (mode.contains(MoveMode.END_X) && !mode.contains(MoveMode.START_X)) {
                ctx.newWidth += mouseX - ctx.lastMouseX
                ctx.box.width = ctx.newWidth.toInt()
            }

            if (mode.contains(MoveMode.START_Y)) {
                ctx.newY += mouseY - ctx.lastMouseY
                ctx.newY = Mth.clamp(ctx.newY, 0.0, this.minecraft!!.window.guiScaledHeight.toDouble() - ctx.box.height)

                if (!mode.contains(MoveMode.END_Y)) {
                    ctx.newHeight -= mouseY - ctx.lastMouseY
                }

                ctx.box.y = ctx.newY.toInt()
                ctx.box.height = ctx.newHeight.toInt()
            }

            if (mode.contains(MoveMode.END_Y) && !mode.contains(MoveMode.START_Y)) {
                ctx.newHeight += mouseY - ctx.lastMouseY
                ctx.box.height = ctx.newHeight.toInt()
            }

            boxes[ctx.index] = ctx.box

            ctx.lastMouseX = mouseX
            ctx.lastMouseY = mouseY
        }
    }

    private data class BoxEditContext(
        val index: Int,
        val box: TranscriptBox,
        val mode: EnumSet<MoveMode>,
        var lastMouseX: Double,
        var lastMouseY: Double,

        var newX: Double = box.x.toDouble(),
        var newY: Double = box.y.toDouble(),
        var newWidth: Double = box.width.toDouble(),
        var newHeight: Double = box.height.toDouble()
    )

    private enum class MoveMode {
        START_X, START_Y,
        END_X, END_Y
    }
}