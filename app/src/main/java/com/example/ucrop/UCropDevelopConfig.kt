package com.example.ucrop

object UCropDevelopConfig {
    /**
     * 图片加载引擎
     */
    var imageEngine: UCropImageEngine? = null

    /**
     * 释放监听器
     */
    fun destroy() {
        imageEngine = null
    }
}