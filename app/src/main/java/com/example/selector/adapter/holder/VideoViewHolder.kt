package com.example.selector.adapter.holder

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.DateUtils
import com.example.selector.utils.StyleUtils
import com.luck.picture.lib.entity.LocalMedia

class VideoViewHolder(itemView: View, config: PictureSelectionconfig) :
    BaseRecyclerMediaHolder(itemView, config) {
    private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        tvDuration.text = DateUtils.formatDurationTime(media.duration)
    }

    init {
        val adapterStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle?.selectMainStyle!!
        val drawableLeft: Int = adapterStyle.adapterDurationDrawableLeft
        if (StyleUtils.checkStyleValidity(drawableLeft)) {
            tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, 0, 0, 0)
        }
        val textSize: Int = adapterStyle.adapterDurationTextSize
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvDuration.textSize = textSize.toFloat()
        }
        val textColor: Int = adapterStyle.adapterDurationTextColor
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvDuration.setTextColor(textColor)
        }
        val shadowBackground: Int = adapterStyle.adapterDurationBackgroundResources
        if (StyleUtils.checkStyleValidity(shadowBackground)) {
            tvDuration.setBackgroundResource(shadowBackground)
        }
        val durationGravity: IntArray = adapterStyle.adapterDurationGravity!!
        if (StyleUtils.checkArrayValidity(durationGravity)) {
            if (tvDuration.layoutParams is RelativeLayout.LayoutParams) {
                (tvDuration.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                for (i in durationGravity) {
                    (tvDuration.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }
    }
}
