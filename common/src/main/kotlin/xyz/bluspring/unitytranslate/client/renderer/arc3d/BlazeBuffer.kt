@file:OptIn(ExperimentalAtomicApi::class)

package xyz.bluspring.unitytranslate.client.renderer.arc3d

import com.google.common.collect.HashBiMap
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import icyllis.arc3d.engine.Buffer
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

class BlazeBuffer(device: BlazeDevice, val source: GpuBuffer) : Buffer(device, source.size(), source.usage()) {
    private val mappings = mutableMapOf<Triple<Int, Long, Long>, GpuBufferSlice.MappedView>()

    override fun onMap(mode: Int, offset: Long, size: Long): Long {
        val triple = Triple(mode, offset, size)
        if (mappings.contains(triple))
            return getPointerFromMapped(this.mappings[triple]!!)

        val view = this.source.map(offset, size, true, mode == kWriteDiscard_MapMode)
        mappings[triple] = view
        return mapped.size.toLong().apply {
            mapped[ptrInc.fetchAndIncrement()] = view
        }
    }

    override fun onUnmap(mode: Int, offset: Long, size: Long) {
        val triple = Triple(mode, offset, size)
        if (mappings.contains(triple)) {
            val view = this.mappings[triple]!!
            mapped.remove(getPointerFromMapped(view))
            view.close()
        }
    }

    override fun onRelease() {
        for (view in mappings.values) {
            mapped.remove(getPointerFromMapped(view))
        }

        this.source.close()
    }

    companion object {
        // this is so fuckin' funny to me
        private val ptrInc = AtomicLong(1L)
        private val mapped = HashBiMap.create<Long, GpuBufferSlice.MappedView>()
        private val pointers = this.mapped.inverse()

        fun getMappedFromPointer(ptr: Long): GpuBufferSlice.MappedView? {
            return this.mapped[ptr]
        }

        fun getPointerFromMapped(view: GpuBufferSlice.MappedView): Long {
            return this.pointers[view] ?: 0L
        }
    }
}
