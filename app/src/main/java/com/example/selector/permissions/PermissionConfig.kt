package com.example.selector.permissions

import android.Manifest

object PermissionConfig {
    /**
     * 当前申请权限
     */
    var CURRENT_REQUEST_PERMISSION: Array<String?>? = null

    /**
     * 读写权限
     */
    val READ_WRITE_EXTERNAL_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /**
     * 写入权限
     */
    val WRITE_EXTERNAL_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /**
     * 相机权限
     */
    val CAMERA = arrayOf(Manifest.permission.CAMERA)
}
