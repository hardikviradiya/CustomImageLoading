package com.hardik.customimageloading.imageLoading

import android.graphics.Bitmap
import android.util.LruCache

/** A memory cache which uses a least-recently used eviction policy.  */
internal class PlatformLruCache(maxByteCount: Int) {
  /** Create a cache with a given maximum size in bytes.  */
  val cache =
    object : LruCache<String, BitmapAndSize>(if (maxByteCount != 0) maxByteCount else 1) {
      override fun sizeOf(
        key: String,
        value: BitmapAndSize
      ): Int = value.byteCount
    }

  operator fun get(key: String): Bitmap? = cache[key]?.bitmap

  operator fun set(
    key: String,
    bitmap: Bitmap
  ) {
    val byteCount = bitmap.allocationByteCount
    // If the bitmap is too big for the cache, don't even attempt to store it. Doing so will cause
    // the cache to be cleared. Instead just evict an existing element with the same key if it
    // exists.
    if (byteCount > maxSize()) {
      cache.remove(key)
      return
    }

    cache.put(key, BitmapAndSize(bitmap, byteCount))
  }

  fun size(): Int = cache.size()

  fun maxSize(): Int = cache.maxSize()

  internal class BitmapAndSize(
    val bitmap: Bitmap,
    val byteCount: Int
  )
}
