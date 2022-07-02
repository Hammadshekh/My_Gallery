package com.example.selector.interfaces

interface OnCallbackListener<T> {
    /**
     * @param data
     */
    fun onCall(data: T)
}