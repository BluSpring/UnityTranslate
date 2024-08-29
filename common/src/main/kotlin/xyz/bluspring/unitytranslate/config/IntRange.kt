package xyz.bluspring.unitytranslate.config

@Retention(AnnotationRetention.RUNTIME)
annotation class IntRange(
    val from: Int,
    val to: Int,
    val increment: Int
)
