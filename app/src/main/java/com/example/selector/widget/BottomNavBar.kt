package com.example.selector.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

open class BottomNavBar : RelativeLayout, View.OnClickListener {
    protected var tvPreview: TextView? = null
    protected var tvImageEditor: TextView? = null
    private var originalCheckbox: CheckBox? = null
    protected var config: PictureSelectionConfig? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    protected fun init() {
        inflateLayout()
        isClickable = true
        isFocusable = true
        config = PictureSelectionConfig.getInstance()
        tvPreview = findViewById(R.id.ps_tv_preview)
        tvImageEditor = findViewById(R.id.ps_tv_editor)
        originalCheckbox = findViewById(R.id.cb_original)
        tvPreview.setOnClickListener(this)
        tvImageEditor.setVisibility(GONE)
        setBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_grey))
        originalCheckbox.setChecked(config.isCheckOriginalImage)
        originalCheckbox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            config.isCheckOriginalImage = isChecked
            originalCheckbox.setChecked(config.isCheckOriginalImage)
            if (bottomNavBarListener != null) {
                bottomNavBarListener!!.onCheckOriginalChange()
                if (isChecked && SelectedManager.getSelectCount() === 0) {
                    bottomNavBarListener!!.onFirstCheckOriginalSelectedChange()
                }
            }
        })
        handleLayoutUI()
    }

    protected fun inflateLayout() {
        inflate(context, R.layout.ps_bottom_nav_bar, this)
    }

    protected open fun handleLayoutUI() {}
    open fun setBottomNavBarStyle() {
        if (config.isDirectReturnSingle) {
            visibility = GONE
            return
        }
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle
        val bottomBarStyle: BottomNavBarStyle = selectorStyle.getBottomBarStyle()
        if (config.isOriginalControl) {
            originalCheckbox!!.visibility = VISIBLE
            val originalDrawableLeft: Int = bottomBarStyle.getBottomOriginalDrawableLeft()
            if (StyleUtils.checkStyleValidity(originalDrawableLeft)) {
                originalCheckbox!!.setButtonDrawable(originalDrawableLeft)
            }
            val bottomOriginalText: String = bottomBarStyle.getBottomOriginalText()
            if (StyleUtils.checkTextValidity(bottomOriginalText)) {
                originalCheckbox!!.text = bottomOriginalText
            }
            val originalTextSize: Int = bottomBarStyle.getBottomOriginalTextSize()
            if (StyleUtils.checkSizeValidity(originalTextSize)) {
                originalCheckbox!!.textSize = originalTextSize.toFloat()
            }
            val originalTextColor: Int = bottomBarStyle.getBottomOriginalTextColor()
            if (StyleUtils.checkStyleValidity(originalTextColor)) {
                originalCheckbox!!.setTextColor(originalTextColor)
            }
        }
        val narBarHeight: Int = bottomBarStyle.getBottomNarBarHeight()
        if (StyleUtils.checkSizeValidity(narBarHeight)) {
            layoutParams.height = narBarHeight
        } else {
            layoutParams.height = DensityUtil.dip2px(context, 46)
        }
        val backgroundColor: Int = bottomBarStyle.getBottomNarBarBackgroundColor()
        if (StyleUtils.checkStyleValidity(backgroundColor)) {
            setBackgroundColor(backgroundColor)
        }
        val previewNormalTextColor: Int = bottomBarStyle.getBottomPreviewNormalTextColor()
        if (StyleUtils.checkStyleValidity(previewNormalTextColor)) {
            tvPreview!!.setTextColor(previewNormalTextColor)
        }
        val previewTextSize: Int = bottomBarStyle.getBottomPreviewNormalTextSize()
        if (StyleUtils.checkSizeValidity(previewTextSize)) {
            tvPreview!!.textSize = previewTextSize.toFloat()
        }
        val bottomPreviewText: String = bottomBarStyle.getBottomPreviewNormalText()
        if (StyleUtils.checkTextValidity(bottomPreviewText)) {
            tvPreview!!.text = bottomPreviewText
        }
        val editorText: String = bottomBarStyle.getBottomEditorText()
        if (StyleUtils.checkTextValidity(editorText)) {
            tvImageEditor!!.text = editorText
        }
        val editorTextSize: Int = bottomBarStyle.getBottomEditorTextSize()
        if (StyleUtils.checkSizeValidity(editorTextSize)) {
            tvImageEditor!!.textSize = editorTextSize.toFloat()
        }
        val editorTextColor: Int = bottomBarStyle.getBottomEditorTextColor()
        if (StyleUtils.checkStyleValidity(editorTextColor)) {
            tvImageEditor!!.setTextColor(editorTextColor)
        }
        val originalDrawableLeft: Int = bottomBarStyle.getBottomOriginalDrawableLeft()
        if (StyleUtils.checkStyleValidity(originalDrawableLeft)) {
            originalCheckbox!!.setButtonDrawable(originalDrawableLeft)
        }
        val originalText: String = bottomBarStyle.getBottomOriginalText()
        if (StyleUtils.checkTextValidity(originalText)) {
            originalCheckbox!!.text = originalText
        }
        val originalTextSize: Int = bottomBarStyle.getBottomOriginalTextSize()
        if (StyleUtils.checkSizeValidity(originalTextSize)) {
            originalCheckbox!!.textSize = originalTextSize.toFloat()
        }
        val originalTextColor: Int = bottomBarStyle.getBottomOriginalTextColor()
        if (StyleUtils.checkStyleValidity(originalTextColor)) {
            originalCheckbox!!.setTextColor(originalTextColor)
        }
    }

    /**
     * 原图选项发生变化
     */
    fun setOriginalCheck() {
        originalCheckbox!!.isChecked = config.isCheckOriginalImage
    }

    /**
     * 选择结果发生变化
     */
    fun setSelectedChange() {
        calculateFileTotalSize()
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle
        val bottomBarStyle: BottomNavBarStyle = selectorStyle.getBottomBarStyle()
        if (SelectedManager.getSelectCount() > 0) {
            tvPreview!!.isEnabled = true
            val previewSelectTextColor: Int = bottomBarStyle.getBottomPreviewSelectTextColor()
            if (StyleUtils.checkStyleValidity(previewSelectTextColor)) {
                tvPreview!!.setTextColor(previewSelectTextColor)
            } else {
                tvPreview!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_fa632d))
            }
            val previewSelectText: String = bottomBarStyle.getBottomPreviewSelectText()
            if (StyleUtils.checkTextValidity(previewSelectText)) {
                if (StyleUtils.checkTextFormatValidity(previewSelectText)) {
                    tvPreview!!.text =
                        java.lang.String.format(previewSelectText, SelectedManager.getSelectCount())
                } else {
                    tvPreview!!.text = previewSelectText
                }
            } else {
                tvPreview!!.text =
                    context.getString(R.string.ps_preview_num, SelectedManager.getSelectCount())
            }
        } else {
            tvPreview!!.isEnabled = false
            val previewNormalTextColor: Int = bottomBarStyle.getBottomPreviewNormalTextColor()
            if (StyleUtils.checkStyleValidity(previewNormalTextColor)) {
                tvPreview!!.setTextColor(previewNormalTextColor)
            } else {
                tvPreview!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
            }
            val previewText: String = bottomBarStyle.getBottomPreviewNormalText()
            if (StyleUtils.checkTextValidity(previewText)) {
                tvPreview!!.text = previewText
            } else {
                tvPreview!!.text = context.getString(R.string.ps_preview)
            }
        }
    }

    /**
     * 计算原图大小
     */
    private fun calculateFileTotalSize() {
        if (config.isOriginalControl) {
            var totalSize: Long = 0
            for (i in 0 until SelectedManager.getSelectCount()) {
                val media: LocalMedia = SelectedManager.getSelectedResult().get(i)
                totalSize += media.getSize()
            }
            if (totalSize > 0) {
                val fileSize: String = PictureFileUtils.formatAccurateUnitFileSize(totalSize)
                originalCheckbox!!.text = context.getString(R.string.ps_original_image, fileSize)
            } else {
                originalCheckbox!!.text = context.getString(R.string.ps_default_original_image)
            }
        } else {
            originalCheckbox!!.text = context.getString(R.string.ps_default_original_image)
        }
    }

    override fun onClick(view: View) {
        if (bottomNavBarListener == null) {
            return
        }
        val id = view.id
        if (id == R.id.ps_tv_preview) {
            bottomNavBarListener!!.onPreview()
        }
    }

    protected var bottomNavBarListener: OnBottomNavBarListener? = null

    /**
     * 预览NarBar的功能事件回调
     *
     * @param listener
     */
    fun setOnBottomNavBarListener(listener: OnBottomNavBarListener?) {
        bottomNavBarListener = listener
    }

    class OnBottomNavBarListener {
        /**
         * 预览
         */
        fun onPreview() {}

        /**
         * 编辑图片
         */
        fun onEditImage() {}

        /**
         * 原图发生变化
         */
        fun onCheckOriginalChange() {}

        /**
         * 首次选择原图并加入选择结果
         */
        fun onFirstCheckOriginalSelectedChange() {}
    }
}
