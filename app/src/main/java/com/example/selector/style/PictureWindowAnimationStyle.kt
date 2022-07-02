package com.example.selector.style

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.annotation.AnimRes

class PictureWindowAnimationStyle : Parcelable {
    /**
     * 相册启动动画
     */
    @AnimRes
    var activityEnterAnimation = 0

    /**
     * 相册退出动画
     */
    @AnimRes
    var activityExitAnimation = 0

    /**
     * 预览界面启动动画
     */
    @AnimRes
    var activityPreviewEnterAnimation = 0

    /**
     * 预览界面退出动画
     */
    @AnimRes
    var activityPreviewExitAnimation = 0

    constructor() {}
    constructor(
        @AnimRes activityEnterAnimation: Int,
        @AnimRes activityExitAnimation: Int,
    ) {
        this.activityEnterAnimation = activityEnterAnimation
        this.activityExitAnimation = activityExitAnimation
        activityPreviewEnterAnimation = activityEnterAnimation
        activityPreviewExitAnimation = activityExitAnimation
    }

    protected constructor(`in`: Parcel) {
        activityEnterAnimation = `in`.readInt()
        activityExitAnimation = `in`.readInt()
        activityPreviewEnterAnimation = `in`.readInt()
        activityPreviewExitAnimation = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(activityEnterAnimation)
        dest.writeInt(activityExitAnimation)
        dest.writeInt(activityPreviewEnterAnimation)
        dest.writeInt(activityPreviewExitAnimation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        /**
         * 默认WindowAnimationStyle
         *
         * @return this
         */
        fun ofDefaultWindowAnimationStyle(): PictureWindowAnimationStyle {
            return PictureWindowAnimationStyle(R.anim.ps_anim_enter, R.anim.ps_anim_exit)
        }

        val CREATOR: Creator<PictureWindowAnimationStyle> =
            object : Creator<PictureWindowAnimationStyle?> {
                override fun createFromParcel(`in`: Parcel): PictureWindowAnimationStyle? {
                    return PictureWindowAnimationStyle(`in`)
                }

                override fun newArray(size: Int): Array<PictureWindowAnimationStyle?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
