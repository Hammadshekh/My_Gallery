package com.example.compress.adapter

import com.example.compress.io.ArrayPoolProvide
import java.io.IOException
import java.io.InputStream

abstract class InputStreamAdapter : InputStreamProvider {
    @Throws(IOException::class)
    override fun open(): () -> InputStream? {
        return openInternal()
    }

    @Throws(IOException::class)
    abstract fun openInternal(): () -> InputStream?
    override fun close() {
        ArrayPoolProvide.instance?.clearMemory()
    }
}