package com.example.selector.magical

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class ViewParams : Parcelable {
    var left = 0
    var top = 0
    var width = 0
    var height = 0

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(left)
        dest.writeInt(top)
        dest.writeInt(width)
        dest.writeInt(height)
    }

    constructor() {}
    private constructor(`in`: Parcel) {
        left = `in`.readInt()
        top = `in`.readInt()
        width = `in`.readInt()
        height = `in`.readInt()
    }

    companion object {
        val CREATOR: Creator<ViewParams> = object : Creator<ViewParams> {
            override fun createFromParcel(source: Parcel): ViewParams {
                return ViewParams(source)
            }

            override fun newArray(size: Int): Array<ViewParams?> {
                return arrayOfNulls(size)
            }
        }
    }

     object CREATOR : Creator<ViewParams> {
        override fun createFromParcel(parcel: Parcel): ViewParams {
            return ViewParams(parcel)
        }

        override fun newArray(size: Int): Array<ViewParams?> {
            return arrayOfNulls(size)
        }
    }
}
