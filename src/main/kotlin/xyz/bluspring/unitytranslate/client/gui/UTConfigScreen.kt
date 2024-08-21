package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.*
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.duck.ScrollableWidget
import xyz.bluspring.unitytranslate.mixin.AbstractWidgetAccessor
import kotlin.math.absoluteValue
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMembers

class UTConfigScreen(private val parent: Screen?) : Screen(Component.literal("UnityTranslate")) {
    override fun init() {
        val width = (this.width / 4).coerceAtLeast(250)

        addRenderableWidget(
            Button.builder(Component.translatable("gui.unitytranslate.config.client")) {
                Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.config.client::class, UnityTranslate.config.client, "client"))
            }
                .pos(this.width / 2 - (width / 2), 75)
                .size(width, Button.DEFAULT_HEIGHT)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.translatable("gui.unitytranslate.config.common")) {
                Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.config.server::class, UnityTranslate.config.server, "common"))
            }
                .pos(this.width / 2 - (width / 2), 75 + Button.DEFAULT_HEIGHT + 5)
                .size(width, Button.DEFAULT_HEIGHT)
                .build()
        )

        addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                this.onClose()
            }
                .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - 15)
                .build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(guiGraphics)

        guiGraphics.fill(0, 50, this.width, this.height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215)

        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }

    inner class UTConfigSubScreen<T : Any>(val configClass: KClass<out T>, val instance: T, val type: String) : Screen(Component.translatable("gui.unitytranslate.config.$type")) {
        lateinit var doneButton: Button
        private var scrollAmount = 0.0
        private var scrolling = false

        val scrollbarPosition: Int
            get() {
                return this.width - 3
            }

        var maxPosition = 0
        val maxScroll: Int
            get() {
                return (this.maxPosition - ((this.height - 50) - 50 - 4)).coerceAtLeast(0)
            }

        override fun init() {
            var y = 75

            for (member in configClass.declaredMembers) {
                if (member.name == "component1")
                    break

                if (member !is KMutableProperty<*> || member.visibility != KVisibility.PUBLIC)
                    continue

                val name = StringWidget(Component.translatable("config.unitytranslate.$type.${member.name}"), font)
                name.x = 35
                name.y = y
                name.alignLeft()
                name.tooltip = Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.desc"))
                (name as ScrollableWidget).updateInitialPosition()

                addRenderableWidget(name)

                val value = member.getter.call(instance)

                if (value is Boolean) {
                    var current: Boolean = value

                    val button = Button.builder(Component.translatable("unitytranslate.value.$current")
                        .withStyle(if (current) ChatFormatting.GREEN else ChatFormatting.RED)
                    ) { btn ->
                        current = !current
                        member.setter.call(instance, current)

                        btn.message = Component.translatable("unitytranslate.value.$current")
                            .withStyle(if (current) ChatFormatting.GREEN else ChatFormatting.RED)
                    }
                        .pos(this.width - Button.SMALL_WIDTH - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                        .width(Button.SMALL_WIDTH)
                        .tooltip(Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.desc")))
                        .build()

                    addRenderableWidget(button)
                } else if (value is Enum<*>) {
                    val enumClass = value::class.java as Class<Enum<*>>
                    val valueOf = enumClass.getDeclaredMethod("values")
                    val values = valueOf.invoke(null) as Array<Enum<*>>
                    var current = value.ordinal

                    val button = Button.builder(Component.literal(values[current].name.propercase())) { btn ->
                        if (hasShiftDown()) {
                            current -= 1
                            if (current < 0)
                                current = values.size - 1
                        } else {
                            current += 1
                            if (current >= values.size)
                                current = 0
                        }

                        btn.message = Component.literal(values[current].name.propercase())
                    }
                        .pos(this.width - Button.SMALL_WIDTH - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                        .width(Button.SMALL_WIDTH)
                        .tooltip(Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.desc")))
                        .build()

                    addRenderableWidget(button)
                } else if (value is MutableList<*> && member.name == "offloadServers") { // special case
                    val actualValue = value as MutableList<UnityTranslateConfig.OffloadedLibreTranslateServer>

                    addRenderableWidget(Button.builder(Component.literal("+")) {
                        actualValue.add(UnityTranslateConfig.OffloadedLibreTranslateServer(""))
                        this.rebuildWidgets()
                    }
                        .pos(this.width - Button.DEFAULT_HEIGHT - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                        .width(Button.DEFAULT_HEIGHT)
                        .build())

                    for ((index, server) in actualValue.withIndex()) {
                        y += 30

                        val boxWidth = (Button.DEFAULT_WIDTH + 20).coerceAtMost(this.width / 3)
                        addRenderableWidget(EditBox(font, this.width / 2 - boxWidth - 5, y - (Button.DEFAULT_HEIGHT / 2) + 3, boxWidth, Button.DEFAULT_HEIGHT, Component.translatable("unitytranslate.value.none")).apply {
                            this.tooltip = Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.website_url.desc"))
                            this.value = server.url
                            this.setResponder {
                                server.url = it
                            }
                        })

                        addRenderableWidget(EditBox(font, this.width / 2 + 5, y - (Button.DEFAULT_HEIGHT / 2) + 3, boxWidth, Button.DEFAULT_HEIGHT, Component.translatable("unitytranslate.value.none")).apply {
                            this.tooltip = Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.api_key.desc"))
                            this.value = server.authKey ?: ""
                            this.setResponder {
                                server.authKey = it
                            }
                        })

                        addRenderableWidget(Button.builder(Component.literal("-")) {
                            actualValue.removeAt(index)
                            this.rebuildWidgets()
                        }
                            .pos(this.width - Button.DEFAULT_HEIGHT - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                            .width(Button.DEFAULT_HEIGHT)
                            .build())

                        addArrows(this@UTConfigSubScreen.width - 17, y, index, actualValue)
                    }
                } else if (value is MutableList<*> && member.name == "translatePriority") { // special case
                    val actualValue = value as MutableList<UnityTranslateConfig.TranslationPriority>

                    for ((index, priority) in actualValue.withIndex()) {
                        y += 30

                        val text = Component.translatable("config.unitytranslate.$type.${member.name}.${priority.name.lowercase()}")
                        val priorityName = StringWidget(text, font)
                        priorityName.x = this.width / 2 - (font.width(text) / 2)
                        priorityName.y = y
                        priorityName.alignCenter()
                        priorityName.tooltip = Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.${priority.name.lowercase()}.desc"))
                        (priorityName as ScrollableWidget).updateInitialPosition()

                        addRenderableWidget(priorityName)
                        addArrows((this.width / 2 + 50).coerceAtMost(this.width - 10), y, index, actualValue)
                    }
                } else if (value is Float) {
                    addRenderableWidget(EditBox(font, this.width - Button.SMALL_WIDTH - 20, y - (Button.DEFAULT_HEIGHT / 2) + 4, Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT, Component.empty())
                        .apply {
                            this.tooltip = Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.desc"))
                            this.value = value.toString()
                            this.setFilter { it.toFloatOrNull() != null || it.isBlank() || it.contains('.') } // TODO: make adjustable via annotation
                            this.setResponder {
                                member.setter.call(instance, Mth.clamp((
                                        if (it.startsWith('.'))
                                            "0$it"
                                        else if (it.endsWith('.'))
                                            "${it}0"
                                        else
                                            it
                                        ).toFloatOrNull() ?: 0.0f, 0.5f, 5.0f
                                ))
                            }
                        })

                    addRenderableWidget(TextAndImageButton.builder(Component.empty(), UnityTranslate.id("textures/gui/arrow_up.png")) {
                        member.setter.call(instance, Mth.clamp(value + 0.1f, 0.5f, 5.0f))
                        this.rebuildWidgets()
                    }
                        .offset(0, 2)
                        .texStart(0, 0)
                        .textureSize(8, 8)
                        .usedTextureSize(8, 8)
                        .build()
                        .apply {
                            this.x = this@UTConfigSubScreen.width - 18
                            this.y = y - 8
                            this.width = 12
                            (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                            (this as ScrollableWidget).updateInitialPosition()

                            if (value >= 5.0) {
                                this.active = false
                            }
                        }
                    )

                    addRenderableWidget(TextAndImageButton.builder(Component.empty(), UnityTranslate.id("textures/gui/arrow_down.png")) {
                        member.setter.call(instance, Mth.clamp(value - 0.1f, 0.5f, 5.0f))
                        this.rebuildWidgets()
                    }
                        .offset(0, 2)
                        .texStart(0, 0)
                        .textureSize(8, 8)
                        .usedTextureSize(8, 8)
                        .build()
                        .apply {
                            this.x = this@UTConfigSubScreen.width - 18
                            this.y = y + 4
                            this.width = 12
                            (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                            (this as ScrollableWidget).updateInitialPosition()

                            if (value <= 0.5) {
                                this.active = false
                            }
                        }
                    )
                }

                y += 30
            }

            maxPosition = y + 20

            doneButton = addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE) {
                    this.onClose()
                }
                    .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - 15)
                    .build()
            )
        }

        private fun <T> addArrows(x: Int, y: Int, index: Int, actualValue: MutableList<T>) {
            addRenderableWidget(TextAndImageButton.builder(Component.empty(), UnityTranslate.id("textures/gui/arrow_up.png")) {
                val oldValue = actualValue[index]
                val oldPrevValue = actualValue[index - 1]
                actualValue[index - 1] = oldValue
                actualValue[index] = oldPrevValue
                this.rebuildWidgets()
            }
                .offset(0, 2)
                .texStart(0, 0)
                .textureSize(8, 8)
                .usedTextureSize(8, 8)
                .build()
                .apply {
                    this.x = x
                    this.y = y - 8
                    this.width = 12
                    (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                    (this as ScrollableWidget).updateInitialPosition()
                    this.active = index > 0
                }
            )

            addRenderableWidget(TextAndImageButton.builder(Component.empty(), UnityTranslate.id("textures/gui/arrow_down.png")) {
                val oldValue = actualValue[index]
                val oldPrevValue = actualValue[index + 1]
                actualValue[index + 1] = oldValue
                actualValue[index] = oldPrevValue
                this.rebuildWidgets()
            }
                .offset(0, 2)
                .texStart(0, 0)
                .textureSize(8, 8)
                .usedTextureSize(8, 8)
                .build()
                .apply {
                    this.x = x
                    this.y = y + 4
                    this.width = 12
                    (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                    (this as ScrollableWidget).updateInitialPosition()
                    this.active = index < actualValue.size - 1
                }
            )
        }

        private fun String.propercase(): String {
            return "${this[0].uppercaseChar()}${this.lowercase().substring(1)}"
        }

        override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            this.renderBackground(guiGraphics)

            guiGraphics.fill(0, 50, this.width, this.height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215)

            guiGraphics.enableScissor(0, 50, this.width, this.height - 50)
            super.render(guiGraphics, mouseX, mouseY, partialTick)
            guiGraphics.disableScissor()

            if (maxScroll.absoluteValue > 0) {
                var scrollbarHeight = ((this.height - 50 - 50) * (this.height - 50 - 50)) / this.maxPosition
                scrollbarHeight = Mth.clamp(scrollbarHeight, 32, this.height - 50 - 50 - 8)

                var scrollbarPosY = (this.scrollAmount * (this.height - 50 - 50 - scrollbarHeight) / maxScroll + 50).toInt()
                if (scrollbarPosY < 50) {
                    scrollbarPosY = 50
                }

                guiGraphics.fill(this.scrollbarPosition, 50, scrollbarPosition + 2, this.height - 50, -16777216)
                guiGraphics.fill(this.scrollbarPosition, scrollbarPosY, scrollbarPosition + 2, scrollbarPosY + scrollbarHeight, -8355712)
                guiGraphics.fill(this.scrollbarPosition, scrollbarPosY, scrollbarPosition + 2 - 1, scrollbarPosY + scrollbarHeight - 1, -4144960)
            }

            doneButton.render(guiGraphics, mouseX, mouseY, partialTick)
        }

        fun updateScroll() {
            for (child in this.children()) {
                if (child == doneButton)
                    continue

                if (child is AbstractWidget) {
                    child.y = (child as ScrollableWidget).initialY - scrollAmount.toInt()
                }
            }
        }

        override fun resize(minecraft: Minecraft, width: Int, height: Int) {
            super.resize(minecraft, width, height)

            if (this.scrollAmount > this.maxScroll)
                this.scrollAmount = this.maxScroll.toDouble()

            updateScroll()
        }

        override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
            if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true
            } else if (button == 0 && scrolling) {
                if (mouseY < 50) {
                    this.scrollAmount = 0.0
                } else if (mouseY > this.height - 50) {
                    this.scrollAmount = this.maxScroll.toDouble()
                } else {
                    val max = this.maxScroll
                    val height = (this.height - 50) - 50
                    val scrollHeight = Mth.clamp(((height * height).toFloat() / this.maxPosition.toFloat()).toInt(), 32, height - 8)
                    val scrollDelta = (max / (height - scrollHeight).toDouble()).coerceAtMost(1.0)
                    this.scrollAmount = Mth.clamp(this.scrollAmount + dragY * scrollDelta, 0.0, this.maxScroll.toDouble())
                }

                updateScroll()
                return true
            }

            return false
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true
            }

            this.scrolling = button == 0 && mouseX >= this.scrollbarPosition && mouseX < this.scrollbarPosition + 6

            if (mouseY > 50 && mouseY <= this.height - 50) {
                return this.scrolling
            }

            return false
        }

        override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
            this.scrollAmount = Mth.clamp(this.scrollAmount - delta * (this.maxPosition / 4.0), 0.0, this.maxScroll.toDouble())
            updateScroll()
            return true
        }

        override fun onClose() {
            Minecraft.getInstance().setScreen(this@UTConfigScreen)
            UnityTranslate.saveConfig()
        }
    }
}