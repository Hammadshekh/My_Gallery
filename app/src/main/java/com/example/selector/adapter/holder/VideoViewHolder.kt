package com.example.selector.adapter.holder

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

class VideoViewHolder(itemView: View, config: PictureSelectionConfig?) :
    BaseRecyclerMediaHolder(itemView, config) {
    private val tvDuration: TextView
    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        tvDuration.setText(DateUtils.formatDurationTime(media.getDuration()))
    }

    init {
        tvDuration = itemView.findViewById(R.id.tv_duration)
        val adapterStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle.getSelectMainStyle()
        val drawableLeft: Int = adapterStyle.getAdapterDurationDrawableLeft()
        if (StyleUtils.checkStyleValidity(drawableLeft)) {
            tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, 0, 0, 0)
        }
        val textSize: Int = adapterStyle.getAdapterDurationTextSize()
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvDuration.textSize = textSize.toFloat()
        }
        val textColor: Int = adapterStyle.getAdapterDurationTextColor()
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvDuration.setTextColor(textColor)
        }
        val shadowBackground: Int = adapterStyle.getAdapterDurationBackgroundResources()
        if (StyleUtils.checkStyleValidity(shadowBackground)) {
            tvDuration.setBackgroundResource(shadowBackground)
        }
        val durationGravity: IntArray = adapterStyle.getAdapterDurationGravity()
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
