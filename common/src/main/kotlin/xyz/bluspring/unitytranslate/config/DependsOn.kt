package xyz.bluspring.unitytranslate.config

@Retention(AnnotationRetention.RUNTIME)
annotation class DependsOn(
    val configName: String
)
