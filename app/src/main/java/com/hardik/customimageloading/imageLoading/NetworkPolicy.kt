package com.hardik.customimageloading.imageLoading

/** Designates the policy to use for network requests.  */
enum class NetworkPolicy(val index: Int) {
  /**
   * Skips checking the disk cache and forces loading through the network.
   */
  NO_CACHE(1 shl 0),

  /**
   * Skips storing the result into the disk cache.
   */
  NO_STORE(1 shl 1),

  /**
   * Forces the request through the disk cache only, skipping network.
   */
  OFFLINE(1 shl 2);

  companion object {
    @JvmStatic fun shouldReadFromDiskCache(networkPolicy: Int) =
      networkPolicy and NO_CACHE.index == 0

    @JvmStatic fun shouldWriteToDiskCache(networkPolicy: Int) =
      networkPolicy and NO_STORE.index == 0

    @JvmStatic fun isOfflineOnly(networkPolicy: Int) =
      networkPolicy and OFFLINE.index != 0
  }
}
