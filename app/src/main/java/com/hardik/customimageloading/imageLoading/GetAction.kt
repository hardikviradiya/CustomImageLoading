package com.hardik.customimageloading.imageLoading

import com.hardik.customimageloading.imageLoading.RequestHandler.Result

internal class GetAction(
  imageLoader: ImageLoader,
  data: Request
) : Action(imageLoader, data) {
  override fun complete(result: Result) = Unit
  override fun error(e: Exception) = Unit
  override fun getTarget() = throw AssertionError()
}
