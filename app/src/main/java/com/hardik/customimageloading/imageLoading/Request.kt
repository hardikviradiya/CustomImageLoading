package com.hardik.customimageloading.imageLoading

import android.graphics.Bitmap.Config
import android.net.Uri
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import com.hardik.customimageloading.imageLoading.ImageLoader.Priority
import com.hardik.customimageloading.imageLoading.ImageLoader.Priority.NORMAL
import okhttp3.Headers
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.TimeUnit.SECONDS

/** Immutable data about an image and the transformations that will be applied to it. */
class Request internal constructor(builder: Builder) {
  /** A unique ID for the request.  */
  @JvmField var id = 0

  /** The time that the request was first submitted (in nanos). */
  @JvmField var started: Long = 0

  /** The [MemoryPolicy] to use for this request. */
  @JvmField val memoryPolicy: Int = builder.memoryPolicy

  /** The [NetworkPolicy] to use for this request. */
  @JvmField val networkPolicy: Int = builder.networkPolicy

  /** HTTP headers for the request  */
  @JvmField val headers: Headers? = builder.headers

  /**
   * The image URI.
   *
   * This is mutually exclusive with [.resourceId].
   */
  @JvmField val uri: Uri? = builder.uri

  /**
   * The image resource ID.
   *
   * This is mutually exclusive with [.uri].
   */
  @JvmField val resourceId: Int = builder.resourceId

  /**
   * Optional stable key for this request to be used instead of the URI or resource ID when
   * caching. Two requests with the same value are considered to be for the same resource.
   */
  val stableKey: String? = builder.stableKey

  /** List of custom transformations to be applied after the built-in transformations. */
  @JvmField var transformations: List<Transformation> =
    if (builder.transformations == null) {
      emptyList()
    } else {
      builder.transformations!!.toList()
    }

  /** Target image width for resizing. */
  @JvmField val targetWidth: Int = builder.targetWidth

  /** Target image height for resizing. */
  @JvmField val targetHeight: Int = builder.targetHeight

  /**
   * True if the final image should use the 'centerCrop' scale technique.
   *
   * This is mutually exclusive with [.centerInside].
   */
  @JvmField val centerCrop: Boolean = builder.centerCrop

  /** If centerCrop is set, controls alignment of centered image */
  @JvmField val centerCropGravity: Int = builder.centerCropGravity

  /**
   * True if the final image should use the 'centerInside' scale technique.
   *
   * This is mutually exclusive with [.centerCrop].
   */
  @JvmField val centerInside: Boolean = builder.centerInside

  @JvmField val onlyScaleDown: Boolean = builder.onlyScaleDown

  /** Amount to rotate the image in degrees. */
  @JvmField val rotationDegrees: Float = builder.rotationDegrees

  /** Rotation pivot on the X axis. */
  @JvmField val rotationPivotX: Float = builder.rotationPivotX

  /** Rotation pivot on the Y axis. */
  @JvmField val rotationPivotY: Float = builder.rotationPivotY

  /** Whether or not [.rotationPivotX] and [.rotationPivotY] are set. */
  @JvmField val hasRotationPivot: Boolean = builder.hasRotationPivot

  /** Target image config for decoding. */
  @JvmField val config: Config? = builder.config

  /** The priority of this request. */
  @JvmField val priority: Priority = checkNotNull(builder.priority)

  /** The cache key for this request. */
  @JvmField var key: String =
    if (Looper.myLooper() == Looper.getMainLooper()) {
      createKey()
    } else {
      createKey(StringBuilder())
    }

  /** User-provided value to track this request. */
  val tag: Any? = builder.tag

