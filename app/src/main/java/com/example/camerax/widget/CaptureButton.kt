package com.example.camerax.widget

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.camerax.CustomCameraConfig
import com.example.camerax.listener.CaptureListener
import com.example.camerax.listener.IObtainCameraView
import com.example.camerax.permissions.PermissionChecker
import com.example.camerax.permissions.PermissionResultCallback
import com.example.camerax.permissions.SimpleXPermissionUtil
import com.example.camerax.utils.DoubleUtils
import com.example.camerax.utils.SimpleXSpUtils

class CaptureButton : View {
    // current button state
    private var state = 0

    // The status of the function that the button can perform (photograph, record, both)
    var buttonFeatures = 0

    // Recording progress outer ring color value
    private var progressColor = -0x11e951ea
    private var event_Y = 0f
    private var mPaint: Paint? = null


     // progress bar width

    private var strokeWidth = 0f

    //Long press the Size whose outer circle radius becomes larger
    private var outside_add_size = 0

    //The size of the inner circle of Chang'an reduced
    private var inside_reduce_size = 0
    private var center_X = 0f
    private var center_Y = 0f
// button radius
    private var button_radius = 0f

  // Outer circle radius
    private var button_outside_radius = 0f

    // Inner circle radius
    private var button_inside_radius = 0f

   //button size
    private var button_size = 0

    //Progress of recording video
    private var progress = 0f

   // Maximum length of time to record video
    private var maxDuration = 0

  //Minimum recording time limit
    private var minDuration = 0

  //Record the current recording time
    private var currentRecordedTime = 0
    private var rectF: RectF? = null
    private var longPressRunnable: LongPressRunnable? = null

   // button callback interface
    private var captureListener: CaptureListener? = null

    //timer
    private var timer: RecordCountDownTimer? = null
    private var isTakeCamera = true
    private val activity: Activity

    constructor(context: Context) : super(context) {
        activity = context as Activity
    }

