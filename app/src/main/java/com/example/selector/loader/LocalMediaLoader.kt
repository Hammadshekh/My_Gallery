package com.example.selector.loader

import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import java.lang.Exception
import java.util.ArrayList

class LocalMediaLoader : IBridgeMediaLoader() {
    override fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo(object : SimpleTask<List<LocalMediaFolder?>?>() {
            fun doInBackground(): List<LocalMediaFolder?> {
                val imageFolders: MutableList<LocalMediaFolder?> = ArrayList<LocalMediaFolder?>()
                val data: Cursor = getContext().getContentResolver().query(QUERY_URI, PROJECTION,
                    selection,
                    selectionArgs,
                    sortOrder)
                try {
                    if (data != null) {
                        val allImageFolder = LocalMediaFolder()
                        val latelyImages: ArrayList<LocalMedia?> = ArrayList<LocalMedia?>()
                        val count = data.count
                        if (count > 0) {
                            data.moveToFirst()
                            do {
                                val media: LocalMedia = parseLocalMedia(data, false) ?: continue
                                val folder: LocalMediaFolder = getImageFolder(media.getPath(),
                                    media.getMimeType(), media.getParentFolderName(), imageFolders)
                                folder.setBucketId(media.getBucketId())
                                folder.getData().add(media)
                                folder.setFolderTotalNum(folder.getFolderTotalNum() + 1)
                                folder.setBucketId(media.getBucketId())
                                latelyImages.add(media)
                                val imageNum: Int = allImageFolder.getFolderTotalNum()
                                allImageFolder.setFolderTotalNum(imageNum + 1)
                            } while (data.moveToNext())
                            val selfFolder: LocalMediaFolder = SandboxFileLoader
                                .loadInAppSandboxFolderFile(getContext(), getConfig().sandboxDir)
                            if (selfFolder != null) {
                                imageFolders.add(selfFolder)
                                allImageFolder.setFolderTotalNum(allImageFolder.getFolderTotalNum() + selfFolder.getFolderTotalNum())
                                allImageFolder.setData(selfFolder.getData())
                                latelyImages.addAll(0, selfFolder.getData())
                                if (MAX_SORT_SIZE > selfFolder.getFolderTotalNum()) {
                                    if (latelyImages.size > MAX_SORT_SIZE) {
                                        SortUtils.sortLocalMediaAddedTime(latelyImages.subList(0,
                                            MAX_SORT_SIZE))
                                    } else {
                                        SortUtils.sortLocalMediaAddedTime(latelyImages)
                                    }
                                }
                            }
                            if (latelyImages.size > 0) {
                                SortUtils.sortFolder(imageFolders)
                                imageFolders.add(0, allImageFolder)
                                allImageFolder.setFirstImagePath(latelyImages[0].getPath())
                                allImageFolder.setFirstMimeType(latelyImages[0].getMimeType())
                                val folderName: String
                                folderName = if (TextUtils.isEmpty(getConfig().defaultAlbumName)) {
                                    if (getConfig().chooseMode === SelectMimeType.ofAudio()) getContext().getString(
                                        R.string.ps_all_audio) else getContext().getString(R.string.ps_camera_roll)
                                } else {
                                    getConfig().defaultAlbumName
                                }
                                allImageFolder.setFolderName(folderName)
                                allImageFolder.setBucketId(PictureConfig.ALL)
                                allImageFolder.setData(latelyImages)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (data != null && !data.isClosed) {
                        data.close()
                    }
                }
                return imageFolders
            }

            fun onSuccess(result: List<LocalMediaFolder?>?) {
                PictureThreadUtils.cancel(this)
                if (query != null) {
                    query.onComplete(result)
                }
            }
        })
    }

    override fun loadOnlyInAppDirAllMedia(listener: OnQueryAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo(object : SimpleTask<LocalMediaFolder?>() {
            fun doInBackground(): LocalMediaFolder {
                return SandboxFileLoader.loadInAppSandboxFolderFile(getContext(),
                    getConfig().sandboxDir)
            }

            fun onSuccess(result: LocalMediaFolder?) {
                PictureThreadUtils.cancel(this)
                if (listener != null) {
                    listener.onComplete(result)
                }
            }
        })
    }

    override fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia?>?,
    ) {
    }

    override fun getAlbumFirstCover(bucketId: Long): String? {
        return null
    }// Access to the audio// Access to video// Gets the image

    // Get all, not including audio
    override val selection: String?
        protected get() {
            val durationCondition: String = getDurationCondition()
            val fileSizeCondition: String = getFileSizeCondition()
            val queryMimeCondition: String = getQueryMimeCondition()
            when (getConfig().chooseMode) {
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
    override val selectionArgs: Array<String>?
        protected get() {
            when (getConfig().chooseMode) {
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
        protected get() = if (TextUtils.isEmpty(getConfig().sortOrder)) ORDER_BY else getConfig().sortOrder

    protected fun parseLocalMedia(data: Cursor, isUsePool: Boolean): LocalMedia? {
        val idColumn = data.getColumnIndexOrThrow(PROJECTION[0])
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
        val dateAdded = data.getLong(dateAddedColumn)
        var mimeType = data.getString(mimeTypeColumn)
        val absolutePath = data.getString(dataColumn)
        val url =
            if (SdkVersionUtils.isQ()) MediaUtils.getRealPathUri(id, mimeType) else absolutePath
        mimeType = if (TextUtils.isEmpty(mimeType)) PictureMimeType.ofJPEG() else mimeType
        // Here, it is solved that some models obtain mimeType and return the format of image / *,
        // which makes it impossible to distinguish the specific type, such as mi 8,9,10 and other models
        if (mimeType.endsWith("image/*")) {
            mimeType = MediaUtils.getMimeTypeFromMediaUrl(absolutePath)
            if (!getConfig().isGif) {
                if (PictureMimeType.isHasGif(mimeType)) {
                    return null
                }
            }
        }
        if (mimeType.endsWith("image/*")) {
            return null
        }
        if (!getConfig().isWebp) {
            if (mimeType.startsWith(PictureMimeType.ofWEBP())) {
                return null
            }
        }
        if (!getConfig().isBmp) {
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
        if (TextUtils.isEmpty(fileName)) {
            fileName = PictureMimeType.getUrlToFileName(absolutePath)
        }
        if (getConfig().isFilterSizeDuration && size > 0 && size < FileSizeUnit.KB) {
            // Filter out files less than 1KB
            return null
        }
        if (PictureMimeType.isHasVideo(mimeType) || PictureMimeType.isHasAudio(mimeType)) {
            if (getConfig().filterVideoMinSecond > 0 && duration < getConfig().filterVideoMinSecond) {
                // If you set the minimum number of seconds of video to display
                return null
            }
            if (getConfig().filterVideoMaxSecond > 0 && duration > getConfig().filterVideoMaxSecond) {
                // If you set the maximum number of seconds of video to display
                return null
            }
            if (getConfig().isFilterSizeDuration && duration <= 0) {
                //If the length is 0, the corrupted video is processed and filtered out
                return null
            }
        }
        val media: LocalMedia = LocalMedia.create()
        media.setId(id)
        media.setBucketId(bucketId)
        media.setPath(url)
        media.setRealPath(absolutePath)
        media.setFileName(fileName)
        media.setParentFolderName(folderName)
        media.setDuration(duration)
        media.setChooseModel(getConfig().chooseMode)
        media.setMimeType(mimeType)
        media.setWidth(width)
        media.setHeight(height)
        media.setSize(size)
        media.setDateAddedTime(dateAdded)
        if (PictureSelectionConfig.onQueryFilterListener != null) {
            if (PictureSelectionConfig.onQueryFilterListener.onFilter(media)) {
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
        imageFolders: MutableList<LocalMediaFolder?>,
    ): LocalMediaFolder {
        for (folder in imageFolders) {
            // Under the same folder, return yourself, otherwise create a new folder
            val name: String = folder.getFolderName()
            if (TextUtils.isEmpty(name)) {
                continue
            }
            if (TextUtils.equals(name, folderName)) {
                return folder
            }
        }
        val newFolder = LocalMediaFolder()
        newFolder.setFolderName(folderName)
        newFolder.setFirstImagePath(firstPath)
        newFolder.setFirstMimeType(firstMimeType)
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
