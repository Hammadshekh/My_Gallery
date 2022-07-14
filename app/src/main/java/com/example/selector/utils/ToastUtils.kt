package com.example.selector.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.example.selector.app.PictureAppMaster
import com.example.selector.threads.PictureThreadUtils

object ToastUtils {
    /**
     * show toast content
     *
     * @param context
     * @param text
     */
    fun showToast(context: Context, text: String?) {
        if (isFastDoubleClick && TextUtils.equals(text, mLastText)) {
            return
        }
        var appContext: Context? = PictureAppMaster.instance!!.appContext
        if (appContext == null) {
            appContext = context.applicationContext
        }
        if (PictureThreadUtils.isInUiThread) {
            Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show()
            mLastText = text
        } else {
            PictureThreadUtils.runOnUiThread(Runnable {
                var appContext: Context? = PictureAppMaster.instance!!.appContext
                if (appContext == null) {
                    appContext = context.applicationContext
                }
                Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show()
                mLastText = text
            })
        }
    }

    private const val TIME: Long = 1000
    private var lastClickTime: Long = 0
    private var mLastText: String? = null
    val isFastDoubleClick: Boolean
        get() {
            val time = System.currentTimeMillis()
            if (time - lastClickTime < TIME) {
                return true
            }
            lastClickTime = time
            return false
        }
}
