package com.example.ucrop.callback

import android.graphics.Bitmap
import android.net.Uri
import com.example.ucrop.model.ExifInfo
import java.lang.Exception


interface BitmapLoadCallback {
    fun onBitmapLoaded(bitmap: Bitmap, exifInfo: ExifInfo, imageInputUri: Uri, imageOutputUri: Uri?)
    fun onFailure(bitmapWorkerException: Exception)
}