package com.hardik.customimageloading.imageLoading

import android.content.Context
import androidx.core.content.ContextCompat
import com.hardik.customimageloading.imageLoading.BitmapUtils.isXmlResource
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.DISK

internal class ResourceDrawableRequestHandler private constructor(
  private val context: Context,
  private val loader: DrawableLoader
) : RequestHandler() {
  override fun canHandleRequest(data: Request): Boolean {
    return data.resourceId != 0 && isXmlResource(context.resources, data.resourceId)
  }

  override fun load(
    imageLoader: ImageLoader,
    request: Request,
    callback: Callback
  ) {
    val drawable = loader.load(request.resourceId)
    if (drawable == null) {
      callback.onError(
        IllegalArgumentException("invalid resId: ${Integer.toHexString(request.resourceId)}")
      )
    } else {
      callback.onSuccess(Result.Drawable(drawable, DISK))
    }
  }

  internal companion object {
    @JvmName("-create")
    internal fun create(
      context: Context,
      loader: DrawableLoader = DrawableLoader { resId -> ContextCompat.getDrawable(context, resId) }
    ) = ResourceDrawableRequestHandler(context, loader)
  }
}
