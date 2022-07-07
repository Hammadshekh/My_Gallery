package com.example.selector.widget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.example.selector.permissions.PermissionChecker
import com.example.selector.permissions.PermissionConfig
import com.example.selector.permissions.PermissionResultCallback
import com.example.selector.utils.SdkVersionUtils
import com.luck.picture.lib.entity.LocalMedia

class PictureOnlyCameraFragment : PictureCommonFragment() {
    val resourceId: Int
        get() = R.layout.ps_empty

    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 这里只有非内存回收状态下才走，否则当内存不足Fragment被回收后会重复执行
        if (savedInstanceState == null) {
            if (SdkVersionUtils.isQ()) {
                openSelectedCamera()
            } else {
                PermissionChecker.instance.requestPermissions(this,
                    PermissionConfig.WRITE_EXTERNAL_STORAGE, object : PermissionResultCallback() {
                        override fun onGranted() {
                            openSelectedCamera()
                        }

                        override fun onDenied() {
                            handlePermissionDenied(PermissionConfig.WRITE_EXTERNAL_STORAGE)
                        }
                    })
            }
        }
    }

    fun dispatchCameraMediaResult(media: LocalMedia?) {
        val selectResultCode: Int = confirmSelect(media, false)
        if (selectResultCode == SelectedManager.ADD_SUCCESS) {
            dispatchTransformResult()
        } else {
            onKeyBackFragmentFinish()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish()
        }
    }

    fun handlePermissionSettingResult(permissions: Array<String>) {
        onPermissionExplainEvent(false, null)
        var isHasPermissions: Boolean
        if (PictureSelectionConfig.onPermissionsEventListener != null) {
            isHasPermissions = PictureSelectionConfig.onPermissionsEventListener
                .hasPermissions(this, permissions)
        } else {
            isHasPermissions = PermissionChecker.isCheckCamera(getContext())
            if (SdkVersionUtils.isQ()) {
            } else {
                isHasPermissions = if (SdkVersionUtils.isR() && config.isAllFilesAccess) {
                    Environment.isExternalStorageManager()
                } else {
                    PermissionChecker.isCheckWriteStorage(getContext())
                }
            }
        }
        if (isHasPermissions) {
            openSelectedCamera()
        } else {
            if (!PermissionChecker.isCheckCamera(getContext())) {
                ToastUtils.showToast(getContext(), getString(R.string.ps_camera))
            } else {
                val isCheckWriteStorage =
                    if (SdkVersionUtils.isR() && config.isAllFilesAccess) Environment.isExternalStorageManager() else PermissionChecker.isCheckWriteStorage(
                        getContext())
                if (!isCheckWriteStorage) {
                    ToastUtils.showToast(getContext(), getString(R.string.ps_jurisdiction))
                }
            }
            onKeyBackFragmentFinish()
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = null
    }

    companion object {
        val fragmentTag = PictureOnlyCameraFragment::class.java.simpleName
            get() = Companion.field

        fun newInstance(): PictureOnlyCameraFragment {
            return PictureOnlyCameraFragment()
        }
    }
}
