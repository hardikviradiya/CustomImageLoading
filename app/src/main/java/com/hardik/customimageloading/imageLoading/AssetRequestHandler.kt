package com.hardik.customimageloading.imageLoading

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import com.hardik.customimageloading.imageLoading.BitmapUtils.decodeStream
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.DISK
import okio.source

internal class AssetRequestHandler(private val context: Context) : RequestHandler() {
  private val lock = Any()

  @Volatile
  private var assetManager: AssetManager? = null

  override fun canHandleRequest(data: Request): Boolean {
    val uri = data.uri
    return uri != null &&
      ContentResolver.SCHEME_FILE == uri.scheme &&
      uri.pathSegments.isNotEmpty() &&
      ANDROID_ASSET == uri.pathSegments[0]
  }

  override fun load(
    imageLoader: ImageLoader,
    request: Request,
    callback: Callback
  ) {
    initializeIfFirstTime()
    var signaledCallback = false
    try {
      assetManager!!.open(getFilePath(request))
        .source()
        .use { source ->
          val bitmap = decodeStream(source, request)
          signaledCallback = true
          callback.onSuccess(Result.Bitmap(bitmap, DISK))
        }
    } catch (e: Exception) {
      if (!signaledCallback) {
        callback.onError(e)
      }
    }
  }

  @Initializer private fun initializeIfFirstTime() {
    if (assetManager == null) {
      synchronized(lock) {
        if (assetManager == null) {
          assetManager = context.assets
        }
      }
    }
  }

  companion object {
    private const val ANDROID_ASSET = "android_asset"
    private const val ASSET_PREFIX_LENGTH =
      "${ContentResolver.SCHEME_FILE}:///$ANDROID_ASSET/".length

    fun getFilePath(request: Request): String {
      val uri = checkNotNull(request.uri)
      return uri.toString()
        .substring(ASSET_PREFIX_LENGTH)
    }
  }
}
