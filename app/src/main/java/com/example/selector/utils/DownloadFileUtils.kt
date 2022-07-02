package com.example.selector.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.URL

object DownloadFileUtils {
    /**
     * 保存文件
     *
     * @param context  上下文
     * @param path     文件路径
     * @param mimeType 文件类型
     * @param listener 结果回调监听
     */
    fun saveLocalFile(
        context: Context, path: String?, mimeType: String,
        listener: OnCallbackListener<String?>?,
    ) {
        PictureThreadUtils.executeByIo(object : SimpleTask<String?>() {
            fun doInBackground(): String? {
                try {
                    val uri: Uri?
                    val contentValues = ContentValues()
                    val time: String = ValueOf.toString(System.currentTimeMillis())
                    uri = if (PictureMimeType.isHasAudio(mimeType)) {
                        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME,
                            DateUtils.getCreateFileName("AUD_"))
                        contentValues.put(MediaStore.Audio.Media.MIME_TYPE,
                            if (TextUtils.isEmpty(mimeType)
                                || mimeType.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO)
                                || mimeType.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE)
                            ) PictureMimeType.MIME_TYPE_AUDIO else mimeType)
                        if (SdkVersionUtils.isQ()) {
                            contentValues.put(MediaStore.Audio.Media.DATE_TAKEN, time)
                            contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_MUSIC)
                        } else {
                            val dir = if (TextUtils.equals(Environment.getExternalStorageState(),
                                    Environment.MEDIA_MOUNTED)
                            ) Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) else context.getExternalFilesDir(
                                Environment.DIRECTORY_MUSIC)!!
                            contentValues.put(MediaStore.MediaColumns.DATA,
                                dir.absolutePath + File.separator
                                        + DateUtils.getCreateFileName("AUD_") + PictureMimeType.AMR)
                        }
                        context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            contentValues)
                    } else if (PictureMimeType.isHasVideo(mimeType)) {
                        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME,
                            DateUtils.getCreateFileName("VID_"))
                        contentValues.put(MediaStore.Video.Media.MIME_TYPE,
                            if (TextUtils.isEmpty(mimeType)
                                || mimeType.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO)
                                || mimeType.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE)
                            ) PictureMimeType.MIME_TYPE_VIDEO else mimeType)
                        if (SdkVersionUtils.isQ()) {
                            contentValues.put(MediaStore.Video.Media.DATE_TAKEN, time)
                            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_MOVIES)
                        } else {
                            val dir = if (TextUtils.equals(Environment.getExternalStorageState(),
                                    Environment.MEDIA_MOUNTED)
                            ) Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) else context.getExternalFilesDir(
                                Environment.DIRECTORY_MOVIES)!!
                            contentValues.put(MediaStore.MediaColumns.DATA,
                                dir.absolutePath + File.separator
                                        + DateUtils.getCreateFileName("VID_") + PictureMimeType.MP4)
                        }
                        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues)
                    } else {
                        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,
                            DateUtils.getCreateFileName("IMG_"))
                        contentValues.put(MediaStore.Images.Media.MIME_TYPE,
                            if (TextUtils.isEmpty(mimeType)
                                || mimeType.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO)
                                || mimeType.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO)
                            ) PictureMimeType.MIME_TYPE_IMAGE else mimeType)
                        if (SdkVersionUtils.isQ()) {
                            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, time)
                            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                                PictureMimeType.DCIM)
                        } else {
                            if (PictureMimeType.isHasGif(mimeType) || PictureMimeType.isUrlHasGif(
                                    path)
                            ) {
                                val dir =
                                    if (TextUtils.equals(Environment.getExternalStorageState(),
                                            Environment.MEDIA_MOUNTED)
                                    ) Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) else context.getExternalFilesDir(
                                        Environment.DIRECTORY_PICTURES)!!
                                contentValues.put(MediaStore.MediaColumns.DATA,
                                    dir.absolutePath + File.separator
                                            + DateUtils.getCreateFileName("IMG_") + PictureMimeType.GIF)
                            }
                        }
                        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues)
                    }
                    if (uri != null) {
                        val inputStream: InputStream
                        inputStream = if (PictureMimeType.isHasHttp(path)) {
                            URL(path).openStream()
                        } else {
                            if (PictureMimeType.isContent(path)) {
                                PictureContentResolver.getContentResolverOpenInputStream(context,
                                    Uri.parse(path))
                            } else {
                                FileInputStream(path)
                            }
                        }
                        val outputStream: OutputStream =
                            PictureContentResolver.getContentResolverOpenOutputStream(context, uri)
                        val bufferCopy: Boolean =
                            PictureFileUtils.writeFileFromIS(inputStream, outputStream)
                        if (bufferCopy) {
                            return PictureFileUtils.getPath(context, uri)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            fun onSuccess(result: String?) {
                PictureThreadUtils.cancel(this)
                if (listener != null) {
                    listener.onCall(result)
                }
            }
        })
    }
}
