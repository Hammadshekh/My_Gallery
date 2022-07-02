package com.example.compress.io

class ByteArrayAdapter : ArrayAdapterInterface<ByteArray?> {
    fun getArrayLength(array: ByteArray): Int {
        return array.size
    }

    override fun newArray(length: Int): ByteArray {
        return ByteArray(length)
    }

    override val elementSizeInBytes: Int
        get() = 1

    companion object {
        val tag = "ByteArrayPool"
            get() = Companion.field
    }
}
