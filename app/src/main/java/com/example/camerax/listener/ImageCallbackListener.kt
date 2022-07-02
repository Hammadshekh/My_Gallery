package com.example.camerax.listener

import android.widget.ImageView

interface ImageCallbackListener {
    /**
     * 加载图片回调
     *
     * @param url       资源url
     * @param imageView 图片渲染控件
     */
    fun onLoadImage(url: String?, imageView: ImageView?)
}