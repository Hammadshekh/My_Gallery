package com.luck.picture.lib.entity

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.TextUtils
import com.example.selector.config.PictureConfig
import com.example.selector.entity.MediaExtraInfo
import com.example.selector.objects.ObjectPools
import com.example.selector.utils.MediaUtils
import com.example.selector.utils.PictureFileUtils
import com.example.ucrop.utils.FileUtils.isContent
import com.example.ucrop.utils.FileUtils.isHasAudio
import com.example.ucrop.utils.FileUtils.isHasVideo
import java.io.File

/**
 * @author：luck
 * @date：2017-5-24 16:21
 * @describe：Media Entity
 * [
](https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-3.0-LocalMedia%E8%AF%B4%E6%98%8E) */
class LocalMedia : Parcelable {
    /**
     * file to ID
     */
    var id: Long = 0

    /**
     * original path
     */
    var path: String? = null

    /**
     * The real path，But you can't get access from AndroidQ
     */
    var realPath: String? = null

    /**
     * # Check the original button to get the return value
     * original path
     */
    var originalPath: String? = null

    /**
     * compress path
     */
    var compressPath: String? = null

    /**
     * cut path
     */
    var cutPath: String? = null

    /**
     * watermark path
     */
    private var watermarkPath: String? = null

    /**
     * video thumbnail path
     */
    var videoThumbnailPath: String? = null

    /**
     * app sandbox path
     */
    var sandboxPath: String? = null

    /**
     * video duration
     */
    var duration: Long = 0

    /**
     * If the selected
     * # Internal use
     */
    var isChecked = false

    /**
     * If the cut
     */
    private var isCut = false

    /**
     * media position of list
     */
    var position = 0

    /**
     * The media number of qq choose styles
     */
    var num = 0

    /**
     * The media resource type
     */
    var mimeType: String? = null

    /**
     * Gallery selection mode
     */
    var chooseModel = 0

    /**
     * If the compressed
     */
    private var compressed = false

    /**
     * image or video width
     *
     *
     * # If zero occurs, the developer needs to handle it extra
     */
    var width = 0

    /**
     * image or video height
     *
     *
     * # If zero occurs, the developer needs to handle it extra
     */
    var height = 0

    /**
     * Crop the width of the picture
     */
    var cropImageWidth = 0

    /**
     * Crop the height of the picture
     */
    var cropImageHeight = 0

    /**
     * Crop ratio x
     */
    var cropOffsetX = 0

    /**
     * Crop ratio y
     */
    var cropOffsetY = 0

    /**
     * Crop Aspect Ratio
     */
    var cropResultAspectRatio = 0f

    /**
     * file size
     */
    var size: Long = 0

    /**
     * Whether the original image is displayed
     */
    private var isOriginal = false

    /**
     * file name
     */
    var fileName: String? = null

    /**
     * Parent  Folder Name
     */
    var parentFolderName: String? = null

    /**
     * bucketId
     */
    var bucketId: Long = PictureConfig.ALL.toLong()

    /**
     * media create time
     */
    var dateAddedTime: Long = 0

    /**
     * custom data
     *
     *
     * User defined data can be expanded freely
     *
     */
    var customData: String? = null

    /**
     * isMaxSelectEnabledMask
     * # For internal use only
     */
    var isMaxSelectEnabledMask = false

    /**
     * isGalleryEnabledMask
     * # For internal use only
     */
    var isGalleryEnabledMask = false

    /**
     * Whether the image has been edited
     * # For internal use only
     */
    private var isEditorImage = false

