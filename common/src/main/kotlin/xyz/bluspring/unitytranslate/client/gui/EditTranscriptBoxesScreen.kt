package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.network.payloads.SetUsedLanguagesPayload
import java.util.*

class EditTranscriptBoxesScreen(val boxes: MutableList<TranscriptBox>, val parent: Screen? = null) : Screen(Component.empty()) {
    val CLOSE_BUTTON = ResourceLocation.fromNamespaceAndPath(UnityTranslate.MOD_ID, "textures/gui/close.png")
    var shouldDisableHudAfter = false

    private val arrowCursor: Long = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
    private var currentShape: Int = GLFW.GLFW_ARROW_CURSOR
    private var currentCursor: Long = arrowCursor

    override fun init() {
        if (!UnityTranslateClient.shouldRenderBoxes) {
            shouldDisableHudAfter = true
            UnityTranslateClient.shouldRenderBoxes = true
        }

        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                this.onClose()
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 50)
                .build()
        )

        this.addRenderableWidget(
            Button.builder(Component.literal("+")) {
                Minecraft.getInstance().setScreen(LanguageSelectScreen(this, true))
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2) - Button.DEFAULT_HEIGHT, this.height - 50)
                .width(Button.DEFAULT_HEIGHT)
                .build()
        )
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)

        if (shouldDisableHudAfter)
            UnityTranslateClient.shouldRenderBoxes = false

        UnityTranslate.saveConfig()

        if (UnityTranslateClient.languageBoxes.isNotEmpty()) {
            val languages = UnityTranslateClient.languageBoxes.map { it.language }.toMutableList()

            if (!languages.contains(UnityTranslate.config.client.language)) {
                languages.add(UnityTranslate.config.client.language)
            }

            if (Minecraft.getInstance().player != null) {
                UnityTranslate.instance.proxy.sendPacketClient(SetUsedLanguagesPayload(languages))
            }
        }

        // make sure that the cursor is reset
        GLFW.glfwSetCursor(this.minecraft!!.window.window, arrowCursor)
    }

    private var boxEditContext: BoxEditContext? = null

    private fun assignCursor(shape: Int): Long {
        if (shape == GLFW.GLFW_ARROW_CURSOR)
            return arrowCursor

        if (currentShape != shape) {
            val cursor = GLFW.glfwCreateStandardCursor(shape)

            if (currentCursor != arrowCursor)
                GLFW.glfwDestroyCursor(currentCursor)

            currentCursor = cursor
            currentShape = shape
        }

        return currentCursor
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var inAnyBox = false

        if (Minecraft.getInstance().player == null) { // assume user is currently configuring in the config screen
            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)

            for (box in boxes) {
                box.render(guiGraphics, partialTick)
            }
        }

        if (this.children().none { it.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) })
            for (box in boxes) {
                if (mouseX >= box.x - 1 && mouseY >= box.y - 1 && mouseX <= box.x + box.width + 1 && mouseY <= box.y + box.height + 1) {
                    if (mouseX >= box.x - 1 && mouseX <= box.x + 1) {
                        if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                            guiGraphics.fill(box.x, box.y - 1, box.x + box.width, box.y + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR))
                        } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                            guiGraphics.fill(box.x, box.y + box.height - 1, box.x + box.width, box.y + box.height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NESW_CURSOR))
                        } else {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                        }

                        guiGraphics.fill(box.x - 1, box.y, box.x + 1, box.y + box.height, FastColor.ARGB32.color(255, 255, 255, 255))
                    } else if (mouseX >= box.x + box.width - 1 && mouseX <= box.x + box.width + 1) {
                        if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                            guiGraphics.fill(box.x, box.y - 1, box.x + box.width, box.y + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NESW_CURSOR))
                        } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                            guiGraphics.fill(box.x, box.y + box.height - 1, box.x + box.width, box.y + box.height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR))
                        } else {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                        }

                        guiGraphics.fill(box.x + box.width - 1, box.y, box.x + box.width + 1, box.y + box.height, FastColor.ARGB32.color(255, 255, 255, 255))
                    } else if (mouseY >= box.y - 1 && mouseY <= box.y + 1) {
                        guiGraphics.fill(box.x, box.y - 1, box.x + box.width, box.y + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_VRESIZE_CURSOR))
                    } else if (mouseY >= box.y + box.height - 1 && mouseY <= box.y + box.height + 1) {
                        guiGraphics.fill(box.x, box.y + box.height - 1, box.x + box.width, box.y + box.height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_VRESIZE_CURSOR))
                    } else {
                        guiGraphics.renderOutline(box.x, box.y, box.width, box.height, FastColor.ARGB32.color(255, 255, 255, 255))

                        val offset = 5
                        if (mouseX >= box.x + offset + 1 && mouseY >= box.y + offset + 1 && mouseX <= box.x + offset + 16 && mouseY <= box.y + offset + 16) {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, arrowCursor)
                            guiGraphics.fill(box.x + offset, box.y + offset, box.x + offset + 16, box.y + offset + 16, FastColor.ARGB32.color(95, 255, 0, 0))
                            guiGraphics.renderOutline(box.x + offset, box.y + offset, 16, 16, FastColor.ARGB32.color(95, 255, 255, 255))
                        } else {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_ALL_CURSOR))
                        }

                        guiGraphics.blit(CLOSE_BUTTON, box.x + offset, box.y + offset, 0f, 0f, 16, 16, 16, 16)
                    }

                    inAnyBox = true
                    break
                }
            }

        if (!inAnyBox) {
            GLFW.glfwSetCursor(this.minecraft!!.window.window, arrowCursor)
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick)

        UnityTranslateClient.renderCreditText(guiGraphics)
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (Minecraft.getInstance().player == null) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)

        if (result)
            return true

        for ((index, box) in boxes.toList().withIndex()) {
            if (mouseX >= box.x - 1 && mouseY >= box.y - 1 && mouseX <= box.x + box.width + 1 && mouseY <= box.y + box.height + 1) {
                val offset = 5
                if (mouseX >= box.x + offset + 1 && mouseY >= box.y + offset + 1 && mouseX <= box.x + offset + 16 && mouseY <= box.y + offset + 16) {
                    boxes.removeAt(index)

                    return true
                }

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

        return false
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
                    ctx.newWidth = Mth.clamp(ctx.newWidth, 12.0, this.minecraft!!.window.guiScaledWidth.toDouble())
                }

                ctx.box.x = ctx.newX.toInt()
                ctx.box.width = ctx.newWidth.toInt()
            }

            if (mode.contains(MoveMode.END_X) && !mode.contains(MoveMode.START_X)) {
                ctx.newWidth += mouseX - ctx.lastMouseX
                ctx.newWidth = Mth.clamp(ctx.newWidth, 12.0, this.minecraft!!.window.guiScaledWidth.toDouble())
                ctx.box.width = ctx.newWidth.toInt()
            }

            if (mode.contains(MoveMode.START_Y)) {
                ctx.newY += mouseY - ctx.lastMouseY
                ctx.newY = Mth.clamp(ctx.newY, 0.0, this.minecraft!!.window.guiScaledHeight.toDouble() - ctx.box.height)

                if (!mode.contains(MoveMode.END_Y)) {
                    ctx.newHeight -= mouseY - ctx.lastMouseY
                    ctx.newHeight = Mth.clamp(ctx.newHeight, 12.0, this.minecraft!!.window.guiScaledHeight.toDouble())
                }

                ctx.box.y = ctx.newY.toInt()
                ctx.box.height = ctx.newHeight.toInt()
            }

            if (mode.contains(MoveMode.END_Y) && !mode.contains(MoveMode.START_Y)) {
                ctx.newHeight += mouseY - ctx.lastMouseY
                ctx.newHeight = Mth.clamp(ctx.newHeight, 12.0, this.minecraft!!.window.guiScaledHeight.toDouble())
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