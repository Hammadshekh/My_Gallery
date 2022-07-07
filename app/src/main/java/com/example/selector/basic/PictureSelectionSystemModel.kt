package com.example.selector.basic

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.mygallery.R
import com.example.selector.PictureSelectorSystemFragment
import com.example.selector.config.FileSizeUnit
import com.example.selector.config.PictureConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.engine.*
import com.example.selector.interfaces.*
import com.example.selector.utils.DoubleUtils
import com.example.selector.utils.PictureFileUtils.TAG
import com.example.selector.utils.SdkVersionUtils
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

class PictureSelectionSystemModel(selector: PictureSelector, chooseMode: Int) {
    private val selectionConfig: PictureSelectionConfig
    private val selector: PictureSelector

    /**
     * @param selectionMode PictureSelector Selection model
     * and [SelectModeConfig.MULTIPLE] or [SelectModeConfig.SINGLE]
     *
     *
     * Use [SelectModeConfig]
     *
     * @return
     */
    fun setSelectionMode(selectionMode: Int): PictureSelectionSystemModel {
        selectionConfig.selectionMode = selectionMode
        return this
    }

    /**
     * Returns whether the calling app has All Files Access on the primary shared/external storage media.
     * Declaring the permission Manifest.permission.MANAGE_EXTERNAL_STORAGE isn't enough to gain the access.
     * To request access, use android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION.
     *
     * @param isAllFilesAccess
     * @return
     */
    fun isAllFilesAccessOf11(isAllFilesAccess: Boolean): PictureSelectionSystemModel {
        selectionConfig.isAllFilesAccess = isAllFilesAccess
        return this
    }

    /**
     * Do you need to display the original controller
     *
     *
     * It needs to be used with setSandboxFileEngine
     * [.setOriginalPath()][LocalMedia]
     *
     *
     * @param isOriginalControl
     * @return
     */
    fun isOriginalControl(isOriginalControl: Boolean): PictureSelectionSystemModel {
        selectionConfig.isCheckOriginalImage = isOriginalControl
        return this
    }

    /**
     * Skip crop mimeType
     *
     * @param mimeTypes Use example [{]
     * @return
     */
    fun setSkipCropMimeType(vararg mimeTypes: String): PictureSelectionSystemModel {
        if (mimeTypes.isNotEmpty()) {
            selectionConfig.skipCropList?.addAll(mutableListOf(*mimeTypes))
        }
        return this
    }

    /**
     * Image Compress the engine
     *
     * @param engine Image Compress the engine
     * Please use [CompressFileEngine]
     * @return
     */
    @Deprecated("")
    fun setCompressEngine(engine: CompressEngine): PictureSelectionSystemModel {
        PictureSelectionConfig.compressEngine = engine
        selectionConfig.isCompressEngine = true
        return this
    }

    /**
     * Image Compress the engine
     *
     * @param engine Image Compress the engine
     * @return
     */
    fun setCompressEngine(engine: CompressFileEngine): PictureSelectionSystemModel {
        PictureSelectionConfig.compressFileEngine = engine
        selectionConfig.isCompressEngine = true
        return this
    }

    /**
     * Image Crop the engine
     *
     * @param engine Image Crop the engine
     * Please Use [CropFileEngine]
     * @return
     */
    @Deprecated("")
    fun setCropEngine(engine: CropEngine): PictureSelectionSystemModel {
        PictureSelectionConfig.cropEngine = engine
        return this
    }

    /**
     * Image Crop the engine
     *
     * @param engine Image Crop the engine
     * @return
     */
    fun setCropEngine(engine: CropFileEngine): PictureSelectionSystemModel {
        PictureSelectionConfig.cropFileEngine = engine
        return this
    }

    /**
     * App Sandbox file path transform
     *
     * @param engine App Sandbox path transform
     * Please Use [UriToFileTransformEngine]
     * @return
     */
    @Deprecated("")
    fun setSandboxFileEngine(engine: SandboxFileEngine): PictureSelectionSystemModel {
        if (SdkVersionUtils.isQ) {
            PictureSelectionConfig.sandboxFileEngine = engine
            selectionConfig.isSandboxFileEngine = true
        } else {
            selectionConfig.isSandboxFileEngine = false
        }
        return this
    }

