package com.example.selector.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class PreviewTitleBar : TitleBar {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
    }

    fun setTitleBarStyle() {
        super.setTitleBarStyle()
        val titleBarStyle: TitleBarStyle = PictureSelectionConfig.selectorStyle.getTitleBarStyle()
        if (StyleUtils.checkStyleValidity(titleBarStyle.getPreviewTitleBackgroundColor())) {
            setBackgroundColor(titleBarStyle.getPreviewTitleBackgroundColor())
        } else if (StyleUtils.checkSizeValidity(titleBarStyle.getTitleBackgroundColor())) {
            setBackgroundColor(titleBarStyle.getTitleBackgroundColor())
        }
        if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleLeftBackResource())) {
            ivLeftBack.setImageResource(titleBarStyle.getTitleLeftBackResource())
        } else if (StyleUtils.checkStyleValidity(titleBarStyle.getPreviewTitleLeftBackResource())) {
            ivLeftBack.setImageResource(titleBarStyle.getPreviewTitleLeftBackResource())
        }
        rlAlbumBg.setOnClickListener(null)
        viewAlbumClickArea.setOnClickListener(null)
        val layoutParams: RelativeLayout.LayoutParams = rlAlbumBg.getLayoutParams() as LayoutParams
        layoutParams.removeRule(RelativeLayout.END_OF)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        rlAlbumBg.setBackgroundResource(R.drawable.ps_ic_trans_1px)
        tvCancel.setVisibility(GONE)
        ivArrow.setVisibility(GONE)
        viewAlbumClickArea.setVisibility(GONE)
    }
}
