package xyz.bluspring.unitytranslate.translator

import net.minecraft.Util
import net.minecraft.util.RandomSource
import net.minecraft.util.random.WeightedRandomList
import org.lwjgl.system.APIUtil
import org.lwjgl.system.JNI
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.SharedLibrary
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque

object TranslatorManager {
    private var instances = ConcurrentLinkedDeque<LibreTranslateInstance>()

    fun queueTranslation(line: String, from: Language, to: Language): CompletableFuture<String> {
        return CompletableFuture.supplyAsync { translateLine(line, from, to) }
    }

    fun translateLine(line: String, from: Language, to: Language): String {
        val possible = instances.filter { it.supportsLanguage(from, to) }.sortedByDescending { it.weight.asInt() }

        if (possible.isEmpty()) {
            UnityTranslate.logger.warn("No available instances available for translating $from to $to!)")
            return line
        }

        val weighted = WeightedRandomList.create(possible)

        val selected = weighted.getRandom(RandomSource.create()).orElseThrow()

        val translated = selected.translate(line, from, to)

        if (translated != null)
            return translated

        for (instance in possible.filter { it != selected }.sortedBy { it.latency }) {
            return instance.translate(line, from, to) ?: continue
        }

        UnityTranslate.logger.warn("Failed to translate $line from $from to $to!")

        return line
    }

    private var isLibraryLoaded = false
    private lateinit var library: SharedLibrary
    private var PFN_cuInit: Long = 0L
    private var PFN_cuDeviceGetCount: Long = 0L
    private var PFN_cuDeviceComputeCapability: Long = 0L

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

                PFN_cuInit = APIUtil.apiGetFunctionAddress(library, "cuInit")
                PFN_cuDeviceGetCount = APIUtil.apiGetFunctionAddress(library, "cuDeviceGetCount")
                PFN_cuDeviceComputeCapability = APIUtil.apiGetFunctionAddress(library, "cuDeviceComputeCapability")

                if (PFN_cuInit == MemoryUtil.NULL || PFN_cuDeviceGetCount == MemoryUtil.NULL || PFN_cuDeviceComputeCapability == MemoryUtil.NULL) {
                    return false
                }
            } catch (e: Throwable) {
                UnityTranslate.logger.warn("An error occurred while searching for CUDA devices! You don't have to report this, don't worry.")
                e.printStackTrace()
                return false
            }

            isLibraryLoaded = true
        }

        val success = 0

        if (JNI.callI(0, PFN_cuInit) != success) {
            return false
        }

        val totalPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())
        if (JNI.callPI(totalPtr, PFN_cuDeviceGetCount) != success) {
            return false
        }

        val totalCudaDevices = MemoryUtil.memGetInt(totalPtr)
        if (totalCudaDevices <= 0)
            return false

        MemoryUtil.nmemFree(totalPtr)

        for (i in 0 until totalCudaDevices) {
            val minorPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())
            val majorPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())

            if (JNI.callPPI(majorPtr, minorPtr, PFN_cuDeviceComputeCapability) != success) {
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

    fun init() {
        loadFromConfig()
    }

    fun loadFromConfig() {
        val list = mutableListOf<LibreTranslateInstance>()

        for (server in UnityTranslate.config.server.offloadServers) {
            try {
                val instance = LibreTranslateInstance(server.url, server.weight)
                list.add(instance)
            } catch (e: Exception) {
                UnityTranslate.logger.error("Failed to load an offloaded server instance!")
                e.printStackTrace()
            }
        }

        instances = ConcurrentLinkedDeque(list)
    }
}