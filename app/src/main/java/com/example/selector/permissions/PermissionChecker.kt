package com.example.selector.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.selector.basic.PictureCommonFragment
import com.example.selector.utils.ActivityCompatHelper
import java.util.ArrayList

class PermissionChecker () {
    fun requestPermissions(
        fragment: Fragment,
        permissionArray: Array<String>,
        callback: PermissionResultCallback?,
    ) {
        val groupList: MutableList<Array<String>> = ArrayList()
        groupList.add(permissionArray)
        requestPermissions(fragment, groupList, REQUEST_CODE, callback)
    }

    fun requestPermissions(
        fragment: Fragment,
        permissionGroupList: List<Array<String>>,
        callback: PermissionResultCallback?,
    ) {
        requestPermissions(fragment, permissionGroupList, REQUEST_CODE, callback)
    }

    private fun requestPermissions(
        fragment: Fragment,
        permissionGroupList: List<Array<String>>,
        requestCode: Int,
        permissionResultCallback: PermissionResultCallback?,
    ) {
        if (ActivityCompatHelper.isDestroy(fragment.activity)) {
            return
        }
        if (fragment is PictureCommonFragment) {
            if (Build.VERSION.SDK_INT < 23) {
                if (permissionResultCallback != null) {
                    permissionResultCallback.onGranted()
                }
                return
            }
            val activity: Activity? = fragment.activity
            val permissionList: ArrayList<String> = ArrayList()
            for (permissionArray in permissionGroupList) {
                for (permission in permissionArray) {
                    if (ContextCompat.checkSelfPermission(activity!!,
                            permission) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionList.add(permission)
                    }
                }
            }
            if (permissionList.size > 0) {
                (fragment).setPermissionsResultAction(
                    permissionResultCallback)
                val requestArray = arrayOfNulls<String>(permissionList.size)
                permissionList.toArray<String>(requestArray)
                fragment.requestPermissions(requestArray, requestCode)
                ActivityCompat.requestPermissions(activity!!, requestArray, requestCode)
            } else {
                permissionResultCallback?.onGranted()
            }
        }
    }

    fun onRequestPermissionsResult(grantResults: IntArray?, action: PermissionResultCallback) {
        if (grantResults?.let { PermissionUtil.isAllGranted(it) } == true) {
            action.onGranted()
        } else {
            action.onDenied()
        }
    }

    companion object {
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
        fun checkSelfPermission(ctx: Context, permissions: Array<String?>?): Boolean {
            var isAllGranted = true
            if (permissions != null) {
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(ctx.applicationContext,
                            permission!!)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        isAllGranted = false
                        break
                    }
                }
            }
            return isAllGranted
        }

        /**
         * 检查读写权限是否存在
         *
         * @return
         */
        fun isCheckReadStorage(context: Context): Boolean {
            return checkSelfPermission(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        /**
         * 检查写入权限是否存在
         *
         * @return
         */
        fun isCheckWriteStorage(context: Context): Boolean {
            return checkSelfPermission(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }

        /**
         * 检查相机权限是否存在
         *
         * @return
         */
        fun isCheckCamera(context: Context): Boolean {
            return checkSelfPermission(context, arrayOf(Manifest.permission.CAMERA))
        }

        /**
         * 权限是否已申请
         *
         * @return
         */
        fun isCheckSelfPermission(context: Context, permissions: Array<String?>?): Boolean {
            return checkSelfPermission(context, permissions)
        }
    }
}
