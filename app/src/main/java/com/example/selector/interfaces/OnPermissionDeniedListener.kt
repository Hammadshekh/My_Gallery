package com.example.selector.interfaces

import androidx.fragment.app.Fragment

interface OnPermissionDeniedListener {
    /**
     * Permission denied
     *
     * @param permissionArray Permission
     * @param requestCode     Jump to the  [# requestCode][.startActivityForResult] used in system settings
     * @param call            if call.onCall(true);Can follow internal logic，Otherwise, press the user's own
     */
    fun onDenied(
        fragment: Fragment?,
        permissionArray: Array<String?>?,
        requestCode: Int,
        call: OnCallbackListener<Boolean?>?,
    )
}