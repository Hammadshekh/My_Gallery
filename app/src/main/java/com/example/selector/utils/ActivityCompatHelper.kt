package com.example.selector.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

object ActivityCompatHelper {
    private const val MIN_FRAGMENT_COUNT = 1
    fun isDestroy(activity: Activity?): Boolean {
        return if (activity == null) {
            true
        } else activity.isFinishing || activity.isDestroyed
    }

    fun checkFragmentNonExits(activity: FragmentActivity, fragmentTag: String?): Boolean {
        if (isDestroy(activity)) {
            return false
        }
        val fragment = activity.supportFragmentManager.findFragmentByTag(fragmentTag)
        return fragment == null
    }

    fun assertValidRequest(context: Context?): Boolean {
        if (context is Activity) {
            return !isDestroy(context)
        } else if (context is ContextWrapper) {
            if (context.baseContext is Activity) {
                val activity = context.baseContext as Activity
                return !isDestroy(activity)
            }
        }
        return true
    }

    fun checkRootFragment(activity: FragmentActivity): Boolean {
        return if (isDestroy(activity)) {
            false
        } else activity.supportFragmentManager.backStackEntryCount == MIN_FRAGMENT_COUNT
    }
}
