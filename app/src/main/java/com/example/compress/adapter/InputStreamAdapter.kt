package com.example.compress.adapter

import java.io.IOException
import java.io.InputStream

abstract class InputStreamAdapter : InputStreamProvider {
    @Throws(IOException::class)
    fun open(): InputStream {
        return openInternal()
    }

    @Throws(IOException::class)
    abstract fun openInternal(): InputStream
    fun close() {
        ArrayPoolProvide.getInstance().clearMemory()
    }
}