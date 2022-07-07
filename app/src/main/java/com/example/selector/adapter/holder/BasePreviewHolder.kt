package com.example.selector.adapter.holder

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.config.PictureConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.photoview.OnViewTapListener
import com.example.selector.photoview.PhotoView
import com.example.selector.utils.BitmapUtils
import com.example.selector.utils.DensityUtil
import com.example.selector.utils.MediaUtils
import com.luck.picture.lib.entity.LocalMedia

open class BasePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected val screenWidth: Int = DensityUtil.getRealScreenWidth(itemView.context)
    protected val screenHeight: Int = DensityUtil.getScreenHeight(itemView.context)
    protected val screenAppInHeight: Int = DensityUtil.getRealScreenHeight(itemView.context)
    protected var media: LocalMedia? = null
    protected val config: PictureSelectionConfig = PictureSelectionConfig.instance!!
    var coverImageView: PhotoView? = null
    private fun findViews(itemView: View) {
        coverImageView = itemView.findViewById(R.id.preview_image)
    }

    /**
     * bind Data
     *
     * @param media
     * @param position
     */
    open fun bindData(media: LocalMedia, position: Int) {
        this.media = media
        val size = getRealSizeFromMedia(media)
        val maxImageSize: IntArray = BitmapUtils.getMaxImageSize(size[0], size[1])
        loadImage(media, maxImageSize[0], maxImageSize[1])
        setScaleDisplaySize(media)
        setCoverScaleType(media)
        setOnClickEventListener()
        setOnLongClickEventListener()
    }

    /**
     * load image cover
     *
     * @param media
     * @param maxWidth
     * @param maxHeight
     */
    protected fun loadImage(media: LocalMedia, maxWidth: Int, maxHeight: Int) {
        if (PictureSelectionConfig.imageEngine != null) {
            val availablePath: String = media.availablePath!!
            if (maxWidth == PictureConfig.UNSET && maxHeight == PictureConfig.UNSET) {
                PictureSelectionConfig.imageEngine!!.loadImage(itemView.context,
                    availablePath,
                    coverImageView)
            } else {
                PictureSelectionConfig.imageEngine!!.loadImage(itemView.context,
                    coverImageView,
                    availablePath,
                    maxWidth,
                    maxHeight)
            }
        }
    }

    private fun setOnClickEventListener() {
        coverImageView?.setOnViewTapListener(object : OnViewTapListener {
            override fun onViewTap(view: View?, x: Float, y: Float) {
                if (mPreviewEventListener != null) {
                    mPreviewEventListener!!.onBackPressed()
                }
            }
        })
    }

    private fun setOnLongClickEventListener() {
        coverImageView?.setOnLongClickListener {
            if (mPreviewEventListener != null) {
                mPreviewEventListener!!.onLongPressDownload(media)
            }
            false
        }
    }

    private fun getRealSizeFromMedia(media: LocalMedia): IntArray {
        return if (media.isCut() && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
            intArrayOf(media.cropImageWidth, media.cropImageHeight)
        } else {
            intArrayOf(media.width, media.height)
        }
    }

    private fun setCoverScaleType(media: LocalMedia) {
        if (MediaUtils.isLongImage(media.width, media.height)) {
            coverImageView?.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            coverImageView?.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    open fun setScaleDisplaySize(media: LocalMedia) {
        if (!config.isPreviewZoomEffect && screenWidth < screenHeight) {
            if (media.width > 0 && media.height > 0) {
                val layoutParams = coverImageView?.layoutParams as FrameLayout.LayoutParams
                layoutParams.width = screenWidth
                layoutParams.height = screenAppInHeight
                layoutParams.gravity = Gravity.CENTER
            }
        }
    }

    /**
     * onViewAttachedToWindow
     */
    open fun onViewAttachedToWindow() {}

    /**
     * onViewDetachedFromWindow
     */
    open fun onViewDetachedFromWindow() {}
    protected var mPreviewEventListener: OnPreviewEventListener? = null
    fun setOnPreviewEventListener(listener: OnPreviewEventListener?) {
        mPreviewEventListener = listener
    }

    interface OnPreviewEventListener {
        fun onBackPressed()
        fun onPreviewVideoTitle(videoName: String?)
        fun onLongPressDownload(media: LocalMedia?)
    }

    companion object {
        /**
         * picture
         */
        const val ADAPTER_TYPE_IMAGE = 1

        /**
         * video
         */
        private const val ADAPTER_TYPE_VIDEO = 2

        /**
         * Audio
         */
        private const val ADAPTER_TYPE_AUDIO = 3
        fun generate(parent: ViewGroup, viewType: Int, resource: Int): BasePreviewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(resource, parent, false)
            return when (viewType) {
                ADAPTER_TYPE_VIDEO -> {
                    PreviewVideoHolder(itemView)
                }
                ADAPTER_TYPE_AUDIO -> {
                    PreviewAudioHolder(itemView)
                }
                else -> {
                    PreviewImageHolder(itemView)
                }
            }
        }
    }

    init {
        findViews(itemView)
    }
}
