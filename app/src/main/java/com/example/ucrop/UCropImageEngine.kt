package com.example.ucrop

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView

interface UCropImageEngine {
    /**
     * load image source
     *
     * @param context
     * @param url
     * @param imageView
     */
    fun loadImage(context: Context?, url: String?, imageView: ImageView?)

    /**
     * load image source
     *
     * @param context
     * @param url
     * @param maxWidth
     * @param maxHeight
     * @param call
     */
    @Deprecated("")
    fun loadImage(
        context: Context?,
        url: Uri?,
        maxWidth: Int,
        maxHeight: Int,
        call: OnCallbackListener<Bitmap?>?,
    )


    interface OnCallbackListener<T> {
        /**
         * @param data
         */
        fun onCall(data: T)
    }
}
