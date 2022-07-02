package com.example.selector.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet

class MarqueeTextView : MediumBoldTextView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    val isFocused: Boolean
        get() = true
    val isSelected: Boolean
        get() = true

    protected fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused) {
            super.onFocusChanged(true, direction, previouslyFocusedRect)
        }
    }

    fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (hasWindowFocus) {
            super.onWindowFocusChanged(true)
        }
    }
}
