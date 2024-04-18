package com.hardik.customimageloading.imageLoading

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.Video
import com.hardik.customimageloading.imageLoading.BitmapUtils.calculateInSampleSize
import com.hardik.customimageloading.imageLoading.BitmapUtils.createBitmapOptions
import com.hardik.customimageloading.imageLoading.BitmapUtils.decodeStream
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom

internal class MediaStoreRequestHandler(context: Context) : ContentStreamRequestHandler(context) {
  override fun canHandleRequest(data: Request): Boolean {
    val uri = data.uri
    return uri != null &&
      ContentResolver.SCHEME_CONTENT == uri.scheme &&
      MediaStore.AUTHORITY == uri.authority
  }

  override fun load(imageLoader: ImageLoader, request: Request, callback: Callback) {
    var signaledCallback = false
    try {
      val contentResolver = context.contentResolver
      val requestUri = checkNotNull(request.uri, { "request.uri == null" })
      val exifOrientation = getExifOrientation(requestUri)

      val mimeType = contentResolver.getType(requestUri)
      val isVideo = mimeType != null && mimeType.startsWith("video/")

      if (request.hasSize()) {
        val imageLoaderKind = getImageLoaderKind(request.targetWidth, request.targetHeight)
        if (!isVideo && imageLoaderKind == ImageLoaderKind.FULL) {
          val source = getSource(requestUri)
          val bitmap = decodeStream(source, request)
          signaledCallback = true
          callback.onSuccess(Result.Bitmap(bitmap, LoadedFrom.DISK, exifOrientation))
          return
        }

        val id = ContentUris.parseId(requestUri)

        val options = checkNotNull(createBitmapOptions(request), { "options == null" })
        options.inJustDecodeBounds = true

        calculateInSampleSize(
          request.targetWidth,
          request.targetHeight,
          imageLoaderKind.width,
          imageLoaderKind.height,
          options,
          request
        )

        val bitmap = if (isVideo) {
          // Since MediaStore doesn't provide the full screen kind thumbnail, we use the mini kind
          // instead which is the largest thumbnail size can be fetched from MediaStore.
          val kind =
            if (imageLoaderKind == ImageLoaderKind.FULL) Video.Thumbnails.MINI_KIND else imageLoaderKind.androidKind
          Video.Thumbnails.getThumbnail(contentResolver, id, kind, options)
        } else {
          MediaStore.Images.Thumbnails.getThumbnail(
            contentResolver,
            id,
            imageLoaderKind.androidKind,
            options
          )
        }

        if (bitmap != null) {
          signaledCallback = true
          callback.onSuccess(Result.Bitmap(bitmap, LoadedFrom.DISK, exifOrientation))
          return
        }
      }

      val source = getSource(requestUri)
      val bitmap = decodeStream(source, request)
      signaledCallback = true
      callback.onSuccess(Result.Bitmap(bitmap, LoadedFrom.DISK, exifOrientation))
    } catch (e: Exception) {
      if (!signaledCallback) {
        callback.onError(e)
      }
    }
  }

  internal enum class ImageLoaderKind(val androidKind: Int, val width: Int, val height: Int) {
    MICRO(MediaStore.Images.Thumbnails.MICRO_KIND, 96, 96),
    MINI(MediaStore.Images.Thumbnails.MINI_KIND, 512, 384),
    FULL(MediaStore.Images.Thumbnails.FULL_SCREEN_KIND, -1, -1);
  }

  companion object {
    fun getImageLoaderKind(targetWidth: Int, targetHeight: Int): ImageLoaderKind {
      return if (targetWidth <= ImageLoaderKind.MICRO.width && targetHeight <= ImageLoaderKind.MICRO.height) {
        ImageLoaderKind.MICRO
      } else if (targetWidth <= ImageLoaderKind.MINI.width && targetHeight <= ImageLoaderKind.MINI.height) {
        ImageLoaderKind.MINI
      } else {
        ImageLoaderKind.FULL
      }
    }
  }
}
