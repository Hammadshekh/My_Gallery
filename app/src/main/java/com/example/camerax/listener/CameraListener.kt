package com.example.camerax.listener

interface CameraListener {

    // The photo was returned successfully
    fun onPictureSuccess(url: String)

    // Recording successfully returned
    fun onRecordSuccess(url: String)

    // Error using camera
    fun onError(videoCaptureError: Int, message: String?, cause: Throwable?)
}
