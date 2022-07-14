package com.example.camerax.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import java.io.*
import java.util.*

object FileUtils {
    const val POSTFIX = ".jpeg"
    const val POST_VIDEO = ".mp4"

    /**
     * @param context
     * @param chooseMode
     * @param format
     * @param outCameraDirectory
     * @return
     */
    fun createCameraFile(
        context: Context,
        chooseMode: Int,
        fileName: String,
        format: String,
        outCameraDirectory: String,
    ): File {
        return createMediaFile(context, chooseMode, fileName, format, outCameraDirectory)
    }

    /**
     * 创建文件
     *
     * @param context
     * @param chooseMode
     * @param fileName
     * @param format
     * @param outCameraDirectory
     * @return
     */
    private fun createMediaFile(
        context: Context,
        chooseMode: Int,
        fileName: String,
        format: String,
        outCameraDirectory: String,
    ): File {
        return createOutFile(context, chooseMode, fileName, format, outCameraDirectory)
    }

    //Create a file
    // ctx context
    // chooseMode select mode
    // fileName file name
    // format file format
    // outCameraDirectory output directory
    //return
    private fun createOutFile(
        ctx: Context,
        chooseMode: Int,
        fileName: String,
        format: String,
        outCameraDirectory: String,
    ): File {
        val context = ctx.applicationContext
        val folderDir: File
        if (TextUtils.isEmpty(outCameraDirectory)) {
            // There is no custom photo storage path outside, use the default
            val rootDir: File?
            if (TextUtils.equals(Environment.MEDIA_MOUNTED,
                    Environment.getExternalStorageState())
            ) {
                rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                folderDir =
                    File(rootDir.absolutePath + File.separator + CameraUtils.CAMERA + File.separator)
            } else {
                rootDir = getRootDirFile(context, chooseMode)
                folderDir = File(rootDir!!.absolutePath + File.separator)
            }
            if (!rootDir!!.exists()) {
                rootDir.mkdirs()
            }
        } else {
            // custom storage path
            folderDir = File(outCameraDirectory)
            if (!Objects.requireNonNull(folderDir.parentFile).exists()) {
                folderDir.parentFile.mkdirs()
            }
        }
        if (!folderDir.exists()) {
            folderDir.mkdirs()
        }
        val isOutFileNameEmpty = TextUtils.isEmpty(fileName)
        if (chooseMode == CameraUtils.TYPE_VIDEO) {
            val newFileVideoName =
                if (isOutFileNameEmpty) DateUtils.getCreateFileName("VID_") + POST_VIDEO else fileName
            return File(folderDir, newFileVideoName)
        }
        val suffix = if (TextUtils.isEmpty(format)) POSTFIX else format
        val newFileImageName =
            if (isOutFileNameEmpty) DateUtils.getCreateFileName("IMG_") + suffix else fileName
        return File(folderDir, newFileImageName)
    }

    // file root directory
    // context
    // type
    // return

    private fun getRootDirFile(context: Context, type: Int): File? {
        return if (type == CameraUtils.TYPE_VIDEO) {
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        } else context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    // Create a temporary path, mainly to solve the prompt that the album picture is deleted after the Huawei mobile phone gives up taking pictures
    // isVideo
    //return

    fun createTempFile(context: Context, isVideo: Boolean): File {
        val externalFilesDir = context.getExternalFilesDir("")
        val tempCameraFile = File(externalFilesDir!!.absolutePath, ".TemporaryCamera")
        if (!tempCameraFile.exists()) {
            tempCameraFile.mkdirs()
        }
        val fileName: String =
            System.currentTimeMillis().toString() + if (isVideo) CameraUtils.MP4 else CameraUtils.JPEG
        return File(tempCameraFile.absolutePath, fileName)
    }

    /**
     * 生成uri
     *
     * @param context
     * @param cameraFile
     * @return
     */
    fun parUri(context: Context, cameraFile: File?): Uri {
        val imageUri: Uri
        val authority = context.packageName + ".luckProvider"
        imageUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            FileProvider.getUriForFile(context, authority, cameraFile!!)
        } else {
            Uri.fromFile(cameraFile)
        }
        return imageUri
    }

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
     * 文件复制
     *
     * @param context
     * @param originalPath
     * @param newPath
     * @return
     */
    fun copyPath(context: Context, originalPath: String, newPath: String?): Boolean {
        var fos: FileOutputStream? = null
        var stream: ByteArrayOutputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(originalPath, options)
            options.inSampleSize = BitmapUtils.computeSize(options.outWidth, options.outHeight)
            options.inJustDecodeBounds = false
            val newBitmap =
                BitmapUtils.toHorizontalMirror(BitmapFactory.decodeFile(originalPath, options))
            stream = ByteArrayOutputStream()
            newBitmap.compress(if (newBitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                90,
                stream)
            newBitmap.recycle()
            fos = FileOutputStream(newPath)
            fos.write(stream.toByteArray())
            fos.flush()
            deleteFile(context, originalPath)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            close(fos)
            close(stream)
        }
        return false
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

    /**
     * delete camera PATH
     *
     * @param context Context
     * @param path    path
     */
    fun deleteFile(context: Context, path: String) {
        try {
            if (isContent(path)) {
                context.contentResolver.delete(Uri.parse(path), null, null)
            } else {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close(c: Closeable?) {
        // java.lang.IncompatibleClassChangeError: interface not implemented
        if (c is Closeable) {
            try {
                c.close()
            } catch (e: Exception) {
                // silence
            }
        }
    }
}
