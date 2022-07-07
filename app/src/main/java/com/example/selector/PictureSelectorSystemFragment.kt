package com.example.selector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.example.mygallery.R
import com.example.selector.basic.PictureCommonFragment
import com.example.selector.config.PermissionEvent
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.config.SelectModeConfig
import com.example.selector.interfaces.OnRequestPermissionListener
import com.example.selector.manager.SelectedManager
import com.example.selector.permissions.PermissionChecker
import com.example.selector.permissions.PermissionConfig
import com.example.selector.permissions.PermissionConfig.READ_WRITE_EXTERNAL_STORAGE
import com.example.selector.permissions.PermissionResultCallback
import com.example.selector.utils.SdkVersionUtils
import com.example.selector.utils.ToastUtils
import java.util.ArrayList

class PictureSelectorSystemFragment : PictureCommonFragment() {
    override val resourceId: Int
        get() = R.layout.ps_empty
    private var mDocMultipleLauncher: ActivityResultLauncher<String>? = null
    private var mDocSingleLauncher: ActivityResultLauncher<String>? = null
    private var mContentsLauncher: ActivityResultLauncher<String>? = null
    private var mContentLauncher: ActivityResultLauncher<String>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createSystemContracts()
        val isCheckReadStorage =
            if (SdkVersionUtils.isR() && config!!.isAllFilesAccess) Environment.isExternalStorageManager() else PermissionChecker.isCheckReadStorage(
                context!!)
        if (isCheckReadStorage) {
            openSystemAlbum()
        } else {
            onPermissionExplainEvent(true, READ_WRITE_EXTERNAL_STORAGE)
            if (PictureSelectionConfig.onPermissionsEventListener != null) {
                onApplyPermissionsEvent(PermissionEvent.EVENT_SYSTEM_SOURCE_DATA,
                    READ_WRITE_EXTERNAL_STORAGE)
            } else {
                PermissionChecker.getInstance().requestPermissions(this,
                    READ_WRITE_EXTERNAL_STORAGE, object : PermissionResultCallback() {
                        override fun onGranted() {
                            openSystemAlbum()
                        }

                        override fun onDenied() {
                            handlePermissionDenied(READ_WRITE_EXTERNAL_STORAGE)
                        }
                    })
            }
        }
    }

    fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String?>?) {
        if (event == PermissionEvent.EVENT_SYSTEM_SOURCE_DATA) {
            PictureSelectionConfig.onPermissionsEventListener.requestPermission(this,
                READ_WRITE_EXTERNAL_STORAGE, object : OnRequestPermissionListener {
                    override fun onCall(permissionArray: Array<String?>?, isResult: Boolean) {
                        if (isResult) {
                            openSystemAlbum()
                        } else {
                            handlePermissionDenied(permissionArray)
                        }
                    }
                })
        }
    }

    /**
     * 打开系统相册
     */
    private fun openSystemAlbum() {
        onPermissionExplainEvent(false, null)
        if (config!!.selectionMode === SelectModeConfig.SINGLE) {
            if (config!!.chooseMode === SelectMimeType.ofAll()) {
                mDocSingleLauncher!!.launch(SelectMimeType.SYSTEM_ALL)
            } else {
                mContentLauncher!!.launch(input)
            }
        } else {
            if (config!!.chooseMode === SelectMimeType.ofAll()) {
                mDocMultipleLauncher!!.launch(SelectMimeType.SYSTEM_ALL)
            } else {
                mContentsLauncher!!.launch(input)
            }
        }
    }

    /**
     * createSystemContracts
     */
    private fun createSystemContracts() {
        if (config!!.selectionMode === SelectModeConfig.SINGLE) {
            if (config!!.chooseMode === SelectMimeType.ofAll()) {
                createSingleDocuments()
            } else {
                createContent()
            }
        } else {
            if (config!!.chooseMode === SelectMimeType.ofAll()) {
                createMultipleDocuments()
            } else {
                createMultipleContents()
            }
        }
    }

    /**
     * 同时获取图片或视频(多选)
     *
     * 部分机型可能不支持多选操作
     */
    private fun createMultipleDocuments() {
        mDocMultipleLauncher =
            registerForActivityResult(object : ActivityResultContract<String?, List<Uri?>>() {
                override fun parseResult(resultCode: Int, intent: Intent?): List<Uri?> {
                    val result: MutableList<Uri?> = ArrayList()
                    if (intent == null) {
                        return result
                    }
                    if (intent.clipData != null) {
                        val clipData = intent.clipData
                        val itemCount = clipData!!.itemCount
                        for (i in 0 until itemCount) {
                            val item = clipData.getItemAt(i)
                            val uri = item.uri
                            result.add(uri)
                        }
                    } else if (intent.data != null) {
                        result.add(intent.data)
                    }
                    return result
                }

                override fun createIntent(context: Context, mimeTypes: String?): Intent {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    intent.type = mimeTypes
                    return intent
                }
            },
                ActivityResultCallback<List<Uri>?> { result ->
                    if (result == null || result.size == 0) {
                        onKeyBackFragmentFinish()
                    } else {
                        for (i in result.indices) {
                            val media = buildLocalMedia(result[i].toString())
                            media.path = if (SdkVersionUtils.isQ()) media.path else media.realPath
                            SelectedManager.addSelectResult(media)
                        }
                        dispatchTransformResult()
                    }
                })
    }

    /**
     * 同时获取图片或视频(单选)
     */
    private fun createSingleDocuments() {
        mDocSingleLauncher =
            registerForActivityResult(object : ActivityResultContract<String?, Uri?>() {
                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                    return intent?.data
                }

                override fun createIntent(context: Context, mimeTypes: String?): Intent {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = mimeTypes
                    return intent
                }
            }) { result ->
                if (result == null) {
                    onKeyBackFragmentFinish()
                } else {
                    val media = buildLocalMedia(result.toString())
                    media.path = if (SdkVersionUtils.isQ()) media.path else media.realPath
                    val selectResultCode = confirmSelect(media, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        dispatchTransformResult()
                    } else {
                        onKeyBackFragmentFinish()
                    }
                }
            }
    }

    /**
     * 获取图片或视频
     *
     * 部分机型可能不支持多选操作
     */
    private fun createMultipleContents() {
        mContentsLauncher =
            registerForActivityResult(object : ActivityResultContract<String?, List<Uri?>>() {
                override fun parseResult(resultCode: Int, intent: Intent?): List<Uri?> {
                    val result: MutableList<Uri?> = ArrayList()
                    if (intent == null) {
                        return result
                    }
                    if (intent.clipData != null) {
                        val clipData = intent.clipData
                        val itemCount = clipData!!.itemCount
                        for (i in 0 until itemCount) {
                            val item = clipData.getItemAt(i)
                            val uri = item.uri
                            result.add(uri)
                        }
                    } else if (intent.data != null) {
                        result.add(intent.data)
                    }
                    return result
                }

                override fun createIntent(context: Context, mimeType: String?): Intent {
                    val intent: Intent
                    intent = if (TextUtils.equals(SelectMimeType.SYSTEM_VIDEO, mimeType)) {
                        Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    } else if (TextUtils.equals(SelectMimeType.SYSTEM_AUDIO, mimeType)) {
                        Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                    } else {
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    }
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    return intent
                }
            },
                ActivityResultCallback<List<Uri>?> { result ->
                    if (result == null || result.size == 0) {
                        onKeyBackFragmentFinish()
                    } else {
                        for (i in result.indices) {
                            val media = buildLocalMedia(result[i].toString())
                            media.path = if (SdkVersionUtils.isQ()) media.path else media.realPath
                            SelectedManager.addSelectResult(media)
                        }
                        dispatchTransformResult()
                    }
                })
    }

    /**
     * 单选图片或视频
     */
    private fun createContent() {
        mContentLauncher =
            registerForActivityResult(object : ActivityResultContract<String?, Uri?>() {
                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                    return intent?.data
                }

                override fun createIntent(context: Context, mimeType: String?): Intent {
                    val intent: Intent
                    intent = if (TextUtils.equals(SelectMimeType.SYSTEM_VIDEO, mimeType)) {
                        Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    } else if (TextUtils.equals(SelectMimeType.SYSTEM_AUDIO, mimeType)) {
                        Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                    } else {
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    }
                    return intent
                }
            }) { result ->
                if (result == null) {
                    onKeyBackFragmentFinish()
                } else {
                    val media = buildLocalMedia(result.toString())
                    media.path = if (SdkVersionUtils.isQ()) media.path else media.realPath
                    val selectResultCode = confirmSelect(media, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        dispatchTransformResult()
                    } else {
                        onKeyBackFragmentFinish()
                    }
                }
            }
    }

    /**
     * 获取选资源取类型
     *
     * @return
     */
    private val input: String
        private get() = if (config!!.chooseMode === SelectMimeType.ofVideo()) {
            SelectMimeType.SYSTEM_VIDEO
        } else if (config!!.chooseMode === SelectMimeType.ofAudio()) {
            SelectMimeType.SYSTEM_AUDIO
        } else {
            SelectMimeType.SYSTEM_IMAGE
        }

    override fun handlePermissionSettingResult(permissions: Array<String>) {
        onPermissionExplainEvent(false, null)
        val isCheckReadStorage: Boolean
        isCheckReadStorage = if (PictureSelectionConfig.onPermissionsEventListener != null) {
            PictureSelectionConfig.onPermissionsEventListener
                .hasPermissions(this, permissions)
        } else {
            if (SdkVersionUtils.isR() && config!!.isAllFilesAccess) {
                Environment.isExternalStorageManager()
            } else {
                PermissionChecker.isCheckReadStorage(requireContext())
            }
        }
        if (isCheckReadStorage) {
            openSystemAlbum()
        } else {
            ToastUtils.showToast(requireContext(), getString(R.string.ps_jurisdiction))
            onKeyBackFragmentFinish()
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = arrayOf()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDocMultipleLauncher != null) {
            mDocMultipleLauncher!!.unregister()
        }
        if (mDocSingleLauncher != null) {
            mDocSingleLauncher!!.unregister()
        }
        if (mContentsLauncher != null) {
            mContentsLauncher!!.unregister()
        }
        if (mContentLauncher != null) {
            mContentLauncher!!.unregister()
        }
    }

    companion object {
        val fragmentTag = PictureSelectorSystemFragment::class.java.simpleName
            get() = Companion.field

        fun newInstance(): PictureSelectorSystemFragment {
            return PictureSelectorSystemFragment()
        }
    }
}

