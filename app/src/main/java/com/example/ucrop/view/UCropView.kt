package com.example.ucrop.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.mygallery.R
import com.example.ucrop.callback.CropBoundsChangeListener
import com.example.ucrop.callback.OverlayViewChangeListener

class UCropView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
) :
    FrameLayout(context, attrs, defStyleAttr) {
    var cropImageView: GestureCropImageView
        private set
    val overlayView: OverlayView

    private fun setListenersToViews() {
        cropImageView.setCropBoundsChangeListener(object : CropBoundsChangeListener {
            override fun onCropAspectRatioChanged(cropRatio: Float) {
                overlayView.setTargetAspectRatio(cropRatio)
            }
        })
        overlayView.setOverlayViewChangeListener(object : OverlayViewChangeListener {
            override fun onCropRectUpdated(cropRect: RectF?) {
                cropRect?.let { cropImageView.setCropRect(it) }
            }

            override fun postTranslate(deltaX: Float, deltaY: Float) {
                cropImageView.postTranslate(deltaX, deltaY)
            }
        })
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(cropImageView)
        cropImageView = GestureCropImageView(context)
        setListenersToViews()
        cropImageView.setCropRect(overlayView.cropViewRect)
        addView(cropImageView, 0)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true)
        cropImageView = findViewById(R.id.image_view_crop)
        overlayView = findViewById(R.id.view_overlay)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView)
        overlayView.processStyledAttributes(a)
        cropImageView.processStyledAttributes(a)
        a.recycle()
        setListenersToViews()
    }
}