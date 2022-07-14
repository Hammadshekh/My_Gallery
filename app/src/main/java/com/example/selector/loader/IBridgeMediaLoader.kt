package com.example.selector.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import com.example.selector.config.PictureMimeType
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.interfaces.OnQueryAlbumListener
import com.example.selector.interfaces.OnQueryAllAlbumListener
import com.example.selector.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.entity.LocalMedia
import java.util.*
import kotlin.math.max

abstract class IBridgeMediaLoader {
    private var context: Context? = null
        private set
    private var mConfig: PictureSelectionConfig? = null

    /**
     * init config
     *
     * @param context
     * @param config  [PictureSelectionConfig]
     */
    fun initConfig(context: Context?, config: PictureSelectionConfig) {
        this.context = context
        mConfig = config
    }

     val config: PictureSelectionConfig
         get() = mConfig!!

    /**
     * query album cover
     *
     * @param bucketId
     */
    abstract fun getAlbumFirstCover(bucketId: Long): String?

    /**
     * query album list
     */
    abstract fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder>)

    /**
     * page query specified contents
     *
     * @param bucketId
     * @param page
     * @param pageSize
     */
    abstract fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia>,
    )

    /**
     * query specified contents
     */
    abstract fun loadOnlyInAppDirAllMedia(query: OnQueryAlbumListener<LocalMediaFolder>)

    /**
     * A filter declaring which rows to return,
     * formatted as an SQL WHERE clause (excluding the WHERE itself).
     * Passing null will return all rows for the given URI.
     */
    private  val selection: String? = null

    /**
     * You may include ?s in selection, which will be replaced by the values from selectionArgs,
     * in the order that they appear in the selection. The values will be bound as Strings.
     */
    private val selectionArgs: Array<String>? = null

    /**
     * How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing null will use the default sort order, which may be unordered.
     */
    private  val sortOrder: String? = null

    /**
     * parse LocalMedia
     *
     * @param data      Cursor
     * @param isUsePool object pool
     */
     abstract fun parseLocalMedia(data: Cursor?, isUsePool: Boolean): LocalMedia?

    /**
     * Get video (maximum or minimum time)
     *
     * @return
     */
    private val durationCondition: String
         get() {
            val maxS =
                if (config.filterVideoMaxSecond == 0) Long.MAX_VALUE else config.filterVideoMaxSecond
            return java.lang.String.format(Locale.CHINA,
                "%d <%s $COLUMN_DURATION and $COLUMN_DURATION <= %d",
                config.filterVideoMinSecond?.let { max(0 , it) },
                "=",
                maxS)
        }

    /**
     * Get media size (maxFileSize or miniFileSize)
     *
     * @return
     */
    private val fileSizeCondition: String
         get() {
            val maxS =
                if (config.filterMaxFileSize.equals(0)) Long.MAX_VALUE else config.filterMaxFileSize
            return java.lang.String.format(Locale.CHINA,
                "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
                Math.max(0, config.filterMinFileSize),
                "=",
                maxS)
        }

    /**
     * getQueryMimeCondition
     *
     * @return
     */
    private val queryMimeCondition: String
         get() {
            val filters: List<String> = config.queryOnlyList!!
            val filterSet = HashSet(filters)
            val iterator: Iterator<String> = filterSet.iterator()
            val stringBuilder = StringBuilder()
            var index = -1
            while (iterator.hasNext()) {
                val value = iterator.next()
                if (TextUtils.isEmpty(value)) {
                    continue
                }
                if (config.chooseMode == SelectMimeType.ofVideo()) {
                    if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE) || value.startsWith(
                            PictureMimeType.MIME_TYPE_PREFIX_AUDIO)
                    ) {
                        continue
                    }
                } else if (config.chooseMode == SelectMimeType.ofImage()) {
                    if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO) || value.startsWith(
                            PictureMimeType.MIME_TYPE_PREFIX_VIDEO)
                    ) {
                        continue
                    }
                } else if (config.chooseMode == SelectMimeType.ofAudio()) {
                    if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO) || value.startsWith(
                            PictureMimeType.MIME_TYPE_PREFIX_IMAGE)
                    ) {
                        continue
                    }
                }
                index++
                stringBuilder.append(if (index == 0) " AND " else " OR ")
                    .append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(value)
                    .append("'")
            }
            if (config.chooseMode != SelectMimeType.ofVideo()) {
                if (config.isGif == true && !filterSet.contains(PictureMimeType.ofGIF())) {
                    stringBuilder.append(NOT_GIF)
                }
            }
            return stringBuilder.toString()
        }

    companion object {
         val TAG = IBridgeMediaLoader::class.java.simpleName
         val QUERY_URI = MediaStore.Files.getContentUri("external")
        const val ORDER_BY = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
         const val NOT_GIF =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/gif')"
         const val GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id"
         const val COLUMN_COUNT = "count"
         const val COLUMN_BUCKET_ID = "bucket_id"
         const val COLUMN_DURATION = "duration"
         const val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"
         const val COLUMN_ORIENTATION = "orientation"
         const val MAX_SORT_SIZE = 60

        /**
         * A list of which columns to return. Passing null will return all columns, which is inefficient.
         */
         val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION)

        /**
         * A list of which columns to return. Passing null will return all columns, which is inefficient.
         */
        val ALL_PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION,
            "COUNT(*) AS " + COLUMN_COUNT)
    }
}
