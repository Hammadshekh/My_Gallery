package com.example.camerax

import com.example.camerax.listener.OnSimpleXPermissionDeniedListener
import com.example.camerax.listener.OnSimpleXPermissionDescriptionListener

object CustomCameraConfig {
    // both can
    const val BUTTON_STATE_BOTH = 0

    //can only take pictures
    const val BUTTON_STATE_ONLY_CAPTURE = 1

    //video only
    const val BUTTON_STATE_ONLY_RECORDER = 2

    //Default maximum recording time
    const val DEFAULT_MAX_RECORD_VIDEO = 60 * 1000

   // Default minimum recording time
    const val DEFAULT_MIN_RECORD_VIDEO = 1500
    const val SP_NAME = "PictureSpUtils"

   // image loading engine
    var imageEngine: CameraImageEngine? = null

    //Custom permission description
    var explainListener: OnSimpleXPermissionDescriptionListener? = null

  //permission denied callback
    var deniedListener: OnSimpleXPermissionDeniedListener? = null

   //release listener
    fun destroy() {
        imageEngine = null
        explainListener = null
        deniedListener = null
    }
}
