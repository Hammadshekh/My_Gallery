package com.example.camerax.listener

import android.content.Context

interface OnSimpleXPermissionDeniedListener {
    /**
     * Permission denied
     *
     * @param permission  Permission
     * @param requestCode Jump to the  [# requestCode][.startActivityForResult] used in system settings
     */
    fun onDenied(context: Context?, permission: String?, requestCode: Int)
}