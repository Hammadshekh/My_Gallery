package com.example.selector.basic

import android.content.Intent
import android.os.Bundle
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

interface IPictureSelectorCommonEvent {
    /**
     * Create a data querier
     */
    fun onCreateLoader()

    /**
     * View Layout
     *
     * @return resource Id
     */
    val resourceId: Int

    /**
     * onKey back fragment or finish
     */
    fun onKeyBackFragmentFinish()

    /**
     * fragment onResume
     */
    fun onFragmentResume()

    /**
     *
    permission denied
     */
    fun handlePermissionDenied(permissionArray: Array<String>)

    /**
     * onSavedInstance
     *
     * @param savedInstanceState
     */
    fun reStartSavedInstance(savedInstanceState: Bundle?)

    /**
     * Permission setting result
     */
    fun handlePermissionSettingResult(permissions: Array<String>)

    /**
     * Set app language
     */
    fun initAppLanguage()

    /**
     *
    Recreate the required engine
     */
    fun onRecreateEngine()

    /**
     * Choose to take a photo or take a video
     */
    fun onSelectedOnlyCamera()

    /**
     * Select camera type; take photo, video, or audio recording
     */
    fun openSelectedCamera()

    /**
     * Photograph
     */
    fun openImageCamera()

    /**
     * shoot video
     */
    fun openVideoCamera()

    /**
     *
    recording
     */
    fun openSoundRecording()

    /**
     *
    select result
     *
     * @param currentMedia
     * @param isSelected
     * @return Returns the state of the current selection
     */
    fun confirmSelect(currentMedia: LocalMedia, isSelected: Boolean): Int

/*    *//**
     *
    Validate co-selection type mode optional conditions
     *
     * @param isSelected      Whether the resource is selected
     * @param curMimeType     Selected resource type
     * @param selectVideoSize   Number of selected videos
     * @param fileSizeFile size
     * @param durationvideo duration
     * @return
     */
    fun checkWithMimeTypeValidity(
        isSelected: Boolean,
        curMimeType: String,
        selectVideoSize: Int,
        fileSize: Long,
        duration: Long,
    ): Boolean

    /**
     *
    Validate a single type schema optional condition
     *
     * @param isSelected    Whether the resource is selected
     * @param curMimeType   Selected resource type
     * @param existMimeType Selected resource type
     * @param fileSizeFile size
     * @param durationvideo duration
     * @return
     */
    fun checkOnlyMimeTypeValidity(
        isSelected: Boolean,
        curMimeType: String,
        existMimeType: String,
        fileSize: Long,
        duration: Long,
    ): Boolean

    /**
     *
    The selection result data has changed
     *
     * @param isAddRemove  isAddRemove
    Add or remove actions
     * @param currentMedia
    the object of the current operation
     */
    fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia)

    /**
     * refresh the specified data
     */
    fun onFixedSelectedChange(oldLocalMedia: LocalMedia)

    /**
     *
    Generated after distributing photos LocalMedia
     *
     * @param media
     */
    fun dispatchCameraMediaResult(media: LocalMedia)

    /**
     * Send notification of selection data changes
     *
     * @param isAddRemove
    Add or remove actions
     * @param currentMedia the object of the current operation
     */
    fun sendSelectedChangeEvent(isAddRemove: Boolean, currentMedia: LocalMedia)

    /**
     *
    refresh the specified data
     */
    fun sendFixedSelectedChangeEvent(currentMedia: LocalMedia)

    /**
     * []
     *
     *
     *
    Sort selection result number in isSelectNumberStyle mode
     *
     */
    fun sendChangeSubSelectPositionEvent(adapterChange: Boolean)

    /**
     * Original image options changed
     */
    fun sendSelectedOriginalChangeEvent()

    /**
     *
    Edit resources
     */
    fun onCheckOriginalChange()

    /**
     * Edit resources
     */
    fun onEditMedia(intent: Intent)

    /**
     *
    select result callback
     *
     * @param result
     */
    fun onResultEvent(result: ArrayList<LocalMedia>)

    /**
     *
    compression
     * @param result
     */
    fun onCrop(result: ArrayList<LocalMedia>)

    /**
     *
    compression
     * @param result
     */
    fun onOldCrop(result: ArrayList<LocalMedia>)

    /**
     * 压缩
     *
     * @param result
     */
    fun onCompress(result: ArrayList<LocalMedia>)

    /**
     *
    compression
     *
     * @param result
     */
    @Deprecated("")
    fun onOldCompress(result: ArrayList<LocalMedia>)

    /**
     *
    Verify if clipping is required
     *
     * @return
     */
    fun checkCropValidity(): Boolean

    /**
     *
    Verify if clipping is required
     *
     * @return
     */
    @Deprecated("")
    fun checkOldCropValidity(): Boolean

    /**
     *
    Verify if compression is required
     *
     * @return
     */
    fun checkCompressValidity(): Boolean

    /**
     * Verify if compression is required
     *
     * @return
     */
    @Deprecated("")
    fun checkOldCompressValidity(): Boolean

    /**
     *
    Verify that sandbox conversion processing is required
     *
     * @return
     */
    fun checkTransformSandboxFile(): Boolean

    /**
     * Verify that sandbox conversion processing is required
     *
     * @return
     */
    @Deprecated("")
    fun checkOldTransformSandboxFile(): Boolean

    /**
     * Verify if a watermark needs to be added
     *
     * @return
     */
    fun checkAddBitmapWatermark(): Boolean

    /**
     *
    Whether permission verification needs to process video thumbnails
     */
    fun checkVideoThumbnail(): Boolean

    /**
     * Access Request
     *
     * @param permissionArray
     */
    fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String>)

    /**
     *
    Permission description
     *
     * @param isDisplayExplain
    Whether to show permission description
     * @param permissionArray
    Rights Groups
     */
    fun onPermissionExplainEvent(isDisplayExplain: Boolean, permissionArray: Array<String>)

    /**
     * Intercept camera events
     *
     * @param cameraMode [SelectMimeType]
     */
    fun onInterceptCameraEvent(cameraMode: Int)

    /**
     * EnterFragment
     */
    fun onEnterFragment()

    /**
     *
    Exit Fragment
     */
    fun onExitFragment()

    /**
     * show loading
     */
    fun showLoading()

    /**
     * dismiss loading
     */
    fun dismissLoading()
}
