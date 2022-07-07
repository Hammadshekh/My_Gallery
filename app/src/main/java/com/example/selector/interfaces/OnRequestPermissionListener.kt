package com.example.selector.interfaces

interface OnRequestPermissionListener {
    /**
     * Permission request result
     *
     * @param permissionArray
     * @param isResult
     */
    fun onCall(permissionArray: Array<String>, isResult: Boolean)
}