package com.example.camerax.listener

import android.widget.ImageView

interface ImageCallbackListener {

    // Load image callback
    // @param url resource url
    // @param imageView image rendering control
    fun onLoadImage(url: String?, imageView: ImageView?)
}