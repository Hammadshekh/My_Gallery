package com.example.camerax.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.example.mygallery.R

class FocusImageView : AppCompatImageView {
    private var mFocusImg = 0
    private var mFocusSucceedImg = 0
    private var mFocusFailedImg = 0
    private var mAnimation: Animation? = null
    private var mHandler: Handler? = null

    @Volatile
    private var isDisappear = false

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView)
        mFocusImg = typedArray.getResourceId(R.styleable.FocusImageView_focus_focusing,
            R.drawable.focus_focusing)
        mFocusSucceedImg = typedArray.getResourceId(R.styleable.FocusImageView_focus_success,
            R.drawable.focus_focused)
        mFocusFailedImg = typedArray.getResourceId(R.styleable.FocusImageView_focus_error,
            R.drawable.focus_failed)
        typedArray.recycle()
    }

    private fun init() {
        visibility = GONE
        mAnimation = AnimationUtils.loadAnimation(context, R.anim.focusview_show)
        mHandler = Handler(Looper.getMainLooper())
    }

    fun setDisappear(disappear: Boolean) {
        isDisappear = disappear
    }

    fun startFocus(point: Point) {
        val params = layoutParams as RelativeLayout.LayoutParams
        params.topMargin = point.y - measuredHeight / 2
        params.leftMargin = point.x - measuredWidth / 2
        layoutParams = params
        visibility = VISIBLE
        setFocusResource(mFocusImg)
        startAnimation(mAnimation)
    }

    fun onFocusSuccess() {
        if (isDisappear) {
            setFocusResource(mFocusSucceedImg)
        }
        mHandler!!.removeCallbacks(null, null)
        mHandler!!.postDelayed({ setFocusGone() }, DELAY_MILLIS)
    }

    fun onFocusFailed() {
        if (isDisappear) {
            setFocusResource(mFocusFailedImg)
        }
        mHandler!!.removeCallbacks(null, null)
        mHandler!!.postDelayed({ setFocusGone() }, DELAY_MILLIS)
    }

    private fun setFocusResource(@DrawableRes resId: Int) {
        setImageResource(resId)
    }

    private fun setFocusGone() {
        if (isDisappear) {
            visibility = GONE
        }
    }

    fun destroy() {
        mHandler!!.removeCallbacks(null, null)
        visibility = GONE
    }

    companion object {
        private const val DELAY_MILLIS: Long = 1000
    }
}