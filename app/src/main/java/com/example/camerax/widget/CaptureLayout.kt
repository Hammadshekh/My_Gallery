package com.example.camerax.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.ColorFilter
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat

class CaptureLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    FrameLayout(context!!, attrs, defStyleAttr) {
    private var captureListener //拍照按钮监听
            : CaptureListener? = null
    private var typeListener //拍照或录制后接结果按钮监听
            : TypeListener? = null
    private var leftClickListener //左边按钮监听
            : ClickListener? = null
    private var rightClickListener //右边按钮监听
            : ClickListener? = null

    fun setTypeListener(typeListener: TypeListener?) {
        this.typeListener = typeListener
    }

    fun setCaptureListener(captureListener: CaptureListener?) {
        this.captureListener = captureListener
    }

    private var progress_bar // 拍照等待loading
            : ProgressBar? = null
    private var btn_capture //拍照按钮
            : CaptureButton? = null
    private var btn_confirm //确认按钮
            : TypeButton? = null
    private var btn_cancel //取消按钮
            : TypeButton? = null
    private var btn_return //返回按钮
            : ReturnButton? = null
    private var iv_custom_left //左边自定义按钮
            : ImageView? = null
    private var iv_custom_right //右边自定义按钮
            : ImageView? = null
    private var txt_tip //提示文本
            : TextView? = null
    private var layout_width = 0
    private val layout_height: Int
    private val button_size: Int
    private var iconLeft = 0
    private var iconRight = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(layout_width, layout_height)
    }

    fun initEvent() {
        //默认TypeButton为隐藏
        iv_custom_right!!.visibility = GONE
        btn_cancel.setVisibility(GONE)
        btn_confirm.setVisibility(GONE)
    }

    fun startTypeBtnAnimator() {
        //拍照录制结果后的动画
        if (iconLeft != 0) iv_custom_left!!.visibility = GONE else btn_return.setVisibility(GONE)
        if (iconRight != 0) iv_custom_right!!.visibility = GONE
        btn_capture!!.visibility = GONE
        btn_cancel.setVisibility(VISIBLE)
        btn_confirm.setVisibility(VISIBLE)
        btn_cancel.setClickable(false)
        btn_confirm.setClickable(false)
        iv_custom_left!!.visibility = GONE
        val animator_cancel: ObjectAnimator =
            ObjectAnimator.ofFloat(btn_cancel, "translationX", layout_width / 4, 0)
        val animator_confirm: ObjectAnimator =
            ObjectAnimator.ofFloat(btn_confirm, "translationX", -layout_width / 4, 0)
        val set = AnimatorSet()
        set.playTogether(animator_cancel, animator_confirm)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                btn_cancel.setClickable(true)
                btn_confirm.setClickable(true)
            }
        })
        set.duration = 500
        set.start()
    }

    private fun initView() {
        setWillNotDraw(false)
        //拍照按钮
        progress_bar = ProgressBar(context)
        val progress_bar_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        progress_bar_param.gravity = Gravity.CENTER
        progress_bar!!.layoutParams = progress_bar_param
        progress_bar!!.visibility = GONE
        btn_capture = CaptureButton(context, button_size)
        val btn_capture_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_capture_param.gravity = Gravity.CENTER
        btn_capture!!.layoutParams = btn_capture_param
        btn_capture!!.setCaptureListener(object : CaptureListener() {
            fun takePictures() {
                if (captureListener != null) {
                    captureListener.takePictures()
                }
                startAlphaAnimation()
            }

            fun recordShort(time: Long) {
                if (captureListener != null) {
                    captureListener.recordShort(time)
                }
            }

            fun recordStart() {
                if (captureListener != null) {
                    captureListener.recordStart()
                }
                startAlphaAnimation()
            }

            fun recordEnd(time: Long) {
                if (captureListener != null) {
                    captureListener.recordEnd(time)
                }
                startTypeBtnAnimator()
            }

            fun changeTime(time: Long) {
                if (captureListener != null) {
                    captureListener.changeTime(time)
                }
            }

            fun recordZoom(zoom: Float) {
                if (captureListener != null) {
                    captureListener.recordZoom(zoom)
                }
            }

            fun recordError() {
                if (captureListener != null) {
                    captureListener.recordError()
                }
            }
        })

        //取消按钮
        btn_cancel = TypeButton(context, TypeButton.TYPE_CANCEL, button_size)
        val btn_cancel_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL
        btn_cancel_param.setMargins(layout_width / 4 - button_size / 2, 0, 0, 0)
        btn_cancel.setLayoutParams(btn_cancel_param)
        btn_cancel.setOnClickListener(OnClickListener {
            if (typeListener != null) {
                typeListener.cancel()
            }
        })

        //确认按钮
        btn_confirm = TypeButton(context, TypeButton.TYPE_CONFIRM, button_size)
        val btn_confirm_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        btn_confirm_param.setMargins(0, 0, layout_width / 4 - button_size / 2, 0)
        btn_confirm.setLayoutParams(btn_confirm_param)
        btn_confirm.setOnClickListener(OnClickListener {
            if (typeListener != null) {
                typeListener.confirm()
            }
        })

        //返回按钮
        btn_return = ReturnButton(context, (button_size / 2.5f).toInt())
        val btn_return_param = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        btn_return_param.gravity = Gravity.CENTER_VERTICAL
        btn_return_param.setMargins(layout_width / 6, 0, 0, 0)
        btn_return.setLayoutParams(btn_return_param)
        btn_return.setOnClickListener(OnClickListener {
            if (leftClickListener != null) {
                leftClickListener.onClick()
            }
        })
        //左边自定义按钮
        iv_custom_left = ImageView(context)
        val iv_custom_param_left = LayoutParams((button_size / 2.5f).toInt(),
            (button_size / 2.5f).toInt())
        iv_custom_param_left.gravity = Gravity.CENTER_VERTICAL
        iv_custom_param_left.setMargins(layout_width / 6, 0, 0, 0)
        iv_custom_left!!.layoutParams = iv_custom_param_left
        iv_custom_left!!.setOnClickListener {
            if (leftClickListener != null) {
                leftClickListener.onClick()
            }
        }

        //右边自定义按钮
        iv_custom_right = ImageView(context)
        val iv_custom_param_right = LayoutParams((button_size / 2.5f).toInt(),
            (button_size / 2.5f).toInt())
        iv_custom_param_right.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        iv_custom_param_right.setMargins(0, 0, layout_width / 6, 0)
        iv_custom_right!!.layoutParams = iv_custom_param_right
        iv_custom_right!!.setOnClickListener {
            if (rightClickListener != null) {
                rightClickListener.onClick()
            }
        }
        txt_tip = TextView(context)
        val txt_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        txt_param.gravity = Gravity.CENTER_HORIZONTAL
        txt_param.setMargins(0, 0, 0, 0)
        txt_tip!!.text = captureTip
        txt_tip!!.setTextColor(-0x1)
        txt_tip!!.gravity = Gravity.CENTER
        txt_tip!!.layoutParams = txt_param
        this.addView(btn_capture)
        this.addView(progress_bar)
        this.addView(btn_cancel)
        this.addView(btn_confirm)
        this.addView(btn_return)
        this.addView(iv_custom_left)
        this.addView(iv_custom_right)
        this.addView(txt_tip)
    }

    private val captureTip: String
        private get() {
            val buttonFeatures: Int = btn_capture.getButtonFeatures()
            return when (buttonFeatures) {
                CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE -> context.getString(R.string.picture_photo_pictures)
                CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER -> context.getString(R.string.picture_photo_recording)
                else -> context.getString(R.string.picture_photo_camera)
            }
        }

    fun setButtonCaptureEnabled(enabled: Boolean) {
        progress_bar!!.visibility = if (enabled) GONE else VISIBLE
        btn_capture!!.setButtonCaptureEnabled(enabled)
    }

    fun setCaptureLoadingColor(color: Int) {
        val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color,
            BlendModeCompat.SRC_IN)
        progress_bar!!.indeterminateDrawable.colorFilter = colorFilter
    }

    fun setProgressColor(color: Int) {
        btn_capture!!.setProgressColor(color)
    }

    fun resetCaptureLayout() {
        btn_capture!!.resetState()
        btn_cancel.setVisibility(GONE)
        btn_confirm.setVisibility(GONE)
        btn_capture!!.visibility = VISIBLE
        txt_tip!!.text = captureTip
        txt_tip!!.visibility = VISIBLE
        if (iconLeft != 0) iv_custom_left!!.visibility = VISIBLE else btn_return.setVisibility(
            VISIBLE)
        if (iconRight != 0) iv_custom_right!!.visibility = VISIBLE
    }

    fun startAlphaAnimation() {
        txt_tip!!.visibility = INVISIBLE
    }

    fun setTextWithAnimation(tip: String?) {
        txt_tip!!.text = tip
        val animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f, 1f, 0f)
        animator_txt_tip.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                txt_tip!!.text = captureTip
                txt_tip!!.alpha = 1f
            }
        })
        animator_txt_tip.duration = 2500
        animator_txt_tip.start()
    }

    fun setDuration(duration: Int) {
        btn_capture!!.setMaxDuration(duration)
    }

    fun setMinDuration(duration: Int) {
        btn_capture!!.setMinDuration(duration)
    }

    fun setButtonFeatures(state: Int) {
        btn_capture.setButtonFeatures(state)
        txt_tip!!.text = captureTip
    }

    fun setTip(tip: String?) {
        txt_tip!!.text = tip
    }

    fun showTip() {
        txt_tip!!.visibility = VISIBLE
    }

    fun setIconSrc(iconLeft: Int, iconRight: Int) {
        this.iconLeft = iconLeft
        this.iconRight = iconRight
        if (this.iconLeft != 0) {
            iv_custom_left!!.setImageResource(iconLeft)
            iv_custom_left!!.visibility = VISIBLE
            btn_return.setVisibility(GONE)
        } else {
            iv_custom_left!!.visibility = GONE
            btn_return.setVisibility(VISIBLE)
        }
        if (this.iconRight != 0) {
            iv_custom_right!!.setImageResource(iconRight)
            iv_custom_right!!.visibility = VISIBLE
        } else {
            iv_custom_right!!.visibility = GONE
        }
    }

    fun setLeftClickListener(leftClickListener: ClickListener?) {
        this.leftClickListener = leftClickListener
    }

    fun setRightClickListener(rightClickListener: ClickListener?) {
        this.rightClickListener = rightClickListener
    }

    init {
        val screenWidth: Int = DensityUtil.getScreenWidth(getContext())
        layout_width =
            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                screenWidth
            } else {
                screenWidth / 2
            }
        button_size = (layout_width / 4.5f).toInt()
        layout_height = button_size + button_size / 5 * 2 + 100
        initView()
        initEvent()
    }
}