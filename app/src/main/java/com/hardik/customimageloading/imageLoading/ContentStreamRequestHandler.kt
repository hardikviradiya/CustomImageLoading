package com.hardik.customimageloading.imageLoading

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
import androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.DISK
import okio.Source
import okio.source
import java.io.FileNotFoundException

internal open class ContentStreamRequestHandler(val context: Context) : RequestHandler() {
  override fun canHandleRequest(data: Request): Boolean =
    ContentResolver.SCHEME_CONTENT == data.uri?.scheme ?: false

  override fun load(
    imageLoader: ImageLoader,
    request: Request,
    callback: Callback
  ) {
    var signaledCallback = false
    try {
      val requestUri = checkNotNull(request.uri)
      val source = getSource(requestUri)
      val bitmap = BitmapUtils.decodeStream(source, request)
      val exifRotation = getExifOrientation(requestUri)
      signaledCallback = true
      callback.onSuccess(Result.Bitmap(bitmap, DISK, exifRotation))
    } catch (e: Exception) {
      if (!signaledCallback) {
        callback.onError(e)
      }
    }
  }

  fun getSource(uri: Uri): Source {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)
      ?: throw FileNotFoundException("can't open input stream, uri: $uri")
    return inputStream.source()
  }

  protected open fun getExifOrientation(uri: Uri): Int {
    val contentResolver = context.contentResolver
    contentResolver.openInputStream(uri)?.use { input ->
      return ExifInterface(input).getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)
    } ?: throw FileNotFoundException("can't open input stream, uri: $uri")
  }
}
