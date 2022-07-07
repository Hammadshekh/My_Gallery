package com.example.selector.interfaces

import java.util.*


interface OnResultCallbackListener<T> {
    /**
     * return LocalMedia result
     *
     * @param result
     */
    fun onResult(result: ArrayList<T>);

    /**
     * Cancel
     */
    fun onCancel();
}