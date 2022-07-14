package com.example.compress.io

class IntegerArrayAdapter : ArrayAdapterInterface<IntArray> {


    override fun newArray(length: Int): IntArray {
        return IntArray(length)
    }

    override val elementSizeInBytes: Int
        get() = 4

    companion object {
        val tag = "IntegerArrayPool"
    }

    override val tag: String?
        get() = TODO("Not yet implemented")

    override fun getArrayLength(array: IntArray): Int {
        return array.size
    }


}
