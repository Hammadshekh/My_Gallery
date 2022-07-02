package com.example.selector.interfaces

import android.content.Context

interface OnVideoThumbnailEventListener {
    /**
     * video thumbnail
     *
     * @param context
     * @param videoPath
     */
    fun onVideoThumbnail(
        context: Context?,
        videoPath: String?,
        call: OnKeyValueResultCallbackListener?,
    )
}
