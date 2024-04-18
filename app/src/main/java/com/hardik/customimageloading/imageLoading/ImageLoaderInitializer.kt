package com.hardik.customimageloading.imageLoading

import android.content.Context
import androidx.startup.Initializer

class ImageLoaderInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    appContext = context
  }

  override fun dependencies() = emptyList<Class<Initializer<*>>>()

  companion object {
    private lateinit var appContext: Context
    private val instance: ImageLoader by lazy {
      ImageLoader
        .Builder(appContext)
        .addEventListener(StatsEventListener())
        .build()
    }
    fun get() = instance
  }
}