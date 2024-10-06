package xyz.bluspring.unitytranslate.util

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.ForkJoinWorkerThread

// i think this might be the longest class name i've ever written.
// For context as to why this is needed - https://stackoverflow.com/a/57551188
// and why this? because Forge. that's why.
// it's always Forge.
class ClassLoaderProviderForkJoinWorkerThreadFactory(private val classLoader: ClassLoader) : ForkJoinWorkerThreadFactory {
    override fun newThread(pool: ForkJoinPool): ForkJoinWorkerThread {
        return ClassLoaderProviderForkJoinWorkerThread(pool, classLoader)
    }

    class ClassLoaderProviderForkJoinWorkerThread(pool: ForkJoinPool, classLoader: ClassLoader) : ForkJoinWorkerThread(pool) {
        init {
            this.contextClassLoader = classLoader
        }
    }
}