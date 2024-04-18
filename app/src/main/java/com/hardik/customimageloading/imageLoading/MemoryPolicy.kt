package com.hardik.customimageloading.imageLoading

/** Designates the policy to use when dealing with memory cache.  */
enum class MemoryPolicy(val index: Int) {
  /** Skips memory cache lookup when processing a request.  */
  NO_CACHE(1 shl 0),

  /**
   * Skips storing the final result into memory cache. Useful for one-off requests
   * to avoid evicting other bitmaps from the cache.
   */
  NO_STORE(1 shl 1);

  companion object {
    @JvmStatic fun shouldReadFromMemoryCache(memoryPolicy: Int) =
      memoryPolicy and NO_CACHE.index == 0

    @JvmStatic fun shouldWriteToMemoryCache(memoryPolicy: Int) =
      memoryPolicy and NO_STORE.index == 0
  }
}
