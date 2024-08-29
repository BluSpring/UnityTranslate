package xyz.bluspring.unitytranslate.translator

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import net.fabricmc.api.EnvType
import net.minecraft.Util
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import org.lwjgl.system.APIUtil
import org.lwjgl.system.JNI
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.SharedLibrary
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketIds
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

object TranslatorManager {
    private var timer: Timer = Timer("UnityTranslate Batch Translate Manager")
    internal val queuedTranslations = ConcurrentLinkedQueue<Translation>()

    private val MULTI_ASTERISK_REGEX = Regex("\\*+")

    var instances = ConcurrentLinkedDeque<LibreTranslateInstance>()
        private set

    fun queueTranslation(line: String, from: Language, to: Language, player: Player, index: Int): CompletableFuture<String> {
        return CompletableFuture<String>().apply {
            val id = "${player.stringUUID}-$index"

            for (previous in queuedTranslations.filter { it.id == id && it.fromLang == from && it.toLang == to }) {
                previous.future.completeExceptionally(Exception("Overridden"))
                queuedTranslations.remove(previous)
            }

            queuedTranslations.add(Translation(
                id,
                line, from, to,
                System.currentTimeMillis(),
                this,
                player, index
            ))
        }
    }

    fun translateLine(line: String, from: Language, to: Language): String? {
        val possible = instances.filter { it.supportsLanguage(from, to) }.sortedByDescending { it.weight.asInt() }

        if (possible.isEmpty()) {
            UnityTranslate.logger.warn("No available instances available for translating $from to $to!)")
            return line
        }

        var index = 0

        for (instance in possible) {
            if (instance.currentlyTranslating >= LibreTranslateInstance.MAX_CONCURRENT_TRANSLATIONS && index++ < possible.size - 1)
                continue

            instance.currentlyTranslating++
            val translated = instance.translate(line, from, to)
            instance.currentlyTranslating--

            if (translated == null) {
                continue
            }

            return translated.replace(MULTI_ASTERISK_REGEX, "**")
        }

        UnityTranslate.logger.warn("Failed to translate $line from $from to $to!")

        return null
    }

    fun batchTranslateLines(lines: List<String>, from: Language, to: Language): List<String>? {
        val possible = instances.filter { it.supportsLanguage(from, to) }.sortedByDescending { it.weight.asInt() }

        if (possible.isEmpty()) {
            UnityTranslate.logger.warn("No available instances available for translating $from to $to!)")
            return lines
        }

        var index = 0

        for (instance in possible) {
            if (instance.currentlyTranslating >= LibreTranslateInstance.MAX_CONCURRENT_TRANSLATIONS && index++ < possible.size - 1)
                continue

            instance.currentlyTranslating++
            val translated = instance.batchTranslate(lines, from, to)
            instance.currentlyTranslating--

            if (translated == null) {
                continue
            }

            return translated.map { it.replace(MULTI_ASTERISK_REGEX, "**") }
        }

        UnityTranslate.logger.warn("Failed to translate lines from $from to $to:")
        for (line in lines) {
            UnityTranslate.logger.warn(" - $line")
        }

        return null
    }

    private var isLibraryLoaded = false
    private lateinit var library: SharedLibrary
    private var PFN_cuInit: Long = 0L
    private var PFN_cuDeviceGetCount: Long = 0L
    private var PFN_cuDeviceComputeCapability: Long = 0L

    private var PFN_cuGetErrorName: Long = 0L
    private var PFN_cuGetErrorString: Long = 0L

