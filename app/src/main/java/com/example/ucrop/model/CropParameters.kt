package com.example.ucrop.model

import android.graphics.Bitmap.CompressFormat
import android.net.Uri

class CropParameters(
    val maxResultImageSizeX: Int, val maxResultImageSizeY: Int,
    val compressFormat: CompressFormat, val compressQuality: Int,
    val imageInputPath: String, val imageOutputPath: String, val exifInfo: ExifInfo,
) {
    var contentImageInputUri: Uri? = null
    var contentImageOutputUri: Uri? = null

}

