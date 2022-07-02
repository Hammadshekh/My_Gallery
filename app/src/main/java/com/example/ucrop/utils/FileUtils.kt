package com.example.ucrop.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.camerax.utils.CameraUtils.MIME_TYPE_PREFIX_IMAGE
import com.example.ucrop.utils.BitmapLoadUtils.close
import java.io.*
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    /**
     * TAG for log messages.
     */
    private const val TAG = "FileUtils"
    const val GIF = ".gif"
    const val WEBP = ".webp"
    const val JPEG = ".jpeg"

    /**
     * is content://
     *
     * @param url
     * @return
     */
    fun isContent(url: String): Boolean {
        return if (TextUtils.isEmpty(url)) {
            false
        } else url.startsWith("content://")
    }

    /**
     * 是否替换Output Uri
     *
     * @param context
     * @param isForbidGifWebp 是否禁止裁剪Gif或webp
     * @param inputUri        裁剪源文件
     * @param outputUri       裁剪输出目录
     * @return
     */
    fun replaceOutputUri(
        context: Context,
        isForbidGifWebp: Boolean,
        inputUri: Uri,
        outputUri: Uri,
    ): Uri {
        var outputUri = outputUri
        try {
            val postfix = getPostfixDefaultEmpty(context, isForbidGifWebp, inputUri)
            if (TextUtils.isEmpty(postfix)) {
                return outputUri
            } else {
                var outputPath =
                    if (isContent(outputUri.toString())) outputUri.toString() else outputUri.path!!
                val lastIndexOf = outputPath.lastIndexOf(".")
                outputPath = outputPath.replace(outputPath.substring(lastIndexOf), postfix)
                outputUri = if (isContent(outputPath)) Uri.parse(outputPath) else Uri.fromFile(File(
                    outputPath))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputUri
    }

    /**
     * 生成图片的后缀
     *
     * @param context
     * @param isForbidGifWebp 是否禁止裁剪Gif或Webp
     * @param inputUri        裁剪源文件
     * @return
     */
    fun getPostfixDefaultJPEG(context: Context, isForbidGifWebp: Boolean, inputUri: Uri): String {
        var postfix = JPEG
        if (isForbidGifWebp) {
            val mimeType = getMimeTypeFromMediaContentUri(context, inputUri)
            if (isGif(mimeType)) {
                postfix = GIF
            } else if (isWebp(mimeType)) {
                postfix = WEBP
            }
        }
        return postfix
    }

    /**
     * 生成图片的后缀
     *
     * @param context
     * @param isForbidGifWebp 是否禁止裁剪Gif或Webp
     * @param inputUri        裁剪源
     * @return
     */
    fun getPostfixDefaultEmpty(context: Context, isForbidGifWebp: Boolean, inputUri: Uri): String {
        var postfix = ""
        if (isForbidGifWebp) {
            val mimeType = getMimeTypeFromMediaContentUri(context, inputUri)
            if (isGif(mimeType)) {
                postfix = GIF
            } else if (isWebp(mimeType)) {
                postfix = WEBP
            }
        }
        return postfix
    }

    /**
     * 获取裁剪源路径
     *
     * @param inputUri
     * @return
     */
    fun getInputPath(inputUri: Uri): String {
        return if (isContent(inputUri.toString())
            || isHasHttp(inputUri.toString())
        ) inputUri.toString() else inputUri.path!!
    }

    /**
     * isVideo
     *
     * @param url
     * @return
     */
    fun isUrlHasVideo(url: String): Boolean {
        return !TextUtils.isEmpty(url) && url.toLowerCase().endsWith(".mp4")
    }

    /**
     * isVideo
     *
     * @param mimeType
     * @return
     */
    fun isHasVideo(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith("video")
    }

    /**
     * isAudio
     *
     * @param mimeType
     * @return
     */
    fun isHasAudio(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith("audio")
    }
    fun isHasImage(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_IMAGE)
    }
    /**
     * is Network image
     *
     * @param path
     * @return
     */
    fun isHasHttp(path: String): Boolean {
        return if (TextUtils.isEmpty(path)) {
            false
        } else path.startsWith("http") || path.startsWith("https") || path.startsWith("/http") || path.startsWith(
            "/https")
    }

    /**
     * isGif
     *
     * @param mimeType
     * @return
     */
    fun isGif(mimeType: String?): Boolean {
        return mimeType != null && (mimeType == "image/gif" || mimeType == "image/GIF")
    }

    /**
     * isWebp
     *
     * @param mimeType
     * @return
     */
    fun isWebp(mimeType: String?): Boolean {
        return mimeType != null && (mimeType == "image/webp" || mimeType == "image/WEBP")
    }

    /**
     * 获取mimeType
     *
     * @param context
     * @param uri
     * @return
     */
    fun getMimeTypeFromMediaContentUri(context: Context, uri: Uri): String? {
        val mimeType: String?
        mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                .toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase())
        }
        return mimeType
    }

    private val sf = SimpleDateFormat("yyyyMMddHHmmssSSS")
    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + sf.format(millis)
    }

    /**
     * 根据时间戳创建文件名
     *
     * @param prefix 前缀名
     * @return
     */
    @JvmName("getCreateFileName1")
    fun getCreateFileName(): String? {
        val millis = System.currentTimeMillis()
        return sf.format(millis)
    }

    /**
     * 根据时间戳创建文件名
     *
     * @param prefix 前缀名
     * @return
     */
    val createFileName: String
        get() {
            val millis = System.currentTimeMillis()
            return sf.format(millis)
        }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (ex: IllegalArgumentException) {
            Log.i(TAG,
                String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", ex.message))
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    return try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id))
                        getDataColumn(context, contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        Log.i(TAG, e.message!!)
                        null
                    }
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) {
                uri.lastPathSegment
            } else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Copies one file into the other with the given paths.
     * In the event that the paths are the same, trying to copy one file to the other
     * will cause both files to become null.
     * Simply skipping this step if the paths are identical.
     *
     * @param pathFrom Represents the source file
     * @param pathTo   Represents the destination file
     */
    @Throws(IOException::class)
    fun copyFile(pathFrom: String, pathTo: String) {
        if (pathFrom.equals(pathTo, ignoreCase = true)) {
            return
        }
        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            inputChannel = FileInputStream(File(pathFrom)).channel
            outputChannel = FileOutputStream(File(pathTo)).channel
            inputChannel.transferTo(0, inputChannel.size(), outputChannel)
        } finally {
            inputChannel?.close()
            outputChannel?.close()
        }
    }

    /**
     * Copies one file into the other with the given Uris.
     * In the event that the Uris are the same, trying to copy one file to the other
     * will cause both files to become null.
     * Simply skipping this step if the paths are identical.
     *
     * @param context The context from which to require the [android.content.ContentResolver]
     * @param uriFrom Represents the source file
     * @param uriTo   Represents the destination file
     */
    @Throws(IOException::class)
    fun copyFile(context: Context, uriFrom: Uri, uriTo: Uri) {
        if (uriFrom == uriTo) {
            return
        }
        var isFrom: InputStream? = null
        var osTo: OutputStream? = null
        try {
            isFrom = context.contentResolver.openInputStream(uriFrom)
            osTo = context.contentResolver.openOutputStream(uriTo)
            if (isFrom is FileInputStream && osTo is FileOutputStream) {
                val inputChannel = isFrom.channel
                val outputChannel = osTo.channel
                inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            } else {
                throw IllegalArgumentException("The input or output URI don't represent a file. " +
                        "uCrop requires then to represent files in order to work properly.")
            }
        } finally {
            isFrom?.close()
            osTo?.close()
        }
    }

    /**
     * 复制文件
     *
     * @param is 文件输入流
     * @param os 文件输出流
     * @return
     */
    fun writeFileFromIS(`is`: InputStream?, os: OutputStream): Boolean {
        var osBuffer: OutputStream? = null
        var isBuffer: BufferedInputStream? = null
        return try {
            isBuffer = BufferedInputStream(`is`)
            osBuffer = BufferedOutputStream(os)
            val data = ByteArray(1024)
            var len: Int
            while (isBuffer.read(data).also { len = it } != -1) {
                os.write(data, 0, len)
            }
            os.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            close(isBuffer)
            close(osBuffer)
        }
    }
}

