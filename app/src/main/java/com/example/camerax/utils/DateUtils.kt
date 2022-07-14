package com.example.camerax.utils

import java.text.SimpleDateFormat

object DateUtils {
    private val sf = SimpleDateFormat("yyyyMMddHHmmssSSS")

    // Create filename based on timestamp
    // prefix prefix name
    // return

    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + sf.format(millis)
    }
}
