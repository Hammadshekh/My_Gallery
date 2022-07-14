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
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.manager.SelectedManager
import com.example.selector.style.BottomNavBarStyle
import com.example.selector.style.PictureSelectorStyle
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.StyleUtils
import com.example.selector.utils.ValueOf

class CompleteSelectView : LinearLayout {
    private var tvSelectNum: TextView? = null
    private var tvComplete: TextView? = null
    private var numberChangeAnimation: Animation? = null
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
        orientation = HORIZONTAL
        tvSelectNum = findViewById(R.id.ps_tv_select_num)
        tvComplete = findViewById(R.id.ps_tv_complete)
        gravity = Gravity.CENTER_VERTICAL
        numberChangeAnimation = AnimationUtils.loadAnimation(context, R.anim.ps_anim_modal_in)
        config = PictureSelectionConfig.instance
    }

    private fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.ps_complete_selected_layout, this)
    }

    /**
     * 完成选择按钮样式
     */
    fun setCompleteSelectViewStyle() {
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle!!
        val selectMainStyle: SelectMainStyle = selectorStyle.selectMainStyle!!
        if (StyleUtils.checkStyleValidity(selectMainStyle.selectNormalBackgroundResources)) {
            setBackgroundResource(selectMainStyle.selectNormalBackgroundResources)
        }
        val selectNormalText: String = selectMainStyle.selectNormalText.toString()
        if (StyleUtils.checkTextValidity(selectNormalText)) {
            if (StyleUtils.checkTextTwoFormatValidity(selectNormalText)) {
                tvComplete!!.text = java.lang.String.format(selectNormalText,
                    SelectedManager.selectCount,
                    config.maxSelectNum)
            } else {
                tvComplete!!.text = selectNormalText
            }
        }
        val selectNormalTextSize: Int = selectMainStyle.selectNormalTextSize
        if (StyleUtils.checkSizeValidity(selectNormalTextSize)) {
            tvComplete!!.textSize = selectNormalTextSize.toFloat()
        }
        val selectNormalTextColor: Int = selectMainStyle.selectNormalTextColor
        if (StyleUtils.checkStyleValidity(selectNormalTextColor)) {
            tvComplete!!.setTextColor(selectNormalTextColor)
        }
        val bottomBarStyle: BottomNavBarStyle = selectorStyle.bottomBarStyle!!
        if (bottomBarStyle.isCompleteCountTips) {
            val selectNumRes: Int = bottomBarStyle.bottomSelectNumResources
            if (StyleUtils.checkStyleValidity(selectNumRes)) {
                tvSelectNum!!.setBackgroundResource(selectNumRes)
            }
            val selectNumTextSize: Int = bottomBarStyle.bottomSelectNumTextSize
            if (StyleUtils.checkSizeValidity(selectNumTextSize)) {
                tvSelectNum!!.textSize = selectNumTextSize.toFloat()
            }
            val selectNumTextColor: Int = bottomBarStyle.bottomSelectNumTextColor
            if (StyleUtils.checkStyleValidity(selectNumTextColor)) {
                tvSelectNum!!.setTextColor(selectNumTextColor)
            }
        }
    }

    /**
     * 选择结果发生变化
     */
    fun setSelectedChange(isPreview: Boolean) {
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle!!
        val selectMainStyle: SelectMainStyle = selectorStyle.selectMainStyle!!
        if (SelectedManager.selectCount > 0) {
            isEnabled = true
            val selectBackground: Int = selectMainStyle.selectBackgroundResources
            if (StyleUtils.checkStyleValidity(selectBackground)) {
                setBackgroundResource(selectBackground)
            } else {
                setBackgroundResource(R.drawable.ps_ic_trans_1px)
            }
            val selectText: String = selectMainStyle.selectText.toString()
            if (StyleUtils.checkTextValidity(selectText)) {
                if (StyleUtils.checkTextTwoFormatValidity(selectText)) {
                    tvComplete!!.text =
                        java.lang.String.format(selectText,
                            SelectedManager.selectCount,
                            config.maxSelectNum)
                } else {
                    tvComplete!!.text = selectText
                }
            } else {
                tvComplete!!.text = context.getString(R.string.ps_completed)
            }
            val selectTextSize: Int = selectMainStyle.selectTextSize
            if (StyleUtils.checkSizeValidity(selectTextSize)) {
                tvComplete!!.textSize = selectTextSize.toFloat()
            }
            val selectTextColor: Int = selectMainStyle.selectTextColor
            if (StyleUtils.checkStyleValidity(selectTextColor)) {
                tvComplete!!.setTextColor(selectTextColor)
            } else {
                tvComplete!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_fa632d))
            }
            if (selectorStyle.bottomBarStyle!!.isCompleteCountTips) {
                if (tvSelectNum!!.visibility == GONE || tvSelectNum!!.visibility == INVISIBLE) {
                    tvSelectNum!!.visibility = VISIBLE
                }
                if (TextUtils.equals(ValueOf.toString(SelectedManager.selectCount),
                        tvSelectNum!!.text)
                ) {
                    // ignore
                } else {
                    tvSelectNum!!.text = ValueOf.toString(SelectedManager.selectCount)
                    tvSelectNum!!.startAnimation(numberChangeAnimation)
                }
            } else {
                tvSelectNum!!.visibility = GONE
            }
        } else {
            if (isPreview && selectMainStyle.isCompleteSelectRelativeTop) {
                isEnabled = true
                val selectBackground: Int = selectMainStyle.selectBackgroundResources
                if (StyleUtils.checkStyleValidity(selectBackground)) {
                    setBackgroundResource(selectBackground)
                } else {
                    setBackgroundResource(R.drawable.ps_ic_trans_1px)
                }
                val selectTextColor: Int = selectMainStyle.selectTextColor
                if (StyleUtils.checkStyleValidity(selectTextColor)) {
                    tvComplete!!.setTextColor(selectTextColor)
                } else {
                    tvComplete!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
                }
            } else {
                isEnabled = config.isEmptyResultReturn
                val normalBackground: Int = selectMainStyle.selectNormalBackgroundResources
                if (StyleUtils.checkStyleValidity(normalBackground)) {
                    setBackgroundResource(normalBackground)
                } else {
                    setBackgroundResource(R.drawable.ps_ic_trans_1px)
                }
                val normalTextColor: Int = selectMainStyle.selectNormalTextColor
                if (StyleUtils.checkStyleValidity(normalTextColor)) {
                    tvComplete!!.setTextColor(normalTextColor)
                } else {
                    tvComplete!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
                }
            }
            tvSelectNum!!.visibility = GONE
            val selectNormalText: String = selectMainStyle.selectNormalText.toString()
            if (StyleUtils.checkTextValidity(selectNormalText)) {
                if (StyleUtils.checkTextTwoFormatValidity(selectNormalText)) {
                    tvComplete!!.text = java.lang.String.format(selectNormalText,
                        SelectedManager.selectCount,
                        config.maxSelectNum)
                } else {
                    tvComplete!!.text = selectNormalText
                }
            } else {
                tvComplete!!.text = context.getString(R.string.ps_please_select)
            }
            val normalTextSize: Int = selectMainStyle.selectNormalTextSize
            if (StyleUtils.checkSizeValidity(normalTextSize)) {
                tvComplete!!.textSize = normalTextSize.toFloat()
            }
        }
    }
}
