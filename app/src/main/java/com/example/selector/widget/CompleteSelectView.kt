package com.example.selector.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class CompleteSelectView : LinearLayout {
    private var tvSelectNum: TextView? = null
    private var tvComplete: TextView? = null
    private var numberChangeAnimation: Animation? = null
    private var config: PictureSelectionConfig? = null

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
        orientation = HORIZONTAL
        tvSelectNum = findViewById(R.id.ps_tv_select_num)
        tvComplete = findViewById(R.id.ps_tv_complete)
        gravity = Gravity.CENTER_VERTICAL
        numberChangeAnimation = AnimationUtils.loadAnimation(context, R.anim.ps_anim_modal_in)
        config = PictureSelectionConfig.getInstance()
    }

    protected fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.ps_complete_selected_layout, this)
    }

    /**
     * 完成选择按钮样式
     */
    fun setCompleteSelectViewStyle() {
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle
        val selectMainStyle: SelectMainStyle = selectorStyle.getSelectMainStyle()
        if (StyleUtils.checkStyleValidity(selectMainStyle.getSelectNormalBackgroundResources())) {
            setBackgroundResource(selectMainStyle.getSelectNormalBackgroundResources())
        }
        val selectNormalText: String = selectMainStyle.getSelectNormalText()
        if (StyleUtils.checkTextValidity(selectNormalText)) {
            if (StyleUtils.checkTextTwoFormatValidity(selectNormalText)) {
                tvComplete!!.text = java.lang.String.format(selectNormalText,
                    SelectedManager.getSelectCount(),
                    config.maxSelectNum)
            } else {
                tvComplete!!.text = selectNormalText
            }
        }
        val selectNormalTextSize: Int = selectMainStyle.getSelectNormalTextSize()
        if (StyleUtils.checkSizeValidity(selectNormalTextSize)) {
            tvComplete!!.textSize = selectNormalTextSize.toFloat()
        }
        val selectNormalTextColor: Int = selectMainStyle.getSelectNormalTextColor()
        if (StyleUtils.checkStyleValidity(selectNormalTextColor)) {
            tvComplete!!.setTextColor(selectNormalTextColor)
        }
        val bottomBarStyle: BottomNavBarStyle = selectorStyle.getBottomBarStyle()
        if (bottomBarStyle.isCompleteCountTips()) {
            val selectNumRes: Int = bottomBarStyle.getBottomSelectNumResources()
            if (StyleUtils.checkStyleValidity(selectNumRes)) {
                tvSelectNum!!.setBackgroundResource(selectNumRes)
            }
            val selectNumTextSize: Int = bottomBarStyle.getBottomSelectNumTextSize()
            if (StyleUtils.checkSizeValidity(selectNumTextSize)) {
                tvSelectNum!!.textSize = selectNumTextSize.toFloat()
            }
            val selectNumTextColor: Int = bottomBarStyle.getBottomSelectNumTextColor()
            if (StyleUtils.checkStyleValidity(selectNumTextColor)) {
                tvSelectNum!!.setTextColor(selectNumTextColor)
            }
        }
    }

    /**
     * 选择结果发生变化
     */
    fun setSelectedChange(isPreview: Boolean) {
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle
        val selectMainStyle: SelectMainStyle = selectorStyle.getSelectMainStyle()
        if (SelectedManager.getSelectCount() > 0) {
            isEnabled = true
            val selectBackground: Int = selectMainStyle.getSelectBackgroundResources()
            if (StyleUtils.checkStyleValidity(selectBackground)) {
                setBackgroundResource(selectBackground)
            } else {
                setBackgroundResource(R.drawable.ps_ic_trans_1px)
            }
            val selectText: String = selectMainStyle.getSelectText()
            if (StyleUtils.checkTextValidity(selectText)) {
                if (StyleUtils.checkTextTwoFormatValidity(selectText)) {
                    tvComplete!!.text =
                        java.lang.String.format(selectText,
                            SelectedManager.getSelectCount(),
                            config.maxSelectNum)
                } else {
                    tvComplete!!.text = selectText
                }
            } else {
                tvComplete!!.text = context.getString(R.string.ps_completed)
            }
            val selectTextSize: Int = selectMainStyle.getSelectTextSize()
            if (StyleUtils.checkSizeValidity(selectTextSize)) {
                tvComplete!!.textSize = selectTextSize.toFloat()
            }
            val selectTextColor: Int = selectMainStyle.getSelectTextColor()
            if (StyleUtils.checkStyleValidity(selectTextColor)) {
                tvComplete!!.setTextColor(selectTextColor)
            } else {
                tvComplete!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_fa632d))
            }
            if (selectorStyle.getBottomBarStyle().isCompleteCountTips()) {
                if (tvSelectNum!!.visibility == GONE || tvSelectNum!!.visibility == INVISIBLE) {
                    tvSelectNum!!.visibility = VISIBLE
                }
                if (TextUtils.equals(ValueOf.toString(SelectedManager.getSelectCount()),
                        tvSelectNum!!.text)
                ) {
                    // ignore
                } else {
                    tvSelectNum.setText(ValueOf.toString(SelectedManager.getSelectCount()))
                    tvSelectNum!!.startAnimation(numberChangeAnimation)
                }
            } else {
                tvSelectNum!!.visibility = GONE
            }
        } else {
            if (isPreview && selectMainStyle.isCompleteSelectRelativeTop()) {
                isEnabled = true
                val selectBackground: Int = selectMainStyle.getSelectBackgroundResources()
                if (StyleUtils.checkStyleValidity(selectBackground)) {
                    setBackgroundResource(selectBackground)
                } else {
                    setBackgroundResource(R.drawable.ps_ic_trans_1px)
                }
                val selectTextColor: Int = selectMainStyle.getSelectTextColor()
                if (StyleUtils.checkStyleValidity(selectTextColor)) {
                    tvComplete!!.setTextColor(selectTextColor)
                } else {
                    tvComplete!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
                }
            } else {
                isEnabled = config.isEmptyResultReturn
                val normalBackground: Int = selectMainStyle.getSelectNormalBackgroundResources()
                if (StyleUtils.checkStyleValidity(normalBackground)) {
                    setBackgroundResource(normalBackground)
                } else {
                    setBackgroundResource(R.drawable.ps_ic_trans_1px)
                }
                val normalTextColor: Int = selectMainStyle.getSelectNormalTextColor()
                if (StyleUtils.checkStyleValidity(normalTextColor)) {
                    tvComplete!!.setTextColor(normalTextColor)
                } else {
                    tvComplete!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
                }
            }
            tvSelectNum!!.visibility = GONE
            val selectNormalText: String = selectMainStyle.getSelectNormalText()
            if (StyleUtils.checkTextValidity(selectNormalText)) {
                if (StyleUtils.checkTextTwoFormatValidity(selectNormalText)) {
                    tvComplete!!.text = java.lang.String.format(selectNormalText,
                        SelectedManager.getSelectCount(),
                        config.maxSelectNum)
                } else {
                    tvComplete!!.text = selectNormalText
                }
            } else {
                tvComplete!!.text = context.getString(R.string.ps_please_select)
            }
            val normalTextSize: Int = selectMainStyle.getSelectNormalTextSize()
            if (StyleUtils.checkSizeValidity(normalTextSize)) {
                tvComplete!!.textSize = normalTextSize.toFloat()
            }
        }
    }
}
