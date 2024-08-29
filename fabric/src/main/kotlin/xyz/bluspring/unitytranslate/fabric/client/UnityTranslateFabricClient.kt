package xyz.bluspring.unitytranslate.fabric.client

import net.fabricmc.api.ClientModInitializer
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.fabric.UnityTranslateFabric

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        UnityTranslateClient()
        UnityTranslateClient.registerKeys()
    }
}