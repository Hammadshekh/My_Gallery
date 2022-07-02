package com.example.selector.adapter.holder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class ImageViewHolder(itemView: View, config: PictureSelectionConfig?) :
    BaseRecyclerMediaHolder(itemView, config) {
    private val ivEditor: ImageView
    private val tvMediaTag: TextView
    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        if (media.isEditorImage() && media.isCut()) {
            ivEditor.visibility = View.VISIBLE
        } else {
            ivEditor.visibility = View.GONE
        }
        tvMediaTag.visibility = View.VISIBLE
        if (PictureMimeType.isHasGif(media.getMimeType())) {
            tvMediaTag.setText(mContext.getString(R.string.ps_gif_tag))
        } else if (PictureMimeType.isHasWebp(media.getMimeType())) {
            tvMediaTag.setText(mContext.getString(R.string.ps_webp_tag))
        } else if (MediaUtils.isLongImage(media.getWidth(), media.getHeight())) {
            tvMediaTag.setText(mContext.getString(R.string.ps_long_chart))
        } else {
            tvMediaTag.visibility = View.GONE
        }
    }

    init {
        tvMediaTag = itemView.findViewById(R.id.tv_media_tag)
        ivEditor = itemView.findViewById(R.id.ivEditor)
        val adapterStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle.getSelectMainStyle()
        val imageEditorRes: Int = adapterStyle.getAdapterImageEditorResources()
        if (StyleUtils.checkStyleValidity(imageEditorRes)) {
            ivEditor.setImageResource(imageEditorRes)
        }
        val editorGravity: IntArray = adapterStyle.getAdapterImageEditorGravity()
        if (StyleUtils.checkArrayValidity(editorGravity)) {
            if (ivEditor.layoutParams is RelativeLayout.LayoutParams) {
                (ivEditor.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                for (i in editorGravity) {
                    (ivEditor.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }
        val tagGravity: IntArray = adapterStyle.getAdapterTagGravity()
        if (StyleUtils.checkArrayValidity(tagGravity)) {
            if (tvMediaTag.layoutParams is RelativeLayout.LayoutParams) {
                (tvMediaTag.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_END)
                (tvMediaTag.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                for (i in tagGravity) {
                    (tvMediaTag.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }
        val background: Int = adapterStyle.getAdapterTagBackgroundResources()
        if (StyleUtils.checkStyleValidity(background)) {
            tvMediaTag.setBackgroundResource(background)
        }
        val textSize: Int = adapterStyle.getAdapterTagTextSize()
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvMediaTag.textSize = textSize.toFloat()
        }
        val textColor: Int = adapterStyle.getAdapterTagTextColor()
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvMediaTag.setTextColor(textColor)
        }
    }
}
