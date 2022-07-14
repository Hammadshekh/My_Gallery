package com.example.selector.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.example.camerax.utils.CameraUtils.CAMERA
import com.example.selector.app.PictureAppMaster
import com.example.selector.basic.PictureContentResolver
import com.example.selector.config.PictureMimeType
import com.example.selector.entity.MediaExtraInfo
import com.example.selector.interfaces.OnCallbackListener
import com.example.selector.threads.PictureThreadUtils
import java.io.*
import java.net.URLConnection
import java.util.*

object MediaUtils {
    /**
     * get uri
     *
     * @param id
     * @return
     */
    fun getRealPathUri(id: Long, mimeType: String?): String {
        val contentUri: Uri = when {
            PictureMimeType.isHasImage(mimeType) -> {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            mimeType?.let { PictureMimeType.isHasVideo(it) } == true -> {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            PictureMimeType.isHasAudio(mimeType) -> {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            else -> {
                MediaStore.Files.getContentUri("external")
            }
        }
        return ContentUris.withAppendedId(contentUri, id).toString()
    }

    /**
     * 获取mimeType
     *
     * @param path
     * @return
     */
    fun getMimeTypeFromMediaUrl(path: String): String {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(path)
        var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.lowercase(Locale.getDefault()))
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = getMimeType(File(path))
        }
        return if (TextUtils.isEmpty(mimeType)) PictureMimeType.MIME_TYPE_JPEG else mimeType!!
    }

    /**
     * 获取mimeType
     *
     * @param url
     * @return
     */
    fun getMimeTypeFromMediaHttpUrl(url: String): String? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        if (url.lowercase(Locale.getDefault()).endsWith(".jpg") || url.lowercase(Locale.getDefault()).endsWith(".jpeg")) {
            return "image/jpeg"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".png")) {
            return "image/png"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".gif")) {
            return "image/gif"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".webp")) {
            return "image/webp"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".bmp")) {
            return "image/bmp"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".mp4")) {
            return "video/mp4"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".avi")) {
            return "video/avi"
        }
        return null
    }

    /**
     * 获取mimeType
     *
     * @param file
     * @return
     */
    private fun getMimeType(file: File): String {
        val fileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }

    /**
     * 是否是长图
     *
     * @param width  图片宽度
     * @param height 图片高度
     * @return
     */
    fun isLongImage(width: Int, height: Int): Boolean {
        return if (width <= 0 || height <= 0) {
            false
        } else height > width * 3
    }

    /**
     * 创建目录名
     *
     * @param absolutePath 资源路径
     * @return
     */
    fun generateCameraFolderName(absolutePath: String): String {
        val folderName: String
        val cameraFile = File(absolutePath)
        folderName = if (cameraFile.parentFile != null) {
            cameraFile.parentFile.name
        } else {
            CAMERA
        }
        return folderName
    }

    /**
     * get Local image width or height
     *
     *
     * Use []
     *
     * @param url
     * @return
     */
    @Deprecated("")
    fun getImageSize(url: String?): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        var inputStream: InputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream = if (url?.let { PictureMimeType.isContent(it) } == true) {
                PictureAppMaster.instance?.appContext.let { it?.let { it1 ->
                    PictureContentResolver.getContentResolverOpenInputStream(it1, Uri.parse(url))
                } }
            } else {
                FileInputStream(url)
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            mediaExtraInfo.width = (options.outWidth)
            mediaExtraInfo.height = (options.outHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            PictureFileUtils.close(inputStream)
        }
        return mediaExtraInfo
    }

    /**
     * get Local image width or height
     *
     * @param url
     * @return
     */
    fun getImageSize(context: Context?, url: String): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        var inputStream: InputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream = if (PictureMimeType.isContent(url)) {
                context?.let { PictureContentResolver.getContentResolverOpenInputStream(it, Uri.parse(url)) }
            } else {
                FileInputStream(url)
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            mediaExtraInfo.width = (options.outWidth)
            mediaExtraInfo.height = (options.outHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            PictureFileUtils.close(inputStream)
        }
        return mediaExtraInfo
    }

    /**
     * get Local video width or height
     *
     * @param context
     * @param url
     * @return
     */
    fun getVideoSize(context: Context?, url: String?): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        val retriever = MediaMetadataRetriever()
        try {
            if (url?.let { PictureMimeType.isContent(it) } == true) {
                retriever.setDataSource(context, Uri.parse(url))
            } else {
                retriever.setDataSource(url)
            }
            val orientation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val width: Int
            val height: Int
            if (TextUtils.equals("90", orientation) || TextUtils.equals("270", orientation)) {
                height =
                    ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                width =
                    ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            } else {
                width =
                    ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                height =
                    ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            }
            mediaExtraInfo.width = width
            mediaExtraInfo.height = height
            mediaExtraInfo.orientation = orientation
            mediaExtraInfo.duration = ValueOf.toLong(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return mediaExtraInfo
    }

    /**
     * get Local video width or height
     *
     * @param context
     * @param url
     * @return
     */
    fun getAudioSize(context: Context?, url: String): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        val retriever = MediaMetadataRetriever()
        try {
            if (PictureMimeType.isContent(url)) {
                retriever.setDataSource(context, Uri.parse(url))
            } else {
                retriever.setDataSource(url)
            }
            mediaExtraInfo.duration = (ValueOf.toLong(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION)))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return mediaExtraInfo
    }

    /**
     * 删除部分手机 拍照在DCIM也生成一张的问题
     *
     * @param id
     */
    fun removeMedia(context: Context, id: Int) {
        try {
            val cr = context.applicationContext.contentResolver
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Images.Media._ID + "=?"
            cr.delete(uri, selection, arrayOf(id.toLong().toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取DCIM文件下最新一条拍照记录
     *
     * @return
     */
    fun getDCIMLastImageId(context: Context, absoluteDir: String): Int {
        var data: Cursor? = null
        return try {
            //selection: 指定查询条件
            val selection = MediaStore.Images.Media.DATA + " like ?"
            //定义selectionArgs：
            val selectionArgs = arrayOf("%$absoluteDir%")
            data = if (SdkVersionUtils.isR()) {
                val queryArgs = createQueryArgsBundle(selection,
                    selectionArgs,
                    1,
                    0,
                    MediaStore.Files.FileColumns._ID + " DESC")
                context.applicationContext.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    queryArgs,
                    null)
            } else {
                val orderBy = MediaStore.Files.FileColumns._ID + " DESC limit 1 offset 0"
                context.applicationContext.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    orderBy)
            }
            if (data != null && data.count > 0 && data.moveToFirst()) {
                val id = data.getInt(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val date = data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                val duration = DateUtils.dateDiffer(date)
                // 最近时间1s以内的图片，可以判定是最新生成的重复照片
                if (duration <= 1) id else -1
            } else {
                -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            data?.close()
        }
    }

    /**
     * getPathMediaBucketId
     *
     * @return
     */
    fun getPathMediaBucketId(context: Context, absolutePath: String): Array<Long> {
        val mediaBucketId = arrayOf(0L, 0L)
        var data: Cursor? = null
        try {
            //selection: 指定查询条件
            val selection = MediaStore.Files.FileColumns.DATA + " like ?"
            //定义selectionArgs：
            val selectionArgs = arrayOf("%$absolutePath%")
            data = if (SdkVersionUtils.isR()) {
                val queryArgs = createQueryArgsBundle(selection,
                    selectionArgs,
                    1,
                    0,
                    MediaStore.Files.FileColumns._ID + " DESC")
                context.contentResolver.query(MediaStore.Files.getContentUri("external"),
                    null,
                    queryArgs,
                    null)
            } else {
                val orderBy = MediaStore.Files.FileColumns._ID + " DESC limit 1 offset 0"
                context.contentResolver.query(MediaStore.Files.getContentUri("external"),
                    null,
                    selection,
                    selectionArgs,
                    orderBy)
            }
            if (data != null && data.count > 0 && data.moveToFirst()) {
                mediaBucketId[0] =
                    data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                mediaBucketId[1] = data.getLong(data.getColumnIndexOrThrow("bucket_id"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            data?.close()
        }
        return mediaBucketId
    }

    /**
     * Key for an SQL style `LIMIT` string that may be present in the
     * query Bundle argument passed to
     * [ContentProvider.query].
     *
     *
     * **Apps targeting [android.os.Build.VERSION_CODES.O] or higher are strongly
     * encourage to use structured query arguments in lieu of opaque SQL query clauses.**
     *
     * @see .QUERY_ARG_LIMIT
     *
     * @see .QUERY_ARG_OFFSET
     */
    const val QUERY_ARG_SQL_LIMIT = "android:query-arg-sql-limit"

    /**
     * R  createQueryArgsBundle
     *
     * @param selection
     * @param selectionArgs
     * @param limitCount
     * @param offset
     * @return
     */
    fun createQueryArgsBundle(
        selection: String?,
        selectionArgs: Array<String>?,
        limitCount: Int,
        offset: Int,
        orderBy: String?,
    ): Bundle {
        val queryArgs = Bundle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, orderBy)
            if (SdkVersionUtils.isR()) {
                queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT,
                    "$limitCount offset $offset")
            }
        }
        return queryArgs
    }

    /**
     * 异步获取视频缩略图地址
     *
     * @param context
     * @param url
     * @param call
     * @return
     */
    fun getAsyncVideoThumbnail(
        context: Context?,
        url: String?,
        call: OnCallbackListener<MediaExtraInfo>,
    ) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<MediaExtraInfo>() {
            override fun doInBackground(): MediaExtraInfo {
                return getVideoThumbnail(context, url!!)
            }

            override fun onSuccess(result: MediaExtraInfo) {
                PictureThreadUtils.cancel(this)
                call.onCall(result)
            }
        })
    }

    /**
     * 获取视频缩略图地址
     *
     * @param context
     * @param url
     * @return
     */
    fun getVideoThumbnail(context: Context?, url: String): MediaExtraInfo {
        var bitmap: Bitmap? = null
        var stream: ByteArrayOutputStream? = null
        var fos: FileOutputStream? = null
        val extraInfo = MediaExtraInfo()
        try {
            val mmr = MediaMetadataRetriever()
            if (PictureMimeType.isContent(url)) {
                mmr.setDataSource(context, Uri.parse(url))
            } else {
                mmr.setDataSource(url)
            }
            bitmap = mmr.frameAtTime
            if (bitmap != null && !bitmap.isRecycled) {
                stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                val videoThumbnailDir: String = context?.let {
                    PictureFileUtils.getVideoThumbnailDir(it)
                }.toString()
                val targetFile =
                    File(videoThumbnailDir, DateUtils.getCreateFileName("vid_") + "_thumb.jpg")
                fos = FileOutputStream(targetFile)
                fos.write(stream.toByteArray())
                fos.flush()
                extraInfo.videoThumbnail = (targetFile.absolutePath)
                extraInfo.width = (bitmap.width)
                extraInfo.height = (bitmap.height)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            PictureFileUtils.close(stream)
            PictureFileUtils.close(fos)
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        return extraInfo
    }

    /**
     * delete camera PATH
     *
     * @param context Context
     * @param path    path
     */
    fun deleteUri(context: Context, path: String) {
        try {
            if (PictureMimeType.isContent(path)) {
                context.contentResolver.delete(Uri.parse(path), null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
