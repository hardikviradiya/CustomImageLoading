package com.hardik.customimageloading.imageLoading

import com.hardik.customimageloading.imageLoading.RequestHandler.Result

internal class FetchAction(
  imageLoader: ImageLoader,
  data: Request,
  private var callback: Callback?
) : Action(imageLoader, data) {
  override fun complete(result: Result) {
    callback?.onSuccess()
  }

  override fun error(e: Exception) {
    callback?.onError(e)
  }

  override fun getTarget() = this

  override fun cancel() {
    super.cancel()
    callback = null
  }
}
