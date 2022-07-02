package com.example.ucrop.utils

import android.content.Context
import android.content.res.Resources

object DensityUtil {
    /**
     * dp2px
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.applicationContext.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId =
            Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return if (result == 0) dip2px(context, 26f) else result
    }
}
