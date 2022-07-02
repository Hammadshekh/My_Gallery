package com.example.compress.io

internal interface ArrayAdapterInterface<T> {
    /**
     * TAG for logging.
     */
    val tag: String?

    /**
     * Return the length of the given array.
     */
    fun getArrayLength(array: T): Int

    /**
     * Allocate and return an array of the specified size.
     */
    fun newArray(length: Int): T

    /**
     * Return the size of an element in the array in bytes (e.g. for int return 4).
     */
    val elementSizeInBytes: Int
}
