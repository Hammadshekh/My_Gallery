package com.example.ucrop.callback

import android.graphics.RectF

interface OverlayViewChangeListener {
    fun onCropRectUpdated(cropRect: RectF?)
    fun postTranslate(deltaX: Float, deltaY: Float)
}