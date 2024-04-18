package com.hardik.customimageloading.imageLoading

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.hardik.customimageloading.imageLoading.RequestHandler.Result
import java.lang.ref.WeakReference

internal class ImageViewAction(
  imageLoader: ImageLoader,
  target: ImageView,
  data: Request,
  val errorDrawable: Drawable?,
  @DrawableRes val errorResId: Int,
  val noFade: Boolean,
  var callback: Callback?
) : Action(imageLoader, data) {
  private val targetReference = WeakReference(target)

  override fun complete(result: Result) {
    val target = targetReference.get() ?: return

    ImageLoaderDrawable.setResult(target, imageLoader.context, result, noFade, imageLoader.indicatorsEnabled)
    callback?.onSuccess()
  }

  override fun error(e: Exception) {
    val target = targetReference.get() ?: return

    val placeholder = target.drawable
    if (placeholder is Animatable) {
      (placeholder as Animatable).stop()
    }
    if (errorResId != 0) {
      target.setImageResource(errorResId)
    } else if (errorDrawable != null) {
      target.setImageDrawable(errorDrawable)
    }
    callback?.onError(e)
  }

  override fun getTarget(): ImageView? {
    return targetReference.get()
  }

  override fun cancel() {
    super.cancel()
    callback = null
  }
}
