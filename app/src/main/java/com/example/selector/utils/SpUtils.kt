package com.example.selector.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.selector.config.PictureConfig

object SpUtils {
    private var pictureSpUtils: SharedPreferences? = null
    private fun getSp(context: Context): SharedPreferences? {
        if (pictureSpUtils == null) {
            pictureSpUtils =
                context.getSharedPreferences(PictureConfig.SP_NAME, Context.MODE_PRIVATE)
        }
        return pictureSpUtils
    }

    fun putString(context: Context, key: String?, value: String?) {
        getSp(context)!!.edit().putString(key, value).apply()
    }

    fun putBoolean(context: Context, key: String?, value: Boolean) {
        getSp(context)!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
        return getSp(context)!!.getBoolean(key, defValue)
    }
}
