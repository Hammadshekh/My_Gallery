package com.example.selector.interfaces

import com.luck.picture.lib.entity.LocalMedia
import java.util.*

interface OnResultCallbackListener<T> : OnResultCallbackListener<LocalMedia>,
    OnResultCallbackListener<LocalMedia> {
    /**
     * return LocalMedia result
     *
     * @param result
     */
    fun onResult(result: ArrayList<T>?)

    /**
     * Cancel
     */
    fun onCancel()
}
