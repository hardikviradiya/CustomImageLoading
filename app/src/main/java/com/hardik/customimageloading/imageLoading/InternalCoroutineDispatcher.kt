package com.hardik.customimageloading.imageLoading

import android.content.Context
import android.net.NetworkInfo
import android.os.Handler
import com.hardik.customimageloading.imageLoading.Dispatcher.Companion.RETRY_DELAY
import com.hardik.customimageloading.imageLoading.ImageLoader.Priority.HIGH
import com.hardik.customimageloading.imageLoading.Utils.OWNER_DISPATCHER
import com.hardik.customimageloading.imageLoading.Utils.VERB_CANCELED
import com.hardik.customimageloading.imageLoading.Utils.log
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
internal class InternalCoroutineDispatcher internal constructor(
  context: Context,
  mainThreadHandler: Handler,
  cache: PlatformLruCache,
  val mainContext: CoroutineContext,
  val backgroundContext: CoroutineContext
) : BaseDispatcher(context, mainThreadHandler, cache) {

  private val scope = CoroutineScope(SupervisorJob() + backgroundContext)
  private val channel = Channel<() -> Unit>(capacity = Channel.UNLIMITED)

  init {
    // Using a channel to enforce sequential access for this class' internal state
    scope.launch {
      while (!channel.isClosedForReceive) {
        channel.receive().invoke()
      }
    }
  }

  override fun shutdown() {
    super.shutdown()
    channel.close()
    scope.cancel()
  }

  override fun dispatchSubmit(action: Action) {
    channel.trySend {
      performSubmit(action)
    }
  }

  override fun dispatchCancel(action: Action) {
    channel.trySend {
      performCancel(action)
    }
  }

  override fun dispatchPauseTag(tag: Any) {
    channel.trySend {
      performPauseTag(tag)
    }
  }

  override fun dispatchResumeTag(tag: Any) {
    channel.trySend {
      performResumeTag(tag)
    }
  }

  override fun dispatchComplete(hunter: BitmapHunter) {
    channel.trySend {
      performComplete(hunter)
    }
  }

  override fun dispatchRetry(hunter: BitmapHunter) {
    scope.launch {
      delay(RETRY_DELAY)
      channel.send {
        performRetry(hunter)
      }
    }
  }

  override fun dispatchFailed(hunter: BitmapHunter) {
    channel.trySend {
      performError(hunter)
    }
  }

  override fun dispatchNetworkStateChange(info: NetworkInfo) {
    channel.trySend {
      performNetworkStateChange(info)
    }
  }

  override fun dispatchAirplaneModeChange(airplaneMode: Boolean) {
    channel.trySend {
      performAirplaneModeChange(airplaneMode)
    }
  }

  override fun dispatchCompleteMain(hunter: BitmapHunter) {
    scope.launch(mainContext) {
      performCompleteMain(hunter)
    }
  }

  override fun dispatchBatchResumeMain(batch: MutableList<Action>) {
    scope.launch(mainContext) {
      performBatchResumeMain(batch)
    }
  }

  override fun dispatchSubmit(hunter: BitmapHunter) {
    val highPriority = hunter.action?.request?.priority == HIGH
    val context = if (highPriority) EmptyCoroutineContext else mainContext

    scope.launch(context) {
      channel.trySend {
        if (hunter.action != null) {
          hunter.job = scope.launch(CoroutineName(hunter.getName())) {
            hunter.run()
          }
        } else {
          hunterMap.remove(hunter.key)
          if (hunter.imageLoader.isLoggingEnabled) {
            log(OWNER_DISPATCHER, VERB_CANCELED, hunter.key)
          }
        }
      }
    }
  }

  override fun isShutdown() = !scope.isActive
}
