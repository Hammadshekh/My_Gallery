package com.example.selector.interfaces

import androidx.fragment.app.Fragment

interface OnCameraInterceptListener {
    /**
     * Intercept camera click events, and users can implement their own camera framework
     *
     * @param fragment    fragment    Fragment to receive result
     * @param cameraMode  Camera mode
     * []
     *
     *
     * If you use your own camera, you need to put the result URL
     * Intent.putExtra(MediaStore.EXTRA_OUTPUT, URI) after taking photos
     *
     * @param requestCode requestCode for result
     */
    fun openCamera(fragment: Fragment?, cameraMode: Int, requestCode: Int)
}
