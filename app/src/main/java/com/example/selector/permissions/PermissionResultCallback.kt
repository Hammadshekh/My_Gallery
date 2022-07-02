package com.example.selector.permissions

interface PermissionResultCallback {
    fun onGranted()
    fun onDenied()
}