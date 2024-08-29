package xyz.bluspring.unitytranslate.duck

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
}