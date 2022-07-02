package com.example.mygallery.adapter


class CustomPreviewAdapter : PicturePreviewAdapter() {
    @NonNull
    fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): BasePreviewHolder {
        return if (viewType == BasePreviewHolder.ADAPTER_TYPE_IMAGE) {
            // 这里以重写自定义图片预览为例
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ps_custom_preview_image, parent, false)
            CustomPreviewImageHolder(itemView)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    class CustomPreviewImageHolder(@NonNull itemView: View?) :
        BasePreviewHolder(itemView) {
        var subsamplingScaleImageView: SubsamplingScaleImageView? = null
        protected fun findViews(itemView: View) {
            super.findViews(itemView)
            subsamplingScaleImageView = itemView.findViewById(R.id.big_preview_image)
        }

        protected fun loadImage(media: LocalMedia, maxWidth: Int, maxHeight: Int) {
            if (!ActivityCompatHelper.assertValidRequest(itemView.getContext())) {
                return
            }
            Glide.with(itemView.getContext())
                .asBitmap()
                .load(media.getAvailablePath())
                .into(object : CustomTarget<Bitmap?>() {
                    fun onResourceReady(
                        @NonNull resource: Bitmap,
                        @Nullable transition: Transition<in Bitmap?>?,
                    ) {
                        if (MediaUtils.isLongImage(resource.getWidth(), resource.getHeight())) {
                            subsamplingScaleImageView.setVisibility(View.VISIBLE)
                            val scale: Float = Math.max(screenWidth / resource.getWidth() as Float,
                                screenHeight / resource.getHeight() as Float)
                            subsamplingScaleImageView.setImage(ImageSource.cachedBitmap(resource),
                                ImageViewState(scale, PointF(0, 0), 0))
                        } else {
                            subsamplingScaleImageView.setVisibility(View.GONE)
                            coverImageView.setImageBitmap(resource)
                        }
                    }

                    fun onLoadFailed(@Nullable errorDrawable: Drawable?) {}
                    fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                })
        }

        protected fun setOnClickEventListener() {
            if (MediaUtils.isLongImage(media.getWidth(), media.getHeight())) {
                subsamplingScaleImageView.setOnClickListener(object : OnClickListener() {
                    fun onClick(v: View?) {
                        if (mPreviewEventListener != null) {
                            mPreviewEventListener.onBackPressed()
                        }
                    }
                })
            } else {
                super.setOnClickEventListener()
            }
        }

        protected fun setOnLongClickEventListener() {
            if (MediaUtils.isLongImage(media.getWidth(), media.getHeight())) {
                subsamplingScaleImageView.setOnLongClickListener(object : OnLongClickListener() {
                    fun onLongClick(view: View?): Boolean {
                        if (mPreviewEventListener != null) {
                            mPreviewEventListener.onLongPressDownload(media)
                        }
                        return false
                    }
                })
            } else {
                super.setOnLongClickEventListener()
            }
        }
    }
}