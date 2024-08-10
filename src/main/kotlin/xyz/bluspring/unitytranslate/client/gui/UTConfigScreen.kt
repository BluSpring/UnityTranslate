package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.config.UnityTranslateConfig
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

                        addRenderableWidget(EditBox(font, this.width / 2 - Button.DEFAULT_WIDTH - 25, y - (Button.DEFAULT_HEIGHT / 2) + 3, Button.DEFAULT_WIDTH + 20, Button.DEFAULT_HEIGHT, Component.translatable("unitytranslate.value.none")).apply {
                            this.tooltip = Tooltip.create(Component.translatable("config.unitytranslate.$type.${member.name}.website_url.desc"))
                            this.value = server.url
                            this.setResponder {
                                server.url = it
                            }
                        })

                        addRenderableWidget(EditBox(font, this.width / 2 + 5, y - (Button.DEFAULT_HEIGHT / 2) + 3, Button.DEFAULT_WIDTH + 20, Button.DEFAULT_HEIGHT, Component.translatable("unitytranslate.value.none")).apply {
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
                    }
                }

                y += 30
            }

            addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE) {
                    this.onClose()
                }
                    .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - 15)
                    .build()
            )
        }

        private fun String.propercase(): String {
            return "${this[0].uppercaseChar()}${this.lowercase().substring(1)}"
        }

        override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            this.renderBackground(guiGraphics)

            guiGraphics.fill(0, 50, this.width, this.height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215)

            super.render(guiGraphics, mouseX, mouseY, partialTick)
        }

        override fun onClose() {
            Minecraft.getInstance().setScreen(this@UTConfigScreen)
            UnityTranslate.saveConfig()
        }
    }
}