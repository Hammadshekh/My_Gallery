package com.example.selector.basic

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.lang.NullPointerException
import java.util.ArrayList

class PictureSelectionCameraModel(selector: PictureSelector, chooseMode: Int) {
    private val selectionConfig: PictureSelectionConfig
    private val selector: PictureSelector

    /**
     * Set App Language
     *
     * @param language [LanguageConfig]
     * @return PictureSelectionModel
     */
    fun setLanguage(language: Int): PictureSelectionCameraModel {
        selectionConfig.language = language
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
    fun setCompressEngine(engine: CompressEngine): PictureSelectionCameraModel {
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
    fun setCompressEngine(engine: CompressFileEngine): PictureSelectionCameraModel {
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
    fun setCropEngine(engine: CropEngine): PictureSelectionCameraModel {
        PictureSelectionConfig.cropEngine = engine
        return this
    }

    /**
     * Image Crop the engine
     *
     * @param engine Image Crop the engine
     * @return
     */
    fun setCropEngine(engine: CropFileEngine): PictureSelectionCameraModel {
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
    fun setSandboxFileEngine(engine: SandboxFileEngine): PictureSelectionCameraModel {
        if (SdkVersionUtils.isQ()) {
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
    fun setSandboxFileEngine(engine: UriToFileTransformEngine): PictureSelectionCameraModel {
        if (SdkVersionUtils.isQ()) {
            PictureSelectionConfig.uriToFileTransformEngine = engine
            selectionConfig.isSandboxFileEngine = true
        } else {
            selectionConfig.isSandboxFileEngine = false
        }
        return this
    }

    /**
     * Intercept camera click events, and users can implement their own camera framework
     *
     * @param listener
     * @return
     */
    fun setCameraInterceptListener(listener: OnCameraInterceptListener): PictureSelectionCameraModel {
        PictureSelectionConfig.onCameraInterceptListener = listener
        return this
    }

    /**
     * Intercept Record Audio click events, and users can implement their own Record Audio framework
     *
     * @param listener
     * @return
     */
    fun setRecordAudioInterceptListener(listener: OnRecordAudioInterceptListener): PictureSelectionCameraModel {
        PictureSelectionConfig.onRecordAudioListener = listener
        return this
    }

    /**
     * Custom interception permission processing
     *
     * @param listener
     * @return
     */
    fun setPermissionsInterceptListener(listener: OnPermissionsInterceptListener): PictureSelectionCameraModel {
        PictureSelectionConfig.onPermissionsEventListener = listener
        return this
    }

    /**
     * permission description
     *
     * @param listener
     * @return
     */
    fun setPermissionDescriptionListener(listener: OnPermissionDescriptionListener): PictureSelectionCameraModel {
        PictureSelectionConfig.onPermissionDescriptionListener = listener
        return this
    }

    /**
     * Permission denied
     *
     * @param listener
     * @return
     */
    fun setPermissionDeniedListener(listener: OnPermissionDeniedListener): PictureSelectionCameraModel {
        PictureSelectionConfig.onPermissionDeniedListener = listener
        return this
    }

    /**
     * Custom limit tips
     *
     * @param listener
     * @return
     */
    fun setSelectLimitTipsListener(listener: OnSelectLimitTipsListener): PictureSelectionCameraModel {
        PictureSelectionConfig.onSelectLimitTipsListener = listener
        return this
    }

    /**
     * You can add a watermark to the image
     *
     * @param listener
     * @return
     */
    fun setAddBitmapWatermarkListener(listener: OnBitmapWatermarkEventListener): PictureSelectionCameraModel {
        if (selectionConfig.chooseMode !== SelectMimeType.ofAudio()) {
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
    fun setVideoThumbnailListener(listener: OnVideoThumbnailEventListener): PictureSelectionCameraModel {
        if (selectionConfig.chooseMode !== SelectMimeType.ofAudio()) {
            PictureSelectionConfig.onVideoThumbnailEventListener = listener
        }
        return this
    }

    /**
     * Do you want to open a foreground service to prevent the system from reclaiming the memory
     * of some models due to the use of cameras
     *
     * @param isForeground
     * @return
     */
    fun isCameraForegroundService(isForeground: Boolean): PictureSelectionCameraModel {
        selectionConfig.isCameraForegroundService = isForeground
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
    fun isAllFilesAccessOf11(isAllFilesAccess: Boolean): PictureSelectionCameraModel {
        selectionConfig.isAllFilesAccess = isAllFilesAccess
        return this
    }

    /**
     * Choose between photographing and shooting in ofAll mode
     *
     * @param ofAllCameraType [or SelectMimeType.ofVideo][SelectMimeType.ofImage]
     * The default is ofAll() mode
     * @return
     */
    fun setOfAllCameraType(ofAllCameraType: Int): PictureSelectionCameraModel {
        selectionConfig.ofAllCameraType = ofAllCameraType
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
    fun isOriginalControl(isOriginalControl: Boolean): PictureSelectionCameraModel {
        selectionConfig.isOriginalControl = isOriginalControl
        selectionConfig.isCheckOriginalImage = isOriginalControl
        return this
    }

    /**
     * The video quality output mode is only for system recording, and there are only two modes: poor quality or high quality
     *
     * @param videoQuality video quality and 0 or 1
     * Use [VideoQuality]
     *
     *
     * There are limitations, only high or low
     *
     * @return
     */
    @Deprecated("")
    fun setVideoQuality(videoQuality: Int): PictureSelectionCameraModel {
        selectionConfig.videoQuality = videoQuality
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter max file size
     * @return
     */
    fun setSelectMaxFileSize(fileKbSize: Long): PictureSelectionCameraModel {
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
    fun setSelectMinFileSize(fileKbSize: Long): PictureSelectionCameraModel {
        if (fileKbSize >= FileSizeUnit.MB) {
            selectionConfig.selectMinFileSize = fileKbSize
        } else {
            selectionConfig.selectMinFileSize = fileKbSize * FileSizeUnit.KB
        }
        return this
    }

    /**
     * camera output image format
     *
     * @param imageFormat PictureSelector media format
     * @return
     */
    fun setCameraImageFormat(imageFormat: String): PictureSelectionCameraModel {
        selectionConfig.cameraImageFormat = imageFormat
        return this
    }

    /**
     * camera output image format
     *
     * @param imageFormat PictureSelector media format
     * @return
     */
    fun setCameraImageFormatForQ(imageFormat: String): PictureSelectionCameraModel {
        selectionConfig.cameraImageFormatForQ = imageFormat
        return this
    }

    /**
     * camera output video format
     *
     * @param videoFormat PictureSelector media format
     * @return
     */
    fun setCameraVideoFormat(videoFormat: String): PictureSelectionCameraModel {
        selectionConfig.cameraVideoFormat = videoFormat
        return this
    }

    /**
     * camera output video format
     *
     * @param videoFormat PictureSelector media format
     * @return
     */
    fun setCameraVideoFormatForQ(videoFormat: String): PictureSelectionCameraModel {
        selectionConfig.cameraVideoFormatForQ = videoFormat
        return this
    }

    /**
     * The max duration of video recording. If it is system recording, there may be compatibility problems
     *
     * @param maxSecond video record second
     * @return
     */
    fun setRecordVideoMaxSecond(maxSecond: Int): PictureSelectionCameraModel {
        selectionConfig.recordVideoMaxSecond = maxSecond
        return this
    }

    /**
     * @param minSecond video record second
     * @return
     */
    fun setRecordVideoMinSecond(minSecond: Int): PictureSelectionCameraModel {
        selectionConfig.recordVideoMinSecond = minSecond
        return this
    }

    /**
     * Select the max number of seconds for video or audio support
     *
     * @param maxDurationSecond select video max second
     * @return
     */
    fun setSelectMaxDurationSecond(maxDurationSecond: Int): PictureSelectionCameraModel {
        selectionConfig.selectMaxDurationSecond = maxDurationSecond * 1000
        return this
    }

    /**
     * Select the min number of seconds for video or audio support
     *
     * @param minDurationSecond select video min second
     * @return
     */
    fun setSelectMinDurationSecond(minDurationSecond: Int): PictureSelectionCameraModel {
        selectionConfig.selectMinDurationSecond = minDurationSecond * 1000
        return this
    }

    /**
     * @param outPutCameraDir Camera output path
     *
     * Audio mode setting is not supported
     * @return
     */
    fun setOutputCameraDir(outPutCameraDir: String): PictureSelectionCameraModel {
        selectionConfig.outPutCameraDir = outPutCameraDir
        return this
    }

    /**
     * @param outPutAudioDir Audio output path
     * @return
     */
    fun setOutputAudioDir(outPutAudioDir: String): PictureSelectionCameraModel {
        selectionConfig.outPutAudioDir = outPutAudioDir
        return this
    }

    /**
     * Camera IMAGE custom local file name
     * # Such as xxx.png
     *
     * @param fileName
     * @return
     */
    fun setOutputCameraImageFileName(fileName: String): PictureSelectionCameraModel {
        selectionConfig.outPutCameraImageFileName = fileName
        return this
    }

    /**
     * Camera VIDEO custom local file name
     * # Such as xxx.png
     *
     * @param fileName
     * @return
     */
    fun setOutputCameraVideoFileName(fileName: String): PictureSelectionCameraModel {
        selectionConfig.outPutCameraVideoFileName = fileName
        return this
    }

    /**
     * Camera VIDEO custom local file name
     * # Such as xxx.amr
     *
     * @param fileName
     * @return
     */
    fun setOutputAudioFileName(fileName: String): PictureSelectionCameraModel {
        selectionConfig.outPutAudioFileName = fileName
        return this
    }

    /**
     * @param selectedList Select the selected picture set
     * @return
     */
    fun setSelectedData(selectedList: List<LocalMedia?>?): PictureSelectionCameraModel {
        if (selectedList == null) {
            return this
        }
        if (selectionConfig.selectionMode === SelectModeConfig.SINGLE && selectionConfig.isDirectReturnSingle) {
            SelectedManager.clearSelectResult()
        } else {
            SelectedManager.addAllSelectResult(ArrayList<Any?>(selectedList))
        }
        return this
    }

    /**
     * After recording with the system camera, does it support playing the video immediately using the system player
     *
     * @param isQuickCapture
     * @return
     */
    fun isQuickCapture(isQuickCapture: Boolean): PictureSelectionCameraModel {
        selectionConfig.isQuickCapture = isQuickCapture
        return this
    }

    /**
     * Set camera direction (after default image)
     */
    fun isCameraAroundState(isCameraAroundState: Boolean): PictureSelectionCameraModel {
        selectionConfig.isCameraAroundState = isCameraAroundState
        return this
    }

    /**
     * Camera image rotation, automatic correction
     */
    fun isCameraRotateImage(isCameraRotateImage: Boolean): PictureSelectionCameraModel {
        selectionConfig.isCameraRotateImage = isCameraRotateImage
        return this
    }

    /**
     * Start PictureSelector
     *
     *
     * The [IBridgePictureBehavior] interface needs to be
     * implemented in the activity or fragment you call to receive the returned results
     *
     *
     *
     * If the navigation component manages fragments,
     * it is recommended to use [] in openCamera mode
     *
     */
    fun forResult() {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity: Activity = selector.getActivity()
                ?: throw NullPointerException("Activity cannot be null")
            selectionConfig.isResultListenerBack = false
            selectionConfig.isActivityResultBack = true
            var fragmentManager: FragmentManager? = null
            if (activity is AppCompatActivity) {
                fragmentManager = activity.supportFragmentManager
            } else if (activity is FragmentActivity) {
                fragmentManager = activity.supportFragmentManager
            }
            if (fragmentManager == null) {
                throw NullPointerException("FragmentManager cannot be null")
            }
            if (activity !is IBridgePictureBehavior) {
                throw NullPointerException("Use only camera openCamera mode," +
                        "Activity or Fragment interface needs to be implemented " + IBridgePictureBehavior::class.java)
            }
            val fragment = fragmentManager.findFragmentByTag(PictureOnlyCameraFragment.TAG)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
            FragmentInjectManager.injectSystemRoomFragment(fragmentManager,
                PictureOnlyCameraFragment.TAG, PictureOnlyCameraFragment.newInstance())
        }
    }

    /**
     * Start PictureSelector Camera
     *
     *
     * If the navigation component manages fragments,
     * it is recommended to use [] in openCamera mode
     *
     *
     * @param call
     */
    fun forResult(call: OnResultCallbackListener<LocalMedia?>?) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity: Activity = selector.getActivity()
                ?: throw NullPointerException("Activity cannot be null")
            if (call == null) {
                throw NullPointerException("OnResultCallbackListener cannot be null")
            }
            // 绑定回调监听
            selectionConfig.isResultListenerBack = true
            selectionConfig.isActivityResultBack = false
            PictureSelectionConfig.onResultCallListener = call
            var fragmentManager: FragmentManager? = null
            if (activity is AppCompatActivity) {
                fragmentManager = activity.supportFragmentManager
            } else if (activity is FragmentActivity) {
                fragmentManager = activity.supportFragmentManager
            }
            if (fragmentManager == null) {
                throw NullPointerException("FragmentManager cannot be null")
            }
            val fragment = fragmentManager.findFragmentByTag(PictureOnlyCameraFragment.TAG)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
            FragmentInjectManager.injectSystemRoomFragment(fragmentManager,
                PictureOnlyCameraFragment.TAG, PictureOnlyCameraFragment.newInstance())
        }
    }

    /**
     * build PictureOnlyCameraFragment
     *
     *
     * The [IBridgePictureBehavior] interface needs to be
     * implemented in the activity or fragment you call to receive the returned results
     *
     */
    fun build(): PictureOnlyCameraFragment {
        val activity: Activity = selector.getActivity()
            ?: throw NullPointerException("Activity cannot be null")
        if (activity !is IBridgePictureBehavior) {
            throw NullPointerException("Use only build PictureOnlyCameraFragment," +
                    "Activity or Fragment interface needs to be implemented " + IBridgePictureBehavior::class.java)
        }
        // 绑定回调监听
        selectionConfig.isResultListenerBack = false
        selectionConfig.isActivityResultBack = true
        PictureSelectionConfig.onResultCallListener = null
        return PictureOnlyCameraFragment()
    }

    /**
     * build and launch PictureSelector Camera
     *
     * @param containerViewId fragment container id
     * @param call
     */
    fun buildLaunch(
        containerViewId: Int,
        call: OnResultCallbackListener<LocalMedia?>?,
    ): PictureOnlyCameraFragment {
        val activity: Activity = selector.getActivity()
            ?: throw NullPointerException("Activity cannot be null")
        if (call == null) {
            throw NullPointerException("OnResultCallbackListener cannot be null")
        }
        // 绑定回调监听
        selectionConfig.isResultListenerBack = true
        selectionConfig.isActivityResultBack = false
        PictureSelectionConfig.onResultCallListener = call
        var fragmentManager: FragmentManager? = null
        if (activity is AppCompatActivity) {
            fragmentManager = activity.supportFragmentManager
        } else if (activity is FragmentActivity) {
            fragmentManager = activity.supportFragmentManager
        }
        if (fragmentManager == null) {
            throw NullPointerException("FragmentManager cannot be null")
        }
        val onlyCameraFragment = PictureOnlyCameraFragment()
        val fragment = fragmentManager.findFragmentByTag(onlyCameraFragment.getFragmentTag())
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        fragmentManager.beginTransaction()
            .add(containerViewId, onlyCameraFragment, onlyCameraFragment.getFragmentTag())
            .addToBackStack(onlyCameraFragment.getFragmentTag())
            .commitAllowingStateLoss()
        return onlyCameraFragment
    }

    /**
     * Start PictureSelector
     *
     * @param requestCode
     */
    fun forResultActivity(requestCode: Int) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity: Activity = selector.getActivity()
                ?: throw NullPointerException("Activity cannot be null")
            selectionConfig.isResultListenerBack = false
            selectionConfig.isActivityResultBack = true
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            val fragment: Fragment = selector.getFragment()
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode)
            } else {
                activity.startActivityForResult(intent, requestCode)
            }
            activity.overridePendingTransition(R.anim.ps_anim_fade_in, 0)
        }
    }

    /**
     * ActivityResultLauncher PictureSelector
     *
     * @param launcher use []
     */
    fun forResultActivity(launcher: ActivityResultLauncher<Intent?>?) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity: Activity = selector.getActivity()
                ?: throw NullPointerException("Activity cannot be null")
            if (launcher == null) {
                throw NullPointerException("ActivityResultLauncher cannot be null")
            }
            selectionConfig.isResultListenerBack = false
            selectionConfig.isActivityResultBack = true
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            launcher.launch(intent)
            activity.overridePendingTransition(R.anim.ps_anim_fade_in, 0)
        }
    }

    /**
     * Start PictureSelector
     *
     * @param call
     */
    fun forResultActivity(call: OnResultCallbackListener<LocalMedia?>?) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity: Activity = selector.getActivity()
                ?: throw NullPointerException("Activity cannot be null")
            if (call == null) {
                throw NullPointerException("OnResultCallbackListener cannot be null")
            }
            // 绑定回调监听
            selectionConfig.isResultListenerBack = true
            selectionConfig.isActivityResultBack = false
            PictureSelectionConfig.onResultCallListener = call
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.ps_anim_fade_in, 0)
        }
    }

    init {
        this.selector = selector
        selectionConfig = PictureSelectionConfig.getCleanInstance()
        selectionConfig.chooseMode = chooseMode
        selectionConfig.isOnlyCamera = true
        selectionConfig.isDisplayTimeAxis = false
        selectionConfig.isPreviewFullScreenMode = false
        selectionConfig.isPreviewZoomEffect = false
        selectionConfig.isOpenClickSound = false
    }
}
