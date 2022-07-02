package com.example.camerax.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList

class PermissionChecker private constructor() {
    fun requestPermissions(
        activity: Activity,
        permissionArray: Array<String>,
        callback: PermissionResultCallback?,
    ) {
        val groupList: MutableList<Array<String>> = ArrayList()
        groupList.add(permissionArray)
        requestPermissions(activity, groupList, REQUEST_CODE, callback)
    }

    fun requestPermissions(
        activity: Activity,
        permissionGroupList: List<Array<String>>,
        callback: PermissionResultCallback?,
    ) {
        requestPermissions(activity, permissionGroupList, REQUEST_CODE, callback)
    }

    private fun requestPermissions(
        activity: Activity,
        permissionGroupList: List<Array<String>>,
        requestCode: Int,
        permissionResultCallback: PermissionResultCallback?,
    ) {
        if (activity is PictureCameraActivity) {
            if (Build.VERSION.SDK_INT < 23) {
                if (permissionResultCallback != null) {
                    permissionResultCallback.onGranted()
                }
                return
            }
            val permissionList: MutableList<String> = ArrayList()
            for (permissionArray in permissionGroupList) {
                for (permission in permissionArray) {
                    if (ContextCompat.checkSelfPermission(activity,
                            permission) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionList.add(permission)
                    }
                }
            }
            if (permissionList.size > 0) {
                (activity as PictureCameraActivity).setPermissionsResultAction(
                    permissionResultCallback)
                val requestArray = arrayOfNulls<String>(permissionList.size)
                permissionList.toArray<String>(requestArray)
                ActivityCompat.requestPermissions(activity, requestArray, requestCode)
            } else {
                if (permissionResultCallback != null) {
                    permissionResultCallback.onGranted()
                }
            }
        }
    }

    fun onRequestPermissionsResult(grantResults: IntArray?, action: PermissionResultCallback) {
        if (PermissionUtil.isAllGranted(grantResults)) {
            action.onGranted()
        } else {
            action.onDenied()
        }
    }

    companion object {
        /**
         * 权限设置
         */
        const val PERMISSION_SETTING_CODE = 1102

        /**
         * 录音权限设置
         */
        const val PERMISSION_RECORD_AUDIO_SETTING_CODE = 1103
        private const val REQUEST_CODE = 10086
        private var mInstance: PermissionChecker? = null
        val instance: PermissionChecker?
            get() {
                if (mInstance == null) {
                    synchronized(PermissionChecker::class.java) {
                        if (mInstance == null) {
                            mInstance = PermissionChecker()
                        }
                    }
                }
                return mInstance
            }

        /**
         * 检查是否有某个权限
         *
         * @param ctx
         * @param permissions
         * @return
         */
        fun checkSelfPermission(ctx: Context, permissions: Array<String?>): Boolean {
            var isAllGranted = true
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(ctx.applicationContext, permission!!)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    isAllGranted = false
                    break
                }
            }
            return isAllGranted
        }
    }
}
