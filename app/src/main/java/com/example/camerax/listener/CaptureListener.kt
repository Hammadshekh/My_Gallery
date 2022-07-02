package com.example.camerax.listener

interface CaptureListener {
    fun takePictures()
    fun recordShort(time: Long)
    fun recordStart()
    fun recordEnd(time: Long)
    fun changeTime(duration: Long)
    fun recordZoom(zoom: Float)
    fun recordError()
}
