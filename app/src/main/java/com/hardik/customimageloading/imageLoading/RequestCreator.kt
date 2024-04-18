package com.hardik.customimageloading.imageLoading

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.hardik.customimageloading.imageLoading.BitmapHunter.Companion.forRequest
import com.hardik.customimageloading.imageLoading.MemoryPolicy.Companion.shouldReadFromMemoryCache
import com.hardik.customimageloading.imageLoading.MemoryPolicy.Companion.shouldWriteToMemoryCache
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom
import com.hardik.customimageloading.imageLoading.ImageLoaderDrawable.Companion.setPlaceholder
import com.hardik.customimageloading.imageLoading.ImageLoaderDrawable.Companion.setResult
import com.hardik.customimageloading.imageLoading.Utils.OWNER_MAIN
import com.hardik.customimageloading.imageLoading.Utils.VERB_COMPLETED
import com.hardik.customimageloading.imageLoading.Utils.checkMain
import com.hardik.customimageloading.imageLoading.Utils.checkNotMain
import com.hardik.customimageloading.imageLoading.Utils.log
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/** Fluent API for building an image download request.  */
class RequestCreator internal constructor(
  private val imageLoader: ImageLoader,
  uri: Uri?,
  resourceId: Int
) {
  private val data = Request.Builder(uri, resourceId, imageLoader.defaultBitmapConfig)

  private var noFade = false
  private var deferred = false
  private var setPlaceholder = true

  @DrawableRes private var placeholderResId = 0

  @DrawableRes private var errorResId = 0
  private var placeholderDrawable: Drawable? = null
  private var errorDrawable: Drawable? = null

  /** Internal use only. Used by [DeferredRequestCreator].  */
  @get:JvmName("-tag")
  internal val tag: Any?
    get() = data.tag

  init {
    check(!imageLoader.shutdown) { "ImageLoading instance already shut down. Cannot submit new requests." }
  }

  /**
   * A placeholder drawable to be used while the image is being loaded. If the requested image is
   * not immediately available in the memory cache then this resource will be set on the target
   * [ImageView].
   *
   * If you are not using a placeholder image but want to clear an existing image (such as when
   * used in an [adapter][android.widget.Adapter]), pass in `null`.
   */
  fun placeholder(placeholderDrawable: Drawable?): RequestCreator {
    check(setPlaceholder) { "Already explicitly declared as no placeholder." }
    check(placeholderResId == 0) { "Placeholder image already set." }
    this.placeholderDrawable = placeholderDrawable
    return this
  }

  /**
   * Assign a tag to this request. Tags are an easy way to logically associate
   * related requests that can be managed together e.g. paused, resumed,
   * or canceled.
   *
   * You can either use simple [String] tags or objects that naturally
   * define the scope of your requests within your app such as a
   * [android.content.Context], an [android.app.Activity], or a
   * [android.app.Fragment].
   *
   * **WARNING:**: ImageLoading will keep a reference to the tag for
   * as long as this tag is paused and/or has active requests. Look out for
   * potential leaks.
   *
   * @see ImageLoading.cancelTag
   * @see ImageLoading.pauseTag
   * @see ImageLoading.resumeTag
   */
  fun tag(tag: Any): RequestCreator {
    data.tag(tag)
    return this
  }

  /** Internal use only. Used by [DeferredRequestCreator].  */
  @JvmName("-unfit")
  internal fun unfit(): RequestCreator {
    deferred = false
    return this
  }

  /** Internal use only. Used by [DeferredRequestCreator].  */
  @JvmName("-clearTag")
  internal fun clearTag(): RequestCreator {
    data.clearTag()
    return this
  }

  /**
   * Resize the image to the specified size in pixels.
   * Use 0 as desired dimension to resize keeping aspect ratio.
   */
  fun resize(targetWidth: Int, targetHeight: Int): RequestCreator {
    data.resize(targetWidth, targetHeight)
    return this
  }

  /**
   * Set the priority of this request.
   *
   *
   * This will affect the order in which the requests execute but does not guarantee it.
   * By default, all requests have [Priority.NORMAL] priority, except for
   * [fetch] requests, which have [Priority.LOW] priority by default.
   */
  fun priority(priority: ImageLoader.Priority): RequestCreator {
    data.priority(priority)
    return this
  }

  /**
   * Synchronously fulfill this request. Must not be called from the main thread.
   */
  @Throws(IOException::class) // TODO make non-null and always throw?
  fun get(): Bitmap? {
    val started = System.nanoTime()
    checkNotMain()
    check(!deferred) { "Fit cannot be used with get." }
    if (!data.hasImage()) {
      return null
    }

    val request = createRequest(started)
    val action = GetAction(imageLoader, request)
    val result =
      forRequest(imageLoader, imageLoader.dispatcher, imageLoader.cache, action).hunt() ?: return null

    val bitmap = result.bitmap
    if (shouldWriteToMemoryCache(request.memoryPolicy)) {
      imageLoader.cache[request.key] = bitmap
    }

    return bitmap
  }

  /**
   * Asynchronously fulfills the request without a [ImageView],
   * and invokes the target [Callback] with the result. This is useful when you want to warm
   * up the cache with an image.
   *
   * *Note:* The [Callback] param is a strong reference and will prevent your
   * [android.app.Activity] or [android.app.Fragment] from being garbage collected
   * until the request is completed.
   *
   * *Note:* It is safe to invoke this method from any thread.
   */
  @JvmOverloads fun fetch(callback: Callback? = null) {
    val started = System.nanoTime()
    check(!deferred) { "Fit cannot be used with fetch." }

    if (data.hasImage()) {
      // Fetch requests have lower priority by default.
      if (!data.hasPriority()) {
        data.priority(ImageLoader.Priority.LOW)
      }

      val request = createRequest(started)
      if (shouldReadFromMemoryCache(request.memoryPolicy)) {
        val bitmap = imageLoader.quickMemoryCacheCheck(request.key)
        if (bitmap != null) {
          if (imageLoader.isLoggingEnabled) {
            log(OWNER_MAIN, VERB_COMPLETED, request.plainId(), "from " + LoadedFrom.MEMORY)
          }
          callback?.onSuccess()
          return
        }
      }

      val action = FetchAction(imageLoader, request, callback)
      imageLoader.submit(action)
    }
  }

  /**
   * Asynchronously fulfills the request into the specified [ImageView] and invokes the
   * target [Callback] if it's not `null`.
   *
   * *Note:* The [Callback] param is a strong reference and will prevent your
   * [android.app.Activity] or [android.app.Fragment] from being garbage collected. If
   * you use this method, it is **strongly** recommended you invoke an adjacent
   * [ImageLoading.cancelRequest] call to prevent temporary leaking.
   *
   * *Note:* This method will automatically support object recycling.
   */
  @JvmOverloads fun into(target: ImageView, callback: Callback? = null) {
    val started = System.nanoTime()
    checkMain()

    if (!data.hasImage()) {
      imageLoader.cancelRequest(target)
      if (setPlaceholder) {
        setPlaceholder(target, getPlaceholderDrawable())
      }
      return
    }

    if (deferred) {
      check(!data.hasSize()) { "Fit cannot be used with resize." }
      val width = target.width
      val height = target.height
      if (width == 0 || height == 0) {
        if (setPlaceholder) {
          setPlaceholder(target, getPlaceholderDrawable())
        }
        imageLoader.defer(target, DeferredRequestCreator(this, target, callback))
        return
      }
      data.resize(width, height)
    }

    val request = createRequest(started)

    if (shouldReadFromMemoryCache(request.memoryPolicy)) {
      val bitmap = imageLoader.quickMemoryCacheCheck(request.key)
      if (bitmap != null) {
        imageLoader.cancelRequest(target)
        val result: RequestHandler.Result = RequestHandler.Result.Bitmap(bitmap, LoadedFrom.MEMORY)
        setResult(target, imageLoader.context, result, noFade, imageLoader.indicatorsEnabled)
        if (imageLoader.isLoggingEnabled) {
          log(OWNER_MAIN, VERB_COMPLETED, request.plainId(), "from " + LoadedFrom.MEMORY)
        }
        callback?.onSuccess()
        return
      }
    }

    if (setPlaceholder) {
      setPlaceholder(target, getPlaceholderDrawable())
    }

    val action = ImageViewAction(
      imageLoader,
      target,
      request,
      errorDrawable,
      errorResId,
      noFade,
      callback
    )

    imageLoader.enqueueAndSubmit(action)
  }

  private fun getPlaceholderDrawable(): Drawable? {
    return if (placeholderResId == 0) {
      placeholderDrawable
    } else {
      ContextCompat.getDrawable(imageLoader.context, placeholderResId)
    }
  }

  /** Create the request optionally passing it through the request transformer.  */
  private fun createRequest(started: Long): Request {
    val id = nextId.getAndIncrement()
    val request = data.build()
    request.id = id
    request.started = started

    val loggingEnabled = imageLoader.isLoggingEnabled
    if (loggingEnabled) {
      log(OWNER_MAIN, Utils.VERB_CREATED, request.plainId(), request.toString())
    }

    val transformed = imageLoader.transformRequest(request)
    if (transformed != request) {
      // If the request was changed, copy over the id and timestamp from the original.
      transformed.id = id
      transformed.started = started
      if (loggingEnabled) {
        log(OWNER_MAIN, Utils.VERB_CHANGED, transformed.logId(), "into $transformed")
      }
    }

    return transformed
  }

  private companion object {
    private val nextId = AtomicInteger()
  }
}
