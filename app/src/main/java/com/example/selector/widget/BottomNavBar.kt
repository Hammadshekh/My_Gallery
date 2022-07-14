package com.example.selector.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.manager.SelectedManager
import com.example.selector.style.BottomNavBarStyle
import com.example.selector.style.PictureSelectorStyle
import com.example.selector.utils.DensityUtil
import com.example.selector.utils.PictureFileUtils
import com.example.selector.utils.StyleUtils
import com.luck.picture.lib.entity.LocalMedia

open class BottomNavBar : RelativeLayout, View.OnClickListener {
    var tvPreview: TextView? = null
    var tvImageEditor: TextView? = null
    private var originalCheckbox: CheckBox? = null
    private var config: PictureSelectionconfig = null

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

    private fun init() {
        inflateLayout()
        isClickable = true
        isFocusable = true
        config = PictureSelectionConfig.instance!!
        tvPreview = findViewById(R.id.ps_tv_preview)
        tvImageEditor = findViewById(R.id.ps_tv_editor)
        originalCheckbox = findViewById(R.id.cb_original)
        tvPreview!!.setOnClickListener(this)
        tvImageEditor!!.visibility = GONE
        setBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_grey))
        originalCheckbox!!.isChecked = config.isCheckOriginalImage
        originalCheckbox!!.setOnCheckedChangeListener { _, isChecked ->
            config.isCheckOriginalImage = isChecked
            originalCheckbox!!.isChecked = config.isCheckOriginalImage
            if (bottomNavBarListener != null) {
                bottomNavBarListener!!.onCheckOriginalChange()
                if (isChecked && SelectedManager.selectCount == 0) {
                    bottomNavBarListener!!.onFirstCheckOriginalSelectedChange()
                }
            }
        }
        handleLayoutUI()
    }

    private fun inflateLayout() {
        inflate(context, R.layout.ps_bottom_nav_bar, this)
    }

    open fun handleLayoutUI() {}
    open fun setBottomNavBarStyle() {
        if (config.isDirectReturnSingle) {
            visibility = GONE
            return
        }
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle!!
        val bottomBarStyle: BottomNavBarStyle? = selectorStyle.bottomBarStyle
        if (config.isOriginalControl) {
            originalCheckbox!!.visibility = VISIBLE
            val originalDrawableLeft: Int = bottomBarStyle!!.bottomOriginalDrawableLeft
            if (StyleUtils.checkStyleValidity(originalDrawableLeft)) {
                originalCheckbox!!.setButtonDrawable(originalDrawableLeft)
            }
            val bottomOriginalText: String = bottomBarStyle.bottomOriginalText.toString()
            if (StyleUtils.checkTextValidity(bottomOriginalText)) {
                originalCheckbox!!.text = bottomOriginalText
            }
            val originalTextSize: Int = bottomBarStyle.bottomOriginalTextSize
            if (StyleUtils.checkSizeValidity(originalTextSize)) {
                originalCheckbox!!.textSize = originalTextSize.toFloat()
            }
            val originalTextColor: Int = bottomBarStyle.bottomOriginalTextColor
            if (StyleUtils.checkStyleValidity(originalTextColor)) {
                originalCheckbox!!.setTextColor(originalTextColor)
            }
        }
        val narBarHeight: Int = bottomBarStyle!!.bottomNarBarHeight
        if (StyleUtils.checkSizeValidity(narBarHeight)) {
            layoutParams.height = narBarHeight
        } else {
            layoutParams.height = DensityUtil.dip2px(context, 46f)
        }
        val backgroundColor: Int = bottomBarStyle.bottomNarBarBackgroundColor
        if (StyleUtils.checkStyleValidity(backgroundColor)) {
            setBackgroundColor(backgroundColor)
        }
        val previewNormalTextColor: Int = bottomBarStyle.bottomPreviewNormalTextColor
        if (StyleUtils.checkStyleValidity(previewNormalTextColor)) {
            tvPreview!!.setTextColor(previewNormalTextColor)
        }
        val previewTextSize: Int = bottomBarStyle.bottomPreviewNormalTextSize
        if (StyleUtils.checkSizeValidity(previewTextSize)) {
            tvPreview!!.textSize = previewTextSize.toFloat()
        }
        val bottomPreviewText: String = bottomBarStyle.bottomPreviewNormalText.toString()
        if (StyleUtils.checkTextValidity(bottomPreviewText)) {
            tvPreview!!.text = bottomPreviewText
        }
        val editorText: String? = bottomBarStyle.bottomEditorText
        if (StyleUtils.checkTextValidity(editorText)) {
            tvImageEditor!!.text = editorText
        }
        val editorTextSize: Int = bottomBarStyle.bottomEditorTextSize
        if (StyleUtils.checkSizeValidity(editorTextSize)) {
            tvImageEditor!!.textSize = editorTextSize.toFloat()
        }
        val editorTextColor: Int = bottomBarStyle.bottomEditorTextColor
        if (StyleUtils.checkStyleValidity(editorTextColor)) {
            tvImageEditor!!.setTextColor(editorTextColor)
        }
        val originalDrawableLeft: Int = bottomBarStyle.bottomOriginalDrawableLeft
        if (StyleUtils.checkStyleValidity(originalDrawableLeft)) {
            originalCheckbox!!.setButtonDrawable(originalDrawableLeft)
        }
        val originalText: String = bottomBarStyle.bottomOriginalText.toString()
        if (StyleUtils.checkTextValidity(originalText)) {
            originalCheckbox!!.text = originalText
        }
        val originalTextSize: Int = bottomBarStyle.bottomOriginalTextSize
        if (StyleUtils.checkSizeValidity(originalTextSize)) {
            originalCheckbox!!.textSize = originalTextSize.toFloat()
        }
        val originalTextColor: Int = bottomBarStyle.bottomOriginalTextColor
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
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle!!
        val bottomBarStyle: BottomNavBarStyle = selectorStyle.bottomBarStyle!!
        if (SelectedManager.selectCount > 0) {
            tvPreview!!.isEnabled = true
            val previewSelectTextColor: Int = bottomBarStyle.bottomPreviewSelectTextColor
            if (StyleUtils.checkStyleValidity(previewSelectTextColor)) {
                tvPreview!!.setTextColor(previewSelectTextColor)
            } else {
                tvPreview!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_fa632d))
            }
            val previewSelectText: String = bottomBarStyle.bottomPreviewSelectText.toString()
            if (StyleUtils.checkTextValidity(previewSelectText)) {
                if (StyleUtils.checkTextFormatValidity(previewSelectText)) {
                    tvPreview!!.text =
                        java.lang.String.format(previewSelectText, SelectedManager.selectCount)
                } else {
                    tvPreview!!.text = previewSelectText
                }
            } else {
                tvPreview!!.text =
                    context.getString(R.string.ps_preview_num, SelectedManager.selectCount)
            }
        } else {
            tvPreview!!.isEnabled = false
            val previewNormalTextColor: Int = bottomBarStyle.bottomPreviewNormalTextColor
            if (StyleUtils.checkStyleValidity(previewNormalTextColor)) {
                tvPreview!!.setTextColor(previewNormalTextColor)
            } else {
                tvPreview!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
            }
            val previewText: String = bottomBarStyle.bottomPreviewNormalText.toString()
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
            for (i in 0 until SelectedManager.selectCount) {
                val media: LocalMedia = SelectedManager.selectedResult[i]
                totalSize += media.size
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

    var bottomNavBarListener: OnBottomNavBarListener? = null

    /**
     * 预览NarBar的功能事件回调
     *
     * @param listener
     */
    fun setOnBottomNavBarListener(listener: OnBottomNavBarListener?) {
        bottomNavBarListener = listener
    }

    open class OnBottomNavBarListener {
        /**
         * 预览
         */
        open fun onPreview() {}

        /**
         * 编辑图片
         */
        open fun onEditImage() {}

        /**
         * 原图发生变化
         */
        open fun onCheckOriginalChange() {}

        /**
         * 首次选择原图并加入选择结果
         */
        open fun onFirstCheckOriginalSelectedChange() {}
    }
}
