package com.example.mygallery.files

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.mygallery.R

class CustomCompleteSelectView : CompleteSelectView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
    }

    private fun inflateLayout() {
        LayoutInflater.from(requireContext()).inflate(R.layout.ps_custom_complete_selected_layout, this)
    }

    fun setCompleteSelectViewStyle() {
        super.setCompleteSelectViewStyle()
    }
}