    private fun logCudaError(code: Int, at: String) {
        if (code == 0)
            return

        // TODO: these return ??? for some reason.
        //       can we figure out why?

        val errorCode = if (PFN_cuGetErrorName != MemoryUtil.NULL) {
            val ptr = MemoryUtil.nmemAlloc(255)
            JNI.callPP(code, ptr, PFN_cuGetErrorName)
            MemoryUtil.memUTF16(ptr).apply {
                MemoryUtil.nmemFree(ptr)
            }
        } else "[CUDA ERROR NAME NOT FOUND]"

        val errorDesc = if (PFN_cuGetErrorString != MemoryUtil.NULL) {
            val ptr = MemoryUtil.nmemAlloc(255)
            JNI.callPP(code, ptr, PFN_cuGetErrorString)
            MemoryUtil.memUTF16(ptr).apply {
                MemoryUtil.nmemFree(ptr)
            }
        } else "[CUDA ERROR DESC NOT FOUND]"

        UnityTranslate.logger.error("CUDA error at $at: $code $errorCode ($errorDesc)")
    }

    private fun isCudaSupported(): Boolean {
        if (!UnityTranslate.config.server.shouldUseCuda) {
            UnityTranslate.logger.info("CUDA is disabled in the config, not enabling CUDA support.")
            return false
        }

        if (!isLibraryLoaded) {
            try {
                library = if (Util.getPlatform() == Util.OS.WINDOWS) {
                    APIUtil.apiCreateLibrary("nvcuda.dll")
                } else if (Util.getPlatform() == Util.OS.LINUX) {
                    APIUtil.apiCreateLibrary("libcuda.so")
                } else {
                    return false
                }

                PFN_cuInit = library.getFunctionAddress("cuInit")
                PFN_cuDeviceGetCount = library.getFunctionAddress("cuDeviceGetCount")
                PFN_cuDeviceComputeCapability = library.getFunctionAddress("cuDeviceComputeCapability")
                PFN_cuGetErrorName = library.getFunctionAddress("cuGetErrorName")
                PFN_cuGetErrorString = library.getFunctionAddress("cuGetErrorString")

                if (PFN_cuInit == MemoryUtil.NULL || PFN_cuDeviceGetCount == MemoryUtil.NULL || PFN_cuDeviceComputeCapability == MemoryUtil.NULL) {
                    // TODO: remove in prod
                    UnityTranslate.logger.info("CUDA results: $PFN_cuInit $PFN_cuDeviceGetCount $PFN_cuDeviceComputeCapability")
                    return false
                }
            } catch (_: UnsatisfiedLinkError) {
                UnityTranslate.logger.warn("CUDA library failed to load! Not attempting to initialize CUDA functions.")
                return false
            } catch (e: Throwable) {
                UnityTranslate.logger.warn("An error occurred while searching for CUDA devices! You don't have to report this, don't worry.")
                e.printStackTrace()
                return false
            }

            isLibraryLoaded = true
        }

        val success = 0

        if (JNI.callI(0, PFN_cuInit).apply {
                logCudaError(this, "init")
            } != success) {
            return false
        }

        val totalPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())
        if (JNI.callPI(totalPtr, PFN_cuDeviceGetCount).apply {
                logCudaError(this, "get device count")
            } != success) {
            return false
        }

        val totalCudaDevices = MemoryUtil.memGetInt(totalPtr)
        UnityTranslate.logger.info("Total CUDA devices: $totalCudaDevices")
        if (totalCudaDevices <= 0) {
            return false
        }

        MemoryUtil.nmemFree(totalPtr)

