package xyz.bluspring.unitytranslate.duck

import net.minecraft.network.chat.Component

interface ScrollableWidget {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("unityTranslate\$getInitialX")
    val initialX: Int

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("unityTranslate\$getInitialY")
    val initialY: Int

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("unityTranslate\$updateInitialPosition")
    fun updateInitialPosition()

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("unityTranslate\$getTooltip")
    @set:JvmName("unityTranslate\$setTooltip")
    var tooltip: Component
}