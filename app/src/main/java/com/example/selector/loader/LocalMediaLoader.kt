package com.example.selector.loader

import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import com.example.mygallery.R
import com.example.selector.config.*
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.interfaces.OnQueryAlbumListener
import com.example.selector.interfaces.OnQueryAllAlbumListener
import com.example.selector.interfaces.OnQueryDataResultListener
import com.example.selector.threads.PictureThreadUtils
import com.example.selector.utils.MediaUtils
import com.example.selector.utils.SdkVersionUtils
import com.example.selector.utils.SortUtils
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

class LocalMediaLoader : IBridgeMediaLoader() {
    override fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder>) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<List<LocalMediaFolder>>() {
            override fun doInBackground(): List<LocalMediaFolder> {
                val imageFolders: MutableList<LocalMediaFolder> = ArrayList<LocalMediaFolder>()
                val data: Cursor = context?.contentResolver?.query(QUERY_URI!!, PROJECTION,
                    selection,
                    selectionArgs,
                    sortOrder)!!
                try {
                    val allImageFolder = LocalMediaFolder()
                    val latelyImages: ArrayList<LocalMedia> = ArrayList<LocalMedia>()
                    val count = data.count
                    if (count > 0) {
                        data.moveToFirst()
                        do {
                            val media: LocalMedia = parseLocalMedia(data, false) ?: continue
                            val folder: LocalMediaFolder? = media.parentFolderName?.let {
                                getImageFolder(media.path!!,
                                    media.mimeType!!, it, imageFolders)
                            }
                            folder?.bucketId = media.bucketId
                            folder?.data?.add(media)
                            folder?.folderTotalNum = folder?.folderTotalNum!! + 1
                            folder.bucketId = media.bucketId
                            latelyImages.add(media)
                            val imageNum: Int = allImageFolder.folderTotalNum
                            allImageFolder.folderTotalNum = imageNum + 1
                        } while (data.moveToNext())
                        val selfFolder: LocalMediaFolder = SandboxFileLoader
                            .loadInAppSandboxFolderFile(context, config.sandboxDir)
                        imageFolders.add(selfFolder)
                        allImageFolder.folderTotalNum = (allImageFolder.folderTotalNum + selfFolder.folderTotalNum)
                        allImageFolder.data = selfFolder.data
                        latelyImages.addAll(0, selfFolder.data!!)
                        if (MAX_SORT_SIZE > selfFolder.folderTotalNum) {
                            if (latelyImages.size > MAX_SORT_SIZE) {
                                SortUtils.sortLocalMediaAddedTime(latelyImages.subList(0,
                                    MAX_SORT_SIZE))
                            } else {
                                SortUtils.sortLocalMediaAddedTime(latelyImages)
                            }
                        }
                        if (latelyImages.size > 0) {
                            SortUtils.sortFolder(imageFolders)
                            imageFolders.add(0, allImageFolder)
                            allImageFolder.firstImagePath = (latelyImages[0].path)
                            allImageFolder.firstMimeType = (latelyImages[0].mimeType)
                            val folderName: String? = if (TextUtils.isEmpty(config.defaultAlbumName)) {
                                if (config.chooseMode == SelectMimeType.ofAudio()) context?.getString(
                                    R.string.ps_all_audio) else context?.getString(R.string.ps_camera_roll)
                            } else {
                                config.defaultAlbumName
                            }
                            allImageFolder.folderName = folderName
                            allImageFolder.bucketId = PictureConfig.ALL.toLong()
                            allImageFolder.data = latelyImages
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (!data.isClosed) {
                        data.close()
                    }
                }
                return imageFolders
            }

           override fun onSuccess(result: List<LocalMediaFolder>) {
                PictureThreadUtils.cancel(this)
                query.onComplete(result )
            }
        })
    }

    override fun loadOnlyInAppDirAllMedia(listener: OnQueryAlbumListener<LocalMediaFolder>) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<LocalMediaFolder?>() {
            override fun doInBackground(): LocalMediaFolder {
                return SandboxFileLoader.loadInAppSandboxFolderFile(context,
                    config.sandboxDir)
            }

            override fun onSuccess(result: LocalMediaFolder?) {
                PictureThreadUtils.cancel(this)
                listener?.onComplete(result)
            }
        })
    }

    override fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia>
    ) {
    }

    override fun getAlbumFirstCover(bucketId: Long): String? {
        return null
    }// Access to the audio// Access to video// Gets the image

    // Get all, not including audio
    override val selection: String?
         get() {
            val durationCondition: String = durationCondition
            val fileSizeCondition: String = fileSizeCondition
            val queryMimeCondition: String = queryMimeCondition
            when (config.chooseMode) {
                SelectMimeType.TYPE_ALL ->                 // Get all, not including audio
                    return getSelectionArgsForAllMediaCondition(durationCondition,
                        fileSizeCondition,
                        queryMimeCondition)
                SelectMimeType.TYPE_IMAGE ->                 // Gets the image
                    return getSelectionArgsForImageMediaCondition(fileSizeCondition,
                        queryMimeCondition)
                SelectMimeType.TYPE_VIDEO ->                 // Access to video
                    return getSelectionArgsForVideoMediaCondition(durationCondition,
                        queryMimeCondition)
                SelectMimeType.TYPE_AUDIO ->                 // Access to the audio
                    return getSelectionArgsForAudioMediaCondition(durationCondition,
                        queryMimeCondition)
            }
            return null
        }// Get audio// Get video// Get photo

    // Get all
    override val selectionArgs: Array<String>
         get() {
            when (config.chooseMode) {
                SelectMimeType.TYPE_ALL ->                 // Get all
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
                SelectMimeType.TYPE_IMAGE ->                 // Get photo
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
                SelectMimeType.TYPE_VIDEO ->                 // Get video
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
                SelectMimeType.TYPE_AUDIO ->                 // Get audio
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
            }
            return emptyArray()
        }

    override val sortOrder: String?
         get() = if (TextUtils.isEmpty(config.sortOrder)) ORDER_BY else config.sortOrder

     override fun parseLocalMedia(data: Cursor?, isUsePool: Boolean): LocalMedia? {
        val idColumn = data?.getColumnIndexOrThrow(PROJECTION[0])
        val dataColumn = data?.getColumnIndexOrThrow(PROJECTION[1])
        val mimeTypeColumn = data?.getColumnIndexOrThrow(PROJECTION[2])
        val widthColumn = data?.getColumnIndexOrThrow(PROJECTION[3])
        val heightColumn = data?.getColumnIndexOrThrow(PROJECTION[4])
        val durationColumn = data?.getColumnIndexOrThrow(PROJECTION[5])
        val sizeColumn = data?.getColumnIndexOrThrow(PROJECTION[6])
        val folderNameColumn = data?.getColumnIndexOrThrow(PROJECTION[7])
        val fileNameColumn = data?.getColumnIndexOrThrow(PROJECTION[8])
        val bucketIdColumn = data?.getColumnIndexOrThrow(PROJECTION[9])
        val dateAddedColumn = data?.getColumnIndexOrThrow(PROJECTION[10])
        val orientationColumn = data?.getColumnIndexOrThrow(PROJECTION[11])
        val id = data?.getLong(idColumn!!)
        val dateAdded = data?.getLong(dateAddedColumn!!)
        var mimeType = mimeTypeColumn?.let { data.getString(it) }
        val absolutePath = dataColumn?.let { data.getString(it) }
        val url =
            if (SdkVersionUtils.isQ) MediaUtils.getRealPathUri(id!!, mimeType) else absolutePath
        mimeType = if (TextUtils.isEmpty(mimeType)) PictureMimeType.ofJPEG() else mimeType
        // Here, it is solved that some models obtain mimeType and return the format of image / *,
        // which makes it impossible to distinguish the specific type, such as mi 8,9,10 and other models
         if (mimeType != null) {
             if (mimeType.endsWith("image/*")) {
                 mimeType = MediaUtils.getMimeTypeFromMediaUrl(absolutePath)
                 if (config.isGif == true) {
                     if (PictureMimeType.isHasGif(mimeType)) {
                         return null
                     }
                 }
             }
         }
         if (mimeType != null) {
             if (mimeType.endsWith("image/*")) {
                 return null
             }
         }
        if (config.isWebp == true) {
            if (mimeType?.startsWith(PictureMimeType.ofWEBP()) == true) {
                return null
            }
        }
        if (config.isBmp) {
            if (mimeType?.let { PictureMimeType.isHasBmp(it) } == true) {
                return null
            }
        }
        var width = data?.getInt(widthColumn!!)
        var height = heightColumn?.let { data.getInt(it) }
        val orientation = data?.getInt(orientationColumn!!)
        if (orientation == 90 || orientation == 270) {
            width = heightColumn?.let { data.getInt(it) }!!
            height = widthColumn?.let { data.getInt(it) }!!
        }
        val duration = durationColumn?.let { data.getLong(it) }
        val size = sizeColumn?.let { data.getLong(it) }
        val folderName = folderNameColumn?.let { data.getString(it) }
        var fileName = fileNameColumn?.let { data.getString(it) }
        val bucketId = bucketIdColumn?.let { data.getLong(it) }
        if (TextUtils.isEmpty(fileName)) {
            fileName = absolutePath?.let { PictureMimeType.getUrlToFileName(it) }
        }
        if (config.isFilterSizeDuration && size!! > 0 && size < FileSizeUnit.KB) {
            // Filter out files less than 1KB
            return null
        }
        if (mimeType?.let { PictureMimeType.isHasVideo(it) } == true || PictureMimeType.isHasAudio(mimeType)) {
            if (config.filterVideoMinSecond!!  > 0 && duration!! < config.filterVideoMinSecond) {
                // If you set the minimum number of seconds of video to display
                return null
            }
            if (config.filterVideoMaxSecond!! in 1 until duration!!) {
                // If you set the maximum number of seconds of video to display
                return null
            }
            if (config.isFilterSizeDuration == true && duration <= 0) {
                //If the length is 0, the corrupted video is processed and filtered out
                return null
            }
        }
        val media: LocalMedia = LocalMedia.create()
         if (id != null) {
             media.id = id
         }
        media.bucketId = bucketId!!
        media.path = url
        media.realPath = absolutePath
        media.fileName = fileName
        media.parentFolderName = folderName
        media.duration = duration!!
        media.chooseModel = config.chooseMode
        media.mimeType = mimeType
         if (width != null) {
             media.width = width
         }
         if (height != null) {
             media.height = height
         }
        media.size = size!!
         if (dateAdded != null) {
             media.dateAddedTime = dateAdded
         }
        if (PictureSelectionConfig.onQueryFilterListener != null) {
            if (PictureSelectionConfig.onQueryFilterListener!!.onFilter(media)) {
                return null
            }
        }
        return media
    }

    /**
     * Create folder
     *
     * @param firstPath
     * @param firstMimeType
     * @param imageFolders
     * @param folderName
     * @return
     */
    private fun getImageFolder(
        firstPath: String,
        firstMimeType: String,
        folderName: String,
        imageFolders: MutableList<LocalMediaFolder>,
    ): LocalMediaFolder {
        for (folder in imageFolders) {
            // Under the same folder, return yourself, otherwise create a new folder
            val name: String = folder?.folderName!!
            if (TextUtils.isEmpty(name)) {
                continue
            }
            if (TextUtils.equals(name, folderName)) {
                return folder!!
            }
        }
        val newFolder = LocalMediaFolder()
        newFolder.folderName = folderName
        newFolder.firstImagePath = firstPath
        newFolder.firstMimeType = firstMimeType
        imageFolders.add(newFolder)
        return newFolder
    }

    companion object {
        /**
         * Video mode conditions
         *
         * @param durationCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForVideoMediaCondition(
            durationCondition: String,
            queryMimeCondition: String,
        ): String {
            return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + durationCondition
        }

        /**
         * Audio mode conditions
         *
         * @param durationCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForAudioMediaCondition(
            durationCondition: String,
            queryMimeCondition: String,
        ): String {
            return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + durationCondition
        }

        /**
         * Query conditions in all modes
         *
         * @param timeCondition
         * @param sizeCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForAllMediaCondition(
            timeCondition: String,
            sizeCondition: String,
            queryMimeCondition: String,
        ): String {
            return "(" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                    queryMimeCondition + " OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " +
                    timeCondition + ") AND " +
                    sizeCondition
        }

        /**
         * Query conditions in image modes
         *
         * @param fileSizeCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForImageMediaCondition(
            fileSizeCondition: String,
            queryMimeCondition: String,
        ): String {
            return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + fileSizeCondition
        }
    }
}
