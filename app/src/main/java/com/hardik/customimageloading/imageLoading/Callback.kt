package com.hardik.customimageloading.imageLoading

interface Callback {
  fun onSuccess()
  fun onError(t: Throwable)
}
