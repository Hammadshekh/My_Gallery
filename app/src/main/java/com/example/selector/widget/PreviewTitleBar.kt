package com.example.selector.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.style.TitleBarStyle
import com.example.selector.utils.StyleUtils

class PreviewTitleBar : TitleBar {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
    }

    override fun setTitleBarStyle() {
        super.setTitleBarStyle()
        val titleBarStyle: TitleBarStyle = PictureSelectionConfig.selectorStyle!!.titleBarStyle!!
        if (StyleUtils.checkStyleValidity(titleBarStyle.previewTitleBackgroundColor)) {
            setBackgroundColor(titleBarStyle.previewTitleBackgroundColor)
        } else if (StyleUtils.checkSizeValidity(titleBarStyle.titleBackgroundColor)) {
            setBackgroundColor(titleBarStyle.titleBackgroundColor)
        }
        if (StyleUtils.checkStyleValidity(titleBarStyle.titleLeftBackResource)) {
            ivLeftBack!!.setImageResource(titleBarStyle.titleLeftBackResource)
        } else if (StyleUtils.checkStyleValidity(titleBarStyle.previewTitleLeftBackResource)) {
            ivLeftBack!!.setImageResource(titleBarStyle.previewTitleLeftBackResource)
        }
        rlAlbumBg!!.setOnClickListener(null)
        viewAlbumClickArea?.setOnClickListener(null)
        val layoutParams: RelativeLayout.LayoutParams = rlAlbumBg!!.layoutParams as LayoutParams
        layoutParams.removeRule(RelativeLayout.END_OF)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        rlAlbumBg!!.setBackgroundResource(R.drawable.ps_ic_trans_1px)
    /*    tvCancel.setVisibility(GONE)
        ivArrow.setVisibility(GONE)*/
        viewAlbumClickArea!!.visibility = GONE
    }
}
