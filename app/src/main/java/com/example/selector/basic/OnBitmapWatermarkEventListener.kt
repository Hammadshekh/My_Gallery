package com.example.selector.basic

import android.content.Context
import com.example.selector.interfaces.OnKeyValueResultCallbackListener

interface OnBitmapWatermarkEventListener {
    /**
     * Add bitmap watermark
     *
     * @param context
     * @param srcPath
     * @param mimeType
     */
    fun onAddBitmapWatermark(
        context: Context?,
        srcPath: String?,
        mimeType: String?,
        call: OnKeyValueResultCallbackListener?,
    )
}

