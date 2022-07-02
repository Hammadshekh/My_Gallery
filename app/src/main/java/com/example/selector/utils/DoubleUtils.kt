package com.example.selector.utils

import android.os.SystemClock

object DoubleUtils {
    private const val TIME: Long = 600
    private var lastClickTime: Long = 0
    val isFastDoubleClick: Boolean
        get() {
            val time = SystemClock.elapsedRealtime()
            if (time - lastClickTime < TIME) {
                return true
            }
            lastClickTime = time
            return false
        }
}