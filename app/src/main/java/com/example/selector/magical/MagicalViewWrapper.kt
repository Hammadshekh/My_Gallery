package com.example.selector.magical

import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout

class MagicalViewWrapper(private val viewWrapper: View) {
    private val params: MarginLayoutParams
    val width: Int
        get() = params.width
    val height: Int
        get() = params.height

    fun setWidth(width: Float) {
        params.width = Math.round(width)
        viewWrapper.layoutParams = params
    }

    fun setHeight(height: Float) {
        params.height = Math.round(height)
        viewWrapper.layoutParams = params
    }

    var marginTop: Int
        get() = params.topMargin
        set(m) {
            params.topMargin = m
            viewWrapper.layoutParams = params
        }
    var marginRight: Int
        get() = params.rightMargin
        set(mr) {
            params.rightMargin = mr
            viewWrapper.layoutParams = params
        }
    var marginLeft: Int
        get() = params.leftMargin
        set(mr) {
            params.leftMargin = mr
            viewWrapper.layoutParams = params
        }
    var marginBottom: Int
        get() = params.bottomMargin
        set(m) {
            params.bottomMargin = m
            viewWrapper.layoutParams = params
        }

    init {
        params = viewWrapper.layoutParams as MarginLayoutParams
        if (params is LinearLayout.LayoutParams) {
            params.gravity = Gravity.START
        }
    }
}
