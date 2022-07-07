package com.example.selector.adapter.holder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.mygallery.R
import com.example.selector.config.PictureMimeType
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.MediaUtils
import com.example.selector.utils.StyleUtils
import com.luck.picture.lib.entity.LocalMedia

class ImageViewHolder(itemView: View, config: PictureSelectionConfig?) :
    BaseRecyclerMediaHolder(itemView, config!!) {
    private val ivEditor: ImageView = itemView.findViewById(R.id.ivEditor)
    private val tvMediaTag: TextView = itemView.findViewById(R.id.tv_media_tag)
    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        if (media.isEditorImage() && media.isCut()) {
            ivEditor.visibility = View.VISIBLE
        } else {
            ivEditor.visibility = View.GONE
        }
        tvMediaTag.visibility = View.VISIBLE
        when {
            media.mimeType?.let { PictureMimeType.isHasGif(it) } == true -> {
                tvMediaTag.text = mContext?.getString(R.string.ps_gif_tag)
            }
            PictureMimeType.isHasWebp(media.mimeType) -> {
                tvMediaTag.text = mContext?.getString(R.string.ps_webp_tag)
            }
            MediaUtils.isLongImage(media.width, media.height) -> {
                tvMediaTag.text = mContext?.getString(R.string.ps_long_chart)
            }
            else -> {
                tvMediaTag.visibility = View.GONE
            }
        }
    }

    init {
        val adapterStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle?.selectMainStyle!!
        val imageEditorRes: Int = adapterStyle.adapterImageEditorResources
        if (StyleUtils.checkStyleValidity(imageEditorRes)) {
            ivEditor.setImageResource(imageEditorRes)
        }
        val editorGravity: IntArray? = adapterStyle.adapterImageEditorGravity
        if (StyleUtils.checkArrayValidity(editorGravity)) {
            if (ivEditor.layoutParams is RelativeLayout.LayoutParams) {
                (ivEditor.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                for (i in editorGravity!!) {
                    (ivEditor.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }
        val tagGravity: IntArray = adapterStyle.adapterTagGravity!!
        if (StyleUtils.checkArrayValidity(tagGravity)) {
            if (tvMediaTag.layoutParams is RelativeLayout.LayoutParams) {
                (tvMediaTag.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_END)
                (tvMediaTag.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                for (i in tagGravity) {
                    (tvMediaTag.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }
        val background: Int = adapterStyle.adapterTagBackgroundResources
        if (StyleUtils.checkStyleValidity(background)) {
            tvMediaTag.setBackgroundResource(background)
        }
        val textSize: Int = adapterStyle.adapterTagTextSize
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvMediaTag.textSize = textSize.toFloat()
        }
        val textColor: Int = adapterStyle.adapterTagTextColor
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvMediaTag.setTextColor(textColor)
        }
    }
}