  override fun toString() =
    buildString {
      append("Request{")
      if (resourceId > 0) {
        append(resourceId)
      } else {
        append(uri)
      }
      for (transformation in transformations) {
        append(' ')
        append(transformation.key())
      }
      if (stableKey != null) {
        append(" stableKey(")
        append(stableKey)
        append(')')
      }
      if (targetWidth > 0) {
        append(" resize(")
        append(targetWidth)
        append(',')
        append(targetHeight)
        append(')')
      }
      if (centerCrop) {
        append(" centerCrop")
      }
      if (centerInside) {
        append(" centerInside")
      }
      if (rotationDegrees != 0f) {
        append(" rotation(")
        append(rotationDegrees)
        if (hasRotationPivot) {
          append(" @ ")
          append(rotationPivotX)
          append(',')
          append(rotationPivotY)
        }
        append(')')
      }
      if (config != null) {
        append(' ')
        append(config)
      }
      append('}')
    }

  // TODO make internal
  fun logId(): String {
    val delta = System.nanoTime() - started
    return if (delta > TOO_LONG_LOG) {
      "${plainId()}+${NANOSECONDS.toSeconds(delta)}s"
    } else {
      "${plainId()}+${NANOSECONDS.toMillis(delta)}ms"
    }
  }

  // TODO make internal
  fun plainId() = "[R$id]"

  // TODO make internal
  val name: String
    get() = uri?.path ?: Integer.toHexString(resourceId)

  // TODO make internal
  fun hasSize(): Boolean = targetWidth != 0 || targetHeight != 0

  // TODO make internal
  fun needsMatrixTransform(): Boolean = hasSize() || rotationDegrees != 0f

  fun newBuilder(): Builder = Builder(this)

  private fun createKey(): String {
    val result = createKey(Utils.MAIN_THREAD_KEY_BUILDER)
    Utils.MAIN_THREAD_KEY_BUILDER.setLength(0)
    return result
  }

  private fun createKey(builder: StringBuilder): String {
    val data = this
    if (data.stableKey != null) {
      builder.ensureCapacity(data.stableKey.length + KEY_PADDING)
      builder.append(data.stableKey)
    } else if (data.uri != null) {
      val path = data.uri.toString()
      builder.ensureCapacity(path.length + KEY_PADDING)
      builder.append(path)
    } else {
      builder.ensureCapacity(KEY_PADDING)
      builder.append(data.resourceId)
    }

    builder.append(KEY_SEPARATOR)

    if (data.rotationDegrees != 0f) {
      builder
        .append("rotation:")
        .append(data.rotationDegrees)

      if (data.hasRotationPivot) {
        builder
          .append('@')
          .append(data.rotationPivotX)
          .append('x')
          .append(data.rotationPivotY)
      }

      builder.append(KEY_SEPARATOR)
    }

    if (data.hasSize()) {
      builder
        .append("resize:")
        .append(data.targetWidth)
        .append('x')
        .append(data.targetHeight)

      builder.append(KEY_SEPARATOR)
    }

    if (data.centerCrop) {
      builder
        .append("centerCrop:")
        .append(data.centerCropGravity)
        .append(KEY_SEPARATOR)
    } else if (data.centerInside) {
      builder
        .append("centerInside")
        .append(KEY_SEPARATOR)
    }

    for (i in data.transformations.indices) {
      builder.append(data.transformations[i].key())
      builder.append(KEY_SEPARATOR)
    }

    return builder.toString()
  }

  /** Builder for creating [Request] instances.  */
  class Builder {
    var uri: Uri? = null
    var resourceId = 0
    var stableKey: String? = null
    var targetWidth = 0
    var targetHeight = 0
    var centerCrop = false
    var centerCropGravity = 0
    var centerInside = false
    var onlyScaleDown = false
    var rotationDegrees = 0f
    var rotationPivotX = 0f
    var rotationPivotY = 0f
    var hasRotationPivot = false
    var transformations: MutableList<Transformation>? = null
    var config: Config? = null
    var priority: Priority? = null

    /** Internal use only. Used by [DeferredRequestCreator]. */
    var tag: Any? = null
    var memoryPolicy = 0
    var networkPolicy = 0
    var headers: Headers? = null

