package com.example.camerax.utils

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils

object CameraUtils {
    const val TYPE_IMAGE = 1
    const val TYPE_VIDEO = 2
    const val CAMERA = "Camera"
    const val MIME_TYPE_PREFIX_IMAGE = "image"
    private const val MIME_TYPE_PREFIX_VIDEO = "video"
    const val MIME_TYPE_IMAGE = "image/jpeg"
    const val MIME_TYPE_VIDEO = "video/mp4"
    private const val DCIM_CAMERA = "DCIM/Camera"
    const val JPEG = ".jpeg"
    const val MP4 = ".mp4"

    // Construct the ContentValues ​​ of the image, which is used to save the photo after taking the photo
    // cameraFileName Resource Name
    // mimeType       Resource Type
    //return

    fun buildImageContentValues(cameraFileName: String, mimeType: String): ContentValues {
        val time = System.currentTimeMillis().toString()
        //ContentValues ​​ is the data information we want this record to contain when it is created
        val values = ContentValues(3)
        if (TextUtils.isEmpty(cameraFileName)) {
            values.put(MediaStore.Images.Media.DISPLAY_NAME, DateUtils.getCreateFileName("IMG_"))
        } else {
            if (cameraFileName.lastIndexOf(".") == -1) {
                values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    DateUtils.getCreateFileName("IMG_"))
            } else {
                val suffix = cameraFileName.substring(cameraFileName.lastIndexOf("."))
                val fileName = cameraFileName.replace(suffix.toRegex(), "")
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            }
        }
        values.put(MediaStore.Images.Media.MIME_TYPE,
            if (TextUtils.isEmpty(mimeType) || mimeType.startsWith(
                    MIME_TYPE_PREFIX_VIDEO)
            ) MIME_TYPE_IMAGE else mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, time)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, DCIM_CAMERA)
        }
        return values
    }

    //Build the ContentValues ​​of the video to save the photo after taking the photo
    // cameraFileName Resource Name
    //mimeType       Resource Type
    //return

    fun buildVideoContentValues(cameraFileName: String, mimeType: String): ContentValues {
        val time = System.currentTimeMillis().toString()
        // ContentValues ​​is the data information we want this record to contain when it is created
        val values = ContentValues(3)
        if (TextUtils.isEmpty(cameraFileName)) {
            values.put(MediaStore.Video.Media.DISPLAY_NAME, DateUtils.getCreateFileName("VID_"))
        } else {
            if (cameraFileName.lastIndexOf(".") == -1) {
                values.put(MediaStore.Video.Media.DISPLAY_NAME, DateUtils.getCreateFileName("VID_"))
            } else {
                val suffix = cameraFileName.substring(cameraFileName.lastIndexOf("."))
                val fileName = cameraFileName.replace(suffix.toRegex(), "")
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            }
        }
        values.put(MediaStore.Video.Media.MIME_TYPE, if (TextUtils.isEmpty(mimeType)
            || mimeType.startsWith(MIME_TYPE_PREFIX_IMAGE)
        ) MIME_TYPE_VIDEO else mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.DATE_TAKEN, time)
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }
        return values
    }
}
