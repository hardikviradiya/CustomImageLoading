package com.hardik.customimageloading.imageLoading

import android.content.ContentResolver
import android.content.Context
import com.hardik.customimageloading.imageLoading.BitmapUtils.decodeResource
import com.hardik.customimageloading.imageLoading.BitmapUtils.isXmlResource
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.DISK

internal class ResourceRequestHandler(private val context: Context) : RequestHandler() {
  override fun canHandleRequest(data: Request): Boolean {
    return if (data.resourceId != 0 && !isXmlResource(context.resources, data.resourceId)) {
      true
    } else {
      data.uri != null && ContentResolver.SCHEME_ANDROID_RESOURCE == data.uri.scheme
    }
  }

  override fun load(
    imageLoader: ImageLoader,
    request: Request,
    callback: Callback
  ) {
    var signaledCallback = false
    try {
      val bitmap = decodeResource(context, request)
      signaledCallback = true
      callback.onSuccess(Result.Bitmap(bitmap, DISK))
    } catch (e: Exception) {
      if (!signaledCallback) {
        callback.onError(e)
      }
    }
  }
}
