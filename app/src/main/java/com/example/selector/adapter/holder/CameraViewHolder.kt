package com.example.selector.adapter.holder

import android.view.View
import android.widget.TextView
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.StyleUtils

class CameraViewHolder(itemView: View) : BaseRecyclerMediaHolder(itemView) {
    init {
        val tvCamera = itemView.findViewById<TextView>(R.id.tvCamera)
        val adapterStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle?.selectMainStyle!!
        val background: Int = adapterStyle.adapterCameraBackgroundColor
        if (StyleUtils.checkStyleValidity(background)) {
            tvCamera.setBackgroundColor(background)
        }
        val drawableTop: Int = adapterStyle.adapterCameraDrawableTop
        if (StyleUtils.checkStyleValidity(drawableTop)) {
            tvCamera.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableTop, 0, 0)
        }
        val text: String = adapterStyle.adapterCameraText.toString()
        if (StyleUtils.checkTextValidity(text)) {
            tvCamera.text = text
        } else {
            if (PictureSelectionConfig.instance?.chooseMode == SelectMimeType.ofAudio()) {
                tvCamera.text = itemView.context.getString(R.string.ps_tape)
            }
        }
        val textSize: Int = adapterStyle.adapterCameraTextSize
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvCamera.textSize = textSize.toFloat()
        }
        val textColor: Int = adapterStyle.adapterCameraTextColor
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvCamera.setTextColor(textColor)
        }
    }
}
