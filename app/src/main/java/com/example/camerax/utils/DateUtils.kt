package com.example.camerax.utils

import java.text.SimpleDateFormat

object DateUtils {
    private val sf = SimpleDateFormat("yyyyMMddHHmmssSSS")

    /**
     * 根据时间戳创建文件名
     *
     * @param prefix 前缀名
     * @return
     */
    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + sf.format(millis)
    }
}
