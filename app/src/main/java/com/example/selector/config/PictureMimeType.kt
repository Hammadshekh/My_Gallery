package com.example.selector.config

import android.text.TextUtils
import java.lang.Exception
import java.util.*

open class PictureMimeType {

    /**
     * isGif
     *
     * @param mimeType
     * @return
     */
    fun isHasGif(mimeType: String?): Boolean {
        return mimeType != null && (mimeType == "image/gif" || mimeType == "image/GIF")
    }

    /**
     * isGif
     *
     * @param url
     * @return
     */
    fun isUrlHasGif(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).endsWith(".gif")
    }

    /**
     * is has image
     *
     * @param url
     * @return
     */
    fun isUrlHasImage(url: String): Boolean {
        return (url.lowercase(Locale.getDefault()).endsWith(".jpg")
                || url.lowercase(Locale.getDefault()).endsWith(".jpeg")
                || url.lowercase(Locale.getDefault()).endsWith(".png")
                || url.lowercase(Locale.getDefault()).endsWith(".heic"))
    }

    /**
     * isWebp
     *
     * @param mimeType
     * @return
     */
    fun isHasWebp(mimeType: String?): Boolean {
        return mimeType != null && mimeType.equals("image/webp", ignoreCase = true)
    }

    /**
     * isWebp
     *
     * @param url
     * @return
     */
    fun isUrlHasWebp(url: String): Boolean {
        return url.toLowerCase().endsWith(".webp")
    }

    /**
     * isVideo
     *
     * @param mimeType
     * @return
     */
    fun isHasVideo(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_VIDEO)
    }

    /**
     * isVideo
     *
     * @param url
     * @return
     */
    fun isUrlHasVideo(url: String): Boolean {
        return url.toLowerCase().endsWith(".mp4")
    }

    /**
     * isAudio
     *
     * @param mimeType
     * @return
     */
    fun isHasAudio(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_AUDIO)
    }

    /**
     * isAudio
     *
     * @param url
     * @return
     */
    fun isUrlHasAudio(url: String): Boolean {
        return url.toLowerCase().endsWith(".amr") || url.toLowerCase().endsWith(".mp3")
    }

    /**
     * isImage
     *
     * @param mimeType
     * @return
     */
     fun isHasImage(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_IMAGE)
    }
    /**
     * isHasBmp
     *
     * @param mimeType
     * @return
     */
    fun isHasBmp(mimeType: String): Boolean {
        return if (TextUtils.isEmpty(mimeType)) {
            false
        } else ofBMP()?.let { mimeType.startsWith(it) } == true
                || ofXmsBMP()?.let { mimeType.startsWith(it) } == true
                || ofWapBMP()?.let { mimeType.startsWith(it) } == true
    }

    /**
     * Determine if it is JPG.
     *
     * @param is image file mimeType
     */
    fun isJPEG(mimeType: String): Boolean {
        return if (TextUtils.isEmpty(mimeType)) {
            false
        } else mimeType.startsWith(MIME_TYPE_JPEG) || mimeType.startsWith(MIME_TYPE_JPG)
    }

    /**
     * Determine if it is JPG.
     *
     * @param is image file mimeType
     */
    fun isJPG(mimeType: String): Boolean {
        return if (TextUtils.isEmpty(mimeType)) {
            false
        } else mimeType.startsWith(MIME_TYPE_JPG)
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
        } else path.startsWith("http") || path.startsWith("https")
    }

    /**
     * Is it the same type
     *
     * @param oldMimeType 已选的资源类型
     * @param newMimeType 当次选中的资源类型
     * @return
     */
    fun isMimeTypeSame(oldMimeType: String, newMimeType: String): Boolean {
        return if (TextUtils.isEmpty(oldMimeType)) {
            true
        } else getMimeType(oldMimeType) == getMimeType(newMimeType)
    }

    /**
     * Picture or video
     *
     * @return
     */
    fun getMimeType(mimeType: String): Int {
        if (TextUtils.isEmpty(mimeType)) {
            return SelectMimeType.TYPE_IMAGE
        }
        return if (mimeType.startsWith(MIME_TYPE_PREFIX_VIDEO)) {
            SelectMimeType.TYPE_VIDEO
        } else if (mimeType.startsWith(MIME_TYPE_PREFIX_AUDIO)) {
            SelectMimeType.TYPE_AUDIO
        } else {
            SelectMimeType.TYPE_IMAGE
        }
    }

    /**
     * Get image suffix
     *
     * @param mineType
     * @return
     */
    fun getLastImgSuffix(mineType: String): String? {
        return try {
            mineType.substring(mineType.lastIndexOf("/")).replace("/", ".")
        } catch (e: Exception) {
            e.printStackTrace()
            JPG
        }
    }

    /**
     * Get url to file name
     *
     * @param path
     * @return
     */
    fun getUrlToFileName(path: String): String? {
        var result: String? = ""
        try {
            val lastIndexOf = path.lastIndexOf("/")
            if (lastIndexOf != -1) {
                result = path.substring(lastIndexOf + 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
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


    fun ofPNG(): String? {
        return MIME_TYPE_PNG
    }

    fun ofJPEG(): String? {
        return MIME_TYPE_JPEG
    }

    fun ofBMP(): String? {
        return MIME_TYPE_BMP
    }

    fun ofXmsBMP(): String? {
        return MIME_TYPE_XMS_BMP
    }

    fun ofWapBMP(): String? {
        return MIME_TYPE_WAP_BMP
    }

    fun ofGIF(): String? {
        return MIME_TYPE_GIF
    }

    fun ofWEBP(): String? {
        return MIME_TYPE_WEBP
    }

    fun of3GP(): String? {
        return MIME_TYPE_3GP
    }

    fun ofMP4(): String? {
        return MIME_TYPE_MP4
    }

    fun ofMPEG(): String? {
        return MIME_TYPE_MPEG
    }

    fun ofAVI(): String? {
        return MIME_TYPE_AVI
    }


    val MIME_TYPE_IMAGE = "image/jpeg"
    val MIME_TYPE_VIDEO = "video/mp4"
    val MIME_TYPE_AUDIO = "audio/mpeg"
    val MIME_TYPE_AUDIO_AMR = "audio/amr"

    val MIME_TYPE_PREFIX_IMAGE = "image"
    val MIME_TYPE_PREFIX_VIDEO = "video"
    val MIME_TYPE_PREFIX_AUDIO = "audio"

    private val MIME_TYPE_PNG = "image/png"
    val MIME_TYPE_JPEG = "image/jpeg"
    private val MIME_TYPE_JPG = "image/jpg"
    private val MIME_TYPE_BMP = "image/bmp"
    private val MIME_TYPE_XMS_BMP = "image/x-ms-bmp"
    private val MIME_TYPE_WAP_BMP = "image/vnd.wap.wbmp"
    private val MIME_TYPE_GIF = "image/gif"
    private val MIME_TYPE_WEBP = "image/webp"

    private val MIME_TYPE_3GP = "video/3gp"
    private val MIME_TYPE_MP4 = "video/mp4"
    private val MIME_TYPE_MPEG = "video/mpeg"
    private val MIME_TYPE_AVI = "video/avi"


    val JPEG = ".jpeg"

    val JPG = ".jpg"

    val PNG = ".png"

    val WEBP = ".webp"

    val GIF = ".gif"

    val BMP = ".bmp"

    val AMR = ".amr"

    val WAV = ".wav"

    val MP3 = ".mp3"

    val MP4 = ".mp4"

    val AVI = ".avi"

    val JPEG_Q = "image/jpeg"

    val PNG_Q = "image/png"

    val MP4_Q = "video/mp4"

    val AVI_Q = "video/avi"

    val AMR_Q = "audio/amr"

    val WAV_Q = "audio/x-wav"
}
