package com.example.camerax.listener

interface CameraListener {
    /**
     * 拍照成功返回
     *
     * @param url
     */
    fun onPictureSuccess(url: String)

    /**
     * 录像成功返回
     *
     * @param url
     */
    fun onRecordSuccess(url: String)

    /**
     * 使用相机出错
     *
     * @param file
     */
    fun onError(videoCaptureError: Int, message: String?, cause: Throwable?)
}
