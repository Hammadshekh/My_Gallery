package com.example.camerax.listener

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener

class CameraXPreviewViewTouchListener(context: Context?) :
    OnTouchListener {
    private val mGestureDetector: GestureDetector
    private val mScaleGestureDetector: ScaleGestureDetector
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        if (!mScaleGestureDetector.isInProgress) {
            mGestureDetector.onTouchEvent(event)
        }
        return true
    }

    //zoom monitor
    private var onScaleGestureListener: OnScaleGestureListener =
        object : SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val delta = detector.scaleFactor
                if (mCustomTouchListener != null) {
                    mCustomTouchListener!!.zoom(delta)
                }
                return true
            }
        }
    private var onGestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {}
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (mCustomTouchListener != null) {
                mCustomTouchListener!!.click(e.x, e.y)
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (mCustomTouchListener != null) {
                mCustomTouchListener!!.doubleClick(e.x, e.y)
            }
            return true
        }
    }
    private var mCustomTouchListener: CustomTouchListener? = null

    interface CustomTouchListener {
        // enlarge
        fun zoom(delta: Float)

        //click
        fun click(x: Float, y: Float)

        //double click
        fun doubleClick(x: Float, y: Float)
    }

    fun setCustomTouchListener(customTouchListener: CustomTouchListener?) {
        mCustomTouchListener = customTouchListener
    }

    init {
        mGestureDetector = GestureDetector(context, onGestureListener)
        mScaleGestureDetector = ScaleGestureDetector(context, onScaleGestureListener)
    }
}
