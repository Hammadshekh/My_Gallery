package com.example.compress.adapter

import java.io.IOException
import java.io.InputStream

interface InputStreamProvider {
    @Throws(IOException::class)
    fun open(): InputStream?
    fun close()
    val index: Int
    val path: String?
}

