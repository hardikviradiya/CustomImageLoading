package com.hardik.customimageloading.imageLoading

import com.hardik.customimageloading.imageLoading.RequestHandler.Result

internal abstract class Action(
  val imageLoader: ImageLoader,
  val request: Request
) {
  var willReplay = false
  var cancelled = false

  abstract fun complete(result: Result)
  abstract fun error(e: Exception)

  abstract fun getTarget(): Any?

  open fun cancel() {
    cancelled = true
  }

  val tag: Any
    get() = request.tag ?: this
}
