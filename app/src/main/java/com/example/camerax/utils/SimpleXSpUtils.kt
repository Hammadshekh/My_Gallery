package com.example.camerax.utils

import android.content.Context
import android.content.SharedPreferences

object SimpleXSpUtils {
    private var pictureSpUtils: SharedPreferences? = null
    private fun getSp(context: Context): SharedPreferences? {
        if (pictureSpUtils == null) {
            pictureSpUtils =
                context.getSharedPreferences(CustomCameraConfig.SP_NAME, Context.MODE_PRIVATE)
        }
        return pictureSpUtils
    }

    fun putBoolean(context: Context, key: String?, value: Boolean) {
        getSp(context)!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
        return getSp(context)!!.getBoolean(key, defValue)
    }
}
