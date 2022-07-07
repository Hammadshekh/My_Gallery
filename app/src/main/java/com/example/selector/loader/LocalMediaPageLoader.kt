package com.example.selector.loader

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.example.mygallery.R
import com.example.selector.config.*
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.entity.MediaData
import com.example.selector.interfaces.OnQueryAlbumListener
import com.example.selector.interfaces.OnQueryAllAlbumListener
import com.example.selector.interfaces.OnQueryDataResultListener
import com.example.selector.threads.PictureThreadUtils
import com.example.selector.utils.*
import com.luck.picture.lib.entity.LocalMedia
import java.io.File
import java.util.*

open class LocalMediaPageLoader : IBridgeMediaLoader() {
    /**
     * Query conditions in all modes
     *
     * @param timeCondition
     * @param sizeCondition
     * @param queryMimeTypeOptions
     * @return
     */
    private fun getSelectionArgsForAllMediaCondition(
        timeCondition: String,
        sizeCondition: String,
        queryMimeTypeOptions: String,
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
            .append(queryMimeTypeOptions).append(" OR ")
            .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=? AND ").append(timeCondition)
            .append(") AND ")
            .append(sizeCondition)
        return if (isWithAllQuery) {
            stringBuilder.toString()
        } else {
            stringBuilder.append(")").append(GROUP_BY_BUCKET_Id).toString()
        }
    }

