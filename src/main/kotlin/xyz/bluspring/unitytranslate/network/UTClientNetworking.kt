package xyz.bluspring.unitytranslate.network

//#if MC >= 1.20.6
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
//#endif
//#if MC >= 1.20.6
//$$ import xyz.bluspring.unitytranslate.network.payloads.SetCurrentLanguagePayload
//$$ import xyz.bluspring.unitytranslate.network.payloads.SetUsedLanguagesPayload
//#endif
import dev.architectury.event.events.client.ClientPlayerEvent
import dev.architectury.networking.NetworkManager
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import xyz.bluspring.unitytranslate.transcript.Transcript
import java.util.*

object UTClientNetworking {
    fun init() {
        val proxy = UnityTranslate.instance.proxy

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.SERVER_SUPPORT) { buf, ctx ->
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.SERVER_SUPPORT) { buf, ctx ->
        //#endif
            UnityTranslateClient.connectedServerHasSupport = true
        }

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register { player ->
            Minecraft.getInstance().execute {
                //#if MC >= 1.20.6
                //$$ proxy.sendPacketClient(SetUsedLanguagesPayload(UnityTranslateClient.languageBoxes.map { it.language }))
                //#else
                val buf = proxy.createByteBuf()
                buf.writeEnumSet(EnumSet.copyOf(UnityTranslateClient.languageBoxes.map { it.language }), Language::class.java)

                proxy.sendPacketClient(PacketIds.SET_USED_LANGUAGES, buf)
                //#endif
            }

            Minecraft.getInstance().execute {
                //#if MC >= 1.20.6
                //$$ proxy.sendPacketClient(SetCurrentLanguagePayload(UnityTranslate.config.client.language))
                //#else
                val buf = proxy.createByteBuf()
                buf.writeEnum(UnityTranslate.config.client.language)

                proxy.sendPacketClient(PacketIds.SET_CURRENT_LANGUAGE, buf)
                //#endif
            }
        }

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register { _ ->
            UnityTranslateClient.connectedServerHasSupport = false
        }

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.SEND_TRANSCRIPT_TO_CLIENT) { buf, ctx ->
        //$$    val sourceId = buf.uuid
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.SEND_TRANSCRIPT) { buf, ctx ->
            val sourceId = buf.readUUID()
        //#endif
            val source = ctx.player.level().getPlayerByUUID(sourceId) ?: return@registerReceiver

            //#if MC >= 1.20.6
            //$$ val sourceLanguage = buf.language
            //$$ val index = buf.index
            //$$ val updateTime = buf.updateTime
            //#else
            val sourceLanguage = buf.readEnum(Language::class.java)
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()

            val totalLanguages = buf.readVarInt()
            //#endif

            val boxes = UnityTranslateClient.languageBoxes

            //#if MC >= 1.20.6
            //$$ for ((language, text) in buf.toSend) {
            //#else
            for (i in 0 until totalLanguages) {
                val language = buf.readEnum(Language::class.java)
                val text = buf.readUtf()
            //#endif

                if (language == UnityTranslateClient.transcriber.language && sourceId == ctx.player?.uuid)
                    continue

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)

                if (box == null && UTVoiceChatCompat.isPlayerAudible(ctx.player)) {
                    TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, source, text, language, updateTime, false), language)
                }
            }
        }

        //#if MC >= 1.20.6
        //$$ registerReceiver(PacketIds.MARK_INCOMPLETE) { buf, ctx ->
        //$$    val from = buf.from
        //$$    val to = buf.to
        //$$    val uuid = buf.uuid
        //$$    val index = buf.index
        //$$    val isIncomplete = buf.isIncomplete
        //#else
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketIds.MARK_INCOMPLETE) { buf, ctx ->
            val from = buf.readEnum(Language::class.java)
            val to = buf.readEnum(Language::class.java)
            val uuid = buf.readUUID()
            val index = buf.readVarInt()
            val isIncomplete = buf.readBoolean()
        //#endif

            val box = UnityTranslateClient.languageBoxes.firstOrNull { it.language == to } ?: return@registerReceiver
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }
    }

    //#if MC >= 1.20.6
    //$$ private fun <T : CustomPacketPayload> registerReceiver(type: TypeAndCodec<RegistryFriendlyByteBuf, T>, receiver: NetworkManager.NetworkReceiver<T>) {
    //$$    NetworkManager.registerReceiver(NetworkManager.Side.S2C, type.type, type.codec, receiver)
    //$$ }
    //#endif
}