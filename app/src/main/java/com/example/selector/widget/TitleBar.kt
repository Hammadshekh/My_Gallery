package com.example.selector.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class TitleBar : RelativeLayout, View.OnClickListener {
    protected var rlAlbumBg: RelativeLayout? = null
    protected var ivLeftBack: ImageView? = null
    var imageArrow: ImageView? = null
        protected set
    var imageDelete: ImageView? = null
        protected set
    protected var tvTitle: MarqueeTextView? = null
    var titleCancelView: TextView? = null
        protected set

    /**
     * title bar line
     *
     * @return
     */
    var titleBarLine: View? = null
        protected set
    protected var viewAlbumClickArea: View? = null
    protected var config: PictureSelectionConfig? = null
    protected var viewTopStatusBar: View? = null
    protected var titleBarLayout: RelativeLayout? = null

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
        ivLeftBack.setOnClickListener(this)
        titleCancelView.setOnClickListener(this)
        rlAlbumBg.setOnClickListener(this)
        titleBarLayout.setOnClickListener(this)
        viewAlbumClickArea.setOnClickListener(this)
        setBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_grey))
        handleLayoutUI()
        if (TextUtils.isEmpty(config.defaultAlbumName)) {
            setTitle(if (config.chooseMode === SelectMimeType.ofAudio()) context.getString(R.string.ps_all_audio) else context.getString(
                R.string.ps_camera_roll))
        } else {
            setTitle(config.defaultAlbumName)
        }
    }

    protected fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.ps_title_bar, this)
    }

    protected fun handleLayoutUI() {}

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

    fun setTitleBarStyle() {
        if (config.isPreviewFullScreenMode) {
            val layoutParams = viewTopStatusBar!!.layoutParams
            layoutParams.height = DensityUtil.getStatusBarHeight(context)
        }
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle
        val titleBarStyle: TitleBarStyle = selectorStyle.getTitleBarStyle()
        val titleBarHeight: Int = titleBarStyle.getTitleBarHeight()
        if (StyleUtils.checkSizeValidity(titleBarHeight)) {
            titleBarLayout!!.layoutParams.height = titleBarHeight
        } else {
            titleBarLayout!!.layoutParams.height = DensityUtil.dip2px(context, 48)
        }
        if (titleBarLine != null) {
            if (titleBarStyle.isDisplayTitleBarLine()) {
                titleBarLine!!.visibility = VISIBLE
                if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleBarLineColor())) {
                    titleBarLine!!.setBackgroundColor(titleBarStyle.getTitleBarLineColor())
                }
            } else {
                titleBarLine!!.visibility = GONE
            }
        }
        val backgroundColor: Int = titleBarStyle.getTitleBackgroundColor()
        if (StyleUtils.checkStyleValidity(backgroundColor)) {
            setBackgroundColor(backgroundColor)
        }
        val backResId: Int = titleBarStyle.getTitleLeftBackResource()
        if (StyleUtils.checkStyleValidity(backResId)) {
            ivLeftBack!!.setImageResource(backResId)
        }
        val titleDefaultText: String = titleBarStyle.getTitleDefaultText()
        if (StyleUtils.checkTextValidity(titleDefaultText)) {
            tvTitle!!.text = titleDefaultText
        }
        val titleTextSize: Int = titleBarStyle.getTitleTextSize()
        if (StyleUtils.checkSizeValidity(titleTextSize)) {
            tvTitle!!.textSize = titleTextSize.toFloat()
        }
        val titleTextColor: Int = titleBarStyle.getTitleTextColor()
        if (StyleUtils.checkStyleValidity(titleTextColor)) {
            tvTitle!!.setTextColor(titleTextColor)
        }
        if (config.isOnlySandboxDir) {
            imageArrow!!.setImageResource(R.drawable.ps_ic_trans_1px)
        } else {
            val arrowResId: Int = titleBarStyle.getTitleDrawableRightResource()
            if (StyleUtils.checkStyleValidity(arrowResId)) {
                imageArrow!!.setImageResource(arrowResId)
            }
        }
        val albumBackgroundRes: Int = titleBarStyle.getTitleAlbumBackgroundResource()
        if (StyleUtils.checkStyleValidity(albumBackgroundRes)) {
            rlAlbumBg!!.setBackgroundResource(albumBackgroundRes)
        }
        if (titleBarStyle.isHideCancelButton()) {
            titleCancelView!!.visibility = GONE
        } else {
            titleCancelView!!.visibility = VISIBLE
            val titleCancelBackgroundResource: Int =
                titleBarStyle.getTitleCancelBackgroundResource()
            if (StyleUtils.checkStyleValidity(titleCancelBackgroundResource)) {
                titleCancelView!!.setBackgroundResource(titleCancelBackgroundResource)
            }
            val titleCancelText: String = titleBarStyle.getTitleCancelText()
            if (StyleUtils.checkTextValidity(titleCancelText)) {
                titleCancelView!!.text = titleCancelText
            }
            val titleCancelTextColor: Int = titleBarStyle.getTitleCancelTextColor()
            if (StyleUtils.checkStyleValidity(titleCancelTextColor)) {
                titleCancelView!!.setTextColor(titleCancelTextColor)
            }
            val titleCancelTextSize: Int = titleBarStyle.getTitleCancelTextSize()
            if (StyleUtils.checkSizeValidity(titleCancelTextSize)) {
                titleCancelView!!.textSize = titleCancelTextSize.toFloat()
            }
        }
        val deleteBackgroundResource: Int = titleBarStyle.getPreviewDeleteBackgroundResource()
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

    protected var titleBarListener: OnTitleBarListener? = null

    /**
     * TitleBar的功能事件回调
     *
     * @param listener
     */
    fun setOnTitleBarListener(listener: OnTitleBarListener?) {
        titleBarListener = listener
    }

    class OnTitleBarListener {
        /**
         * 双击标题栏
         */
        fun onTitleDoubleClick() {}

        /**
         * 关闭页面
         */
        fun onBackPressed() {}

        /**
         * 显示专辑列表
         */
        fun onShowAlbumPopWindow(anchor: View?) {}
    }
}