    /**
     * App Sandbox file path transform
     *
     * @param engine App Sandbox path transform
     * @return
     */
    fun setSandboxFileEngine(engine: UriToFileTransformEngine): PictureSelectionSystemModel {
        if (SdkVersionUtils.isQ) {
            PictureSelectionConfig.uriToFileTransformEngine = engine
            selectionConfig.isSandboxFileEngine = true
        } else {
            selectionConfig.isSandboxFileEngine = false
        }
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter max file size
     * @return
     */
    fun setSelectMaxFileSize(fileKbSize: Long): PictureSelectionSystemModel {
        if (fileKbSize >= FileSizeUnit.MB) {
            selectionConfig.selectMaxFileSize = fileKbSize
        } else {
            selectionConfig.selectMaxFileSize = fileKbSize * FileSizeUnit.KB
        }
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter min file size
     * @return
     */
    fun setSelectMinFileSize(fileKbSize: Long): PictureSelectionSystemModel {
        if (fileKbSize >= FileSizeUnit.MB) {
            selectionConfig.selectMinFileSize = fileKbSize
        } else {
            selectionConfig.selectMinFileSize = fileKbSize * FileSizeUnit.KB
        }
        return this
    }

    /**
     * Select the max number of seconds for video or audio support
     *
     * @param maxDurationSecond select video max second
     * @return
     */
    fun setSelectMaxDurationSecond(maxDurationSecond: Int): PictureSelectionSystemModel {
        selectionConfig.selectMaxDurationSecond = maxDurationSecond * 1000
        return this
    }

    /**
     * Select the min number of seconds for video or audio support
     *
     * @param minDurationSecond select video min second
     * @return
     */
    fun setSelectMinDurationSecond(minDurationSecond: Int): PictureSelectionSystemModel {
        selectionConfig.selectMinDurationSecond = minDurationSecond * 1000
        return this
    }

    /**
     * Custom interception permission processing
     *
     * @param listener
     * @return
     */
    fun setPermissionsInterceptListener(listener: OnPermissionsInterceptListener): PictureSelectionSystemModel {
        PictureSelectionConfig.onPermissionsEventListener = listener
        return this
    }

    /**
     * permission description
     *
     * @param listener
     * @return
     */
    fun setPermissionDescriptionListener(listener: OnPermissionDescriptionListener): PictureSelectionSystemModel {
        PictureSelectionConfig.onPermissionDescriptionListener = listener
        return this
    }

    /**
     * Permission denied
     *
     * @param listener
     * @return
     */
    fun setPermissionDeniedListener(listener: OnPermissionDeniedListener): PictureSelectionSystemModel {
        PictureSelectionConfig.onPermissionDeniedListener = listener
        return this
    }

    /**
     * Custom limit tips
     *
     * @param listener
     * @return
     */
    fun setSelectLimitTipsListener(listener: OnSelectLimitTipsListener): PictureSelectionSystemModel {
        PictureSelectionConfig.onSelectLimitTipsListener = listener
        return this
    }

    /**
     * You need to filter out the content that does not meet the selection criteria
     *
     * @param listener
     * @return
     */
    fun setSelectFilterListener(listener: OnSelectFilterListener): PictureSelectionSystemModel {
        PictureSelectionConfig.onSelectFilterListener = listener
        return this
    }

    /**
     * You can add a watermark to the image
     *
     * @param listener
     * @return
     */
    fun setAddBitmapWatermarkListener(listener: OnBitmapWatermarkEventListener): PictureSelectionSystemModel {
        if (selectionConfig.chooseMode != SelectMimeType.ofAudio()) {
            PictureSelectionConfig.onBitmapWatermarkListener = listener
        }
        return this
    }

    /**
     * Process video thumbnails
     *
     * @param listener
     * @return
     */
    fun setVideoThumbnailListener(listener: OnVideoThumbnailEventListener): PictureSelectionSystemModel {
        if (selectionConfig.chooseMode != SelectMimeType.ofAudio()) {
            PictureSelectionConfig.onVideoThumbnailEventListener = listener
        }
        return this
    }

    /**
     * Call the system library to obtain resources
     *
     *
     * Using the system gallery library, some API functions will not be supported
     *
     *
     * @param call
     */
    fun forSystemResult(call: OnResultCallbackListener<LocalMedia>) {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            PictureSelectionConfig.onResultCallListener = call
            selectionConfig.isResultListenerBack = true
            selectionConfig.isActivityResultBack = false
            var fragmentManager: FragmentManager? = null
            if (activity is AppCompatActivity) {
                fragmentManager = activity.supportFragmentManager
            } else if (activity is FragmentActivity) {
                fragmentManager = activity.supportFragmentManager
            }
            if (fragmentManager == null) {
                throw NullPointerException("FragmentManager cannot be null")
            }
            val fragment = fragmentManager.findFragmentByTag(TAG)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
            FragmentInjectManager.injectSystemRoomFragment(fragmentManager,
                TAG, PictureSelectorSystemFragment.newInstance())
        }
    }

    /**
     * Call the system library to obtain resources
     *
     *
     * Using the system gallery library, some API functions will not be supported
     *
     *
     *
     * The [IBridgePictureBehavior] interface needs to be
     * implemented in the activity or fragment you call to receive the returned results
     *
     */
    fun forSystemResult() {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            if (activity !is IBridgePictureBehavior) {
                throw NullPointerException("Use only forSystemResult();," +
                        "Activity or Fragment interface needs to be implemented " + IBridgePictureBehavior::class.java)
            }
            selectionConfig.isActivityResultBack = true
            PictureSelectionConfig.onResultCallListener = null
            selectionConfig.isResultListenerBack = false
            var fragmentManager: FragmentManager? = null
            if (activity is AppCompatActivity) {
                fragmentManager = (activity as AppCompatActivity).supportFragmentManager
            } else if (activity is FragmentActivity) {
                fragmentManager = (activity as FragmentActivity).supportFragmentManager
            }
            if (fragmentManager == null) {
                throw NullPointerException("FragmentManager cannot be null")
            }
            val fragment = fragmentManager.findFragmentByTag(TAG)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
            FragmentInjectManager.injectSystemRoomFragment(fragmentManager,
                TAG, PictureSelectorSystemFragment.newInstance())
        }
    }

