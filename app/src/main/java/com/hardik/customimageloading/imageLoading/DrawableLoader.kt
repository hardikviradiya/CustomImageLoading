package com.hardik.customimageloading.imageLoading

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

internal fun interface DrawableLoader {
  fun load(@DrawableRes resId: Int): Drawable?
}
