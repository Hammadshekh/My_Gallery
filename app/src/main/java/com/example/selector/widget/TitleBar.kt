package com.example.selector.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.style.PictureSelectorStyle
import com.example.selector.style.TitleBarStyle
import com.example.selector.utils.DensityUtil
import com.example.selector.utils.StyleUtils

open class TitleBar : RelativeLayout, View.OnClickListener {
    var rlAlbumBg: RelativeLayout? = null
    var ivLeftBack: ImageView? = null
    var imageArrow: ImageView? = null
        private set
    var imageDelete: ImageView? = null
        private set
    private var tvTitle: MarqueeTextView? = null
    var titleCancelView: TextView? = null
        private set

    /**
     * title bar line
     *
     * @return
     */
    var titleBarLine: View? = null
        private set
    var viewAlbumClickArea: View? = null
    private var config: PictureSelectionConfig? = null
    private var viewTopStatusBar: View? = null
    private var titleBarLayout: RelativeLayout? = null

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
        viewTopStatusBar = findViewById(R.id.top_status_bar)
        titleBarLayout = findViewById(R.id.rl_title_bar)
        ivLeftBack = findViewById(R.id.ps_iv_left_back)
        rlAlbumBg = findViewById(R.id.ps_rl_album_bg)
        imageDelete = findViewById(R.id.ps_iv_delete)
        viewAlbumClickArea = findViewById(R.id.ps_rl_album_click)
        tvTitle = findViewById(R.id.ps_tv_title)
        imageArrow = findViewById(R.id.ps_iv_arrow)
        titleCancelView = findViewById(R.id.ps_tv_cancel)
        titleBarLine = findViewById(R.id.title_bar_line)
        ivLeftBack!!.setOnClickListener(this)
        titleCancelView!!.setOnClickListener(this)
        rlAlbumBg!!.setOnClickListener(this)
        titleBarLayout!!.setOnClickListener(this)
        viewAlbumClickArea!!.setOnClickListener(this)
        setBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_grey))
        handleLayoutUI()
        if (TextUtils.isEmpty(config.defaultAlbumName)) {
            setTitle(if (config.chooseMode == SelectMimeType.ofAudio()) context.getString(R.string.ps_all_audio) else context.getString(
                R.string.ps_camera_roll))
        } else {
            setTitle(config.defaultAlbumName)
        }
    }

    private fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.ps_title_bar, this)
    }

    private fun handleLayoutUI() {}

    /**
     * Set title
     *
     * @param title
     */
    fun setTitle(title: String?) {
        tvTitle!!.text = title
    }

    /**
     * Get title text
     */
    val titleText: String
        get() = tvTitle!!.text.toString()

    open fun setTitleBarStyle() {
        if (config.isPreviewFullScreenMode) {
            val layoutParams = viewTopStatusBar!!.layoutParams
            layoutParams.height = DensityUtil.getStatusBarHeight(context)
        }
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle!!
        val titleBarStyle: TitleBarStyle = selectorStyle.titleBarStyle!!
        val titleBarHeight: Int = titleBarStyle.titleBarHeight
        if (StyleUtils.checkSizeValidity(titleBarHeight)) {
            titleBarLayout!!.layoutParams.height = titleBarHeight
        } else {
            titleBarLayout!!.layoutParams.height = DensityUtil.dip2px(context, 48f)
        }
        if (titleBarLine != null) {
            if (titleBarStyle.isDisplayTitleBarLine) {
                titleBarLine!!.visibility = VISIBLE
                if (StyleUtils.checkStyleValidity(titleBarStyle.titleBarLineColor)) {
                    titleBarLine!!.setBackgroundColor(titleBarStyle.titleBarLineColor)
                }
            } else {
                titleBarLine!!.visibility = GONE
            }
        }
        val backgroundColor: Int = titleBarStyle.titleBackgroundColor
        if (StyleUtils.checkStyleValidity(backgroundColor)) {
            setBackgroundColor(backgroundColor)
        }
        val backResId: Int = titleBarStyle.titleLeftBackResource
        if (StyleUtils.checkStyleValidity(backResId)) {
            ivLeftBack!!.setImageResource(backResId)
        }
        val titleDefaultText: String = titleBarStyle.titleDefaultText.toString()
        if (StyleUtils.checkTextValidity(titleDefaultText)) {
            tvTitle!!.text = titleDefaultText
        }
        val titleTextSize: Int = titleBarStyle.titleTextSize
        if (StyleUtils.checkSizeValidity(titleTextSize)) {
            tvTitle!!.textSize = titleTextSize.toFloat()
        }
        val titleTextColor: Int = titleBarStyle.titleTextColor
        if (StyleUtils.checkStyleValidity(titleTextColor)) {
            tvTitle!!.setTextColor(titleTextColor)
        }
        if (config.isOnlySandboxDir) {
            imageArrow!!.setImageResource(R.drawable.ps_ic_trans_1px)
        } else {
            val arrowResId: Int = titleBarStyle.titleDrawableRightResource
            if (StyleUtils.checkStyleValidity(arrowResId)) {
                imageArrow!!.setImageResource(arrowResId)
            }
        }
        val albumBackgroundRes: Int = titleBarStyle.titleAlbumBackgroundResource
        if (StyleUtils.checkStyleValidity(albumBackgroundRes)) {
            rlAlbumBg!!.setBackgroundResource(albumBackgroundRes)
        }
        if (titleBarStyle.isHideCancelButton) {
            titleCancelView!!.visibility = GONE
        } else {
            titleCancelView!!.visibility = VISIBLE
            val titleCancelBackgroundResource: Int =
                titleBarStyle.titleCancelBackgroundResource
            if (StyleUtils.checkStyleValidity(titleCancelBackgroundResource)) {
                titleCancelView!!.setBackgroundResource(titleCancelBackgroundResource)
            }
            val titleCancelText: String = titleBarStyle.titleCancelText.toString()
            if (StyleUtils.checkTextValidity(titleCancelText)) {
                titleCancelView!!.text = titleCancelText
            }
            val titleCancelTextColor: Int = titleBarStyle.titleCancelTextColor
            if (StyleUtils.checkStyleValidity(titleCancelTextColor)) {
                titleCancelView!!.setTextColor(titleCancelTextColor)
            }
            val titleCancelTextSize: Int = titleBarStyle.titleCancelTextSize
            if (StyleUtils.checkSizeValidity(titleCancelTextSize)) {
                titleCancelView!!.textSize = titleCancelTextSize.toFloat()
            }
        }
        val deleteBackgroundResource: Int = titleBarStyle.previewDeleteBackgroundResource
        if (StyleUtils.checkStyleValidity(deleteBackgroundResource)) {
            imageDelete!!.setBackgroundResource(deleteBackgroundResource)
        } else {
            imageDelete!!.setBackgroundResource(R.drawable.ps_ic_delete)
        }
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.ps_iv_left_back || id == R.id.ps_tv_cancel) {
            if (titleBarListener != null) {
                titleBarListener!!.onBackPressed()
            }
        } else if (id == R.id.ps_rl_album_bg || id == R.id.ps_rl_album_click) {
            if (titleBarListener != null) {
                titleBarListener!!.onShowAlbumPopWindow(this)
            }
        } else if (id == R.id.rl_title_bar) {
            if (titleBarListener != null) {
                titleBarListener!!.onTitleDoubleClick()
            }
        }
    }

    private var titleBarListener: OnTitleBarListener? = null

    /**
     * TitleBar的功能事件回调
     *
     * @param listener
     */
    fun setOnTitleBarListener(listener: OnTitleBarListener?) {
        titleBarListener = listener
    }

    open class OnTitleBarListener {
        /**
         * 双击标题栏
         */
        open fun onTitleDoubleClick() {}

        /**
         * 关闭页面
         */
        open fun onBackPressed() {}

        /**
         * 显示专辑列表
         */
        open fun onShowAlbumPopWindow(anchor: View?) {}
    }
}