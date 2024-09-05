plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    val neoforge_1_21_01 = createNode("1.21.1-neoforge", 1_21_01, "srg")
    val fabric_1_21_01 = createNode("1.21.1-fabric", 1_21_01, "yarn")

    val neoforge_1_20_06 = createNode("1.20.6-neoforge", 1_20_06, "srg")
    val fabric_1_20_06 = createNode("1.20.6-fabric", 1_20_06, "yarn")

    val neoforge_1_20_04 = createNode("1.20.4-neoforge", 1_20_04, "srg")
    val fabric_1_20_04 = createNode("1.20.4-fabric", 1_20_04, "yarn")

    val forge_1_20_01 = createNode("1.20.1-forge", 1_20_01, "srg")
    val fabric_1_20_01 = createNode("1.20.1-fabric", 1_20_01, "yarn")

    neoforge_1_21_01.link(fabric_1_21_01)
    fabric_1_21_01.link(fabric_1_20_06)
    neoforge_1_20_06.link(fabric_1_20_06)
    fabric_1_20_06.link(fabric_1_20_04)
    neoforge_1_20_04.link(fabric_1_20_04)
    fabric_1_20_04.link(fabric_1_20_01)
    forge_1_20_01.link(fabric_1_20_01)
}