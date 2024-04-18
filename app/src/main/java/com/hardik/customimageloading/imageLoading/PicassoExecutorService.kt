package com.hardik.customimageloading.imageLoading

import android.os.Process
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * The default [java.util.concurrent.ExecutorService] used for new [ImageLoading] instances.
 */
class ImageLoaderExecutorService(
  threadCount: Int = DEFAULT_THREAD_COUNT,
  threadFactory: ThreadFactory = ImageLoaderThreadFactory()
) : ThreadPoolExecutor(
  threadCount,
  threadCount,
  0,
  MILLISECONDS,
  PriorityBlockingQueue(),
  threadFactory
) {
  override fun submit(task: Runnable): Future<*> {
    val ftask = ImageLoaderFutureTask(task as BitmapHunter)
    execute(ftask)
    return ftask
  }

  private class ImageLoaderThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable): Thread = ImageLoaderThread(r)

    private class ImageLoaderThread(r: Runnable) : Thread(r) {
      override fun run() {
        name = Utils.THREAD_IDLE_NAME
        Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND)
        super.run()
      }
    }
  }

  private class ImageLoaderFutureTask(private val hunter: BitmapHunter) :
    FutureTask<BitmapHunter>(hunter, null), Comparable<ImageLoaderFutureTask> {
    override fun compareTo(other: ImageLoaderFutureTask): Int {
      val p1 = hunter.priority
      val p2 = other.hunter.priority

      // High-priority requests are "lesser" so they are sorted to the front.
      // Equal priorities are sorted by sequence number to provide FIFO ordering.
      return if (p1 == p2) hunter.sequence - other.hunter.sequence else p2.ordinal - p1.ordinal
    }
  }

  private companion object {
    private const val DEFAULT_THREAD_COUNT = 3
  }
}
