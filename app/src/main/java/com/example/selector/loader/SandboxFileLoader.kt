package com.example.selector.loader

import android.content.Context
import android.text.TextUtils
import com.example.selector.config.PictureMimeType
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.entity.MediaExtraInfo
import com.example.selector.utils.MediaUtils
import com.example.selector.utils.SdkVersionUtils
import com.example.selector.utils.SortUtils
import com.example.selector.utils.ValueOf
import com.luck.picture.lib.entity.LocalMedia
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object SandboxFileLoader {
    /**
     * 查询应用内部目录的图片
     *
     * @param context    上下文
     * @param sandboxDir 资源目标路径
     */
    fun loadInAppSandboxFolderFile(context: Context?, sandboxDir: String?): LocalMediaFolder? {
        val list: ArrayList<LocalMedia>? = loadInAppSandboxFile(context, sandboxDir)
        var folder: LocalMediaFolder? = null
        if (list != null && list.size > 0) {
            SortUtils.sortLocalMediaAddedTime(list)
            val firstMedia: LocalMedia = list[0]
            folder = LocalMediaFolder()
            folder.folderName = firstMedia.parentFolderName
            folder.firstImagePath = firstMedia.path
            folder.firstMimeType = firstMedia.mimeType
            folder.bucketId = firstMedia.bucketId
            folder.folderTotalNum = list.size
            folder.data = list
        }
        return folder
    }

    /**
     * 查询应用内部目录的图片
     *
     * @param context    上下文
     * @param sandboxDir 资源目标路径
     */
    fun loadInAppSandboxFile(context: Context?, sandboxDir: String?): ArrayList<LocalMedia>? {
        if (TextUtils.isEmpty(sandboxDir)) {
            return null
        }
        val list: ArrayList<LocalMedia> = ArrayList<LocalMedia>()
        val sandboxFile = File(sandboxDir)
        if (sandboxFile.exists()) {
            val files = sandboxFile.listFiles { file -> !file.isDirectory } ?: return list
            val config: PictureSelectionConfig = PictureSelectionConfig.instance!!
            var md: MessageDigest? = null
            try {
                md = MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            for (f in files) {
                val mimeType: String = MediaUtils.getMimeTypeFromMediaUrl(f.absolutePath)
                if (config.chooseMode == SelectMimeType.ofImage()) {
                    if (!PictureMimeType.isHasImage(mimeType)) {
                        continue
                    }
                } else if (config.chooseMode == SelectMimeType.ofVideo()) {
                    if (!PictureMimeType.isHasVideo(mimeType)) {
                        continue
                    }
                } else if (config.chooseMode == SelectMimeType.ofAudio()) {
                    if (!PictureMimeType.isHasAudio(mimeType)) {
                        continue
                    }
                }
                if (config.queryOnlyList != null && config.queryOnlyList!!.size > 0 && !config.queryOnlyList!!.contains(
                        mimeType)
                ) {
                    continue
                }
                if (!config.isGif) {
                    if (PictureMimeType.isHasGif(mimeType)) {
                        continue
                    }
                }
                val absolutePath = f.absolutePath
                val size = f.length()
                if (size <= 0) {
                    continue
                }
                val id: Long = if (md != null) {
                    md.update(absolutePath.toByteArray())
                    BigInteger(1, md.digest()).toLong()
                } else {
                    f.lastModified() / 1000
                }
                val bucketId: Long = ValueOf.toLong(sandboxFile.name.hashCode())
                val dateAdded = f.lastModified() / 1000
                var duration: Long
                var width: Int
                var height: Int
                if (PictureMimeType.isHasVideo(mimeType)) {
                    val videoSize: MediaExtraInfo = MediaUtils.getVideoSize(context, absolutePath)
                    width = videoSize.width
                    height = videoSize.height
                    duration = videoSize.duration
                } else if (PictureMimeType.isHasAudio(mimeType)) {
                    val audioSize: MediaExtraInfo = MediaUtils.getAudioSize(context, absolutePath)
                    width = audioSize.width
                    height = audioSize.height
                    duration = audioSize.duration
                } else {
                    val imageSize: MediaExtraInfo = MediaUtils.getImageSize(context, absolutePath)
                    width = imageSize.width
                    height = imageSize.height
                    duration = 0L
                }
                if (PictureMimeType.isHasVideo(mimeType) || PictureMimeType.isHasAudio(mimeType)) {
                    if (config.filterVideoMinSecond > 0 && duration < config.filterVideoMinSecond) {
                        // If you set the minimum number of seconds of video to display
                        continue
                    }
                    if (config.filterVideoMaxSecond > 0 && duration > config.filterVideoMaxSecond) {
                        // If you set the maximum number of seconds of video to display
                        continue
                    }
                    if (duration == 0L) {
                        //If the length is 0, the corrupted video is processed and filtered out
                        continue
                    }
                }
                val media: LocalMedia = LocalMedia.create()
                media.id = id
                media.path = absolutePath
                media.realPath = absolutePath
                media.fileName = f.name
                media.parentFolderName = sandboxFile.name
                media.duration = duration
                media.chooseModel = config.chooseMode
                media.mimeType = mimeType
                media.width = width
                media.height = height
                media.size = size
                media.bucketId =bucketId
                media.dateAddedTime = dateAdded
                if (PictureSelectionConfig.onQueryFilterListener != null) {
                    if (PictureSelectionConfig.onQueryFilterListener!!.onFilter(media)) {
                        continue
                    }
                }
                media.sandboxPath = if (SdkVersionUtils.isQ) absolutePath else null
                list.add(media)
            }
        }
        return list
    }
}
