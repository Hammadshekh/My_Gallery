package com.example.selector.interfaces

import android.content.Context
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

interface OnPreviewInterceptListener {
    /**
     * Custom preview event
     *
     * @param context
     * @param position         preview current position
     * @param totalNum         source total num
     * @param page             page
     * @param currentBucketId  current source bucket id
     * @param currentAlbumName current album name
     * @param isShowCamera     current album show camera
     * @param data             preview source
     * @param isBottomPreview  from bottomNavBar preview mode
     */
    fun onPreview(
        context: Context?, position: Int, totalNum: Int, page: Int,
        currentBucketId: Long, currentAlbumName: String?, isShowCamera: Boolean,
        data: ArrayList<LocalMedia>, isBottomPreview: Boolean,
    )
}
