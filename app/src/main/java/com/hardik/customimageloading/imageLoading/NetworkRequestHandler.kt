package com.hardik.customimageloading.imageLoading

import android.net.NetworkInfo
import com.hardik.customimageloading.imageLoading.BitmapUtils.decodeStream
import com.hardik.customimageloading.imageLoading.NetworkPolicy.Companion.isOfflineOnly
import com.hardik.customimageloading.imageLoading.NetworkPolicy.Companion.shouldReadFromDiskCache
import com.hardik.customimageloading.imageLoading.NetworkPolicy.Companion.shouldWriteToDiskCache
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.DISK
import com.hardik.customimageloading.imageLoading.ImageLoader.LoadedFrom.NETWORK
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

internal class NetworkRequestHandler(
  private val callFactory: Call.Factory
) : RequestHandler() {
  override fun canHandleRequest(data: Request): Boolean {
    val uri = data.uri ?: return false
    val scheme = uri.scheme
    return SCHEME_HTTP.equals(scheme, ignoreCase = true) ||
      SCHEME_HTTPS.equals(scheme, ignoreCase = true)
  }

  override fun load(imageLoader: ImageLoader, request: Request, callback: Callback) {
    val callRequest = createRequest(request)
    callFactory
      .newCall(callRequest)
      .enqueue(object : okhttp3.Callback {
        override fun onResponse(call: Call, response: Response) {
          if (!response.isSuccessful) {
            callback.onError(ResponseException(response.code()))
            return
          }

          // Cache response is only null when the response comes fully from the network. Both
          // completely cached and conditionally cached responses will have a non-null cache
          // response.
          val loadedFrom = if (response.cacheResponse() == null) NETWORK else DISK

          // Sometimes response content length is zero when requests are being replayed.
          // Haven't found root cause to this but retrying the request seems safe to do so.
          val body = response.body()
          if (loadedFrom == DISK && body!!.contentLength() == 0L) {
            body.close()
            callback.onError(
              ContentLengthException("Received response with 0 content-length header.")
            )
            return
          }
          if (loadedFrom == NETWORK && body!!.contentLength() > 0) {
            imageLoader.downloadFinished(body.contentLength())
          }
          try {
            val bitmap = decodeStream(body!!.source(), request)
            callback.onSuccess(Result.Bitmap(bitmap, loadedFrom))
          } catch (e: IOException) {
            body!!.close()
            callback.onError(e)
          }
        }

        override fun onFailure(call: Call, e: IOException) {
          callback.onError(e)
        }
      })
  }

  override val retryCount: Int
    get() = 2

  override fun shouldRetry(airplaneMode: Boolean, info: NetworkInfo?): Boolean =
    info == null || info.isConnected

  override fun supportsReplay(): Boolean = true

  private fun createRequest(request: Request): okhttp3.Request {
    var cacheControl: CacheControl? = null
    val networkPolicy = request.networkPolicy
    if (networkPolicy != 0) {
      cacheControl = if (isOfflineOnly(networkPolicy)) {
        CacheControl.FORCE_CACHE
      } else {
        val builder = CacheControl.Builder()
        if (!shouldReadFromDiskCache(networkPolicy)) {
          builder.noCache()
        }
        if (!shouldWriteToDiskCache(networkPolicy)) {
          builder.noStore()
        }
        builder.build()
      }
    }

    val uri = checkNotNull(request.uri) { "request.uri == null" }
    val builder = okhttp3.Request.Builder().url(uri.toString())
    if (cacheControl != null) {
      builder.cacheControl(cacheControl)
    }
    val requestHeaders = request.headers
    if (requestHeaders != null) {
      builder.headers(requestHeaders)
    }
    return builder.build()
  }

  internal class ContentLengthException(message: String) : RuntimeException(message)
  internal class ResponseException(
    val code: Int
  ) : RuntimeException("HTTP $code")

  private companion object {
    private const val SCHEME_HTTP = "http"
    private const val SCHEME_HTTPS = "https"
  }
}
