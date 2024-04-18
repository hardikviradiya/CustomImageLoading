package com.hardik.customimageloading.imageLoading

import android.net.NetworkInfo

internal interface Dispatcher {
  fun shutdown()

  fun dispatchSubmit(action: Action)

  fun dispatchCancel(action: Action)

  fun dispatchPauseTag(tag: Any)

  fun dispatchResumeTag(tag: Any)

  fun dispatchComplete(hunter: BitmapHunter)

  fun dispatchRetry(hunter: BitmapHunter)

  fun dispatchFailed(hunter: BitmapHunter)

  fun dispatchNetworkStateChange(info: NetworkInfo)

  fun dispatchAirplaneModeChange(airplaneMode: Boolean)

  fun dispatchSubmit(hunter: BitmapHunter)

  fun dispatchCompleteMain(hunter: BitmapHunter)

  fun dispatchBatchResumeMain(batch: MutableList<Action>)

  fun isShutdown(): Boolean

  companion object {
    const val RETRY_DELAY = 500L
  }
}
