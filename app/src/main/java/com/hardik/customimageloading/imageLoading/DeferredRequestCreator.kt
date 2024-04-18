package com.hardik.customimageloading.imageLoading

import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewTreeObserver
import android.widget.ImageView
import java.lang.ref.WeakReference

internal class DeferredRequestCreator(
  private val creator: RequestCreator,
  target: ImageView,
  internal var callback: Callback?
) : ViewTreeObserver.OnPreDrawListener, OnAttachStateChangeListener {
  private val targetReference = WeakReference(target)

  init {
    target.addOnAttachStateChangeListener(this)

    // Only add the pre-draw listener if the view is already attached.
    if (target.windowToken != null) {
      onViewAttachedToWindow(target)
    }
  }

  override fun onViewAttachedToWindow(view: View) {
    view.viewTreeObserver.addOnPreDrawListener(this)
  }

  override fun onViewDetachedFromWindow(view: View) {
    view.viewTreeObserver.removeOnPreDrawListener(this)
  }

  override fun onPreDraw(): Boolean {
    val target = targetReference.get() ?: return true

    val vto = target.viewTreeObserver
    if (!vto.isAlive) {
      return true
    }

    val width = target.width
    val height = target.height

    if (width <= 0 || height <= 0) {
      return true
    }

    target.removeOnAttachStateChangeListener(this)
    vto.removeOnPreDrawListener(this)
    targetReference.clear()

    creator.unfit().resize(width, height).into(target, callback)
    return true
  }

  fun cancel() {
    creator.clearTag()
    callback = null

    val target = targetReference.get() ?: return
    targetReference.clear()
    target.removeOnAttachStateChangeListener(this)

    val vto = target.viewTreeObserver
    if (vto.isAlive) {
      vto.removeOnPreDrawListener(this)
    }
  }

  val tag: Any?
    get() = creator.tag
}