        for (i in 0 until totalCudaDevices) {
            val minorPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())
            val majorPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())

            if (JNI.callPPI(majorPtr, minorPtr, i, PFN_cuDeviceComputeCapability).apply {
                    logCudaError(this, "get device compute capability $i")
                } != success) {
                continue
            }

            val majorVersion = MemoryUtil.memGetInt(majorPtr)
            val minorVersion = MemoryUtil.memGetInt(minorPtr)

            MemoryUtil.nmemFree(majorPtr)
            MemoryUtil.nmemFree(minorPtr)

            UnityTranslate.logger.info("Found device with CUDA compute capability major $majorVersion minor $minorVersion.")

            return true
        }

        return false
    }

    val supportsCuda = isCudaSupported().apply {
        if (this)
            UnityTranslate.logger.info("CUDA is supported, using GPU for translations.")
        else
            UnityTranslate.logger.info("CUDA is not supported, using CPU for translations.")
    }

    fun installLibreTranslate() {
        LocalLibreTranslateInstance.installLibreTranslate().thenApplyAsync {
            try {
                LocalLibreTranslateInstance.launchLibreTranslate(it, instances::add)
            } catch (e: Throwable) {
                UnityTranslate.logger.error("Failed to launch local LibreTranslate instance!")
                e.printStackTrace()
            }
        }
    }

    fun init() {
        loadFromConfig()

        LifecycleEvent.SERVER_STARTING.register {
            if (UnityTranslate.config.server.shouldRunTranslationServer && LocalLibreTranslateInstance.canRunLibreTranslate()) {
                if (UnityTranslate.instance.proxy.isClient() && !LocalLibreTranslateInstance.isLibreTranslateInstalled()) {
                    UnityTranslateClient.openDownloadRequest()
                } else {
                    installLibreTranslate()
                }
            }
        }

        LifecycleEvent.SERVER_STOPPING.register {
            timer.cancel()
        }

        PlayerEvent.PLAYER_QUIT.register { player ->
            queuedTranslations.removeIf { it.player.uuid == player.uuid }
        }
    }

    fun loadFromConfig() {
        val list = mutableListOf<LibreTranslateInstance>()

        timer.cancel()
        timer = Timer("UnityTranslate Batch Translate Manager")

        for (server in UnityTranslate.config.server.offloadServers) {
            try {
                val instance = LibreTranslateInstance(server.url, server.weight, server.authKey)
                list.add(0, instance)
            } catch (e: Exception) {
                UnityTranslate.logger.error("Failed to load an offloaded server instance!")
                e.printStackTrace()
            }
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val queueLater = ConcurrentLinkedQueue<Translation>()
                val toTranslate = mutableMapOf<Pair<Language, Language>, MutableList<Translation>>()

                while (queuedTranslations.isNotEmpty()) {
                    val translation = queuedTranslations.remove()

                    toTranslate.computeIfAbsent(translation.fromLang to translation.toLang) { mutableListOf() }
                        .add(translation)
                }

                toTranslate.forEach { (from, to), allTranslations ->
                    val translations = allTranslations.filter {
                        if (it.player is ServerPlayer)
                            !it.player.hasDisconnected()
                        else true
                    }

                    CompletableFuture.supplyAsync {
                        batchTranslateLines(translations.map { it.text }, from, to)
                    }
                        .whenCompleteAsync { t, u ->
                            translations.forEachIndexed { i, translation ->
                                if (t != null && u == null) {
                                    broadcastIncomplete(false, translation)
                                    translation.future.completeAsync { t[i] }
                                } else {
                                    if (translation.player is ServerPlayer) {
                                        broadcastIncomplete(true, translation)
                                    }

                                    translation.attempts++
                                    queueLater.add(translation)
                                }
                            }
                        }
                }

                for (translation in queueLater) {
                    if (queuedTranslations.any { it.id == translation.id && it.queueTime > translation.queueTime } || translation.attempts > 3)
                        continue

                    queuedTranslations.add(translation)
                }
            }
        }, 0L, (UnityTranslate.config.server.batchTranslateInterval * 1000.0).toLong())

        instances = ConcurrentLinkedDeque(list)
    }

    private fun broadcastIncomplete(isIncomplete: Boolean, translation: Translation) {
        if (translation.player !is ServerPlayer)
            return

        val buf = UnityTranslate.instance.proxy.createByteBuf()
        buf.writeEnum(translation.fromLang)
        buf.writeEnum(translation.toLang)
        buf.writeUUID(translation.player.uuid)
        buf.writeVarInt(translation.index)
        buf.writeBoolean(isIncomplete)

        val source = translation.player

        if (UnityTranslate.hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                UnityTranslate.instance.proxy.sendPacketServer(player, PacketIds.MARK_INCOMPLETE, buf)
            }
        } else {
            UnityTranslate.instance.proxy.sendPacketServer(source, PacketIds.MARK_INCOMPLETE, buf)
        }
    }
}