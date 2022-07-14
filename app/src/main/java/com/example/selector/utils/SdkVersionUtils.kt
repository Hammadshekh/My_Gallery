package com.example.selector.utils

import android.os.Build

object SdkVersionUtils {
    const val R = 30

    /**
     * 判断是否是低于Android LOLLIPOP版本
     *
     * @return
     */
    val isMinM: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M

    /**
     * 判断是否是Android O版本
     *
     * @return
     */
    val isO: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /**
     * 判断是否是Android N版本
     *
     * @return
     */
    val isMaxN: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    /**
     * 判断是否是Android N版本
     *
     * @return
     */
    val isN: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.N

    /**
     * 判断是否是Android Q版本
     *
     * @return
     */
    val isQ: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * Determine whether it is the Android R version
     *
     * @return
     */
    fun isR(): Boolean {
        return Build.VERSION.SDK_INT >= R
    }
}
