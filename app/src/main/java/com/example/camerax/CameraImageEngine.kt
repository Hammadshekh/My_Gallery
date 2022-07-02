package com.example.camerax

import android.content.Context
import android.widget.ImageView

interface CameraImageEngine {
    /**
     * load image source
     *
     * @param context
     * @param url
     * @param imageView
     */
    fun loadImage(context: Context?, url: String?, imageView: ImageView?)
}
