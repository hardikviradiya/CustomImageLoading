package com.hardik.customimageloading.imageLoading

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.hardik.customimageloading.imageLoading.BitmapUtils.decodeStream
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.DISK
import java.io.FileNotFoundException

internal class FileRequestHandler(context: Context) : ContentStreamRequestHandler(context) {
  override fun canHandleRequest(data: Request): Boolean {
    val uri = data.uri
    return uri != null && ContentResolver.SCHEME_FILE == uri.scheme
  }

  override fun load(
    imageLoader: ImageLoader,
    request: Request,
    callback: Callback
  ) {
    var signaledCallback = false
    try {
      val requestUri = checkNotNull(request.uri)
      val source = getSource(requestUri)
      val bitmap = decodeStream(source, request)
      val exifRotation = getExifOrientation(requestUri)
      signaledCallback = true
      callback.onSuccess(Result.Bitmap(bitmap, DISK, exifRotation))
    } catch (e: Exception) {
      if (!signaledCallback) {
        callback.onError(e)
      }
    }
  }

  override fun getExifOrientation(uri: Uri): Int {
    val path = uri.path ?: throw FileNotFoundException("path == null, uri: $uri")
    return ExifInterface(path).getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL
    )
  }
}
