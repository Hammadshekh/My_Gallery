package com.example.compress.io

class IntegerArrayAdapter(override val tag: String?) : ArrayAdapterInterface<IntArray?> {
   override fun getArrayLength(array: IntArray?): Int {
        return array?.size!!
    }

    override fun newArray(length: Int): IntArray {
        return IntArray(length)
    }

    override val elementSizeInBytes: Int
        get() = 4

    companion object {
        val tag = "IntegerArrayPool"
            get() = Companion.field
    }


}
