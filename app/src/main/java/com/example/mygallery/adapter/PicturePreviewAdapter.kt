package com.example.mygallery.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.adapter.holder.BasePreviewHolder
import com.example.selector.adapter.holder.PreviewAudioHolder
import com.example.selector.adapter.holder.PreviewVideoHolder
import com.example.selector.config.InjectResourceSource
import com.example.selector.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia

open class PicturePreviewAdapter : RecyclerView.Adapter<BasePreviewHolder>() {
    private var mData: List<LocalMedia>? = null
    private var onPreviewEventListener: BasePreviewHolder.OnPreviewEventListener? = null
    private val mHolderCache: LinkedHashMap<Int, BasePreviewHolder> =
        LinkedHashMap()

    fun getCurrentHolder(position: Int): BasePreviewHolder? {
        return mHolderCache[position]
    }

    fun setData(list: List<LocalMedia>?) {
        mData = list
    }

    fun setOnPreviewEventListener(listener: BasePreviewHolder.OnPreviewEventListener?) {
        onPreviewEventListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasePreviewHolder {
        val layoutResourceId: Int
        return when (viewType) {
            BasePreviewHolder.ADAPTER_TYPE_VIDEO -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(
                    parent.context,
                    InjectResourceSource.PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE
                )
                BasePreviewHolder.generate(
                    parent,
                    viewType,
                    if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_preview_video
                )
            }
            BasePreviewHolder.ADAPTER_TYPE_AUDIO -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(
                    parent.context,
                    InjectResourceSource.PREVIEW_ITEM_AUDIO_LAYOUT_RESOURCE
                )
                BasePreviewHolder.generate(
                    parent,
                    viewType,
                    if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_preview_audio
                )
            }
            else -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(
                    parent.context,
                    InjectResourceSource.PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE
                )
                BasePreviewHolder.generate(
                    parent,
                    viewType,
                    if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_preview_image
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BasePreviewHolder, position: Int) {
        holder.setOnPreviewEventListener(onPreviewEventListener)
        val media = getItem(position)
        mHolderCache[position] = holder
        holder.bindData(media!!, position)
    }

    private fun getItem(position: Int): LocalMedia? {
        return if (position > mData!!.size) {
            null
        } else mData!![position]
    }

    override fun getItemViewType(position: Int): Int {
        return if (mData!![position].mimeType?.let { PictureMimeType.isHasVideo(it) } == true) {
            BasePreviewHolder.ADAPTER_TYPE_VIDEO
        } else if (PictureMimeType.isHasAudio(mData!![position].mimeType)) {
            BasePreviewHolder.ADAPTER_TYPE_AUDIO
        } else {
            BasePreviewHolder.ADAPTER_TYPE_IMAGE
        }
    }

    override fun getItemCount(): Int {
        return if (mData != null) mData!!.size else 0
    }

    override fun onViewAttachedToWindow(holder: BasePreviewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: BasePreviewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }

    /**
     *
    Set the zoom method of the cover
     *
     * @param position
     */
    fun setCoverScaleType(position: Int) {
        val currentHolder: BasePreviewHolder? = getCurrentHolder(position)
        if (currentHolder != null) {
            val media = getItem(position)
            if (media!!.width == 0 && media.height == 0) {
                currentHolder.coverImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                currentHolder.coverImageView!!.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }

    /**
     *
    Set play button state
     *
     * @param position
     */
    open fun setVideoPlayButtonUI(position: Int) {
        val currentHolder = getCurrentHolder(position)
        if (currentHolder is PreviewVideoHolder) {
            if (!currentHolder.isPlaying()) {
                currentHolder.ivPlayButton.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Set autoplay video
     *
     * @param position
     */
    fun startAutoVideoPlay(position: Int) {
        val currentHolder: BasePreviewHolder? = getCurrentHolder(position)
        if (currentHolder is PreviewVideoHolder) {
            val videoHolder: PreviewVideoHolder? = currentHolder as PreviewVideoHolder?
            videoHolder!!.startPlay()
        }
    }

    /**
     * isPlaying
     *
     * @param position
     * @return
     */
    fun isPlaying(position: Int): Boolean {
        val currentHolder: BasePreviewHolder? = getCurrentHolder(position)
        return if (currentHolder is PreviewVideoHolder) {
            (currentHolder as PreviewVideoHolder?)!!.isPlaying()
        } else false
    }

  //Release current video related
    fun destroy() {
        for (key in mHolderCache.keys) {
            val holder: BasePreviewHolder? = mHolderCache[key]
            if (holder is PreviewVideoHolder) {
                val videoHolder: PreviewVideoHolder? = holder as PreviewVideoHolder?
                videoHolder!!.releaseVideo()
            } else if (holder is PreviewAudioHolder) {
                val audioHolder: PreviewAudioHolder? = holder as PreviewAudioHolder?
                audioHolder!!.releaseAudio()
            }
        }
    }
}
