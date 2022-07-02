package com.example.selector.interfaces

import androidx.fragment.app.Fragment

interface OnPermissionsInterceptListener {
    /**
     * Custom Permissions management
     *
     * @param fragment
     * @param permissionArray Permissions array
     * @param call
     */
    fun requestPermission(
        fragment: Fragment?,
        permissionArray: Array<String?>?,
        call: OnRequestPermissionListener?,
    )

    /**
     * Verify permission application status
     *
     * @param fragment
     * @param permissionArray
     * @return
     */
    fun hasPermissions(fragment: Fragment?, permissionArray: Array<String?>?): Boolean
}
