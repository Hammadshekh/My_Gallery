package com.example.selector.config

import com.example.ucrop.BuildConfig.LIBRARY_PACKAGE_NAME

object PictureConfig {
    const val SP_NAME = "PictureSpUtils"
    private val KEY: String = LIBRARY_PACKAGE_NAME
    const val EXTRA_RESULT_SELECTION = "extra_result_media"
    val EXTRA_PICTURE_SELECTOR_CONFIG = KEY + ".PictureSelectorConfig"
    const val CAMERA_FACING = "android.intent.extras.CAMERA_FACING"
    val EXTRA_ALL_FOLDER_SIZE = KEY + ".all_folder_size"
    const val EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture"
    val EXTRA_EXTERNAL_PREVIEW = KEY + ".external_preview"
    val EXTRA_DISPLAY_CAMERA = KEY + ".display_camera"
    val EXTRA_BOTTOM_PREVIEW = KEY + ".bottom_preview"
    val EXTRA_CURRENT_ALBUM_NAME = KEY + ".current_album_name"
    val EXTRA_CURRENT_PAGE = KEY + ".current_page"
    val EXTRA_CURRENT_BUCKET_ID = KEY + ".current_bucketId"
    val EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE = KEY + ".external_preview_display_delete"
    val EXTRA_PREVIEW_CURRENT_POSITION = KEY + ".current_preview_position"
    val EXTRA_PREVIEW_CURRENT_ALBUM_TOTAL = KEY + ".current_album_total"
    val EXTRA_CURRENT_CHOOSE_MODE = KEY + ".current_choose_mode"
    val EXTRA_MODE_TYPE_SOURCE = KEY + ".mode_type_source"
    const val MAX_PAGE_SIZE = 60
    const val MIN_PAGE_SIZE = 10
    const val CAMERA_BEFORE = 1
    const val DEFAULT_SPAN_COUNT = 4
    const val REQUEST_CAMERA = 909
    const val CHOOSE_REQUEST = 188
    const val REQUEST_GO_SETTING = 1102
    const val ALL = -1
    const val UNSET = -1
    const val MODE_TYPE_SYSTEM_SOURCE = 1
    const val MODE_TYPE_EXTERNAL_PREVIEW_SOURCE = 2
}