    /**
     * Start PictureSelector
     *
     * @param requestCode
     */
    fun forSystemResultActivity(requestCode: Int) {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            selectionConfig.isResultListenerBack = false
            selectionConfig.isActivityResultBack = true
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            intent.putExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE,
                PictureConfig.MODE_TYPE_SYSTEM_SOURCE)
            val fragment: Fragment = selector.fragment!!
            fragment.startActivityForResult(intent, requestCode)
            activity.overridePendingTransition(R.anim.ps_anim_fade_in, 0)
        }
    }

    /**
     * ActivityResultLauncher PictureSelector
     *
     * @param launcher use []
     */
    fun forSystemResultActivity(launcher: ActivityResultLauncher<Intent?>?) {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            if (launcher == null) {
                throw NullPointerException("ActivityResultLauncher cannot be null")
            }
            selectionConfig.isResultListenerBack = false
            selectionConfig.isActivityResultBack = true
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            intent.putExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE,
                PictureConfig.MODE_TYPE_SYSTEM_SOURCE)
            launcher.launch(intent)
            activity.overridePendingTransition(R.anim.ps_anim_fade_in, 0)
        }
    }

    /**
     * Start PictureSelector
     *
     * @param call
     */
    fun forSystemResultActivity(call: OnResultCallbackListener<LocalMedia>) {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            // 绑定回调监听
            selectionConfig.isResultListenerBack = true
            selectionConfig.isActivityResultBack = false
            PictureSelectionConfig.onResultCallListener = call
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            intent.putExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE,
                PictureConfig.MODE_TYPE_SYSTEM_SOURCE)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.ps_anim_fade_in, 0)
        }
    }

    init {
        this.selector = selector
        selectionConfig = PictureSelectionConfig.cleanInstance
        selectionConfig.chooseMode = chooseMode
        selectionConfig.isPreviewFullScreenMode = false
        selectionConfig.isPreviewZoomEffect = false
    }
}
