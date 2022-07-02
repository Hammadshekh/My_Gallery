package com.example.mygallery.files

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class CustomTitleBar : TitleBar, View.OnClickListener {
    val titleCancelView: TextView
        get() = tvCancel

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
    }

    protected fun inflateLayout() {
        inflate(getContext(), R.layout.ps_custom_title_bar, this)
    }

    fun setTitleBarStyle() {
        super.setTitleBarStyle()
    }
}