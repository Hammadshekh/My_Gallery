package com.example.selector.interfaces

import androidx.fragment.app.Fragment

interface OnPermissionDescriptionListener {
    /**
     * Permission description
     *
     * @param fragment
     * @param permissionArray
     */
    fun onPermissionDescription(fragment: Fragment?, permissionArray: Array<String>)

    /**
     * onDismiss
     */
    fun onDismiss(fragment: Fragment?)
}