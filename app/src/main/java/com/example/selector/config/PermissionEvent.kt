package com.example.selector.config

object PermissionEvent {
    const val EVENT_SOURCE_DATA = -1
    const val EVENT_SYSTEM_SOURCE_DATA = -2
    val EVENT_IMAGE_CAMERA: Int = SelectMimeType.ofImage()
    val EVENT_VIDEO_CAMERA: Int = SelectMimeType.ofVideo()
}