package xyz.bluspring.unitytranslate.translator

import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.common.LifecycleEvent
import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.util.HttpUtil
import net.minecraft.util.Mth
import oshi.SystemInfo
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.zip.ZipFile

class LocalLibreTranslateInstance private constructor(val process: Process, val port: Int) : LibreTranslateInstance("http://127.0.0.1:$port", 150) {
    init {
        info("Started local LibreTranslate instance on port $port.")

        LifecycleEvent.SERVER_STOPPING.register {
            process.destroy()
        }

        if (UnityTranslate.instance.proxy.isClient())
            registerEventsClient()
    }

    private fun registerEventsClient() {
        ClientLifecycleEvent.CLIENT_STOPPING.register {
            process.destroy()
        }
    }

    companion object {
        const val DOWNLOAD_URL = "https://nightly.link/BluSpring/LibreTranslate/workflows/build/main/{PLATFORM}%20Artifacts%20%28{TYPE}%29.zip?completed=true"
        private var lastPid = -1L
        var hasStarted = false
        var currentInstance: LocalLibreTranslateInstance? = null

        val libreTranslateDir = File(UnityTranslate.instance.proxy.gameDir.toFile(), ".unitytranslate")

        fun canRunLibreTranslate(): Boolean {
            val systemInfo = SystemInfo()

            return (Runtime.getRuntime().availableProcessors() >= 2 || TranslatorManager.supportsCuda) &&
                    // Require a minimum of 2 GiB free for LibreTranslate
                    ((systemInfo.hardware.memory.total - Runtime.getRuntime().maxMemory()) / 1048576L) >= 2048
        }

        // TODO: make translatable
        private fun warn(text: String) {
            if (UnityTranslate.instance.proxy.isClient()) {
                UnityTranslateClient.displayMessage(Component.literal(text), true)
            } else {
                UnityTranslate.logger.warn(text)
            }
        }

        private fun info(text: String) {
            if (UnityTranslate.instance.proxy.isClient()) {
                UnityTranslateClient.displayMessage(Component.literal(text), false)
            } else {
                UnityTranslate.logger.info(text)
            }
        }

        fun killOpenInstances() {
            if (lastPid == -1L)
                return

            ProcessHandle.of(lastPid)
                .ifPresent {
                    info("Detected LibreTranslate instance ${lastPid}, killing.")
                    it.destroyForcibly()

                    lastPid = -1L
                }

            currentInstance = null
        }

        private fun clearDeadDirectories() {
            val files = libreTranslateDir.listFiles()

            if (files != null) {
                for (file in files) {
                    if (!file.isDirectory)
                        continue

                    if (file.name.startsWith("_MEI")) {
                        if (!file.deleteRecursively()) {
                            warn("Failed to delete unused LibreTranslate directories, this may mean a dead LibreTranslate instance is running on your computer!")
                            warn("Please try to terminate any \"libretranslate.exe\" processes that you see running, then restart your game.")
                        }
                    }
                }
            }
        }

        fun launchLibreTranslate(source: File, consumer: Consumer<LibreTranslateInstance>) {
            val port = if (HttpUtil.isPortAvailable(5000)) 5000 else HttpUtil.getAvailablePort()

            if (lastPid != -1L) {
                killOpenInstances()
            }

            clearDeadDirectories()

            if (!source.canExecute()) {
                if (!source.setExecutable(true)) {
                    UnityTranslate.logger.error("Unable to start local LibreTranslate instance! You may have to manually set the execute permission on the file yourself!")
                    UnityTranslate.logger.error("File path: ${source.absolutePath}")
                    return
                }
            }

            val processBuilder = ProcessBuilder(listOf(
                source.absolutePath,
                "--update-models",
                "--port",
                "$port",
                "--threads",
                "${Mth.clamp(UnityTranslate.config.server.libreTranslateThreads, 1, Runtime.getRuntime().availableProcessors())}",
                "--disable-web-ui",
                "--disable-files-translation"
            ))

            processBuilder.directory(libreTranslateDir)

            val environment = processBuilder.environment()
            environment["PYTHONIOENCODING"] = "utf-8"
            environment["PYTHONLEGACYWINDOWSSTDIO"] = "utf-8"

            if (UnityTranslate.instance.proxy.isDev || System.getenv("unitytranslate.enableLogging") == "true") {
                processBuilder
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
            }

            val process = processBuilder.start()
            lastPid = process.pid()

            val timer = Timer()

            process.onExit()
                .whenCompleteAsync { process, e ->
                    e?.printStackTrace()

                    if (!hasStarted) {
                        timer.cancel()
                        warn("LibreTranslate appears to have exited with code ${process.exitValue()}, not proceeding with local translator instance.")
                    }

                    hasStarted = false
                }

            var attempts = 0
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        val instance = LocalLibreTranslateInstance(process, port)

                        currentInstance = instance
                        consumer.accept(instance)

                        timer.cancel()
                        hasStarted = true
                    } catch (_: Exception) {
                    }
                }
            }, 2500L, 2500L)
        }

        fun isLibreTranslateInstalled(): Boolean {
            val supportsCuda = TranslatorManager.supportsCuda
            val platform = Util.getPlatform()

            if (platform != Util.OS.WINDOWS && platform != Util.OS.OSX && platform != Util.OS.LINUX) {
                return false
            }

            val file = File(libreTranslateDir, "libretranslate/libretranslate${if (supportsCuda) "_cuda" else ""}${if (platform == Util.OS.WINDOWS) ".exe" else ""}")
            return file.exists()
        }

        fun installLibreTranslate(): CompletableFuture<File> {
            val supportsCuda = TranslatorManager.supportsCuda
            val platform = Util.getPlatform()

            if (platform != Util.OS.WINDOWS && platform != Util.OS.OSX && platform != Util.OS.LINUX) {
                throw IllegalStateException("Unsupported platform! (Detected platform: $platform)")
            }

            val file = File(libreTranslateDir, "libretranslate/libretranslate${if (supportsCuda) "_cuda" else ""}${if (platform == Util.OS.WINDOWS) ".exe" else ""}")
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()

            return CompletableFuture.supplyAsync {
                if (!file.exists()) {
                    if (file.parentFile.usableSpace <= 4L * 1024L * 1024L * 1024L) {
                        warn("Current drive doesn't have enough space for local LibreTranslate instance! Not installing LibreTranslate.")
                        throw IndexOutOfBoundsException()
                    }

                    info("Downloading LibreTranslate instance for platform ${platform.name} (CUDA: $supportsCuda)")

                    val download = URL(DOWNLOAD_URL
                        .replace("{PLATFORM}", when (platform) {
                            Util.OS.WINDOWS -> "Windows"
                            Util.OS.OSX -> "MacOS"
                            Util.OS.LINUX -> "Linux"
                            else -> ""
                        })
                        .replace("{TYPE}", if (supportsCuda) "CUDA" else "CPU")
                    )

                    val archive = File(file.parentFile.parentFile, "LibreTranslate_temp.zip")

                    if (archive.exists()) {
                        if (file.parentFile.exists()) {
                            file.parentFile.delete()
                        }

                        archive.delete()
                    }

                    archive.deleteOnExit()
                    val fileStream = archive.outputStream()
                    val downloadStream = download.openStream()
                    downloadStream.transferTo(fileStream)

                    fileStream.close()
                    downloadStream.close()

                    info("Extracting LibreTranslate instance...")

                    val zip = ZipFile(archive)
                    for (entry in zip.entries()) {
                        val extracted = File(file.parentFile, entry.name)
                        if (entry.isDirectory)
                            extracted.mkdirs()
                        else {
                            extracted.parentFile.mkdirs()

                            val stream = zip.getInputStream(entry)
                            val extractStream = extracted.outputStream()

                            stream.transferTo(extractStream)
                            extractStream.close()
                            stream.close()
                        }
                    }

                    zip.close()

                    info("Deleting temporary file...")
                    archive.delete()

                    info("LibreTranslate instance successfully installed!")
                }

                file
            }
        }
    }
}