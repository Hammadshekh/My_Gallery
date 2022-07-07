package com.example.selector.adapter.holder

import android.content.Context
import android.graphics.ColorFilter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.adapter.PictureImageGridAdapter
import com.example.selector.config.PictureMimeType.isHasAudio
import com.example.selector.config.PictureMimeType.isHasImage
import com.example.selector.config.PictureMimeType.isHasVideo
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectModeConfig
import com.example.selector.manager.SelectedManager
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.AnimUtils
import com.example.selector.utils.StyleUtils
import com.example.selector.utils.ValueOf
import com.luck.picture.lib.entity.LocalMedia

open class BaseRecyclerMediaHolder : RecyclerView.ViewHolder {
    var ivPicture: ImageView? = null
    private var tvCheck: TextView? = null
    private var btnCheck: View? = null
    var mContext: Context? = null
    var config: PictureSelectionConfig? = null
    private var isSelectNumberStyle = false
    private var isHandleMask = false
    private var defaultColorFilter: ColorFilter? = null
    private var selectColorFilter: ColorFilter? = null
    private var maskWhiteColorFilter: ColorFilter? = null

    constructor(itemView: View) : super(itemView)
    constructor(itemView: View, config: PictureSelectionConfig) : super(itemView) {
        this.config = config
        mContext = itemView.context
        defaultColorFilter = StyleUtils.getColorFilter(mContext, R.color.ps_color_20)
        selectColorFilter = StyleUtils.getColorFilter(mContext, R.color.ps_color_80)
        maskWhiteColorFilter = StyleUtils.getColorFilter(mContext, R.color.ps_color_half_white)
        val selectMainStyle: SelectMainStyle =
            PictureSelectionConfig.selectorStyle?.selectMainStyle!!
        isSelectNumberStyle = selectMainStyle.isSelectNumberStyle
        ivPicture = itemView.findViewById(R.id.ivPicture)
        tvCheck = itemView.findViewById(R.id.tvCheck)
        btnCheck = itemView.findViewById(R.id.btnCheck)
        if (config.selectionMode == SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
            tvCheck?.visibility = View.GONE
            btnCheck?.visibility = View.GONE
        } else {
            tvCheck?.visibility = View.VISIBLE
            btnCheck?.visibility = View.VISIBLE
        }
        isHandleMask = (!config.isDirectReturnSingle
                && (config.selectionMode == SelectModeConfig.SINGLE || config.selectionMode == SelectModeConfig.MULTIPLE))
        val textSize: Int = selectMainStyle.adapterSelectTextSize
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvCheck?.textSize = textSize.toFloat()
        }
        val textColor: Int = selectMainStyle.adapterSelectTextColor
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvCheck?.setTextColor(textColor)
        }
        val adapterSelectBackground: Int = selectMainStyle.selectBackground
        if (StyleUtils.checkStyleValidity(adapterSelectBackground)) {
            tvCheck?.setBackgroundResource(adapterSelectBackground)
        }
        val selectStyleGravity: IntArray = selectMainStyle.adapterSelectStyleGravity!!
        if (StyleUtils.checkArrayValidity(selectStyleGravity)) {
            if (tvCheck?.layoutParams is RelativeLayout.LayoutParams) {
                (tvCheck?.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_END)
                for (i in selectStyleGravity) {
                    (tvCheck?.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
            if (btnCheck?.layoutParams is RelativeLayout.LayoutParams) {
                (btnCheck?.layoutParams as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_END)
                for (i in selectStyleGravity) {
                    (btnCheck?.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
            val clickArea: Int = selectMainStyle.adapterSelectClickArea
            if (StyleUtils.checkSizeValidity(clickArea)) {
                val clickAreaParams = btnCheck?.layoutParams
                clickAreaParams?.width = clickArea
                clickAreaParams?.height = clickArea
            }
        }
    }

    /**
     * bind Data
     *
     * @param media
     * @param position
     */
    open fun bindData(media: LocalMedia, position: Int) {
        media.position = absoluteAdapterPosition
        selectedMedia(isSelected(media))
        if (isSelectNumberStyle) {
            notifySelectNumberStyle(media)
        }
        if (isHandleMask && config?.isMaxSelectEnabledMask == true) {
            dispatchHandleMask(media)
        }
        var path: String = media.path!!
        if (media.isEditorImage()) {
            path = media.cutPath!!
        }
        loadCover(path)
        tvCheck!!.setOnClickListener { btnCheck!!.performClick() }
        btnCheck!!.setOnClickListener(View.OnClickListener {
            if (media.isMaxSelectEnabledMask || listener == null) {
                return@OnClickListener
            }
            val resultCode: Int = listener!!.onSelected(tvCheck, position, media)
            if (resultCode == SelectedManager.INVALID) {
                return@OnClickListener
            }
            if (resultCode == SelectedManager.ADD_SUCCESS) {
                if (config?.isSelectZoomAnim == true) {
                    AnimUtils.selectZoom(ivPicture)
                }
            }
            selectedMedia(isSelected(media))
        })
        itemView.setOnLongClickListener { v ->
            if (listener != null) {
                listener!!.onItemLongClick(v, position)
            }
            false
        }
        itemView.setOnClickListener(View.OnClickListener {
            if (media.isMaxSelectEnabledMask || listener == null) {
                return@OnClickListener
            }
            val isPreview =
                (isHasImage(media.mimeType) && config!!.isEnablePreviewImage || config!!.isDirectReturnSingle
                        || isHasVideo(media.mimeType) && (config!!.isEnablePreviewVideo
                        || config!!.selectionMode == SelectModeConfig.SINGLE) || isHasAudio(
                     media.mimeType) && (config!!.isEnablePreviewAudio
                        || config!!.selectionMode == SelectModeConfig.SINGLE))
            if (isPreview) {
                listener?.onItemClick(tvCheck, position, media)
            } else {
                btnCheck!!.performClick()
            }
        })

    }

    /**
     *Load resource cover
     * */
    protected open fun loadCover(path: String?) {
        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine!!.loadGridImage(ivPicture!!.context, path, ivPicture)
        }
    }

    /**
     * Process the masking effect after reaching the selection condition
     */
    private fun dispatchHandleMask(media: LocalMedia) {
        var isEnabledMask = false
        if (SelectedManager.selectCount > 0 && !SelectedManager.getSelectedResult()
                .contains(media)
        ) {
            if (config?.isWithVideoImage == true) {
                isEnabledMask = if (config?.selectionMode == SelectModeConfig.SINGLE) {
                    SelectedManager.selectCount == Int.MAX_VALUE
                } else {
                    SelectedManager.selectCount == config?.maxSelectNum
                }
            } else {
                if (isHasVideo(SelectedManager.topResultMimeType)) {
                    val maxSelectNum: Int = if (config?.selectionMode == SelectModeConfig.SINGLE) {
                        Int.MAX_VALUE
                    } else {
                        if (config!!.maxVideoSelectNum > 0) config!!.maxVideoSelectNum else config!!.maxSelectNum
                    }
                    isEnabledMask = (SelectedManager.selectCount == maxSelectNum
                            || isHasImage(media.mimeType))
                } else {
                    val maxSelectNum: Int = if (config!!.selectionMode == SelectModeConfig.SINGLE) {
                        Int.MAX_VALUE
                    } else {
                        config!!.maxSelectNum
                    }
                    isEnabledMask = (SelectedManager.selectCount == maxSelectNum
                            || isHasVideo(media.mimeType))
                }
            }
        }
        if (isEnabledMask) {
            ivPicture!!.colorFilter = maskWhiteColorFilter
            media.isMaxSelectEnabledMask = true
        } else {
            media.isMaxSelectEnabledMask = false
        }
    }

    /**
     * Set selected zoom animation
     *
     * @param isChecked
     */
    private fun selectedMedia(isChecked: Boolean) {
        if (tvCheck!!.isSelected != isChecked) {
            tvCheck!!.isSelected = isChecked
        }
        if (config!!.isDirectReturnSingle) {
            ivPicture!!.colorFilter = defaultColorFilter
        } else {
            ivPicture!!.colorFilter = if (isChecked) selectColorFilter else defaultColorFilter
        }
    }

    /**
     *
    Check if LocalMedia is selected
     *
     * @param currentMedia
     * @return
     */
    private fun isSelected(currentMedia: LocalMedia): Boolean {
        val selectedResult: List<LocalMedia> = SelectedManager.getSelectedResult()
        val isSelected = selectedResult.contains(currentMedia)
        if (isSelected) {
            val compare: LocalMedia = currentMedia.compareLocalMedia!!
            if (compare.isEditorImage()) {
                currentMedia.cutPath = (compare.cutPath)
                currentMedia.setCut(!TextUtils.isEmpty(compare.cutPath))
                currentMedia.setEditorImage(compare.isEditorImage())
            }
        }
        return isSelected
    }

    /**
     * Sort selections by number
     */
    private fun notifySelectNumberStyle(currentMedia: LocalMedia) {
        tvCheck!!.text = ""
        for (i in 0 until SelectedManager.selectCount) {
            val media: LocalMedia = SelectedManager.getSelectedResult()[i]
            if (TextUtils.equals(media.getPath(), currentMedia.getPath())
                || media.id == currentMedia.id
            ) {
                currentMedia.num = (media.num)
                media.position = (currentMedia.position)
                tvCheck?.text = ValueOf.toString(currentMedia.num)
            }
        }
    }

    private var listener: PictureImageGridAdapter.OnItemClickListener? = null
    fun setOnItemClickListener(listener: PictureImageGridAdapter.OnItemClickListener?) {
        this.listener = listener
    }

    companion object {
        fun generate(
            parent: ViewGroup,
            viewType: Int,
            resource: Int,
            config: PictureSelectionConfig?,
        ): BaseRecyclerMediaHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(resource, parent, false)
            return when (viewType) {
                PictureImageGridAdapter.ADAPTER_TYPE_CAMERA -> CameraViewHolder(itemView)
                PictureImageGridAdapter.ADAPTER_TYPE_VIDEO -> VideoViewHolder(itemView, config)
                PictureImageGridAdapter.ADAPTER_TYPE_AUDIO -> AudioViewHolder(itemView, config)
                else -> ImageViewHolder(itemView, config)
            }
        }
    }
}
