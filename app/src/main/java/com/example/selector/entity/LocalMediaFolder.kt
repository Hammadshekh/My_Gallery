package com.example.selector.entity

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.example.selector.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

open class LocalMediaFolder : Parcelable {
    /**
     * folder bucketId
     */
    var bucketId: Long = PictureConfig.ALL.toLong()

    /**
     * folder name
     */
    var folderName: String? = null

    /**
     * folder first path
     */
    var firstImagePath: String? = null

    /**
     * first data mime type
     */
    var firstMimeType: String? = null

    /**
     * folder total media num
     */
    var folderTotalNum = 0

    /**
     * There are selected resources in the current directory
     */
    var isSelectTag = false

    /**
     * current folder data
     *
     *
     * In isPageStrategy mode, there is no data for the first time
     *
     */
    var data: ArrayList<LocalMedia>? = ArrayList()

    /**
     * # Internal use
     * setCurrentDataPage
     */
    var currentDataPage = 1

    /**
     * # Internal use
     * is load more
     */
    var isHasMore = false

    constructor() {}
    private constructor(`in`: Parcel) {
        bucketId = `in`.readLong()
        firstImagePath = `in`.readString()
        firstMimeType = `in`.readString()
        folderTotalNum = `in`.readInt()
        isSelectTag = `in`.readByte().toInt() != 0
        data = `in`.createTypedArrayList(LocalMedia.CREATOR)
        currentDataPage = `in`.readInt()
        isHasMore = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(bucketId)
        dest.writeString(folderName)
        dest.writeString(firstImagePath)
        dest.writeString(firstMimeType)
        dest.writeInt(folderTotalNum)
        dest.writeByte((if (isSelectTag) 1 else 0).toByte())
        dest.writeTypedList(data)
        dest.writeInt(currentDataPage)
        dest.writeByte((if (isHasMore) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

/*    fun getFolderName(): String {
        return if (TextUtils.isEmpty(folderName)) "unknown" else folderName!!
    }

    fun setFolderName(folderName: String?) {
        this.folderName = folderName
    }

    fun getData(): ArrayList<LocalMedia> {
        return if (data != null) data!! else ArrayList()
    }

    fun setData(data: ArrayList<LocalMedia>?) {
        this.data = data
    }*/

    companion object {
        val CREATOR: Creator<LocalMediaFolder?> = object : Creator<LocalMediaFolder?> {
            override fun createFromParcel(`in`: Parcel): LocalMediaFolder? {
                return LocalMediaFolder(`in`)
            }

            override fun newArray(size: Int): Array<LocalMediaFolder?> {
                return arrayOfNulls(size)
            }
        }
    }

    object CREATOR : Creator<LocalMediaFolder> {
        override fun createFromParcel(parcel: Parcel): LocalMediaFolder {
            return LocalMediaFolder(parcel)
        }

        override fun newArray(size: Int): Array<LocalMediaFolder?> {
            return arrayOfNulls(size)
        }
    }
}
