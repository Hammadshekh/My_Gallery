package com.example.ucrop.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

open class AspectRatio : Parcelable {
    val aspectRatioTitle: String?
    private val aspectRatioX: Float
    private val aspectRatioY: Float

    constructor(aspectRatioTitle: String?, aspectRatioX: Float, aspectRatioY: Float) {
        this.aspectRatioTitle = aspectRatioTitle
        this.aspectRatioX = aspectRatioX
        this.aspectRatioY = aspectRatioY
    }

    private constructor(`in`: Parcel) {
        aspectRatioTitle = `in`.readString()
        aspectRatioX = `in`.readFloat()
        aspectRatioY = `in`.readFloat()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(aspectRatioTitle)
        dest.writeFloat(aspectRatioX)
        dest.writeFloat(aspectRatioY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Creator<AspectRatio?> = object : Creator<AspectRatio?> {
            override fun createFromParcel(`in`: Parcel): AspectRatio? {
                return AspectRatio(`in`)
            }

            override fun newArray(size: Int): Array<AspectRatio?> {
                return arrayOfNulls(size)
            }
        }
    }

     object CREATOR : Creator<AspectRatio> {
        override fun createFromParcel(parcel: Parcel): AspectRatio {
            return AspectRatio(parcel)
        }

        override fun newArray(size: Int): Array<AspectRatio?> {
            return arrayOfNulls(size)
        }
    }
    fun getAspectRatioX(): Float {
        return aspectRatioX
    }

    fun getAspectRatioY(): Float {
        return aspectRatioY
    }
}
