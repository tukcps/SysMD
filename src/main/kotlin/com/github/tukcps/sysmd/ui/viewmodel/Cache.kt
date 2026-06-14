package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import com.github.tukcps.sysmd.settings
import kotlinx.io.files.Path

/**
 * Cache to store Images, so scrolling will be smooth.
 * @param size number of pictures that will be cached.
 */
class Cache<K, V>(val size: Int = 50) {
    private val cache = HashMap<K, V>()
    private var insertionOrder = arrayListOf<K>()

    fun put(key: K, value: V): K? {
        var evictedKey: K? = null
        if (cache.size >= size) {
            evictedKey = insertionOrder.removeFirst()
            cache.remove(evictedKey)
        }
        cache[key] = value
        insertionOrder.addLast(key)

        return evictedKey
    }

    operator fun get(key: K):V? = cache[key]

    override fun toString() = cache.toString()

}

val imageCache = Cache<Path, ImageBitmap>(settings.imagesToCache)