package com.example.ucrop.utils

import android.graphics.*
import android.graphics.drawable.Drawable

class FastBitmapDrawable(var bitmap: Bitmap?) : Drawable() {
    private val mPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private var mBitmap: Bitmap? = null
    private var mAlpha = 255
    private var mWidth = 0
    private var mHeight = 0
    override fun draw(canvas: Canvas) {
        if (mBitmap != null && !mBitmap!!.isRecycled) {
            canvas.drawBitmap(mBitmap!!, null, bounds, mPaint)
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setFilterBitmap(filterBitmap: Boolean) {
        mPaint.isFilterBitmap = filterBitmap
    }

    override fun getAlpha(): Int {
        return mAlpha
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
        mPaint.alpha = alpha
    }

    override fun getIntrinsicWidth(): Int {
        return mWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mHeight
    }

    override fun getMinimumWidth(): Int {
        return mWidth
    }

    override fun getMinimumHeight(): Int {
        return mHeight
    }
    @JvmName("getBitmap1")
    fun getBitmap(): Bitmap? {
        return mBitmap
    }

}
