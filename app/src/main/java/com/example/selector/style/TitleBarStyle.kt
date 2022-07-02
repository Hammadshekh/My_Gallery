package com.example.selector.style

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class TitleBarStyle : Parcelable {
    /**
     * 是否隐藏标题栏
     */
    var isHideTitleBar = false

    /**
     * 标题栏左边关闭样式
     */
    var titleLeftBackResource = 0

    /**
     * 预览标题栏左边关闭样式
     */
    var previewTitleLeftBackResource = 0

    /**
     * 标题栏默认文案
     */
    var titleDefaultText: String? = null

    /**
     * 标题栏字体大小
     */
    var titleTextSize = 0

    /**
     * 标题栏字体色值
     */
    var titleTextColor = 0

    /**
     * 标题栏背景
     */
    var titleBackgroundColor = 0

    /**
     * 预览标题栏背景
     */
    var previewTitleBackgroundColor = 0

    /**
     * 标题栏高度
     *
     *
     * use  unit dp
     *
     */
    var titleBarHeight = 0

    /**
     * 标题栏专辑背景
     */
    var titleAlbumBackgroundResource = 0

    /**
     * 标题栏位置居左
     */
    var isAlbumTitleRelativeLeft = false

    /**
     * 标题栏右边向上图标
     */
    var titleDrawableRightResource = 0

    /**
     * 标题栏右边取消按钮背景
     */
    var titleCancelBackgroundResource = 0

    /**
     * 是否隐藏取消按钮
     */
    var isHideCancelButton = false

    /**
     * 外部预览删除
     */
    var previewDeleteBackgroundResource = 0

    /**
     * 标题栏右边默认文本
     */
    var titleCancelText: String? = null

    /**
     * 标题栏右边文本字体大小
     */
    var titleCancelTextSize = 0

    /**
     * 标题栏右边文本字体色值
     */
    var titleCancelTextColor = 0

    /**
     * 标题栏底部线条色值
     */
    var titleBarLineColor = 0

    /**
     * 是否显示标题栏底部线条
     */
    var isDisplayTitleBarLine = false

    constructor() {}
    protected constructor(`in`: Parcel) {
        isHideTitleBar = `in`.readByte().toInt() != 0
        titleLeftBackResource = `in`.readInt()
        previewTitleLeftBackResource = `in`.readInt()
        titleDefaultText = `in`.readString()
        titleTextSize = `in`.readInt()
        titleTextColor = `in`.readInt()
        titleBackgroundColor = `in`.readInt()
        previewTitleBackgroundColor = `in`.readInt()
        titleBarHeight = `in`.readInt()
        titleAlbumBackgroundResource = `in`.readInt()
        isAlbumTitleRelativeLeft = `in`.readByte().toInt() != 0
        titleDrawableRightResource = `in`.readInt()
        titleCancelBackgroundResource = `in`.readInt()
        isHideCancelButton = `in`.readByte().toInt() != 0
        previewDeleteBackgroundResource = `in`.readInt()
        titleCancelText = `in`.readString()
        titleCancelTextSize = `in`.readInt()
        titleCancelTextColor = `in`.readInt()
        titleBarLineColor = `in`.readInt()
        isDisplayTitleBarLine = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (isHideTitleBar) 1 else 0).toByte())
        dest.writeInt(titleLeftBackResource)
        dest.writeInt(previewTitleLeftBackResource)
        dest.writeString(titleDefaultText)
        dest.writeInt(titleTextSize)
        dest.writeInt(titleTextColor)
        dest.writeInt(titleBackgroundColor)
        dest.writeInt(previewTitleBackgroundColor)
        dest.writeInt(titleBarHeight)
        dest.writeInt(titleAlbumBackgroundResource)
        dest.writeByte((if (isAlbumTitleRelativeLeft) 1 else 0).toByte())
        dest.writeInt(titleDrawableRightResource)
        dest.writeInt(titleCancelBackgroundResource)
        dest.writeByte((if (isHideCancelButton) 1 else 0).toByte())
        dest.writeInt(previewDeleteBackgroundResource)
        dest.writeString(titleCancelText)
        dest.writeInt(titleCancelTextSize)
        dest.writeInt(titleCancelTextColor)
        dest.writeInt(titleBarLineColor)
        dest.writeByte((if (isDisplayTitleBarLine) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Creator<TitleBarStyle> = object : Creator<TitleBarStyle?> {
            override fun createFromParcel(`in`: Parcel): TitleBarStyle? {
                return TitleBarStyle(`in`)
            }

            override fun newArray(size: Int): Array<TitleBarStyle?> {
                return arrayOfNulls(size)
            }
        }
    }
}
