package com.example.selector.adapter

import android.graphics.ColorFilter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.config.InjectResourceSource
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.StyleUtils
import com.example.ucrop.utils.FileUtils.isHasVideo
import com.luck.picture.lib.entity.LocalMedia
import java.util.ArrayList

class PreviewGalleryAdapter(private val isBottomPreview: Boolean, list: List<LocalMedia>) :
    RecyclerView.Adapter<PreviewGalleryAdapter.ViewHolder>() {
    private val mData: MutableList<LocalMedia>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResourceId: Int = InjectResourceSource.getLayoutResource(parent.context,
            InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE)
        val itemView = LayoutInflater.from(parent.context)
            .inflate(if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_preview_gallery_item,
                parent,
                false)
        return ViewHolder(itemView)
    }

    val data: List<Any>
        get() = mData

    fun clear() {
        mData.clear()
    }

    /**
     * Add selected to gallery effect
     *
     * @param currentMedia
     */
    fun addGalleryData(currentMedia: LocalMedia) {
        val lastCheckPosition = lastCheckPosition
        if (lastCheckPosition != RecyclerView.NO_POSITION) {
            val lastSelectedMedia: LocalMedia = mData[lastCheckPosition]
            lastSelectedMedia.isChecked = false
            notifyItemChanged(lastCheckPosition)
        }
        if (isBottomPreview && mData.contains(currentMedia)) {
            val currentPosition = getCurrentPosition(currentMedia)
            val media: LocalMedia = mData[currentPosition]
            media.isGalleryEnabledMask = true
            media.isChecked = true
            notifyItemChanged(currentPosition)
        } else {
            currentMedia.isChecked = true
            mData.add(currentMedia)
            notifyItemChanged(mData.size - 1)
        }
    }

    /**
     * Remove unselected results from gallery
     *
     * @param currentMedia
     */
    fun removeGalleryData(currentMedia: LocalMedia) {
        val currentPosition = getCurrentPosition(currentMedia)
        if (currentPosition != RecyclerView.NO_POSITION) {
            if (isBottomPreview) {
                val media: LocalMedia = mData[currentPosition]
                media.isGalleryEnabledMask = true
                notifyItemChanged(currentPosition)
            } else {
                mData.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
            }
        }
    }

    /**
     *
    Whether the current LocalMedia is selected
     *
     * @param currentMedia
     */
    fun isSelectMedia(currentMedia: LocalMedia) {
        val lastCheckPosition = lastCheckPosition
        if (lastCheckPosition != RecyclerView.NO_POSITION) {
            val lastSelectedMedia: LocalMedia = mData[lastCheckPosition]
            lastSelectedMedia.isChecked = false
            notifyItemChanged(lastCheckPosition)
        }
        val currentPosition = getCurrentPosition(currentMedia)
        if (currentPosition != RecyclerView.NO_POSITION) {
            val media: LocalMedia = mData[currentPosition]
            media.isChecked = true
            notifyItemChanged(currentPosition)
        }
    }

    /**
     *
    Get the last selected position of the gallery
     *
     * @return
     */
    val lastCheckPosition: Int
        get() {
            for (i in mData.indices) {
                val media: LocalMedia = mData[i]
                if (media.isChecked) {
                    return i
                }
            }
            return RecyclerView.NO_POSITION
        }

    /**
     *
    Get the location of the current gallery LocalMedia
     *
     * @param currentMedia
     * @return
     */
    private fun getCurrentPosition(currentMedia: LocalMedia): Int {
        for (i in mData.indices) {
            val media: LocalMedia = mData[i]
            if (TextUtils.equals(media.path, currentMedia.path)
                || media.id == currentMedia.id
            ) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: LocalMedia = mData[position]
        val colorFilter: ColorFilter = StyleUtils.getColorFilter(holder.itemView.context,
            if (item.isGalleryEnabledMask) R.color.ps_color_half_white else R.color.ps_color_transparent)!!
        if (item.isChecked && item.isGalleryEnabledMask) {
            holder.viewBorder.visibility = View.VISIBLE
        } else {
            holder.viewBorder.visibility = if (item.isChecked) View.VISIBLE else View.GONE
        }
        var path: String = item.path.toString()
        if (item.isEditorImage() && !TextUtils.isEmpty(item.cutPath)) {
            path = item.cutPath.toString()
            holder.ivEditor.visibility = View.VISIBLE
        } else {
            holder.ivEditor.visibility = View.GONE
        }
        holder.ivImage.colorFilter = colorFilter
        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine!!.loadGridImage(holder.itemView.context,
                path,
                holder.ivImage)
        }
        holder.ivPlay.visibility =
            if (isHasVideo(item.mimeType)) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { view ->
            if (listener != null) {
                listener!!.onItemClick(holder.absoluteAdapterPosition, item, view)
            }
        }
        holder.itemView.setOnLongClickListener { v ->
            if (mItemLongClickListener != null) {
                val adapterPosition: Int = holder.absoluteAdapterPosition
                mItemLongClickListener!!.onItemLongClick(holder, adapterPosition, v)
            }
            true
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        var ivPlay: ImageView = itemView.findViewById(R.id.ivPlay)
        var ivEditor: ImageView = itemView.findViewById(R.id.ivEditor)
        var viewBorder: View = itemView.findViewById(R.id.viewBorder)

        init {
            val selectMainStyle: SelectMainStyle =
                PictureSelectionConfig.selectorStyle?.selectMainStyle!!
            if (StyleUtils.checkStyleValidity(selectMainStyle.adapterImageEditorResources)) {
                ivEditor.setImageResource(selectMainStyle.adapterImageEditorResources)
            }
            if (StyleUtils.checkStyleValidity(selectMainStyle.adapterPreviewGalleryFrameResource)) {
                viewBorder.setBackgroundResource(selectMainStyle.adapterPreviewGalleryFrameResource)
            }
            val adapterPreviewGalleryItemSize: Int =
                selectMainStyle.adapterPreviewGalleryItemSize
            if (StyleUtils.checkSizeValidity(adapterPreviewGalleryItemSize)) {
                val params = RelativeLayout.LayoutParams(adapterPreviewGalleryItemSize,
                    adapterPreviewGalleryItemSize)
                itemView.layoutParams = params
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    private var listener: OnItemClickListener? = null
    fun setItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, media: LocalMedia?, v: View?)
    }

    private var mItemLongClickListener: OnItemLongClickListener? = null
    fun setItemLongClickListener(listener: OnItemLongClickListener?) {
        mItemLongClickListener = listener
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(holder: RecyclerView.ViewHolder?, position: Int, v: View?)
    }

    init {
        mData = ArrayList<LocalMedia>(list)
        for (i in mData.indices) {
            val media: LocalMedia = mData[i]
            media.isGalleryEnabledMask = false
            media.isChecked=false
        }
    }
}
