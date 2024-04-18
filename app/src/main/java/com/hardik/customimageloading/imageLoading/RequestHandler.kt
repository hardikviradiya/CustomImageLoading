package com.hardik.customimageloading.imageLoading

import android.net.NetworkInfo
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom
import java.io.IOException

/**
 * `RequestHandler` allows you to extend ImageLoading to load images in ways that are not
 * supported by default in the library.
 *
 * <h2>Usage</h2>
 * `RequestHandler` must be subclassed to be used. You will have to override two methods
 * ([canHandleRequest] and [load]) with your custom logic to load images.
 *
 * You should then register your [RequestHandler] using
 * [ImageLoading.Builder.addRequestHandler]
 *
 * **Note:** This is a beta feature. The API is subject to change in a backwards incompatible
 * way at any time.
 *
 * @see ImageLoading.Builder.addRequestHandler
 */
abstract class RequestHandler {
  /**
   * [Result] represents the result of a [load] call in a [RequestHandler].
   *
   * @see RequestHandler
   * @see [load]
   */
  sealed class Result(
    /**
     * Returns the resulting [ImageLoading.LoadedFrom] generated from a [load] call.
     */
    val loadedFrom: LoadedFrom,
    /**
     * Returns the resulting EXIF rotation generated from a [load] call.
     */
    val exifRotation: Int = 0
  ) {
    class Bitmap(
      val bitmap: android.graphics.Bitmap,
      loadedFrom: LoadedFrom,
      exifRotation: Int = 0
    ) : Result(loadedFrom, exifRotation)

    class Drawable(
      val drawable: android.graphics.drawable.Drawable,
      loadedFrom: LoadedFrom,
      exifRotation: Int = 0
    ) : Result(loadedFrom, exifRotation)
  }

  interface Callback {
    fun onSuccess(result: Result?)
    fun onError(t: Throwable)
  }

  /**
   * Whether or not this [RequestHandler] can handle a request with the given [Request].
   */
  abstract fun canHandleRequest(data: Request): Boolean

  /**
   * Loads an image for the given [Request].
   * @param request the data from which the image should be resolved.
   */
  @Throws(IOException::class)
  abstract fun load(
    imageLoader: ImageLoader,
    request: Request,
    callback: Callback
  )

  open val retryCount = 0

  open fun shouldRetry(
    airplaneMode: Boolean,
    info: NetworkInfo?
  ) = false

  open fun supportsReplay() = false
}
