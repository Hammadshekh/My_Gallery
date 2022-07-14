package com.example.camerax.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.Gravity
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.example.camerax.CustomCameraConfig
import com.example.camerax.listener.CaptureListener
import com.example.camerax.listener.ClickListener
import com.example.camerax.listener.TypeListener
import com.example.camerax.utils.DensityUtil
import com.example.mygallery.R

class CaptureLayout (
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    FrameLayout(context!!, attrs, defStyleAttr) {
    private var captureListener //Camera button monitor
            : CaptureListener? = null
    private var typeListener //Take a picture or record followed by the result button monitoring
            : TypeListener? = null
    private var leftClickListener //left button monitor
            : ClickListener? = null
    private var rightClickListener //Right button monitor
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

    private fun initEvent() {
        //默认TypeButton为隐藏
        iv_custom_right!!.visibility = GONE
        btn_cancel!!.visibility = GONE
        btn_confirm!!.visibility = GONE
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun startTypeBtnAnimator() {
        //The animation after taking a picture and recording the result
        if (iconLeft != 0) iv_custom_left!!.visibility = GONE else btn_return!!.visibility =
            GONE
        if (iconRight != 0) iv_custom_right!!.visibility = GONE
        btn_capture!!.visibility = GONE
        btn_cancel!!.visibility = VISIBLE
        btn_confirm!!.visibility = VISIBLE
        btn_cancel!!.isClickable = false
        btn_confirm!!.isClickable = false
        iv_custom_left!!.visibility = GONE
        val animator_cancel: ObjectAnimator =
            ObjectAnimator.ofFloat(btn_cancel, "translationX", layout_width / 4f, 0f)
        val animator_confirm: ObjectAnimator =
            ObjectAnimator.ofFloat(btn_confirm, "translationX", -layout_width / 4f, 0f)
        val set = AnimatorSet()
        set.playTogether(animator_cancel, animator_confirm)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                btn_cancel!!.isClickable = true
                btn_confirm!!.isClickable = true
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
        btn_capture!!.setCaptureListener(object : CaptureListener {
            override fun takePictures() {
                captureListener?.takePictures()
                startAlphaAnimation()
            }

            override fun recordShort(time: Long) {
                captureListener?.recordShort(time)
            }

            override fun recordStart() {
                captureListener?.recordStart()
                startAlphaAnimation()
            }

            override fun recordEnd(time: Long) {
                captureListener?.recordEnd(time)
                startTypeBtnAnimator()
            }

            override fun changeTime(time: Long) {
                captureListener?.changeTime(time)
            }

            override fun recordZoom(zoom: Float) {
                captureListener?.recordZoom(zoom)
            }

            override fun recordError() {
                captureListener?.recordError()
            }
        })

        //取消按钮
        btn_cancel = TypeButton(context, TypeButton.TYPE_CANCEL, button_size)
        val btn_cancel_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL
        btn_cancel_param.setMargins(layout_width / 4 - button_size / 2, 0, 0, 0)
        btn_cancel!!.layoutParams = btn_cancel_param
        btn_cancel!!.setOnClickListener(OnClickListener {
            typeListener?.cancel()
        })

        //确认按钮
        btn_confirm = TypeButton(context, TypeButton.TYPE_CONFIRM, button_size)
        val btn_confirm_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        btn_confirm_param.setMargins(0, 0, layout_width / 4 - button_size / 2, 0)
        btn_confirm!!.layoutParams = btn_confirm_param
        btn_confirm!!.setOnClickListener(OnClickListener {
            typeListener?.confirm()
        })

        //返回按钮
        btn_return = ReturnButton(context, (button_size / 2.5f).toInt())
        val btn_return_param = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        btn_return_param.gravity = Gravity.CENTER_VERTICAL
        btn_return_param.setMargins(layout_width / 6, 0, 0, 0)
        btn_return!!.setLayoutParams(btn_return_param)
        btn_return!!.setOnClickListener(OnClickListener {
            leftClickListener?.onClick()
        })
        //左边自定义按钮
        iv_custom_left = ImageView(context)
        val iv_custom_param_left = LayoutParams((button_size / 2.5f).toInt(),
            (button_size / 2.5f).toInt())
        iv_custom_param_left.gravity = Gravity.CENTER_VERTICAL
        iv_custom_param_left.setMargins(layout_width / 6, 0, 0, 0)
        iv_custom_left!!.layoutParams = iv_custom_param_left
        iv_custom_left!!.setOnClickListener {
            leftClickListener?.onClick()
        }

        //右边自定义按钮
        iv_custom_right = ImageView(context)
        val iv_custom_param_right = LayoutParams((button_size / 2.5f).toInt(),
            (button_size / 2.5f).toInt())
        iv_custom_param_right.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        iv_custom_param_right.setMargins(0, 0, layout_width / 6, 0)
        iv_custom_right!!.layoutParams = iv_custom_param_right
        iv_custom_right!!.setOnClickListener {
            rightClickListener?.onClick()
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
         get() {
            val buttonFeatures: Int = btn_capture!!.buttonFeatures
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
        btn_cancel!!.visibility = GONE
        btn_confirm!!.visibility = GONE
        btn_capture!!.visibility = VISIBLE
        txt_tip!!.text = captureTip
        txt_tip!!.visibility = VISIBLE
        if (iconLeft != 0) iv_custom_left!!.visibility = VISIBLE else btn_return!!.visibility =
            VISIBLE
        if (iconRight != 0) iv_custom_right!!.visibility = VISIBLE
    }

    fun startAlphaAnimation() {
        txt_tip!!.visibility = INVISIBLE
    }

    @SuppressLint("ObjectAnimatorBinding")
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
        btn_capture!!.buttonFeatures = state
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
            btn_return!!.visibility = GONE
        } else {
            iv_custom_left!!.visibility = GONE
            btn_return!!.visibility = VISIBLE
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
        val screenWidth: Int = DensityUtil.getScreenWidth(requireContext())
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