    internal constructor(
      uri: Uri?,
      resourceId: Int,
      bitmapConfig: Config?
    ) {
      this.uri = uri
      this.resourceId = resourceId
      config = bitmapConfig
    }

    internal constructor(request: Request) {
      uri = request.uri
      resourceId = request.resourceId
      stableKey = request.stableKey
      targetWidth = request.targetWidth
      targetHeight = request.targetHeight
      centerCrop = request.centerCrop
      centerInside = request.centerInside
      centerCropGravity = request.centerCropGravity
      rotationDegrees = request.rotationDegrees
      rotationPivotX = request.rotationPivotX
      rotationPivotY = request.rotationPivotY
      hasRotationPivot = request.hasRotationPivot
      onlyScaleDown = request.onlyScaleDown
      transformations = request.transformations.toMutableList()
      config = request.config
      priority = request.priority
      memoryPolicy = request.memoryPolicy
      networkPolicy = request.networkPolicy
      headers = request.headers
    }

    fun hasImage(): Boolean {
      return uri != null || resourceId != 0
    }

    fun hasSize(): Boolean {
      return targetWidth != 0 || targetHeight != 0
    }

    fun hasPriority(): Boolean {
      return priority != null
    }

    /**
     * Set the target image Uri.
     *
     * This will clear an image resource ID if one is set.
     */
    fun setUri(uri: Uri) = apply {
      this.uri = uri
      resourceId = 0
    }

    /**
     * Set the target image resource ID.
     *
     * This will clear an image Uri if one is set.
     */
    fun setResourceId(@DrawableRes resourceId: Int) = apply {
      require(resourceId != 0) { "Image resource ID may not be 0." }
      this.resourceId = resourceId
      uri = null
    }

    /**
     * Assign a tag to this request.
     */
    fun tag(tag: Any) = apply {
      check(this.tag == null) { "Tag already set." }
      this.tag = tag
    }

    /** Internal use only. Used by [DeferredRequestCreator].  */
    fun clearTag() = apply {
      tag = null
    }

    /**
     * Resize the image to the specified size in pixels.
     * Use 0 as desired dimension to resize keeping aspect ratio.
     */
    fun resize(@Px targetWidth: Int, @Px targetHeight: Int) = apply {
      require(targetWidth >= 0) { "Width must be positive number or 0." }
      require(targetHeight >= 0) { "Height must be positive number or 0." }
      require(
        !(targetHeight == 0 && targetWidth == 0)
      ) { "At least one dimension has to be positive number." }
      this.targetWidth = targetWidth
      this.targetHeight = targetHeight
    }

    /** Execute request using the specified priority.  */
    fun priority(priority: Priority) = apply {
      check(this.priority == null) { "Priority already set." }
      this.priority = priority
    }

    /**
     * Specifies the [NetworkPolicy] to use for this request. You may specify additional
     * policy options using the varargs parameter.
     */
    fun networkPolicy(
      policy: NetworkPolicy,
      vararg additional: NetworkPolicy
    ) = apply {
      networkPolicy = networkPolicy or policy.index

      for (i in additional.indices) {
        this.networkPolicy = this.networkPolicy or additional[i].index
      }
    }

    /** Create the immutable [Request] object.  */
    fun build(): Request {
      check(!(centerInside && centerCrop)) {
        "Center crop and center inside can not be used together."
      }
      check(!(centerCrop && targetWidth == 0 && targetHeight == 0)) {
        "Center crop requires calling resize with positive width and height."
      }
      check(!(centerInside && targetWidth == 0 && targetHeight == 0)) {
        "Center inside requires calling resize with positive width and height."
      }
      if (priority == null) {
        priority = NORMAL
      }
      return Request(this)
    }
  }

  internal companion object {
    private val TOO_LONG_LOG = SECONDS.toNanos(5)
    private const val KEY_PADDING = 50 // Determined by exact science.
    const val KEY_SEPARATOR = '\n'
  }
}
