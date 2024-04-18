package com.hardik.customimageloading.utils

import android.content.res.Resources


class Util {
    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }
}