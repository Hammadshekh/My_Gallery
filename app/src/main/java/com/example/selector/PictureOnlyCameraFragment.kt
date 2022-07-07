package com.example.selector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.example.mygallery.R
import com.example.selector.basic.PictureCommonFragment
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.manager.SelectedManager
import com.example.selector.permissions.PermissionChecker
import com.example.selector.permissions.PermissionConfig
import com.example.selector.permissions.PermissionResultCallback
import com.example.selector.utils.SdkVersionUtils
import com.example.selector.utils.ToastUtils
import com.luck.picture.lib.entity.LocalMedia

class PictureOnlyCameraFragment : PictureCommonFragment() {
    override val resourceId: Int
        get() = R.layout.ps_empty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 这里只有非内存回收状态下才走，否则当内存不足Fragment被回收后会重复执行
        if (savedInstanceState == null) {
            if (SdkVersionUtils.isQ) {
                openSelectedCamera()
            } else {
                PermissionChecker.instance?.requestPermissions(requireParentFragment(),
                    PermissionConfig.WRITE_EXTERNAL_STORAGE, object : PermissionResultCallback {
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

    override fun dispatchCameraMediaResult(media: LocalMedia) {
        val selectResultCode: Int = confirmSelect(media, false)
        if (selectResultCode == SelectedManager.ADD_SUCCESS) {
            dispatchTransformResult()
        } else {
            onKeyBackFragmentFinish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish()
        }
    }

    override fun handlePermissionSettingResult(permissions: Array<String>) {
        onPermissionExplainEvent(false, emptyArray())
        var isHasPermissions: Boolean
        if (PictureSelectionConfig.onPermissionsEventListener != null) {
            isHasPermissions = PictureSelectionConfig.onPermissionsEventListener!!
                .hasPermissions(this, permissions)
        } else {
            isHasPermissions = PermissionChecker.isCheckCamera(requireContext())
            if (SdkVersionUtils.isQ) {
            } else {
                isHasPermissions = if (SdkVersionUtils.isR() && config.isAllFilesAccess) {
                    Environment.isExternalStorageManager()
                } else {
                    PermissionChecker.isCheckWriteStorage(requireContext())
                }
            }
        }
        if (isHasPermissions) {
            openSelectedCamera()
        } else {
            if (!PermissionChecker.isCheckCamera(requireContext())) {
                ToastUtils.showToast(requireContext(), getString(R.string.ps_camera))
            } else {
                val isCheckWriteStorage =
                    if (SdkVersionUtils.isR() && config.isAllFilesAccess) Environment.isExternalStorageManager() else PermissionChecker.isCheckWriteStorage(
                        requireContext())
                if (!isCheckWriteStorage) {
                    ToastUtils.showToast(requireContext(), getString(R.string.ps_jurisdiction))
                }
            }
            onKeyBackFragmentFinish()
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = emptyArray()
    }

    companion object {
     /*   val fragmentTag =PictureOnlyCameraFragment::class.java.simpleName
            get() = Companion.field*/

        fun newInstance(): PictureOnlyCameraFragment {
            return PictureOnlyCameraFragment()
        }
    }
}
