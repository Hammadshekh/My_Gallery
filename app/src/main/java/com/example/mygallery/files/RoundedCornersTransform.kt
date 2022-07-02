package com.example.mygallery.files

import android.graphics.*

/*
class RoundedCornersTransform(private val roundingRadius: Float) : Transformation {
    fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }
        val bitmap = Bitmap.createBitmap(size, size, source.config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader =
            BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true
        val r = if (size / roundingRadius > 0) roundingRadius else 8f
        canvas.drawRoundRect(RectF(0, 0, source.width.toFloat(), source.height.toFloat()),
            r,
            r,
            paint)
        squaredBitmap.recycle()
        return bitmap
    }

    fun key(): String {
        return "rounded_corners"
    }
}
*/
