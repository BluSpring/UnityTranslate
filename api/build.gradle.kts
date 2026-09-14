plugins {
    java
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

setupCommonUnmodded("api", javaVersion = 17)

val stubs by sourceSets.creating

dependencies {
    api(libs.bundles.kotlin)
    api(libs.slf4j.api.get())
    api(libs.datafixerupper.get())
    "stubsApi"(libs.datafixerupper.get())
    api(libs.unitytranslatelib.get())
    api(libs.joml)
    api(libs.bundles.arc3d.api)

    compileOnly(stubs.output)
}
