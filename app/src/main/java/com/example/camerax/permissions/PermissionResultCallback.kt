package com.example.camerax.permissions

interface PermissionResultCallback {
    fun onGranted()
    fun onDenied()
}