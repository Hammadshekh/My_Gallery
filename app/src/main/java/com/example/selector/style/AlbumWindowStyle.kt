package com.example.selector.style

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class AlbumWindowStyle : Parcelable {
    /**
     * 专辑列表item背景色值
     */
    var albumAdapterItemBackground = 0

    /**
     * 专辑列表选中样式
     */
    var albumAdapterItemSelectStyle = 0

    /**
     * 专辑名称字体大小
     */
    var albumAdapterItemTitleSize = 0

    /**
     * 专辑名称字体色值
     */
    var albumAdapterItemTitleColor = 0

    constructor() {}
    protected constructor(`in`: Parcel) {
        albumAdapterItemBackground = `in`.readInt()
        albumAdapterItemSelectStyle = `in`.readInt()
        albumAdapterItemTitleSize = `in`.readInt()
        albumAdapterItemTitleColor = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(albumAdapterItemBackground)
        dest.writeInt(albumAdapterItemSelectStyle)
        dest.writeInt(albumAdapterItemTitleSize)
        dest.writeInt(albumAdapterItemTitleColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Creator<AlbumWindowStyle> = object : Creator<AlbumWindowStyle?> {
            override fun createFromParcel(`in`: Parcel): AlbumWindowStyle? {
                return AlbumWindowStyle(`in`)
            }

            override fun newArray(size: Int): Array<AlbumWindowStyle?> {
                return arrayOfNulls(size)
            }
        }
    }
}