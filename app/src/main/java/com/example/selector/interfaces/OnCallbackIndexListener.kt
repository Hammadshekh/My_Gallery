package com.example.selector.interfaces

interface OnCallbackIndexListener<T> {
    /**
     * @param data
     * @param index
     */
    fun onCall(data: T, index: Int)
}