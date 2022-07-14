package com.example.mygallery.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView

class CustomPreviewFragment : PictureSelectorPreviewFragment() {
    val fragmentTag: String
        get() = CustomPreviewFragment::class.java.simpleName

    private fun createAdapter(): PicturePreviewAdapter {
        return CustomPreviewAdapter()
    }

    private fun setMagicalViewAction() {
        //If the isPreview Zoom Effect effect is enabled, this method needs to be overloaded
        magicalView.setOnMojitoViewCallback(object : OnMagicalViewCallback() {
            fun onBeginBackMinAnim() {
                val currentHolder: BasePreviewHolder =
                    viewPageAdapter.getCurrentHolder(viewPager.getCurrentItem())
                        ?: return
                if (currentHolder.coverImageView.getVisibility() === View.GONE) {
                    currentHolder.coverImageView.setVisibility(View.VISIBLE)
                }
                if (currentHolder is PreviewVideoHolder) {
                    val videoHolder: PreviewVideoHolder = currentHolder as PreviewVideoHolder
                    if (videoHolder.ivPlayButton.getVisibility() === View.VISIBLE) {
                        videoHolder.ivPlayButton.setVisibility(View.GONE)
                    }
                }
            }

            fun onBeginBackMinMagicalFinish(isResetSize: Boolean) {
                val itemViewParams: ViewParams =
                    BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)
                        ?: return
                val currentHolder: BasePreviewHolder =
                    viewPageAdapter.getCurrentHolder(viewPager.getCurrentItem())
                        ?: return
                currentHolder.coverImageView.layoutParams.width = itemViewParams.width
                currentHolder.coverImageView.layoutParams.height = itemViewParams.height
                currentHolder.coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            }

            fun onBeginMagicalAnimComplete(mojitoView: MagicalView?, showImmediately: Boolean) {
                val currentHolder: BasePreviewHolder =
                    viewPageAdapter.getCurrentHolder(viewPager.getCurrentItem())
                        ?: return
                val media: LocalMedia = mData.get(viewPager.getCurrentItem())
                val realWidth: Int
                val realHeight: Int
                if (media.isCut() && media.cropImageWidth  > 0 && media.cropImageHeight > 0) {
                    realWidth = media.cropImageWidth
                    realHeight = media.cropImageHeight
                } else {
                    realWidth = media.width
                    realHeight = media.height
                }
                if (MediaUtils.isLongImage(realWidth, realHeight)) {
                    currentHolder.coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
                } else {
                    currentHolder.coverImageView.setScaleType(ImageView.ScaleType.FIT_CENTER)
                }
                if (currentHolder is PreviewVideoHolder) {
                    val videoHolder: PreviewVideoHolder = currentHolder as PreviewVideoHolder
                    if (videoHolder.ivPlayButton.getVisibility() === View.GONE) {
                        videoHolder.ivPlayButton.setVisibility(View.VISIBLE)
                    }
                }
            }

            fun onBackgroundAlpha(alpha: Float) {
                for (i in 0 until mAnimViews.size()) {
                    if (mAnimViews.get(i) is TitleBar) {
                        continue
                    }
                    mAnimViews.get(i).setAlpha(alpha)
                }
            }

            fun onMagicalViewFinish() {
                onBackCurrentFragment()
            }
        })
    }

    companion object {
        fun newInstance(): CustomPreviewFragment {
            val fragment = CustomPreviewFragment()
            fragment.setArguments(Bundle())
            return fragment
        }
    }
}
