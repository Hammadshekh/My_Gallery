package com.example.selector.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.adapter.holder.BaseRecyclerMediaHolder
import com.example.selector.config.InjectResourceSource
import com.example.selector.config.PictureSelectionConfig
import com.example.ucrop.utils.FileUtils.isHasAudio
import com.example.ucrop.utils.FileUtils.isHasVideo
import com.luck.picture.lib.entity.LocalMedia
import java.util.ArrayList

class PictureImageGridAdapter(context: Context, private val mConfig: PictureSelectionConfig) :
    RecyclerView.Adapter<BaseRecyclerMediaHolder>() {
    var isDisplayCamera = false
    private var mData: ArrayList<LocalMedia> = ArrayList<LocalMedia>()
    private val mContext: Context = context
    fun notifyItemPositionChanged(position: Int) {
        this.notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataAndDataSetChanged(result: ArrayList<LocalMedia>?) {
        if (result != null) {
            mData = result
            notifyDataSetChanged()
        }
    }

    val data: ArrayList<LocalMedia>
        get() = mData
    val isDataEmpty: Boolean
        get() = mData.size == 0

    override fun getItemViewType(position: Int): Int {
        return if (isDisplayCamera && position == 0) {
            ADAPTER_TYPE_CAMERA
        } else {
            val adapterPosition = if (isDisplayCamera) position - 1 else position
            val mimeType: String = mData[adapterPosition].mimeType.toString()
            if (isHasVideo(mimeType)) {
                return ADAPTER_TYPE_VIDEO
            } else if (isHasAudio(mimeType)) {
                return ADAPTER_TYPE_AUDIO
            }
            ADAPTER_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerMediaHolder {
        return BaseRecyclerMediaHolder.generate(parent,
            viewType,
            getItemResourceId(viewType),
            mConfig)
    }

    /**
     * getItemResourceId
     *
     * @param viewType
     * @return
     */
    private fun getItemResourceId(viewType: Int): Int {
        val layoutResourceId: Int
        return when (viewType) {
            ADAPTER_TYPE_CAMERA -> R.layout.ps_item_grid_camera
            ADAPTER_TYPE_VIDEO -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(mContext,
                    InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE)
                if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_item_grid_video
            }
            ADAPTER_TYPE_AUDIO -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(mContext,
                    InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE)
                if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_item_grid_audio
            }
            else -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(mContext,
                    InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE)
                if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_item_grid_image
            }
        }
    }

    override fun onBindViewHolder(holder: BaseRecyclerMediaHolder, position: Int) {
        if (getItemViewType(position) == ADAPTER_TYPE_CAMERA) {
            holder.itemView.setOnClickListener {
                if (listener != null) {
                    listener!!.openCameraClick()
                }
            }
        } else {
            val adapterPosition = if (isDisplayCamera) position - 1 else position
            val media: LocalMedia = mData[adapterPosition]
            holder.bindData(media, adapterPosition)
            holder.setOnItemClickListener(listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isDisplayCamera) mData.size + 1 else mData.size
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        /**
         * Photograph
         */
        fun openCameraClick()

        /**
         * List item click event
         *
         * @param selectedView
         * @param position
         * @param media
         */
        fun onItemClick(selectedView: View?, position: Int, media: LocalMedia?)

        /**
         *
        List item long press event
         *
         * @param itemView
         * @param position
         */
        fun onItemLongClick(itemView: View?, position: Int)

        /**
         *
        List check click event
         *
         * @param selectedView
         * @param position
         * @param media
         */
        fun onSelected(selectedView: View?, position: Int, media: LocalMedia?): Int
    }

    companion object {
        /**
         * Photograph
         */
        const val ADAPTER_TYPE_CAMERA = 1

        /**
         *
        picture
         */
        const val ADAPTER_TYPE_IMAGE = 2

        /**
         * Video
         */
        const val ADAPTER_TYPE_VIDEO = 3

        /**
         * audio
         */
        const val ADAPTER_TYPE_AUDIO = 4
    }

}
