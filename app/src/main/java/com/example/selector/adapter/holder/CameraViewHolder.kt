package com.example.selector.adapter.holder

import android.view.View
import android.widget.TextView

class CameraViewHolder(itemView: View) : BaseRecyclerMediaHolder(itemView) {
    init {
        val tvCamera = itemView.findViewById<TextView>(R.id.tvCamera)
        val adapterStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle.getSelectMainStyle()
        val background: Int = adapterStyle.getAdapterCameraBackgroundColor()
        if (StyleUtils.checkStyleValidity(background)) {
            tvCamera.setBackgroundColor(background)
        }
        val drawableTop: Int = adapterStyle.getAdapterCameraDrawableTop()
        if (StyleUtils.checkStyleValidity(drawableTop)) {
            tvCamera.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableTop, 0, 0)
        }
        val text: String = adapterStyle.getAdapterCameraText()
        if (StyleUtils.checkTextValidity(text)) {
            tvCamera.text = text
        } else {
            if (PictureSelectionConfig.getInstance().chooseMode === SelectMimeType.ofAudio()) {
                tvCamera.text = itemView.context.getString(R.string.ps_tape)
            }
        }
        val textSize: Int = adapterStyle.getAdapterCameraTextSize()
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvCamera.textSize = textSize.toFloat()
        }
        val textColor: Int = adapterStyle.getAdapterCameraTextColor()
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvCamera.setTextColor(textColor)
        }
    }
}
