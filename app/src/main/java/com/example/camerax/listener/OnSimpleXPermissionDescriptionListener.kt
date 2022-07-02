package com.example.camerax.listener

import android.content.Context
import android.view.ViewGroup

interface OnSimpleXPermissionDescriptionListener {
    /**
     * Permission description
     *
     * @param context
     * @param permission
     */
    fun onPermissionDescription(context: Context?, viewGroup: ViewGroup?, permission: String?)

    /**
     * onDismiss
     */
    fun onDismiss(viewGroup: ViewGroup?)
}