    constructor() {}
    private constructor(`in`: Parcel) {
        id = `in`.readLong()
        path = `in`.readString()
        realPath = `in`.readString()
        originalPath = `in`.readString()
        compressPath = `in`.readString()
        cutPath = `in`.readString()
        watermarkPath = `in`.readString()
        videoThumbnailPath = `in`.readString()
        sandboxPath = `in`.readString()
        duration = `in`.readLong()
        isChecked = `in`.readByte().toInt() != 0
        isCut = `in`.readByte().toInt() != 0
        position = `in`.readInt()
        num = `in`.readInt()
        mimeType = `in`.readString()
        chooseModel = `in`.readInt()
        compressed = `in`.readByte().toInt() != 0
        width = `in`.readInt()
        height = `in`.readInt()
        cropImageWidth = `in`.readInt()
        cropImageHeight = `in`.readInt()
        cropOffsetX = `in`.readInt()
        cropOffsetY = `in`.readInt()
        cropResultAspectRatio = `in`.readFloat()
        size = `in`.readLong()
        isOriginal = `in`.readByte().toInt() != 0
        fileName = `in`.readString()
        parentFolderName = `in`.readString()
        bucketId = `in`.readLong()
        dateAddedTime = `in`.readLong()
        customData = `in`.readString()
        isMaxSelectEnabledMask = `in`.readByte().toInt() != 0
        isGalleryEnabledMask = `in`.readByte().toInt() != 0
        isEditorImage = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(path)
        dest.writeString(realPath)
        dest.writeString(originalPath)
        dest.writeString(compressPath)
        dest.writeString(cutPath)
        dest.writeString(watermarkPath)
        dest.writeString(videoThumbnailPath)
        dest.writeString(sandboxPath)
        dest.writeLong(duration)
        dest.writeByte((if (isChecked) 1 else 0).toByte())
        dest.writeByte((if (isCut) 1 else 0).toByte())
        dest.writeInt(position)
        dest.writeInt(num)
        dest.writeString(mimeType)
        dest.writeInt(chooseModel)
        dest.writeByte((if (compressed) 1 else 0).toByte())
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeInt(cropImageWidth)
        dest.writeInt(cropImageHeight)
        dest.writeInt(cropOffsetX)
        dest.writeInt(cropOffsetY)
        dest.writeFloat(cropResultAspectRatio)
        dest.writeLong(size)
        dest.writeByte((if (isOriginal) 1 else 0).toByte())
        dest.writeString(fileName)
        dest.writeString(parentFolderName)
        dest.writeLong(bucketId)
        dest.writeLong(dateAddedTime)
        dest.writeString(customData)
        dest.writeByte((if (isMaxSelectEnabledMask) 1 else 0).toByte())
        dest.writeByte((if (isGalleryEnabledMask) 1 else 0).toByte())
        dest.writeByte((if (isEditorImage) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }
    /**
     * 获取当前匹配上的对象
     */
    /**
     * 当前匹配上的对象
     */
    var compareLocalMedia: LocalMedia? = null
        private set

    /**
     * 重写equals进行值的比较
     *
     * @param o
     * @return
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is LocalMedia) return false
        val isCompare = (TextUtils.equals(path, o.path)
                || TextUtils.equals(realPath, o.realPath)
                || id == o.id)
        compareLocalMedia = if (isCompare) o else null
        return isCompare
    }

    /**
     * get real and effective resource path
     *
     * @return
     */
    val availablePath: String
        get() {
            var path = path
            if (isCut()) {
                path = cutPath
            }
            if (isCompressed()) {
                path = compressPath
            }
            if (isToSandboxPath) {
                path = sandboxPath
            }
            if (isOriginal()) {
                path = originalPath
            }
            if (isWatermarkPath()) {
                path = getWatermarkPath()
            }
            return path!!
        }

    fun isCut(): Boolean {
        return isCut && !TextUtils.isEmpty(cutPath)
    }

    fun setCut(cut: Boolean) {
        isCut = cut
    }
    @JvmName("getPath1")
    fun getPath(): String? {
        return path
    }

    fun isCompressed(): Boolean {
        return compressed && !TextUtils.isEmpty(compressPath)
    }

    fun setCompressed(compressed: Boolean) {
        this.compressed = compressed
    }

    fun isOriginal(): Boolean {
        return isOriginal && !TextUtils.isEmpty(originalPath)
    }

    fun setOriginal(original: Boolean) {
        isOriginal = original
    }

    fun isEditorImage(): Boolean {
        return isEditorImage && !TextUtils.isEmpty(cutPath)
    }
    @JvmName("setPath1")
    fun setPath(path: String?) {
        this.path = path
    }

    fun setEditorImage(editorImage: Boolean) {
        isEditorImage = editorImage
    }

    val isToSandboxPath: Boolean
        get() = !TextUtils.isEmpty(sandboxPath)

    fun isWatermarkPath(): Boolean {
        return !TextUtils.isEmpty(getWatermarkPath())
    }

    fun getWatermarkPath(): String? {
        return watermarkPath
    }

    fun setWatermarkPath(watermarkPath: String?) {
        this.watermarkPath = watermarkPath
    }

    /**
     * 回收对象池
     */
    fun recycle() {
        sPool?.release(this)
    }

    companion object {
        val CREATOR: Creator<LocalMedia> = object : Creator<LocalMedia> {
            override fun createFromParcel(`in`: Parcel): LocalMedia {
                return LocalMedia(`in`)
            }

            override fun newArray(size: Int): Array<LocalMedia?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * 构造网络资源下的LocalMedia
         *
         * @param url      网络url
         * @param mimeType 资源类型 [# PictureMimeType.ofGIF()][]
         * @return
         */
        fun generateHttpAsLocalMedia(url: String?): LocalMedia {
            val media = create()
            media.path = url
            media.mimeType = url?.let { MediaUtils.getMimeTypeFromMediaHttpUrl(it) }
            return media
        }

        /**
         * 构造网络资源下的LocalMedia
         *
         * @param url      网络url
         * @param mimeType 资源类型 [# PictureMimeType.ofGIF()][]
         * @return
         */
        fun generateHttpAsLocalMedia(url: String?, mimeType: String?): LocalMedia {
            val media = create()
            media.path = url
            media.mimeType = mimeType
            return media
        }

        /**
         * 构造本地资源下的LocalMedia
         *
         * @param context 上下文
         * @param path    本地路径
         * @return
         */
        fun generateLocalMedia(context: Context?, path: String?): LocalMedia {
            val media = create()
            val cameraFile = if (isContent(path!!)) File(PictureFileUtils.getPath(
                context!!,
                Uri.parse(path))) else File(path)
            media.path = path
            media.realPath = cameraFile.absolutePath
            media.fileName = cameraFile.name
            media.parentFolderName =
                MediaUtils.generateCameraFolderName(cameraFile.absolutePath)
            media.mimeType = MediaUtils.getMimeTypeFromMediaUrl(cameraFile.absolutePath)
            media.size = cameraFile.length()
            media.dateAddedTime = cameraFile.lastModified() / 1000
            val realPath = cameraFile.absolutePath
            if (realPath.contains("Android/data/") || realPath.contains("data/user/")) {
                media.id = System.currentTimeMillis()
                val parentFile = cameraFile.parentFile
                media.bucketId = parentFile?.name?.hashCode()?.toLong() ?: 0L
            } else {
                val mediaBucketId: Array<Long> =
                    MediaUtils.getPathMediaBucketId(context!!, media.realPath!!)
                media.id =
                    if (mediaBucketId[0].equals(0) ) System.currentTimeMillis() else mediaBucketId[0]
                media.bucketId = mediaBucketId[1]
            }
            val mediaExtraInfo: MediaExtraInfo
            when {
                isHasVideo(media.mimeType) -> {
                    mediaExtraInfo = MediaUtils.getVideoSize(context, path)
                    media.width = mediaExtraInfo.width
                    media.height = mediaExtraInfo.height
                    media.duration = mediaExtraInfo.duration
                }
                isHasAudio(media.mimeType) -> {
                    mediaExtraInfo = MediaUtils.getAudioSize(context, path)
                    media.duration = mediaExtraInfo.duration
                }
                else -> {
                    mediaExtraInfo = MediaUtils.getImageSize(context, path)
                    media.width = mediaExtraInfo.width
                    media.height = mediaExtraInfo.height
                }
            }
            return media
        }

        /**
         * 构造网络资源下的LocalMedia
         *
         * @param url      网络url
         * @param mimeType 资源类型 [# PictureMimeType.ofGIF()][]
         * Use []
         * @return
         */
        @Deprecated("")
        fun generateLocalMedia(url: String?, mimeType: String?): LocalMedia {
            val media = create()
            media.path = url
            media.mimeType = mimeType
            return media
        }

        /**
         * 创建LocalMedia对象
         *
         * @return
         */
        fun create(): LocalMedia {
            return LocalMedia()
        }

        /**
         * 对象池
         */
        private var sPool: ObjectPools.SynchronizedPool<LocalMedia>? = null

        /**
         * 从对象池里取LocalMedia
         */
        fun obtain(): LocalMedia {
            if (sPool == null) {
                sPool = ObjectPools.SynchronizedPool()
            }
            val media: LocalMedia = sPool?.acquire()!!
            return media ?: create()
        }

        /**
         * 销毁对象池
         */
        fun destroyPool() {
            if (sPool != null) {
                sPool?.destroy()
                sPool = null
            }
        }
    }

     object CREATOR : Creator<LocalMedia> {
        override fun createFromParcel(parcel: Parcel): LocalMedia {
            return LocalMedia(parcel)
        }

        override fun newArray(size: Int): Array<LocalMedia?> {
            return arrayOfNulls(size)
        }
    }
}