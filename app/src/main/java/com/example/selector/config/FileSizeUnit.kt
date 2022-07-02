package com.example.selector.config

object FileSizeUnit {
    const val KB: Long = 1024
    const val MB = (1024 * 1024).toLong()
    const val GB = (1024 * 1024 * 1024).toLong()
    const val ACCURATE_GB = 1000 * 1000 * 1000
    const val ACCURATE_MB = 1000 * 1000
    const val ACCURATE_KB = 1000
}