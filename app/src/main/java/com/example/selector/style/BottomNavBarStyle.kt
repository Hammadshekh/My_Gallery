package com.example.selector.style

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class BottomNavBarStyle : Parcelable {
    /**
     * 底部导航栏背景色
     */
    var bottomNarBarBackgroundColor = 0

    /**
     * 底部预览页NarBar背景色
     */
    var bottomPreviewNarBarBackgroundColor = 0

    /**
     * 底部导航栏高度
     *
     *
     * use unit dp
     *
     */
    var bottomNarBarHeight = 0

    /**
     * 底部预览文本
     */
    var bottomPreviewNormalText: String? = null

    /**
     * 底部预览文本字体大小
     */
    var bottomPreviewNormalTextSize = 0

    /**
     * 底部预览文本正常字体色值
     */
    var bottomPreviewNormalTextColor = 0

    /**
     * 底部选中预览文本
     */
    var bottomPreviewSelectText: String? = null

    /**
     * 底部预览文本选中字体色值
     */
    var bottomPreviewSelectTextColor = 0

    /**
     * 底部编辑文字
     */
    var bottomEditorText: String? = null

    /**
     * 底部编辑文字大小
     */
    var bottomEditorTextSize = 0

    /**
     * 底部编辑文字色值
     */
    var bottomEditorTextColor = 0

    /**
     * 底部原图文字DrawableLeft
     */
    var bottomOriginalDrawableLeft = 0

    /**
     * 底部原图文字
     */
    var bottomOriginalText: String? = null

    /**
     * 底部原图文字大小
     */
    var bottomOriginalTextSize = 0

    /**
     * 底部原图文字色值
     */
    var bottomOriginalTextColor = 0

    /**
     * 已选数量背景样式
     */
    var bottomSelectNumResources = 0

    /**
     * 已选数量文字大小
     */
    var bottomSelectNumTextSize = 0

    /**
     * 已选数量文字颜色
     */
    var bottomSelectNumTextColor = 0

    /**
     * 是否显示已选数量圆点提醒
     */
    var isCompleteCountTips = true

    constructor() {}
    protected constructor(`in`: Parcel) {
        bottomNarBarBackgroundColor = `in`.readInt()
        bottomPreviewNarBarBackgroundColor = `in`.readInt()
        bottomNarBarHeight = `in`.readInt()
        bottomPreviewNormalText = `in`.readString()
        bottomPreviewNormalTextSize = `in`.readInt()
        bottomPreviewNormalTextColor = `in`.readInt()
        bottomPreviewSelectText = `in`.readString()
        bottomPreviewSelectTextColor = `in`.readInt()
        bottomEditorText = `in`.readString()
        bottomEditorTextSize = `in`.readInt()
        bottomEditorTextColor = `in`.readInt()
        bottomOriginalDrawableLeft = `in`.readInt()
        bottomOriginalText = `in`.readString()
        bottomOriginalTextSize = `in`.readInt()
        bottomOriginalTextColor = `in`.readInt()
        bottomSelectNumResources = `in`.readInt()
        bottomSelectNumTextSize = `in`.readInt()
        bottomSelectNumTextColor = `in`.readInt()
        isCompleteCountTips = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(bottomNarBarBackgroundColor)
        dest.writeInt(bottomPreviewNarBarBackgroundColor)
        dest.writeInt(bottomNarBarHeight)
        dest.writeString(bottomPreviewNormalText)
        dest.writeInt(bottomPreviewNormalTextSize)
        dest.writeInt(bottomPreviewNormalTextColor)
        dest.writeString(bottomPreviewSelectText)
        dest.writeInt(bottomPreviewSelectTextColor)
        dest.writeString(bottomEditorText)
        dest.writeInt(bottomEditorTextSize)
        dest.writeInt(bottomEditorTextColor)
        dest.writeInt(bottomOriginalDrawableLeft)
        dest.writeString(bottomOriginalText)
        dest.writeInt(bottomOriginalTextSize)
        dest.writeInt(bottomOriginalTextColor)
        dest.writeInt(bottomSelectNumResources)
        dest.writeInt(bottomSelectNumTextSize)
        dest.writeInt(bottomSelectNumTextColor)
        dest.writeByte((if (isCompleteCountTips) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Creator<BottomNavBarStyle> = object : Creator<BottomNavBarStyle?> {
            override fun createFromParcel(`in`: Parcel): BottomNavBarStyle? {
                return BottomNavBarStyle(`in`)
            }

            override fun newArray(size: Int): Array<BottomNavBarStyle?> {
                return arrayOfNulls(size)
            }
        }
    }
}
