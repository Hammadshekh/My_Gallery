package com.example.mygallery.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import java.io.File
import java.lang.Exception

object ImageCacheUtils {
    /**
     * Get image cache based on url
     * Glide 4.x please call this method
     * Note: This method must be performed in a child thread
     *
     * @param context
     * @param url
     * @return
     */
    fun getCacheFileTo4x(context: Context?, url: String?): File? {
        return try {
            Glide.with(context!!).downloadOnly().load(url).submit().get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 根据url获取图片缓存
     * Glide 3.x请调用此方法
     * 注意：此方法必须在子线程中进行
     *
     * @param context
     * @param url
     * @return
     */
    fun getCacheFileTo3x(context: Context?, url: String?): File? {
        return try {
            Glide.with(context!!).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
