package com.example.mygallery.engine

import android.content.Context
import android.widget.ImageView

interface ImageEngine {
    fun loadImage(context: Context?, url: String?, imageView: ImageView?)
    fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        maxWidth: Int,
        maxHeight: Int,
    )

    fun loadAlbumCover(context: Context?, url: String?, imageView: ImageView?)
    fun loadGridImage(context: Context?, url: String?, imageView: ImageView?)

    // When the recyclerview slides quickly, the callback can be used to pause the loading of resources
    fun pauseRequests(context: Context?)

    // When the recyclerview is slow or stops sliding, the callback can do some operations to restore resource loading
    fun resumeRequests(context: Context?)
}