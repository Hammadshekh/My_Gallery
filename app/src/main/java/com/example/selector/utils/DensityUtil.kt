package com.example.selector.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.selector.immersive.RomUtils

object DensityUtil {
    /**
     * 获取屏幕真实宽度
     *
     * @param context
     * @return
     */
    fun getRealScreenWidth(context: Context): Int {
        val wm =
            context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

    /**
     * 获取屏幕真实高度
     *
     * @param context
     * @return
     */
    fun getRealScreenHeight(context: Context): Int {
        val wm =
            context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

    /**
     * 获取屏幕高度(不包含状态栏高度)
     *
     * @param context
     * @return
     */
    fun getScreenHeight(context: Context): Int {
        return getRealScreenHeight(context) - getStatusNavigationBarHeight(context)
    }

    /**
     * 获取状态栏和导航栏高度
     *
     * @param context
     * @return
     */
    private fun getStatusNavigationBarHeight(context: Context): Int {
        return if (isNavBarVisible(context)) {
            getStatusBarHeight(context) + getNavigationBarHeight(context)
        } else {
            getStatusBarHeight(context)
        }
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

    /**
     * Return whether the navigation bar visible.
     *
     * Call it in onWindowFocusChanged will get right result.
     *
     * @param window The window.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isNavBarVisible(context: Context?): Boolean {
        var isVisible = false
        if (context !is Activity) {
            return false
        }
        val activity = context
        val window = activity.window
        val decorView = window.decorView as ViewGroup
        var i = 0
        val count = decorView.childCount
        while (i < count) {
            val child = decorView.getChildAt(i)
            val id = child.id
            if (id != View.NO_ID) {
                val resourceEntryName = getResNameById(activity, id)
                if ("navigationBarBackground" == resourceEntryName && child.visibility == View.VISIBLE) {
                    isVisible = true
                    break
                }
            }
            i++
        }
        if (isVisible) {
            // For Samsung mobile phones, non-OneUI2 versions below android10, such as s8, note8 and other devices,
            // There is a bug in the display of the navigation bar: "When the user hides the navigation bar and displays the input method, the navigation bar will follow the display", which will lead to a wrong judgment after hiding the input method
            // This issue is fixed in OneUI 2 & android 10 version
            if (RomUtils.isSamsung
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            ) {
                try {
                    return Settings.Global.getInt(activity.contentResolver,
                        "navigationbar_hide_bar_enabled") == 0
                } catch (ignore: Exception) {
                }
            }
            val visibility = decorView.systemUiVisibility
            isVisible = visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0
        }
        return isVisible
    }

    /**
     * getResNameById
     *
     * @param context
     * @param id
     * @return
     */
    private fun getResNameById(context: Context, id: Int): String {
        return try {
            context.resources.getResourceEntryName(id)
        } catch (ignore: Exception) {
            ""
        }
    }

    /**
     * 获取导航栏宽度
     *
     * @param context
     * @return
     */
    @TargetApi(14)
    fun getNavigationBarWidth(context: Context): Int {
        val result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (isNavBarVisible(context)) {
                return getInternalDimensionSize(context, "navigation_bar_width")
            }
        }
        return result
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    @TargetApi(14)
    fun getNavigationBarHeight(context: Context): Int {
        val result = 0
        val res = context.resources
        val mInPortrait = res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (isNavBarVisible(context)) {
            val key: String
            key = if (mInPortrait) {
                "navigation_bar_height"
            } else {
                "navigation_bar_height_landscape"
            }
            return getInternalDimensionSize(context, key)
        }
        return result
    }

    private fun getInternalDimensionSize(context: Context, key: String): Int {
        val result = 0
        try {
            val resourceId = Resources.getSystem().getIdentifier(key, "dimen", "android")
            if (resourceId > 0) {
                val sizeOne = context.resources.getDimensionPixelSize(resourceId)
                val sizeTwo = Resources.getSystem().getDimensionPixelSize(resourceId)
                return if (sizeTwo >= sizeOne) {
                    sizeTwo
                } else {
                    val densityOne = context.resources.displayMetrics.density
                    val densityTwo = Resources.getSystem().displayMetrics.density
                    val f = sizeOne * densityTwo / densityOne
                    (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
                }
            }
        } catch (ignored: NotFoundException) {
            return 0
        }
        return result
    }

    @SuppressLint("NewApi")
    private fun getSmallestWidthDp(activity: Activity): Float {
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.windowManager.defaultDisplay.getRealMetrics(metrics)
        } else {
            activity.windowManager.defaultDisplay.getMetrics(metrics)
        }
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        return widthDp.coerceAtMost(heightDp)
    }

    fun isNavigationAtBottom(activity: Activity): Boolean {
        val res = activity.resources
        val mInPortrait = res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return getSmallestWidthDp(activity) >= 600 || mInPortrait
    }

    /**
     * dp2px
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.applicationContext.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
