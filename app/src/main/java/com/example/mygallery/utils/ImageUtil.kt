package com.example.mygallery.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas

object ImageUtil {
    /**
     * 设置水印图片在左上角
     *
     * @param context     上下文
     * @param src
     * @param watermark
     * @param paddingLeft
     * @param paddingTop
     * @return
     */
    fun createWaterMaskLeftTop(
        context: Context?,
        src: Bitmap?,
        watermark: Bitmap,
        paddingLeft: Int,
        paddingTop: Int,
    ): Bitmap? {
        return createWaterMaskBitmap(src, watermark,
            DensityUtil.dip2px(context, paddingLeft), DensityUtil.dip2px(context, paddingTop))
    }

    /**
     * 设置水印图片到右上角
     *
     * @param context
     * @param src
     * @param watermark
     * @param paddingRight
     * @param paddingTop
     * @return
     */
    fun createWaterMaskRightTop(
        context: Context?,
        src: Bitmap,
        watermark: Bitmap,
        paddingRight: Int,
        paddingTop: Int,
    ): Bitmap? {
        return createWaterMaskBitmap(src, watermark,
            src.width - watermark.width - DensityUtil.dip2px(context, paddingRight),
            DensityUtil.dip2px(context, paddingTop))
    }

    private fun createWaterMaskBitmap(
        src: Bitmap?,
        watermark: Bitmap,
        paddingLeft: Int,
        paddingTop: Int,
    ): Bitmap? {
        if (src == null) {
            return null
        }
        val width = src.width
        val height = src.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(src, 0f, 0f, null)
        canvas.drawBitmap(watermark, paddingLeft.toFloat(), paddingTop.toFloat(), null)
        canvas.save()
        canvas.restore()
        return newBitmap
    }
}