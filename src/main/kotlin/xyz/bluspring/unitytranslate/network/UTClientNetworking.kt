package xyz.bluspring.unitytranslate.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.PacketIds
import xyz.bluspring.unitytranslate.client.UnityTranslateClient.Companion.connectedServerHasSupport
import xyz.bluspring.unitytranslate.client.UnityTranslateClient.Companion.languageBoxes
import xyz.bluspring.unitytranslate.client.UnityTranslateClient.Companion.transcriber
import java.util.*

object UTClientNetworking {
    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SERVER_SUPPORT) { client, listener, buf, sender ->
            connectedServerHasSupport = true
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            client.execute {
                val buf = PacketByteBufs.create()
                buf.writeEnumSet(EnumSet.copyOf(languageBoxes.map { it.language }), Language::class.java)

                ClientPlayNetworking.send(PacketIds.SET_USED_LANGUAGES, buf)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            connectedServerHasSupport = false
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SEND_TRANSCRIPT) { client, listener, buf, sender ->
            val sourceId = buf.readUUID()
            val source = client.level!!.getPlayerByUUID(sourceId) ?: return@registerGlobalReceiver

            val sourceLanguage = buf.readEnum(Language::class.java)
            val index = buf.readVarInt()
            val updateTime = buf.readVarLong()

            val totalLanguages = buf.readVarInt()

            val boxes = languageBoxes

            for (i in 0 until totalLanguages) {
                val language = buf.readEnum(Language::class.java)
                val text = buf.readUtf()

                if (language == transcriber.language && sourceId == client.player?.uuid)
                    continue

                val box = boxes.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(PacketIds.MARK_INCOMPLETE) { client, listener, buf, sender ->
            val from = buf.readEnum(Language::class.java)
            val to = buf.readEnum(Language::class.java)
            val uuid = buf.readUUID()
            val index = buf.readVarInt()
            val isIncomplete = buf.readBoolean()

            val box = languageBoxes.firstOrNull { it.language == to } ?: return@registerGlobalReceiver
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }
    }
}