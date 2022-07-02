package com.example.selector.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.style.BottomNavBarStyle
import com.example.selector.utils.StyleUtils

class PreviewBottomNavBar : BottomNavBar {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
    }

    override fun handleLayoutUI() {
        tvPreview?.visibility = GONE
        tvImageEditor?.setOnClickListener(this)
        tvImageEditor?.visibility = if (PictureSelectionConfig.onEditMediaEventListener != null) VISIBLE else GONE
    }

    fun isDisplayEditor(isHasVideo: Boolean) {
        tvImageEditor?.visibility = if (PictureSelectionConfig.onEditMediaEventListener != null && !isHasVideo) VISIBLE else GONE
    }

    val editor: TextView
        get() = tvImageEditor!!

    override fun setBottomNavBarStyle() {
        super.setBottomNavBarStyle()
        val bottomBarStyle: BottomNavBarStyle =
            PictureSelectionConfig.selectorStyle.getBottomBarStyle()
        if (StyleUtils.checkStyleValidity(bottomBarStyle.getBottomPreviewNarBarBackgroundColor())) {
            setBackgroundColor(bottomBarStyle.getBottomPreviewNarBarBackgroundColor())
        } else if (StyleUtils.checkSizeValidity(bottomBarStyle.getBottomNarBarBackgroundColor())) {
            setBackgroundColor(bottomBarStyle.getBottomNarBarBackgroundColor())
        }
    }

    override fun onClick(view: View) {
        super.onClick(view)
        if (view.id == R.id.ps_tv_editor) {
            if (bottomNavBarListener != null) {
                bottomNavBarListener.onEditImage()
            }
        }
    }
}
