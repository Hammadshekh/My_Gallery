package com.example.selector.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.example.mygallery.R

class RoundCornerRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    RelativeLayout(context, attrs, defStyleAttr) {
    private val path: Path
    private val cornerSize: Float
    private val isTopNormal: Boolean
    private val isBottomNormal: Boolean
    private val mRect = RectF()
    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        path.reset()
        mRect.right = w.toFloat()
        mRect.bottom = h.toFloat()
        var cornerRadii: FloatArray
        if (!isTopNormal && !isBottomNormal) {
            path.addRoundRect(mRect, cornerSize, cornerSize, Path.Direction.CW)
        } else {
            if (isTopNormal) {
                cornerRadii =
                    floatArrayOf(0f, 0f, 0f, 0f, cornerSize, cornerSize, cornerSize, cornerSize)
                path.addRoundRect(mRect, cornerRadii, Path.Direction.CW)
            }
            if (isBottomNormal) {
                cornerRadii =
                    floatArrayOf(cornerSize, cornerSize, cornerSize, cornerSize, 0f, 0f, 0f, 0f)
                path.addRoundRect(mRect, cornerRadii, Path.Direction.CW)
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs,
            R.styleable.PictureRoundCornerRelativeLayout,
            defStyleAttr,
            0)
        cornerSize = a.getDimension(R.styleable.PictureRoundCornerRelativeLayout_corners, 0f)
        isTopNormal = a.getBoolean(R.styleable.PictureRoundCornerRelativeLayout_topNormal, false)
        isBottomNormal =
            a.getBoolean(R.styleable.PictureRoundCornerRelativeLayout_bottomNormal, false)
        a.recycle()
        path = Path()
    }
}