    constructor(context: Context, size: Int) : super(context) {
        activity = context as Activity
        button_size = size
        button_radius = size / 2.0f
        button_outside_radius = button_radius
        button_inside_radius = button_radius * 0.75f
        strokeWidth = (size / 15).toFloat()
        outside_add_size = size / 8
        inside_reduce_size = size / 8
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        progress = 0f
        longPressRunnable = LongPressRunnable()
        state = STATE_IDLE
        buttonFeatures = CustomCameraConfig.BUTTON_STATE_BOTH
        maxDuration = CustomCameraConfig.DEFAULT_MAX_RECORD_VIDEO
        minDuration = CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO
        center_X = ((button_size + outside_add_size * 2) / 2).toFloat()
        center_Y = ((button_size + outside_add_size * 2) / 2).toFloat()
        rectF = RectF(
            center_X - (button_radius + outside_add_size - strokeWidth / 2),
            center_Y - (button_radius + outside_add_size - strokeWidth / 2),
            center_X + (button_radius + outside_add_size - strokeWidth / 2),
            center_Y + (button_radius + outside_add_size - strokeWidth / 2))
        timer = RecordCountDownTimer(maxDuration.toLong(), (maxDuration / 360).toLong())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(button_size + outside_add_size * 2, button_size + outside_add_size * 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint!!.style = Paint.Style.FILL
        val outside_color = -0x11232324
        mPaint!!.color = outside_color
        canvas.drawCircle(center_X, center_Y, button_outside_radius, mPaint!!)
        val inside_color = -0x1
        mPaint!!.color = inside_color
        canvas.drawCircle(center_X, center_Y, button_inside_radius, mPaint!!)
        if (state == STATE_RECORDER_ING) {
            mPaint!!.color = progressColor
            mPaint!!.style = Paint.Style.STROKE
            mPaint!!.strokeWidth = strokeWidth
            canvas.drawArc(rectF!!, -90f, progress, false, mPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTakeCamera) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.pointerCount > 1 || state != STATE_IDLE)
                    event_Y = event.y
                    state = STATE_PRESS
                    if (buttonFeatures != CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE) {
                        postDelayed(longPressRunnable, 500)
                    }
                }
                MotionEvent.ACTION_MOVE -> if (captureListener != null && state == STATE_RECORDER_ING && (buttonFeatures == CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER
                            || buttonFeatures == CustomCameraConfig.BUTTON_STATE_BOTH)
                ) {
                    captureListener!!.recordZoom(event_Y - event.y)
                }
                MotionEvent.ACTION_UP -> handlerPressByState()
            }
        }
        return true
    }

    private val customCameraView: ViewGroup?
        private get() {
            if (activity is IObtainCameraView) {
                val cameraView: IObtainCameraView = activity as IObtainCameraView
                return cameraView.customCameraView
            }
            return null
        }

    private fun handlerPressByState() {
        removeCallbacks(longPressRunnable)
        when (state) {
            STATE_PRESS -> if (captureListener != null && (buttonFeatures == CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE || buttonFeatures ==
                        CustomCameraConfig.BUTTON_STATE_BOTH)
            ) {
                startCaptureAnimation(button_inside_radius)
            } else {
                state = STATE_IDLE
            }
            STATE_LONG_PRESS, STATE_RECORDER_ING -> if (PermissionChecker.checkSelfPermission(
                    context, arrayOf(Manifest.permission.RECORD_AUDIO))
            ) {
                timer!!.cancel()
                recordEnd()
            }
        }
        state = STATE_IDLE
    }

    fun recordEnd() {
        if (captureListener != null) {
            if (currentRecordedTime < minDuration) {
                captureListener!!.recordShort(currentRecordedTime.toLong())
            } else {
                captureListener!!.recordEnd(currentRecordedTime.toLong())
            }
        }
        resetRecordAnim()
    }

    private fun resetRecordAnim() {
        state = STATE_BAN
        progress = 0f
        invalidate()
        startRecordAnimation(
            button_outside_radius,
            button_radius,
            button_inside_radius,
            button_radius * 0.75f
        )
    }

    private fun startCaptureAnimation(inside_start: Float) {
        val inside_anim = ValueAnimator.ofFloat(inside_start, inside_start * 0.75f, inside_start)
        inside_anim.addUpdateListener { animation: ValueAnimator ->
            button_inside_radius = animation.animatedValue as Float
            invalidate()
        }
        inside_anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                if (captureListener != null) {
                    captureListener!!.takePictures()
                }
                state = STATE_BAN
            }
        })
        inside_anim.duration = 50
        inside_anim.start()
    }

    private fun startRecordAnimation(
        outside_start: Float,
        outside_end: Float,
        inside_start: Float,
        inside_end: Float,
    ) {
        val outside_anim = ValueAnimator.ofFloat(outside_start, outside_end)
        val inside_anim = ValueAnimator.ofFloat(inside_start, inside_end)
        //外圆动画监听
        outside_anim.addUpdateListener { animation: ValueAnimator ->
            button_outside_radius = animation.animatedValue as Float
            invalidate()
        }
        inside_anim.addUpdateListener { animation: ValueAnimator ->
            button_inside_radius = animation.animatedValue as Float
            invalidate()
        }
        val set = AnimatorSet()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (DoubleUtils.isFastDoubleClick) {
                    return
                }
                //设置为录制状态
                if (state == STATE_LONG_PRESS) {
                    if (captureListener != null) captureListener!!.recordStart()
                    state = STATE_RECORDER_ING
                    timer!!.start()
                } else {
                    state = STATE_IDLE
                }
            }
        })
        set.playTogether(outside_anim, inside_anim)
        set.duration = 100
        set.start()
    }

    private fun updateProgress(millisUntilFinished: Long) {
        currentRecordedTime = (maxDuration - millisUntilFinished).toInt()
        progress = 360f - millisUntilFinished / maxDuration.toFloat() * 360f
        invalidate()
    }

    private inner class RecordCountDownTimer internal constructor(
        millisInFuture: Long,
        countDownInterval: Long,
    ) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            updateProgress(millisUntilFinished)
            captureListener?.changeTime(millisUntilFinished)
        }

        override fun onFinish() {
            recordEnd()
        }
    }

    private inner class LongPressRunnable : Runnable {
        override fun run() {
            state = STATE_LONG_PRESS
            if (PermissionChecker.checkSelfPermission(context,
                    arrayOf(Manifest.permission.RECORD_AUDIO))
            ) {
                startRecordAnimation(button_outside_radius,
                    button_outside_radius + outside_add_size,
                    button_inside_radius,
                    button_inside_radius - inside_reduce_size)
            } else {
                onExplainCallback()
                handlerPressByState()
                PermissionChecker.getInstance()!!.requestPermissions(activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    object : PermissionResultCallback {
                        override fun onGranted() {
                            postDelayed(longPressRunnable, 500)
                            val customCameraView = customCameraView
                            if (customCameraView != null && CustomCameraConfig.explainListener != null) {
                                CustomCameraConfig.explainListener!!.onDismiss(customCameraView)
                            }
                        }

                        override fun onDenied() {
                            if (CustomCameraConfig.deniedListener != null) {
                                SimpleXSpUtils.putBoolean(context,
                                    Manifest.permission.RECORD_AUDIO,
                                    true)
                                CustomCameraConfig.deniedListener!!.onDenied(context,
                                    Manifest.permission.RECORD_AUDIO,
                                    PermissionChecker.PERMISSION_RECORD_AUDIO_SETTING_CODE)
                                val customCameraView = customCameraView
                                if (customCameraView != null && CustomCameraConfig.explainListener != null) {
                                    CustomCameraConfig.explainListener!!.onDismiss(customCameraView)
                                }
                            } else {
                                SimpleXPermissionUtil.goIntentSetting(activity,
                                    PermissionChecker.PERMISSION_RECORD_AUDIO_SETTING_CODE)
                            }
                        }
                    })
            }
        }
    }

    private fun onExplainCallback() {
        if (CustomCameraConfig.explainListener != null) {
            if (!SimpleXSpUtils.getBoolean(context, Manifest.permission.RECORD_AUDIO, false)) {
                val customCameraView = customCameraView
                if (customCameraView != null) {
                    CustomCameraConfig.explainListener!!.onPermissionDescription(context,
                        customCameraView,
                        Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    fun setMaxDuration(duration: Int) {
        maxDuration = duration
        timer = RecordCountDownTimer(maxDuration.toLong(), (maxDuration / 360).toLong())
    }

    fun setMinDuration(duration: Int) {
        minDuration = duration
    }

    fun setCaptureListener(captureListener: CaptureListener?) {
        this.captureListener = captureListener
    }

    fun setProgressColor(progressColor: Int) {
        this.progressColor = progressColor
    }

    val isIdle: Boolean
        get() = state == STATE_IDLE

    fun setButtonCaptureEnabled(enabled: Boolean) {
        isTakeCamera = enabled
    }

    fun resetState() {
        state = STATE_IDLE
    }

    companion object {
        // idle state
        const val STATE_IDLE = 0x001

      // pressed state
        const val STATE_PRESS = 0x002

        // long press
        const val STATE_LONG_PRESS = 0x003

        // recording status
        const val STATE_RECORDER_ING = 0x004

       // forbidden state
        const val STATE_BAN = 0x005
    }
}
