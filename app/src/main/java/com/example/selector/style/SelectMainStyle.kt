package com.example.selector.style

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class SelectMainStyle : Parcelable {
    /**
     * 状态栏背景色
     */
    var statusBarColor = 0

    /**
     * 导航栏背景色
     */
    var navigationBarColor = 0

    /**
     * 状态栏字体颜色，非黑即白
     */
    var isDarkStatusBarBlack = false

    /**
     * 完成按钮从底部放在右上角
     */
    var isCompleteSelectRelativeTop = false

    /**
     * 预览页选择按钮从顶部放在右下角
     */
    var isPreviewSelectRelativeBottom = false

    /**
     * 预览页是否显示选择画廊
     */
    var isPreviewDisplaySelectGallery = false

    /**
     * 预览页选择按钮MarginRight
     *
     *
     * unit dp
     *
     */
    var previewSelectMarginRight = 0

    /**
     * 预览背景色
     */
    var previewBackgroundColor = 0

    /**
     * 预览页选择按钮文本
     */
    var previewSelectText: String? = null

    /**
     * 预览页选择按钮字体大小
     */
    var previewSelectTextSize = 0

    /**
     * 预览页选择按钮字体颜色
     */
    var previewSelectTextColor = 0

    /**
     * 勾选样式
     */
    var selectBackground = 0

    /**
     * 预览样式勾选样式
     */
    var previewSelectBackground = 0

    /**
     * 勾选样式是否使用数量类型
     */
    var isSelectNumberStyle = false

    /**
     * 预览页勾选样式是否使用数量类型
     */
    var isPreviewSelectNumberStyle = false

    /**
     * 列表背景色
     */
    var mainListBackgroundColor = 0

    /**
     * 选择按钮默认文本
     */
    var selectNormalText: String? = null

    /**
     * 选择按钮默认文本字体大小
     */
    var selectNormalTextSize = 0

    /**
     * 选择按钮默认文本字体色值
     */
    var selectNormalTextColor = 0

    /**
     * 选择按钮默认背景
     */
    var selectNormalBackgroundResources = 0

    /**
     * 选择按钮文本
     */
    var selectText: String? = null

    /**
     * 选择按钮文本字体大小
     */
    var selectTextSize = 0

    /**
     * 选择按钮文本字体色值
     */
    var selectTextColor = 0

    /**
     * 选择按钮选中背景
     */
    var selectBackgroundResources = 0

    /**
     * RecyclerView列表item间隙
     *
     *
     * use unit dp
     *
     */
    var adapterItemSpacingSize = 0

    /**
     * 是否显示左右间距
     */
    var isAdapterItemIncludeEdge = false

    /**
     * 勾选样式字体大小
     */
    var adapterSelectTextSize = 0

    /**
     * 勾选按钮点击区域
     *
     *
     * use unit dp
     *
     */
    var adapterSelectClickArea = 0

    /**
     * 勾选样式字体色值
     */
    var adapterSelectTextColor = 0

    /**
     * 勾选样式位置
     * []
     */
    var adapterSelectStyleGravity: IntArray? = null

    /**
     * 资源类型标识
     */
    var adapterDurationDrawableLeft = 0

    /**
     * 时长文字字体大小
     */
    var adapterDurationTextSize = 0

    /**
     * 时长文字颜色
     */
    var adapterDurationTextColor = 0

    /**
     * 时长文字位置
     * []
     */
    var adapterDurationGravity: IntArray? = null

    /**
     * 时长文字阴影背景
     */
    var adapterDurationBackgroundResources = 0

    /**
     * 拍照按钮背景色
     */
    var adapterCameraBackgroundColor = 0

    /**
     * 拍照按钮图标
     */
    var adapterCameraDrawableTop = 0

    /**
     * 拍照按钮文本
     */
    var adapterCameraText: String? = null

    /**
     * 拍照按钮文本字体色值
     */
    var adapterCameraTextColor = 0

    /**
     * 拍照按钮文本字体大小
     */
    var adapterCameraTextSize = 0

    /**
     * 资源图标识的背景
     */
    var adapterTagBackgroundResources = 0

    /**
     * 资源标识的字体大小
     */
    var adapterTagTextSize = 0

    /**
     * 资源标识的字体色值
     */
    var adapterTagTextColor = 0

    /**
     * 资源标识的位置
     * []
     */
    var adapterTagGravity: IntArray? = null

    /**
     * 图片被编辑标识
     */
    var adapterImageEditorResources = 0

    /**
     * 图片被编辑标识位置
     * []
     */
    var adapterImageEditorGravity: IntArray? = null

    /**
     * 预览页画廊边框样式
     */
    var adapterPreviewGalleryFrameResource = 0

    /**
     * 预览页画廊背景色
     */
    var adapterPreviewGalleryBackgroundResource = 0

    /**
     * 预览页画廊item大小
     *
     *
     * use unit dp
     *
     */
    var adapterPreviewGalleryItemSize = 0

    constructor() {}
    private constructor(`in`: Parcel) {
        statusBarColor = `in`.readInt()
        navigationBarColor = `in`.readInt()
        isDarkStatusBarBlack = `in`.readByte().toInt() != 0
        isCompleteSelectRelativeTop = `in`.readByte().toInt() != 0
        isPreviewSelectRelativeBottom = `in`.readByte().toInt() != 0
        isPreviewDisplaySelectGallery = `in`.readByte().toInt() != 0
        previewSelectMarginRight = `in`.readInt()
        previewBackgroundColor = `in`.readInt()
        previewSelectText = `in`.readString()
        previewSelectTextSize = `in`.readInt()
        previewSelectTextColor = `in`.readInt()
        selectBackground = `in`.readInt()
        previewSelectBackground = `in`.readInt()
        isSelectNumberStyle = `in`.readByte().toInt() != 0
        isPreviewSelectNumberStyle = `in`.readByte().toInt() != 0
        mainListBackgroundColor = `in`.readInt()
        selectNormalText = `in`.readString()
        selectNormalTextSize = `in`.readInt()
        selectNormalTextColor = `in`.readInt()
        selectNormalBackgroundResources = `in`.readInt()
        selectText = `in`.readString()
        selectTextSize = `in`.readInt()
        selectTextColor = `in`.readInt()
        selectBackgroundResources = `in`.readInt()
        adapterItemSpacingSize = `in`.readInt()
        isAdapterItemIncludeEdge = `in`.readByte().toInt() != 0
        adapterSelectTextSize = `in`.readInt()
        adapterSelectClickArea = `in`.readInt()
        adapterSelectTextColor = `in`.readInt()
        adapterSelectStyleGravity = `in`.createIntArray()
        adapterDurationDrawableLeft = `in`.readInt()
        adapterDurationTextSize = `in`.readInt()
        adapterDurationTextColor = `in`.readInt()
        adapterDurationGravity = `in`.createIntArray()
        adapterDurationBackgroundResources = `in`.readInt()
        adapterCameraBackgroundColor = `in`.readInt()
        adapterCameraDrawableTop = `in`.readInt()
        adapterCameraText = `in`.readString()
        adapterCameraTextColor = `in`.readInt()
        adapterCameraTextSize = `in`.readInt()
        adapterTagBackgroundResources = `in`.readInt()
        adapterTagTextSize = `in`.readInt()
        adapterTagTextColor = `in`.readInt()
        adapterTagGravity = `in`.createIntArray()
        adapterImageEditorResources = `in`.readInt()
        adapterImageEditorGravity = `in`.createIntArray()
        adapterPreviewGalleryFrameResource = `in`.readInt()
        adapterPreviewGalleryBackgroundResource = `in`.readInt()
        adapterPreviewGalleryItemSize = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(statusBarColor)
        dest.writeInt(navigationBarColor)
        dest.writeByte((if (isDarkStatusBarBlack) 1 else 0).toByte())
        dest.writeByte((if (isCompleteSelectRelativeTop) 1 else 0).toByte())
        dest.writeByte((if (isPreviewSelectRelativeBottom) 1 else 0).toByte())
        dest.writeByte((if (isPreviewDisplaySelectGallery) 1 else 0).toByte())
        dest.writeInt(previewSelectMarginRight)
        dest.writeInt(previewBackgroundColor)
        dest.writeString(previewSelectText)
        dest.writeInt(previewSelectTextSize)
        dest.writeInt(previewSelectTextColor)
        dest.writeInt(selectBackground)
        dest.writeInt(previewSelectBackground)
        dest.writeByte((if (isSelectNumberStyle) 1 else 0).toByte())
        dest.writeByte((if (isPreviewSelectNumberStyle) 1 else 0).toByte())
        dest.writeInt(mainListBackgroundColor)
        dest.writeString(selectNormalText)
        dest.writeInt(selectNormalTextSize)
        dest.writeInt(selectNormalTextColor)
        dest.writeInt(selectNormalBackgroundResources)
        dest.writeString(selectText)
        dest.writeInt(selectTextSize)
        dest.writeInt(selectTextColor)
        dest.writeInt(selectBackgroundResources)
        dest.writeInt(adapterItemSpacingSize)
        dest.writeByte((if (isAdapterItemIncludeEdge) 1 else 0).toByte())
        dest.writeInt(adapterSelectTextSize)
        dest.writeInt(adapterSelectClickArea)
        dest.writeInt(adapterSelectTextColor)
        dest.writeIntArray(adapterSelectStyleGravity)
        dest.writeInt(adapterDurationDrawableLeft)
        dest.writeInt(adapterDurationTextSize)
        dest.writeInt(adapterDurationTextColor)
        dest.writeIntArray(adapterDurationGravity)
        dest.writeInt(adapterDurationBackgroundResources)
        dest.writeInt(adapterCameraBackgroundColor)
        dest.writeInt(adapterCameraDrawableTop)
        dest.writeString(adapterCameraText)
        dest.writeInt(adapterCameraTextColor)
        dest.writeInt(adapterCameraTextSize)
        dest.writeInt(adapterTagBackgroundResources)
        dest.writeInt(adapterTagTextSize)
        dest.writeInt(adapterTagTextColor)
        dest.writeIntArray(adapterTagGravity)
        dest.writeInt(adapterImageEditorResources)
        dest.writeIntArray(adapterImageEditorGravity)
        dest.writeInt(adapterPreviewGalleryFrameResource)
        dest.writeInt(adapterPreviewGalleryBackgroundResource)
        dest.writeInt(adapterPreviewGalleryItemSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Creator<SelectMainStyle?> = object : Creator<SelectMainStyle?> {
            override fun createFromParcel(`in`: Parcel): SelectMainStyle? {
                return SelectMainStyle(`in`)
            }

            override fun newArray(size: Int): Array<SelectMainStyle?> {
                return arrayOfNulls(size)
            }
        }
    }
}
