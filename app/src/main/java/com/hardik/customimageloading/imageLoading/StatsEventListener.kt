package com.hardik.customimageloading.imageLoading

import android.graphics.Bitmap

class StatsEventListener : EventListener {
  private var maxCacheSize = 0
  private var cacheSize = 0

  private var cacheHits = 0L
  private var cacheMisses = 0L
  private var totalDownloadSize = 0L
  private var totalOriginalBitmapSize = 0L
  private var totalTransformedBitmapSize = 0L

  private var averageDownloadSize = 0.0
  private var averageOriginalBitmapSize = 0.0
  private var averageTransformedBitmapSize = 0.0

  private var downloadCount = 0
  private var originalBitmapCount = 0
  private var transformedBitmapCount = 0

  override fun cacheMaxSize(maxSize: Int) {
    maxCacheSize = maxSize
  }

  override fun cacheSize(size: Int) {
    cacheSize = size
  }

  override fun cacheHit() {
    cacheHits++
  }

  override fun cacheMiss() {
    cacheMisses++
  }

  override fun downloadFinished(size: Long) {
    downloadCount++
    totalDownloadSize += size
    averageDownloadSize = average(downloadCount, totalDownloadSize)
  }

  override fun bitmapDecoded(bitmap: Bitmap) {
    val bitmapSize = bitmap.allocationByteCount

    originalBitmapCount++
    totalOriginalBitmapSize += bitmapSize
    averageOriginalBitmapSize = average(originalBitmapCount, totalOriginalBitmapSize)
  }

  override fun bitmapTransformed(bitmap: Bitmap) {
    val bitmapSize = bitmap.allocationByteCount

    transformedBitmapCount++
    totalTransformedBitmapSize += bitmapSize
    averageTransformedBitmapSize = average(originalBitmapCount, totalTransformedBitmapSize)
  }

  private fun average(
    count: Int,
    totalSize: Long
  ): Double = totalSize * 1.0 / count

}