    /**
     * Query conditions in image modes
     *
     * @param fileSizeCondition
     * @param queryMimeTypeOptions
     * @return
     */
    private fun getSelectionArgsForImageMediaCondition(
        fileSizeCondition: String,
        queryMimeTypeOptions: String,
    ): String {
        val stringBuilder = StringBuilder()
        return if (isWithAllQuery) {
            stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeTypeOptions).append(" AND ").append(fileSizeCondition).toString()
        } else {
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeTypeOptions).append(") AND ").append(fileSizeCondition).append(")")
                .append(GROUP_BY_BUCKET_Id).toString()
        }
    }

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
        val stringBuilder = StringBuilder()
        return if (isWithAllQuery) {
            stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition).toString()
        } else {
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(") AND ").append(durationCondition).append(")")
                .append(GROUP_BY_BUCKET_Id).toString()
        }
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
        val stringBuilder = StringBuilder()
        return if (isWithAllQuery) {
            stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition).toString()
        } else {
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(") AND ").append(durationCondition).append(")")
                .append(GROUP_BY_BUCKET_Id).toString()
        }
    }

    override fun getAlbumFirstCover(bucketId: Long): String? {
        var data: Cursor? = null
        try {
            data = if (SdkVersionUtils.isR()) {
                val queryArgs: Bundle = MediaUtils.createQueryArgsBundle(getPageSelection(bucketId),
                    getPageSelectionArgs(bucketId),
                    1,
                    0,
                    sortOrder)
                context?.contentResolver!!.query(QUERY_URI, arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.MediaColumns.MIME_TYPE,
                    MediaStore.MediaColumns.DATA), queryArgs, null)
            } else {
                val orderBy = "$sortOrder limit 1 offset 0"
                context?.contentResolver!!.query(QUERY_URI,
                    arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.DATA),
                    getPageSelection(bucketId),
                    getPageSelectionArgs(bucketId),
                    orderBy)
            }
            if (data != null && data.count > 0) {
                if (data.moveToFirst()) {
                    val id =
                        data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                    val mimeType =
                        data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                    return if (SdkVersionUtils.isQ) MediaUtils.getRealPathUri(id,
                        mimeType) else data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                }
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (data != null && !data.isClosed) {
                data.close()
            }
        }
        return null
    }

    override fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia>
    ) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<MediaData?>() {
            override fun doInBackground(): MediaData {
                var data: Cursor? = null
                try {
                    data = if (SdkVersionUtils.isR()) {
                        val queryArgs: Bundle =
                            MediaUtils.createQueryArgsBundle(getPageSelection(bucketId),
                                getPageSelectionArgs(bucketId),
                                pageSize,
                                (page - 1) * pageSize,
                                sortOrder)
                        context!!.contentResolver
                            .query(QUERY_URI, PROJECTION, queryArgs, null)
                    } else {
                        val orderBy =
                            if (page == PictureConfig.ALL) sortOrder else sortOrder + " limit " + pageSize + " offset " + (page - 1) * pageSize
                        context!!.contentResolver.query(QUERY_URI,
                            PROJECTION,
                            getPageSelection(bucketId),
                            getPageSelectionArgs(bucketId),
                            orderBy)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(TAG, "loadMedia Page Data Error: " + e.message)
                    return MediaData()
                } finally {
                    if (data != null && !data.isClosed) {
                        data.close()
                    }
                }
                return MediaData()
            }

            override fun onSuccess(result: MediaData?) {
                PictureThreadUtils.cancel(this)
                (if (result!!.data != null) result.data else ArrayList())?.let {
                    query?.onComplete(it,
                        result.isHasNextMore)
                }
            }
        })
    }

    override fun loadOnlyInAppDirAllMedia(query: OnQueryAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<LocalMediaFolder?>() {
            override fun doInBackground(): LocalMediaFolder {
                return SandboxFileLoader.loadInAppSandboxFolderFile(context,
                    config!!.sandboxDir)!!
            }

            override fun onSuccess(result: LocalMediaFolder?) {
                PictureThreadUtils.cancel(this)
                query?.onComplete(result)
            }
        })
    }

    /**
     * Query the local gallery data
     *
     * @param query
     */
    override fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder>) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<List<LocalMediaFolder>>() {
            override fun doInBackground(): List<LocalMediaFolder> {
                val data: Cursor = context?.contentResolver?.query(QUERY_URI,
                    if (isWithAllQuery) PROJECTION else ALL_PROJECTION,
                    selection, selectionArgs, sortOrder)!!
                try {
                    val count = data.count
                    var totalCount = 0
                    val mediaFolders: MutableList<LocalMediaFolder> =
                        ArrayList<LocalMediaFolder>()
                    if (count > 0) {
                        if (isWithAllQuery) {
                            val countMap: MutableMap<Long, Long> = HashMap()
                            while (data.moveToNext()) {
                                if (config?.isPageSyncAsCount == true) {
                                    val media: LocalMedia = parseLocalMedia(data, true)
                                        ?: continue
                                    media.recycle()
                                }
                                val bucketId =
                                    data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID))
                                var newCount = countMap[bucketId]
                                if (newCount == null) {
                                    newCount = 1L
                                } else {
                                    newCount++
                                }
                                countMap[bucketId] = newCount
                            }
                            if (data.moveToFirst()) {
                                val hashSet: MutableSet<Long> = HashSet()
                                do {
                                    val bucketId =
                                        data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID))
                                    if (hashSet.contains(bucketId)) {
                                        continue
                                    }
                                    val mediaFolder = LocalMediaFolder()
                                    mediaFolder.bucketId = bucketId
                                    val bucketDisplayName = data.getString(
                                        data.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME))
                                    val mimeType =
                                        data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                                    if (!countMap.containsKey(bucketId)) {
                                        continue
                                    }
                                    val size = countMap[bucketId]!!
                                    val id =
                                        data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                                    mediaFolder.folderName = bucketDisplayName
                                    mediaFolder.folderTotalNum = ValueOf.toInt(size)
                                    mediaFolder.firstImagePath = MediaUtils.getRealPathUri(id,
                                        mimeType)
                                    mediaFolder.firstMimeType = mimeType
                                    mediaFolders.add(mediaFolder)
                                    hashSet.add(bucketId)
                                    totalCount += size.toInt()
                                } while (data.moveToNext())
                            }
                        } else {
                            data.moveToFirst()
                            do {
                                val url =
                                    data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                                val bucketDisplayName =
                                    data.getString(data.getColumnIndexOrThrow(
                                        COLUMN_BUCKET_DISPLAY_NAME))
                                val mimeType =
                                    data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                                val bucketId =
                                    data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID))
                                val size = data.getInt(data.getColumnIndexOrThrow(COLUMN_COUNT))
                                val mediaFolder = LocalMediaFolder()
                                mediaFolder.bucketId = bucketId
                                mediaFolder.firstImagePath = url
                                mediaFolder.folderName = bucketDisplayName
                                mediaFolder.firstMimeType = mimeType
                                mediaFolder.folderTotalNum = size
                                mediaFolders.add(mediaFolder)
                                totalCount += size
                            } while (data.moveToNext())
                        }
                        // 相机胶卷
                        val allMediaFolder = LocalMediaFolder()
                        val selfFolder: LocalMediaFolder = SandboxFileLoader
                            .loadInAppSandboxFolderFile(context, config?.sandboxDir)!!
                        mediaFolders.add(selfFolder)
                        val firstImagePath: String = selfFolder.firstImagePath!!
                        val file = File(firstImagePath)
                        val lastModified = file.lastModified()
                        totalCount += selfFolder.folderTotalNum
                        allMediaFolder.data = ArrayList()
                        if (data.moveToFirst()) {
                            allMediaFolder.firstImagePath = (if (SdkVersionUtils.isQ) getFirstUri(
                                data) else getFirstUrl(data))
                            allMediaFolder.firstMimeType = getFirstCoverMimeType(data)
                            val lastModified2: Long = if (PictureMimeType.isContent(allMediaFolder.firstImagePath!!)) {
                                    val path: String =
                                        PictureFileUtils.getPath(context!!,
                                            Uri.parse(allMediaFolder.firstImagePath))!!
                                    File(path).lastModified()
                                } else {
                                    File(allMediaFolder.firstImagePath).lastModified()
                                }
                            if (lastModified > lastModified2) {
                                allMediaFolder.firstImagePath = selfFolder.firstImagePath
                                allMediaFolder.firstMimeType = selfFolder.firstMimeType
                            }
                        }
                        if (totalCount == 0) {
                            return mediaFolders
                        }
                        SortUtils.sortFolder(mediaFolders)
                        allMediaFolder.folderTotalNum = totalCount
                        allMediaFolder.bucketId = PictureConfig.ALL.toLong()
                        val folderName: String = if (TextUtils.isEmpty(config?.defaultAlbumName)) {
                            if (config?.chooseMode!!  == SelectMimeType.ofAudio()) context!!.getString(
                                R.string.ps_all_audio) else context!!.getString(R.string.ps_camera_roll)
                        } else ({
                            config!!.defaultAlbumName
                        }).toString()
                        allMediaFolder.folderName = folderName
                        mediaFolders.add(0, allMediaFolder)
                        if (config?.isSyncCover == true) {
                            if (config?.chooseMode!! == SelectMimeType.ofAll()) {
                                synchronousFirstCover(mediaFolders)
                            }
                        }
                        return mediaFolders
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(TAG, "loadAllMedia Data Error: " + e.message)
                } finally {
                    if (!data.isClosed) {
                        data.close()
                    }
                }
                return ArrayList<LocalMediaFolder>()
            }

            override fun onSuccess(result: List<LocalMediaFolder>) {
                PictureThreadUtils.cancel(this)
                LocalMedia.destroyPool()
                query.onComplete(result)
            }
        })
    }

    /**
     * Synchronous  First data Cover
     *
     * @param mediaFolders
     */
    private fun synchronousFirstCover(mediaFolders: List<LocalMediaFolder?>) {
        for (i in mediaFolders.indices) {
            val mediaFolder: LocalMediaFolder = mediaFolders[i] ?: continue
            val firstCover = getAlbumFirstCover(mediaFolder.bucketId)
            if (TextUtils.isEmpty(firstCover)) {
                continue
            }
            mediaFolder.firstImagePath = firstCover
        }
    }

    private fun getPageSelection(bucketId: Long): String? {
        val durationCondition: String = durationCondition
        val sizeCondition: String = fileSizeCondition
            val queryMimeCondition: String = queryMimeCondition
        when (config!!.chooseMode) {
            SelectMimeType.TYPE_ALL ->                 //  Gets the all
                return getPageSelectionArgsForAllMediaCondition(bucketId,
                    queryMimeCondition,
                    durationCondition,
                    sizeCondition)
            SelectMimeType.TYPE_IMAGE ->                 // Gets the image of the specified type
                return getPageSelectionArgsForImageMediaCondition(bucketId,
                    queryMimeCondition,
                    sizeCondition)
            SelectMimeType.TYPE_VIDEO ->                 //  Gets the video or video
                return getPageSelectionArgsForVideoMediaCondition(bucketId,
                    queryMimeCondition,
                    durationCondition,
                    sizeCondition)
            SelectMimeType.TYPE_AUDIO ->                 //  Gets the video or audio
                return getPageSelectionArgsForAudioMediaCondition(bucketId,
                    queryMimeCondition,
                    durationCondition,
                    sizeCondition)
        }
        return null
    }

    private fun getPageSelectionArgs(bucketId: Long): Array<String>? {
        when (config!!.chooseMode) {
            SelectMimeType.TYPE_ALL -> {
                return if (bucketId.equals(PictureConfig.ALL)) {
                    // ofAll
                    arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
                } else arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                    ValueOf.toString(bucketId)
                )
                //  Gets the specified album directory
            }
            SelectMimeType.TYPE_IMAGE ->                 // Get photo
                return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    bucketId)
            SelectMimeType.TYPE_VIDEO ->                 // Get video
                return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    bucketId)
            SelectMimeType.TYPE_AUDIO ->                 // Get audio
                return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO,
                    bucketId)
        }
        return null
    }// Access to the audio// Access to video// Get Images

    // Get all, not including audio
    override val selection: String?
         get() {
            val durationCondition: String = durationCondition
            val fileSizeCondition: String = fileSizeCondition
            val queryMimeCondition: String = queryMimeCondition
            when (config!!.chooseMode) {
                SelectMimeType.TYPE_ALL ->                 // Get all, not including audio
                    return getSelectionArgsForAllMediaCondition(durationCondition,
                        fileSizeCondition,
                        queryMimeCondition)
                SelectMimeType.TYPE_IMAGE ->                 // Get Images
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
    override val selectionArgs: Array<String>?
  get() {
            when (config!!.chooseMode) {
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
            return null
        }

    override val sortOrder: String
         get() = if (TextUtils.isEmpty(config?.sortOrder)) ORDER_BY else config?.sortOrder!!

    /**
     * 查询方式
     */
    private val isWithAllQuery: Boolean
        get() = if (SdkVersionUtils.isQ) {
            true
        } else {
            config!!.isPageSyncAsCount
        }

    override fun parseLocalMedia(data: Cursor?, isUsePool: Boolean): LocalMedia? {
        val idColumn = data!!.getColumnIndexOrThrow(PROJECTION[0])
        val dataColumn = data.getColumnIndexOrThrow(PROJECTION[1])
        val mimeTypeColumn = data.getColumnIndexOrThrow(PROJECTION[2])
        val widthColumn = data.getColumnIndexOrThrow(PROJECTION[3])
        val heightColumn = data.getColumnIndexOrThrow(PROJECTION[4])
        val durationColumn = data.getColumnIndexOrThrow(PROJECTION[5])
        val sizeColumn = data.getColumnIndexOrThrow(PROJECTION[6])
        val folderNameColumn = data.getColumnIndexOrThrow(PROJECTION[7])
        val fileNameColumn = data.getColumnIndexOrThrow(PROJECTION[8])
        val bucketIdColumn = data.getColumnIndexOrThrow(PROJECTION[9])
        val dateAddedColumn = data.getColumnIndexOrThrow(PROJECTION[10])
        val orientationColumn = data.getColumnIndexOrThrow(PROJECTION[11])
        val id = data.getLong(idColumn)
        var mimeType = data.getString(mimeTypeColumn)
        val absolutePath = data.getString(dataColumn)
        val url =
            if (SdkVersionUtils.isQ) MediaUtils.getRealPathUri(id, mimeType) else absolutePath
        mimeType = if (TextUtils.isEmpty(mimeType)) PictureMimeType.ofJPEG() else mimeType
        if (config?.isFilterInvalidFile == true) {
            if (PictureMimeType.isHasImage(mimeType)) {
                if (!TextUtils.isEmpty(absolutePath) && !PictureFileUtils.isImageFileExists(
                        absolutePath)
                ) {
                    return null
                }
            } else {
                if (!PictureFileUtils.isFileExists(absolutePath)) {
                    return null
                }
            }
        }
        // Here, it is solved that some models obtain mimeType and return the format of image / *,
        // which makes it impossible to distinguish the specific type, such as mi 8,9,10 and other models
        if (mimeType.endsWith("image/*")) {
            mimeType = MediaUtils.getMimeTypeFromMediaUrl(absolutePath)
            if (config?.isGif == true) {
                if (PictureMimeType.isHasGif(mimeType)) {
                    return null
                }
            }
        }
        if (mimeType.endsWith("image/*")) {
            return null
        }
        if (config?.isWebp == true) {
            if (mimeType.startsWith(PictureMimeType.ofWEBP())) {
                return null
            }
        }
        if (config!!.isBmp) {
            if (PictureMimeType.isHasBmp(mimeType)) {
                return null
            }
        }
        var width = data.getInt(widthColumn)
        var height = data.getInt(heightColumn)
        val orientation = data.getInt(orientationColumn)
        if (orientation == 90 || orientation == 270) {
            width = data.getInt(heightColumn)
            height = data.getInt(widthColumn)
        }
        val duration = data.getLong(durationColumn)
        val size = data.getLong(sizeColumn)
        val folderName = data.getString(folderNameColumn)
        var fileName = data.getString(fileNameColumn)
        val bucketId = data.getLong(bucketIdColumn)
        val dateAdded = data.getLong(dateAddedColumn)
        if (TextUtils.isEmpty(fileName)) {
            fileName = PictureMimeType.getUrlToFileName(absolutePath)
        }
        if (config!!.isFilterSizeDuration && size > 0 && size < FileSizeUnit.KB) {
            // Filter out files less than 1KB
            return null
        }
        if (PictureMimeType.isHasVideo(mimeType) || PictureMimeType.isHasAudio(mimeType)) {
            if (config?.filterVideoMinSecond!! > 0 && duration < config!!.filterVideoMinSecond) {
                // If you set the minimum number of seconds of video to display
                return null
            }
            if (config?.filterVideoMaxSecond!! in 1 until duration) {
                // If you set the maximum number of seconds of video to display
                return null
            }
            if (config!!.isFilterSizeDuration && duration <= 0) {
                //If the length is 0, the corrupted video is processed and filtered out
                return null
            }
        }
        val media: LocalMedia = if (isUsePool) LocalMedia.obtain() else LocalMedia.create()
        media.id = id
        media.bucketId = bucketId
        media.setPath(url)
        media.realPath = absolutePath
        media.fileName = fileName
        media.parentFolderName = folderName
        media.duration = duration
        media.chooseModel = config?.chooseMode !!
        media.mimeType = mimeType
        media.width = width
        media.height = height
        media.size = size
        media.dateAddedTime = dateAdded
        if (PictureSelectionConfig.onQueryFilterListener != null) {
            if (PictureSelectionConfig.onQueryFilterListener!!.onFilter(media)) {
                return null
            }
        }
        return media
    }

    companion object {
        /**
         * Gets a file of the specified type
         *
         * @param mediaType
         * @return
         */
        private fun getSelectionArgsForPageSingleMediaType(
            mediaType: Int,
            bucketId: Long,
        ): Array<String> {
            return if (bucketId.equals(PictureConfig.ALL)) arrayOf(mediaType.toString()) else arrayOf(
                mediaType.toString(),
                ValueOf.toString(bucketId))
        }

        /**
         * Get cover uri
         *
         * @param cursor
         * @return
         */
        private fun getFirstUri(cursor: Cursor): String {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val mimeType =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
            return MediaUtils.getRealPathUri(id, mimeType)
        }

        /**
         * Get cover uri mimeType
         *
         * @param cursor
         * @return
         */
        private fun getFirstCoverMimeType(cursor: Cursor): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
        }

        /**
         * Get cover url
         *
         * @param cursor
         * @return
         */
        private fun getFirstUrl(cursor: Cursor): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
        }

        private fun getPageSelectionArgsForAllMediaCondition(
            bucketId: Long,
            queryMimeCondition: String,
            durationCondition: String,
            sizeCondition: String,
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                .append("=?").append(queryMimeCondition).append(" OR ")
                .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=? AND ")
                .append(durationCondition).append(") AND ")
            return if (bucketId.equals(PictureConfig.ALL) ) {
                stringBuilder.append(sizeCondition).toString()
            } else {
                stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition)
                    .toString()
            }
        }

        private fun getPageSelectionArgsForImageMediaCondition(
            bucketId: Long,
            queryMimeCondition: String,
            sizeCondition: String,
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
            return if (bucketId.equals(PictureConfig.ALL) ) {
                stringBuilder.append(queryMimeCondition).append(") AND ").append(sizeCondition)
                    .toString()
            } else {
                stringBuilder.append(queryMimeCondition).append(") AND ").append(COLUMN_BUCKET_ID)
                    .append("=? AND ").append(sizeCondition).toString()
            }
        }

        private fun getPageSelectionArgsForVideoMediaCondition(
            bucketId: Long,
            queryMimeCondition: String,
            durationCondition: String,
            sizeCondition: String,
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition)
                .append(") AND ")
            return if (bucketId.equals(PictureConfig.ALL)) {
                stringBuilder.append(sizeCondition).toString()
            } else {
                stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition)
                    .toString()
            }
        }

        private fun getPageSelectionArgsForAudioMediaCondition(
            bucketId: Long,
            queryMimeCondition: String,
            durationCondition: String,
            sizeCondition: String,
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition)
                .append(") AND ")
            return if (bucketId.equals(PictureConfig.ALL)) {
                stringBuilder.append(sizeCondition).toString()
            } else {
                stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition)
                    .toString()
            }
        }
    }
}
