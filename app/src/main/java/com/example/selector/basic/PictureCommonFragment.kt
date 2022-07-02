package com.example.selector.basic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.mygallery.R
import com.example.selector.config.InjectResourceSource
import com.example.selector.config.PictureConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.dialog.PictureLoadingDialog
import com.example.selector.immersive.ImmersiveManager
import com.example.selector.interfaces.OnCallbackListener
import com.example.selector.loader.IBridgeMediaLoader
import com.example.selector.permissions.PermissionChecker
import com.example.selector.permissions.PermissionConfig
import com.example.selector.permissions.PermissionConfig.CURRENT_REQUEST_PERMISSION
import com.example.selector.permissions.PermissionResultCallback
import com.example.selector.permissions.PermissionUtil
import com.example.selector.style.PictureWindowAnimationStyle
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.SpUtils
import com.example.selector.utils.ToastUtils
import com.luck.picture.lib.entity.LocalMedia
import org.json.JSONArray
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.lang.NullPointerException
import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

/**
 * @author：luck
 * @date：2021/11/19 10:02 下午
 * @describe：PictureCommonFragment
 */
abstract class PictureCommonFragment : Fragment(),
    IPictureSelectorCommonEvent {
    /**
     * PermissionResultCallback
     */
    private var mPermissionResultCallback: PermissionResultCallback? = null

    /**
     * IBridgePictureBehavior
     */
    protected var iBridgePictureBehavior: IBridgePictureBehavior? = null

    /**
     * page
     */
    protected var mPage = 1

    /**
     * Media Loader engine
     */
    protected var mLoader: IBridgeMediaLoader? = null

    /**
     * PictureSelector Config
     */
    protected var config: PictureSelectionConfig? = null

    /**
     * Loading Dialog
     */
    private var mLoadingDialog: PictureLoadingDialog? = null

    /**
     * click sound
     */
    private var soundPool: SoundPool? = null

    /**
     * click sound effect id
     */
    private var soundID = 0

    /**
     * fragment enter anim duration
     */
    private var enterAnimDuration: Long = 0

    /**
     * tipsDialog
     */
    protected var tipsDialog: Dialog? = null
    override fun onCreateLoader() {}
    override val resourceId: Int
        get() = 0

    override fun onFragmentResume() {}
    override fun reStartSavedInstance(savedInstanceState: Bundle?) {}
    override fun onCheckOriginalChange() {}
    override fun dispatchCameraMediaResult(media: LocalMedia?) {}
    override fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia?) {}
    override fun onFixedSelectedChange(oldLocalMedia: LocalMedia?) {}
    override fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {}
    override fun handlePermissionSettingResult(permissions: Array<String>) {}
    override fun onEditMedia(intent: Intent?) {}
    override fun onEnterFragment() {}
    override fun onExitFragment() {}
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (mPermissionResultCallback != null) {
            PermissionChecker.instance?.onRequestPermissionsResult(grantResults,
                mPermissionResultCallback!!)
            mPermissionResultCallback = null
        }
    }

    /**
     * Set PermissionResultCallback
     *
     * @param callback
     */
    fun setPermissionsResultAction(callback: PermissionResultCallback?) {
        mPermissionResultCallback = callback
    }

    fun handlePermissionDenied(permissionArray: Array<String?>) {
        CURRENT_REQUEST_PERMISSION = permissionArray
        if (permissionArray != null && permissionArray.isNotEmpty()) {
            SpUtils.putBoolean(requireContext(), permissionArray[0], true)
        }
        if (PictureSelectionConfig.onPermissionDeniedListener != null) {
            onPermissionExplainEvent(false, null)
            PictureSelectionConfig.onPermissionDeniedListener
                .onDenied(this, permissionArray, PictureConfig.REQUEST_GO_SETTING,
                    object : OnCallbackListener<Boolean?> {
                        override fun onCall(data: Boolean?) {
                            if (data == true) {
                                handlePermissionSettingResult(CURRENT_REQUEST_PERMISSION)
                            }
                        }
                    })
        } else {
            if (config!!.isAllFilesAccess) {
                var isReadWrite = false
                if (permissionArray != null && permissionArray.size > 0) {
                    for (s in permissionArray) {
                        isReadWrite =
                            (TextUtils.equals(s, Manifest.permission.READ_EXTERNAL_STORAGE)
                                    || TextUtils.equals(s,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                }
                PermissionUtil.goIntentSetting(this, isReadWrite, PictureConfig.REQUEST_GO_SETTING)
            } else {
                PermissionUtil.goIntentSetting(this, PictureConfig.REQUEST_GO_SETTING)
            }
        }
    }
    /**
     * 使用PictureSelector 默认方式进入
     *
     * @return
     */
    private val isNormalDefaultEnter: Boolean
         get() = activity is PictureSelectorSupporterActivity || activity is PictureSelectorTransparentActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return if (resourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
            inflater.inflate(resourceId, container, false)
        } else super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLoadingDialog = PictureLoadingDialog(context)
        if (savedInstanceState != null) {
            config = savedInstanceState.getParcelable(PictureConfig.EXTRA_PICTURE_SELECTOR_CONFIG)
        }
        if (config == null) {
            config = PictureSelectionConfig.instance
        }
        if (PictureSelectionConfig.viewLifecycle != null) {
            PictureSelectionConfig.viewLifecycle.onViewCreated(this, view, savedInstanceState)
        }
        setRequestedOrientation()
        setTranslucentStatusBar()
        setRootViewKeyListener(requireView())
        if (config.isOpenClickSound && !config?.isOnlyCamera!!) {
            soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
            soundID = soundPool!!.load(context, R.raw.ps_click_music, 1)
        }
    }

    /**
     * 设置透明状态栏
     */
    private fun setTranslucentStatusBar() {
        if (config?.isPreviewFullScreenMode) {
            val selectMainStyle: SelectMainStyle =
                PictureSelectionConfig.selectorStyle.getSelectMainStyle()
            ImmersiveManager.translucentStatusBar(activity, selectMainStyle.isDarkStatusBarBlack)
        }
    }

    /**
     * 设置回退监听
     *
     * @param view
     */
    fun setRootViewKeyListener(view: View) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onKeyBackFragmentFinish()
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initAppLanguage()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (config != null) {
            outState.putParcelable(PictureConfig.EXTRA_PICTURE_SELECTOR_CONFIG, config)
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val windowAnimationStyle: PictureWindowAnimationStyle =
            PictureSelectionConfig.selectorStyle.getWindowAnimationStyle()
        val loadAnimation: Animation
        if (enter) {
            loadAnimation = if (windowAnimationStyle.activityEnterAnimation !== 0) {
                AnimationUtils.loadAnimation(context, windowAnimationStyle.activityEnterAnimation)
            } else {
                AnimationUtils.loadAnimation(context, R.anim.ps_anim_alpha_enter)
            }
            enterAnimationDuration = loadAnimation.duration
            onEnterFragment()
        } else {
            loadAnimation = if (windowAnimationStyle.activityExitAnimation !== 0) {
                AnimationUtils.loadAnimation(context, windowAnimationStyle.activityExitAnimation)
            } else {
                AnimationUtils.loadAnimation(context, R.anim.ps_anim_alpha_exit)
            }
            onExitFragment()
        }
        return loadAnimation
    }

    var enterAnimationDuration: Long
        get() {
            val DIFFERENCE: Long = 50
            val duration =
                if (enterAnimDuration > DIFFERENCE) enterAnimDuration - DIFFERENCE else enterAnimDuration
            return if (duration >= 0) duration else 0
        }
        set(duration) {
            enterAnimDuration = duration
        }

    override fun confirmSelect(currentMedia: LocalMedia, isSelected: Boolean): Int {
        if (PictureSelectionConfig.onSelectFilterListener != null) {
            if (PictureSelectionConfig.onSelectFilterListener.onSelectFilter(currentMedia)) {
                var isSelectLimit = false
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    isSelectLimit = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_NOT_SUPPORT_SELECT_LIMIT)
                }
                if (isSelectLimit) {
                } else {
                    ToastUtils.showToast(context, getString(R.string.ps_select_no_support))
                }
                return SelectedManager.INVALID
            }
        }
        val checkSelectValidity = isCheckSelectValidity(currentMedia, isSelected)
        if (checkSelectValidity != SelectedManager.SUCCESS) {
            return SelectedManager.INVALID
        }
        val selectedResult: MutableList<LocalMedia> = SelectedManager.getSelectedResult()
        val resultCode: Int
        if (isSelected) {
            selectedResult.remove(currentMedia)
            resultCode = SelectedManager.REMOVE
        } else {
            if (config.selectionMode === SelectModeConfig.SINGLE) {
                if (selectedResult.size > 0) {
                    sendFixedSelectedChangeEvent(selectedResult[0])
                    selectedResult.clear()
                }
            }
            selectedResult.add(currentMedia)
            currentMedia.setNum(selectedResult.size)
            resultCode = SelectedManager.ADD_SUCCESS
            playClickEffect()
        }
        sendSelectedChangeEvent(resultCode == SelectedManager.ADD_SUCCESS, currentMedia)
        return resultCode
    }

    /**
     * 验证选择的合法性
     *
     * @param currentMedia 当前选中资源
     * @param isSelected   选中或是取消
     * @return
     */
    protected fun isCheckSelectValidity(currentMedia: LocalMedia, isSelected: Boolean): Int {
        val curMimeType: String = currentMedia.getMimeType()
        val curDuration: Long = currentMedia.getDuration()
        val curFileSize: Long = currentMedia.getSize()
        val selectedResult: List<LocalMedia> = SelectedManager.getSelectedResult()
        if (config.isWithVideoImage) {
            // 共选型模式
            var selectVideoSize = 0
            for (i in selectedResult.indices) {
                val mimeType: String = selectedResult[i].getMimeType()
                if (PictureMimeType.isHasVideo(mimeType)) {
                    selectVideoSize++
                }
            }
            if (checkWithMimeTypeValidity(isSelected,
                    curMimeType,
                    selectVideoSize,
                    curFileSize,
                    curDuration)
            ) {
                return SelectedManager.INVALID
            }
        } else {
            // 单一型模式
            if (checkOnlyMimeTypeValidity(isSelected,
                    curMimeType,
                    SelectedManager.getTopResultMimeType(),
                    curFileSize,
                    curDuration)
            ) {
                return SelectedManager.INVALID
            }
        }
        return SelectedManager.SUCCESS
    }

    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    fun checkWithMimeTypeValidity(
        isSelected: Boolean,
        curMimeType: String,
        selectVideoSize: Int,
        fileSize: Long,
        duration: Long,
    ): Boolean {
        if (config.selectMaxFileSize > 0) {
            if (fileSize > config.selectMaxFileSize) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config,
                            SelectLimitType.SELECT_MAX_FILE_SIZE_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                val maxFileSize: String = PictureFileUtils.formatFileSize(config.selectMaxFileSize)
                showTipsDialog(getString(R.string.ps_select_max_size, maxFileSize))
                return true
            }
        }
        if (config.selectMinFileSize > 0) {
            if (fileSize < config.selectMinFileSize) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config,
                            SelectLimitType.SELECT_MIN_FILE_SIZE_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                val minFileSize: String = PictureFileUtils.formatFileSize(config.selectMinFileSize)
                showTipsDialog(getString(R.string.ps_select_min_size, minFileSize))
                return true
            }
        }
        if (PictureMimeType.isHasVideo(curMimeType)) {
            if (config.selectionMode === SelectModeConfig.MULTIPLE) {
                if (config.maxVideoSelectNum <= 0) {
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context,
                                    config,
                                    SelectLimitType.SELECT_NOT_WITH_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    // 如果视频可选数量是0
                    showTipsDialog(getString(R.string.ps_rule))
                    return true
                }
                if (!isSelected && SelectedManager.getSelectedResult()
                        .size() >= config.maxSelectNum
                ) {
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context,
                                    config,
                                    SelectLimitType.SELECT_MAX_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(getString(R.string.ps_message_max_num, config.maxSelectNum))
                    return true
                }
                if (!isSelected && selectVideoSize >= config.maxVideoSelectNum) {
                    // 如果选择的是视频
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context,
                                    config,
                                    SelectLimitType.SELECT_MAX_VIDEO_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(getTipsMsg(context, curMimeType, config.maxVideoSelectNum))
                    return true
                }
            }
            if (!isSelected && config.selectMinDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration) < config.selectMinDurationSecond
            ) {
                // 视频小于最低指定的长度
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config,
                            SelectLimitType.SELECT_MIN_VIDEO_SECOND_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_select_video_min_second,
                    config.selectMinDurationSecond / 1000))
                return true
            }
            if (!isSelected && config.selectMaxDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration) > config.selectMaxDurationSecond
            ) {
                // 视频时长超过了指定的长度
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config,
                            SelectLimitType.SELECT_MAX_VIDEO_SECOND_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_select_video_max_second,
                    config.selectMaxDurationSecond / 1000))
                return true
            }
        } else {
            if (config.selectionMode === SelectModeConfig.MULTIPLE) {
                if (!isSelected && SelectedManager.getSelectedResult()
                        .size() >= config.maxSelectNum
                ) {
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context, config,
                                    SelectLimitType.SELECT_MAX_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(getString(R.string.ps_message_max_num, config.maxSelectNum))
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("StringFormatInvalid")
    fun checkOnlyMimeTypeValidity(
        isSelected: Boolean,
        curMimeType: String,
        existMimeType: String?,
        fileSize: Long,
        duration: Long,
    ): Boolean {
        if (PictureMimeType.isMimeTypeSame(existMimeType, curMimeType)) {
            // ignore
        } else {
            if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                    .onSelectLimitTips(context,
                        config,
                        SelectLimitType.SELECT_NOT_WITH_SELECT_LIMIT)
                if (isSelectLimit) {
                    return true
                }
            }
            showTipsDialog(getString(R.string.ps_rule))
            return true
        }
        if (config.selectMaxFileSize > 0) {
            if (fileSize > config.selectMaxFileSize) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config,
                            SelectLimitType.SELECT_MAX_FILE_SIZE_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                val maxFileSize: String = PictureFileUtils.formatFileSize(config.selectMaxFileSize)
                showTipsDialog(getString(R.string.ps_select_max_size, maxFileSize))
                return true
            }
        }
        if (config.selectMinFileSize > 0) {
            if (fileSize < config.selectMinFileSize) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config,
                            SelectLimitType.SELECT_MIN_FILE_SIZE_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                val minFileSize: String = PictureFileUtils.formatFileSize(config.selectMinFileSize)
                showTipsDialog(getString(R.string.ps_select_min_size, minFileSize))
                return true
            }
        }
        if (PictureMimeType.isHasVideo(curMimeType)) {
            if (config.selectionMode === SelectModeConfig.MULTIPLE) {
                config.maxVideoSelectNum =
                    if (config.maxVideoSelectNum > 0) config.maxVideoSelectNum else config.maxSelectNum
                if (!isSelected && SelectedManager.getSelectCount() >= config.maxVideoSelectNum) {
                    // 如果先选择的是视频
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context,
                                    config,
                                    SelectLimitType.SELECT_MAX_VIDEO_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(getTipsMsg(context, curMimeType, config.maxVideoSelectNum))
                    return true
                }
            }
            if (!isSelected && config.selectMinDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration) < config.selectMinDurationSecond
            ) {
                // 视频小于最低指定的长度
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MIN_VIDEO_SECOND_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_select_video_min_second,
                    config.selectMinDurationSecond / 1000))
                return true
            }
            if (!isSelected && config.selectMaxDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration) > config.selectMaxDurationSecond
            ) {
                // 视频时长超过了指定的长度
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MAX_VIDEO_SECOND_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_select_video_max_second,
                    config.selectMaxDurationSecond / 1000))
                return true
            }
        } else if (PictureMimeType.isHasAudio(curMimeType)) {
            if (config.selectionMode === SelectModeConfig.MULTIPLE) {
                if (!isSelected && SelectedManager.getSelectedResult()
                        .size() >= config.maxSelectNum
                ) {
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context,
                                    config,
                                    SelectLimitType.SELECT_MAX_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(getTipsMsg(context, curMimeType, config.maxSelectNum))
                    return true
                }
            }
            if (!isSelected && config.selectMinDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration) < config.selectMinDurationSecond
            ) {
                // 音频小于最低指定的长度
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MIN_AUDIO_SECOND_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_select_audio_min_second,
                    config.selectMinDurationSecond / 1000))
                return true
            }
            if (!isSelected && config.selectMaxDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration) > config.selectMaxDurationSecond
            ) {
                // 音频时长超过了指定的长度
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MAX_AUDIO_SECOND_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_select_audio_max_second,
                    config.selectMaxDurationSecond / 1000))
                return true
            }
        } else {
            if (config.selectionMode === SelectModeConfig.MULTIPLE) {
                if (!isSelected && SelectedManager.getSelectedResult()
                        .size() >= config.maxSelectNum
                ) {
                    if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                        val isSelectLimit: Boolean =
                            PictureSelectionConfig.onSelectLimitTipsListener
                                .onSelectLimitTips(context,
                                    config,
                                    SelectLimitType.SELECT_MAX_SELECT_LIMIT)
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(getTipsMsg(context, curMimeType, config.maxSelectNum))
                    return true
                }
            }
        }
        return false
    }

    /**
     * 提示Dialog
     *
     * @param tips
     */
    private fun showTipsDialog(tips: String) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        try {
            if (tipsDialog != null && tipsDialog!!.isShowing) {
                return
            }
            tipsDialog = RemindDialog.buildDialog(context, tips)
            tipsDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun sendSelectedChangeEvent(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val fragments = activity!!.supportFragmentManager.fragments
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onSelectedChange(isAddRemove, currentMedia)
                }
            }
        }
    }

    override fun sendFixedSelectedChangeEvent(currentMedia: LocalMedia?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val fragments = activity!!.supportFragmentManager.fragments
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onFixedSelectedChange(currentMedia)
                }
            }
        }
    }

    override fun sendSelectedOriginalChangeEvent() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val fragments = activity!!.supportFragmentManager.fragments
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onCheckOriginalChange()
                }
            }
        }
    }

    override fun openSelectedCamera() {
        when (config.chooseMode) {
            SelectMimeType.TYPE_ALL -> if (config.ofAllCameraType === SelectMimeType.ofImage()) {
                openImageCamera()
            } else if (config.ofAllCameraType === SelectMimeType.ofVideo()) {
                openVideoCamera()
            } else {
                onSelectedOnlyCamera()
            }
            SelectMimeType.TYPE_IMAGE -> openImageCamera()
            SelectMimeType.TYPE_VIDEO -> openVideoCamera()
            SelectMimeType.TYPE_AUDIO -> openSoundRecording()
            else -> {}
        }
    }

    override fun onSelectedOnlyCamera() {
        val selectedDialog: PhotoItemSelectedDialog = PhotoItemSelectedDialog.newInstance()
        selectedDialog.setOnItemClickListener(object : OnItemClickListener() {
            fun onItemClick(v: View?, position: Int) {
                when (position) {
                    PhotoItemSelectedDialog.IMAGE_CAMERA -> if (PictureSelectionConfig.onCameraInterceptListener != null) {
                        onInterceptCameraEvent(SelectMimeType.TYPE_IMAGE)
                    } else {
                        openImageCamera()
                    }
                    PhotoItemSelectedDialog.VIDEO_CAMERA -> if (PictureSelectionConfig.onCameraInterceptListener != null) {
                        onInterceptCameraEvent(SelectMimeType.TYPE_VIDEO)
                    } else {
                        openVideoCamera()
                    }
                    else -> {}
                }
            }
        })
        selectedDialog.setOnDismissListener(object : OnDismissListener() {
            fun onDismiss(isCancel: Boolean, dialog: DialogInterface?) {
                if (config.isOnlyCamera && isCancel) {
                    onKeyBackFragmentFinish()
                }
            }
        })
        selectedDialog.show(childFragmentManager, "PhotoItemSelectedDialog")
    }

    override fun openImageCamera() {
        onPermissionExplainEvent(true, PermissionConfig.CAMERA)
        if (PictureSelectionConfig.onPermissionsEventListener != null) {
            onApplyPermissionsEvent(PermissionEvent.EVENT_IMAGE_CAMERA, PermissionConfig.CAMERA)
        } else {
            PermissionChecker.getInstance().requestPermissions(this, PermissionConfig.CAMERA,
                object : PermissionResultCallback() {
                    fun onGranted() {
                        startCameraImageCapture()
                    }

                    fun onDenied() {
                        handlePermissionDenied(PermissionConfig.CAMERA)
                    }
                })
        }
    }

    /**
     * Start ACTION_IMAGE_CAPTURE
     */
    protected fun startCameraImageCapture() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            onPermissionExplainEvent(false, null)
            if (PictureSelectionConfig.onCameraInterceptListener != null) {
                onInterceptCameraEvent(SelectMimeType.TYPE_IMAGE)
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(activity!!.packageManager) != null) {
                    ForegroundService.startForegroundService(context)
                    val imageUri: Uri = MediaStoreUtils.createCameraOutImageUri(
                        context, config)
                    if (imageUri != null) {
                        if (config.isCameraAroundState) {
                            cameraIntent.putExtra(PictureConfig.CAMERA_FACING,
                                PictureConfig.CAMERA_BEFORE)
                        }
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA)
                    }
                }
            }
        }
    }

    override fun openVideoCamera() {
        onPermissionExplainEvent(true, PermissionConfig.CAMERA)
        if (PictureSelectionConfig.onPermissionsEventListener != null) {
            onApplyPermissionsEvent(PermissionEvent.EVENT_VIDEO_CAMERA, PermissionConfig.CAMERA)
        } else {
            PermissionChecker.getInstance().requestPermissions(this, PermissionConfig.CAMERA,
                object : PermissionResultCallback() {
                    fun onGranted() {
                        startCameraVideoCapture()
                    }

                    fun onDenied() {
                        handlePermissionDenied(PermissionConfig.CAMERA)
                    }
                })
        }
    }

    /**
     * Start ACTION_VIDEO_CAPTURE
     */
    protected fun startCameraVideoCapture() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            onPermissionExplainEvent(false, null)
            if (PictureSelectionConfig.onCameraInterceptListener != null) {
                onInterceptCameraEvent(SelectMimeType.TYPE_VIDEO)
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                if (cameraIntent.resolveActivity(activity!!.packageManager) != null) {
                    ForegroundService.startForegroundService(context)
                    val videoUri: Uri = MediaStoreUtils.createCameraOutVideoUri(
                        context, config)
                    if (videoUri != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                        if (config.isCameraAroundState) {
                            cameraIntent.putExtra(PictureConfig.CAMERA_FACING,
                                PictureConfig.CAMERA_BEFORE)
                        }
                        cameraIntent.putExtra(PictureConfig.EXTRA_QUICK_CAPTURE,
                            config.isQuickCapture)
                        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,
                            config.recordVideoMaxSecond)
                        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, config.videoQuality)
                        startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA)
                    }
                }
            }
        }
    }

    override fun openSoundRecording() {
        if (PictureSelectionConfig.onRecordAudioListener != null) {
            ForegroundService.startForegroundService(context)
            PictureSelectionConfig.onRecordAudioListener.onRecordAudio(this,
                PictureConfig.REQUEST_CAMERA)
        } else {
            throw NullPointerException(OnRecordAudioInterceptListener::class.java.getSimpleName() + " interface needs to be implemented for recording")
        }
    }

    /**
     * 拦截相机事件并处理返回结果
     */
    override fun onInterceptCameraEvent(cameraMode: Int) {
        ForegroundService.startForegroundService(context)
        PictureSelectionConfig.onCameraInterceptListener.openCamera(this,
            cameraMode,
            PictureConfig.REQUEST_CAMERA)
    }

    /**
     * 权限申请
     *
     * @param permissionArray
     */
    override fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String>) {
        PictureSelectionConfig.onPermissionsEventListener.requestPermission(this, permissionArray,
            object : OnRequestPermissionListener() {
                fun onCall(permissionArray: Array<String?>, isResult: Boolean) {
                    if (isResult) {
                        if (event == PermissionEvent.EVENT_VIDEO_CAMERA) {
                            startCameraVideoCapture()
                        } else {
                            startCameraImageCapture()
                        }
                    } else {
                        handlePermissionDenied(permissionArray)
                    }
                }
            })
    }

    /**
     * 权限说明
     *
     * @param permissionArray
     */
    fun onPermissionExplainEvent(
        isDisplayExplain: Boolean,
        permissionArray: Array<String>,
    ) {
        if (PictureSelectionConfig.onPermissionDescriptionListener != null) {
            if (isDisplayExplain) {
                if (PermissionChecker.isCheckSelfPermission(context, permissionArray)) {
                    SpUtils.putBoolean(context, permissionArray!![0], false)
                } else {
                    if (!SpUtils.getBoolean(context, permissionArray!![0], false)) {
                        PictureSelectionConfig.onPermissionDescriptionListener.onPermissionDescription(
                            this,
                            permissionArray)
                    }
                }
            } else {
                PictureSelectionConfig.onPermissionDescriptionListener.onDismiss(this)
            }
        }
    }

    /**
     * 点击选择的音效
     */
    private fun playClickEffect() {
        if (soundPool != null && config.isOpenClickSound) {
            soundPool!!.play(soundID, 0.1f, 0.5f, 0, 1, 1f)
        }
    }

    /**
     * 释放音效资源
     */
    private fun releaseSoundPool() {
        try {
            if (soundPool != null) {
                soundPool!!.release()
                soundPool = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ForegroundService.stopService(context)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.REQUEST_CAMERA) {
                dispatchHandleCamera(data)
            } else if (requestCode == Crop.REQUEST_EDIT_CROP) {
                onEditMedia(data)
            } else if (requestCode == Crop.REQUEST_CROP) {
                val selectedResult: List<LocalMedia> = SelectedManager.getSelectedResult()
                try {
                    if (selectedResult.size == 1) {
                        val media: LocalMedia = selectedResult[0]
                        val output: Uri = Crop.getOutput(data)
                        media.setCutPath(if (output != null) output.path else "")
                        media.setCut(!TextUtils.isEmpty(media.getCutPath()))
                        media.setCropImageWidth(Crop.getOutputImageWidth(data))
                        media.setCropImageHeight(Crop.getOutputImageHeight(data))
                        media.setCropOffsetX(Crop.getOutputImageOffsetX(data))
                        media.setCropOffsetY(Crop.getOutputImageOffsetY(data))
                        media.setCropResultAspectRatio(Crop.getOutputCropAspectRatio(data))
                        media.setCustomData(Crop.getOutputCustomExtraData(data))
                        media.setSandboxPath(media.getCutPath())
                    } else {
                        var extra = data!!.getStringExtra(MediaStore.EXTRA_OUTPUT)
                        if (TextUtils.isEmpty(extra)) {
                            extra = data.getStringExtra(CustomIntentKey.EXTRA_OUTPUT_URI)
                        }
                        val array = JSONArray(extra)
                        if (array.length() == selectedResult.size) {
                            for (i in selectedResult.indices) {
                                val media: LocalMedia = selectedResult[i]
                                val item = array.optJSONObject(i)
                                media.setCutPath(item.optString(CustomIntentKey.EXTRA_OUT_PUT_PATH))
                                media.setCut(!TextUtils.isEmpty(media.getCutPath()))
                                media.setCropImageWidth(item.optInt(CustomIntentKey.EXTRA_IMAGE_WIDTH))
                                media.setCropImageHeight(item.optInt(CustomIntentKey.EXTRA_IMAGE_HEIGHT))
                                media.setCropOffsetX(item.optInt(CustomIntentKey.EXTRA_OFFSET_X))
                                media.setCropOffsetY(item.optInt(CustomIntentKey.EXTRA_OFFSET_Y))
                                media.setCropResultAspectRatio(item.optDouble(CustomIntentKey.EXTRA_ASPECT_RATIO)
                                    .toFloat())
                                media.setCustomData(item.optString(CustomIntentKey.EXTRA_CUSTOM_EXTRA_DATA))
                                media.setSandboxPath(media.getCutPath())
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtils.showToast(context, e.message)
                }
                val result: ArrayList<LocalMedia> = ArrayList<Any?>(selectedResult)
                if (checkCompressValidity()) {
                    onCompress(result)
                } else if (checkOldCompressValidity()) {
                    onOldCompress(result)
                } else {
                    onResultEvent(result)
                }
            }
        } else if (resultCode == Crop.RESULT_CROP_ERROR) {
            val throwable = if (data != null) Crop.getError(data) else Throwable("image crop error")
            if (throwable != null) {
                ToastUtils.showToast(context, throwable.message)
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == PictureConfig.REQUEST_CAMERA) {
                MediaUtils.deleteUri(context, config.cameraPath)
            } else if (requestCode == PictureConfig.REQUEST_GO_SETTING) {
                handlePermissionSettingResult(PermissionConfig.CURRENT_REQUEST_PERMISSION)
            }
        }
    }

    /**
     * 相机事件回调处理
     */
    private fun dispatchHandleCamera(intent: Intent?) {
        PictureThreadUtils.executeByIo(object : SimpleTask<LocalMedia?>() {
            fun doInBackground(): LocalMedia? {
                val outputPath = getOutputPath(intent)
                if (!TextUtils.isEmpty(outputPath)) {
                    config.cameraPath = outputPath
                }
                if (TextUtils.isEmpty(config.cameraPath)) {
                    return null
                }
                if (config.chooseMode === SelectMimeType.ofAudio()) {
                    copyOutputAudioToDir()
                }
                return buildLocalMedia(config.cameraPath)
            }

            fun onSuccess(result: LocalMedia?) {
                PictureThreadUtils.cancel(this)
                if (result != null) {
                    onScannerScanFile(result)
                    dispatchCameraMediaResult(result)
                }
            }
        })
    }

    /**
     * copy录音文件至指定目录
     */
    private fun copyOutputAudioToDir() {
        try {
            if (!TextUtils.isEmpty(config.outPutAudioDir) && PictureMimeType.isContent(config.cameraPath)) {
                val inputStream: InputStream =
                    PictureContentResolver.getContentResolverOpenInputStream(
                        context,
                        Uri.parse(config.cameraPath))
                val audioFileName: String
                audioFileName = if (TextUtils.isEmpty(config.outPutAudioFileName)) {
                    ""
                } else {
                    if (config.isOnlyCamera) config.outPutAudioFileName else System.currentTimeMillis()
                        .toString() + "_" + config.outPutAudioFileName
                }
                val outputFile: File = PictureFileUtils.createCameraFile(context,
                    config.chooseMode, audioFileName, "", config.outPutAudioDir)
                val outputStream = FileOutputStream(outputFile.absolutePath)
                val isCopyStatus: Boolean =
                    PictureFileUtils.writeFileFromIS(inputStream, outputStream)
                if (isCopyStatus) {
                    MediaUtils.deleteUri(context, config.cameraPath)
                    config.cameraPath = outputFile.absolutePath
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 尝试匹配查找自定义相机返回的路径
     *
     * @param data
     * @return
     */
    protected fun getOutputPath(data: Intent?): String? {
        if (data == null) {
            return null
        }
        val outPutUri = data.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
            ?: return null
        return if (PictureMimeType.isContent(outPutUri.toString())) outPutUri.toString() else outPutUri.path
    }

    /**
     * 刷新相册
     *
     * @param media 要刷新的对象
     */
    private fun onScannerScanFile(media: LocalMedia) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        if (SdkVersionUtils.isQ()) {
            if (PictureMimeType.isHasVideo(media.getMimeType()) && PictureMimeType.isContent(config.cameraPath)) {
                PictureMediaScannerConnection(activity, media.getRealPath())
            }
        } else {
            val path: String =
                if (PictureMimeType.isContent(config.cameraPath)) media.getRealPath() else config.cameraPath
            PictureMediaScannerConnection(activity, path)
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                val dirFile = File(path)
                val lastImageId: Int = MediaUtils.getDCIMLastImageId(context, dirFile.parent)
                if (lastImageId != -1) {
                    MediaUtils.removeMedia(context, lastImageId)
                }
            }
        }
    }

    /**
     * buildLocalMedia
     *
     * @param absolutePath
     */
    protected fun buildLocalMedia(absolutePath: String?): LocalMedia {
        val media: LocalMedia = LocalMedia.generateLocalMedia(context, absolutePath)
        media.setChooseModel(config.chooseMode)
        if (SdkVersionUtils.isQ() && !PictureMimeType.isContent(absolutePath)) {
            media.setSandboxPath(absolutePath)
        } else {
            media.setSandboxPath(null)
        }
        if (config.isCameraRotateImage && PictureMimeType.isHasImage(media.getMimeType())) {
            BitmapUtils.rotateImage(context, absolutePath)
        }
        return media
    }

    /**
     * 验证完成选择的先决条件
     *
     * @return
     */
    private fun checkCompleteSelectLimit(): Boolean {
        if (config.selectionMode !== SelectModeConfig.MULTIPLE || config.isOnlyCamera) {
            return false
        }
        if (config.isWithVideoImage) {
            // 共选型模式
            val selectedResult: ArrayList<LocalMedia> = SelectedManager.getSelectedResult()
            var selectImageSize = 0
            var selectVideoSize = 0
            for (i in selectedResult.indices) {
                val mimeType: String = selectedResult[i].getMimeType()
                if (PictureMimeType.isHasVideo(mimeType)) {
                    selectVideoSize++
                } else {
                    selectImageSize++
                }
            }
            if (config.minSelectNum > 0) {
                if (selectImageSize < config.minSelectNum) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config, SelectLimitType.SELECT_MIN_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                    showTipsDialog(getString(R.string.ps_min_img_num,
                        java.lang.String.valueOf(config.minSelectNum)))
                    return true
                }
            }
            if (config.minVideoSelectNum > 0) {
                if (selectVideoSize < config.minVideoSelectNum) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MIN_VIDEO_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                    showTipsDialog(
                        getString(R.string.ps_min_video_num,
                            java.lang.String.valueOf(config.minVideoSelectNum)))
                    return true
                }
            }
        } else {
            // 单类型模式
            val mimeType: String = SelectedManager.getTopResultMimeType()
            if (PictureMimeType.isHasImage(mimeType) && config.minSelectNum > 0 && SelectedManager.getSelectCount() < config.minSelectNum) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context, config, SelectLimitType.SELECT_MIN_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_min_img_num,
                    java.lang.String.valueOf(config.minSelectNum)))
                return true
            }
            if (PictureMimeType.isHasVideo(mimeType) && config.minVideoSelectNum > 0 && SelectedManager.getSelectCount() < config.minVideoSelectNum) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MIN_VIDEO_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_min_video_num,
                    java.lang.String.valueOf(config.minVideoSelectNum)))
                return true
            }
            if (PictureMimeType.isHasAudio(mimeType) && config.minAudioSelectNum > 0 && SelectedManager.getSelectCount() < config.minAudioSelectNum) {
                if (PictureSelectionConfig.onSelectLimitTipsListener != null) {
                    val isSelectLimit: Boolean = PictureSelectionConfig.onSelectLimitTipsListener
                        .onSelectLimitTips(context,
                            config,
                            SelectLimitType.SELECT_MIN_AUDIO_SELECT_LIMIT)
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(getString(R.string.ps_min_audio_num,
                    java.lang.String.valueOf(config.minAudioSelectNum)))
                return true
            }
        }
        return false
    }

    /**
     * 分发处理结果，比如压缩、裁剪、沙盒路径转换
     */
    protected fun dispatchTransformResult() {
        if (checkCompleteSelectLimit()) {
            return
        }
        val selectedResult: ArrayList<LocalMedia> = SelectedManager.getSelectedResult()
        val result: ArrayList<LocalMedia> = ArrayList<Any?>(selectedResult)
        if (checkCropValidity()) {
            onCrop(result)
        } else if (checkOldCropValidity()) {
            onOldCrop(result)
        } else if (checkCompressValidity()) {
            onCompress(result)
        } else if (checkOldCompressValidity()) {
            onOldCompress(result)
        } else {
            onResultEvent(result)
        }
    }

    fun onCrop(result: ArrayList<LocalMedia>) {
        var srcUri: Uri? = null
        var destinationUri: Uri? = null
        val dataCropSource = ArrayList<String>()
        for (i in result.indices) {
            val media: LocalMedia = result[i]
            dataCropSource.add(media.getAvailablePath())
            if (srcUri == null && PictureMimeType.isHasImage(media.getMimeType())) {
                val currentCropPath: String = media.getAvailablePath()
                srcUri =
                    if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(
                            currentCropPath)
                    ) {
                        Uri.parse(currentCropPath)
                    } else {
                        Uri.fromFile(File(currentCropPath))
                    }
                val fileName: String = DateUtils.getCreateFileName("CROP_").toString() + ".jpg"
                val externalFilesDir =
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val outputFile = File(externalFilesDir!!.absolutePath, fileName)
                destinationUri = Uri.fromFile(outputFile)
            }
        }
        PictureSelectionConfig.cropFileEngine.onStartCrop(this,
            srcUri,
            destinationUri,
            dataCropSource,
            Crop.REQUEST_CROP)
    }

    fun onOldCrop(result: ArrayList<LocalMedia>) {
        var currentLocalMedia: LocalMedia? = null
        for (i in result.indices) {
            val item: LocalMedia = result[i]
            if (PictureMimeType.isHasImage(result[i].getMimeType())) {
                currentLocalMedia = item
                break
            }
        }
        PictureSelectionConfig.cropEngine.onStartCrop(this,
            currentLocalMedia,
            result,
            Crop.REQUEST_CROP)
    }

    fun onCompress(result: ArrayList<LocalMedia>) {
        showLoading()
        val queue: ConcurrentHashMap<String, LocalMedia> = ConcurrentHashMap<String, LocalMedia>()
        val source = ArrayList<Uri>()
        for (i in result.indices) {
            val media: LocalMedia = result[i]
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                val availablePath: String = media.getAvailablePath()
                val uri =
                    if (PictureMimeType.isContent(availablePath)) Uri.parse(availablePath) else Uri.fromFile(
                        File(availablePath))
                source.add(uri)
                queue[availablePath] = media
            }
        }
        if (queue.size == 0) {
            onResultEvent(result)
        } else {
            PictureSelectionConfig.compressFileEngine.onStartCompress(context,
                source,
                object : OnKeyValueResultCallbackListener() {
                    fun onCallback(srcPath: String, compressPath: String?) {
                        if (TextUtils.isEmpty(srcPath)) {
                            onResultEvent(result)
                        } else {
                            val media: LocalMedia? = queue[srcPath]
                            if (media != null) {
                                media.setCompressPath(compressPath)
                                media.setCompressed(!TextUtils.isEmpty(compressPath))
                                media.setSandboxPath(if (SdkVersionUtils.isQ()) media.getCompressPath() else null)
                                queue.remove(srcPath)
                            }
                            if (queue.size == 0) {
                                onResultEvent(result)
                            }
                        }
                    }
                })
        }
    }

    override fun onOldCompress(result: ArrayList<LocalMedia>?) {
        showLoading()
        PictureSelectionConfig.compressEngine.onStartCompress(context, result,
            object : OnCallbackListener<ArrayList<LocalMedia?>?>() {
                fun onCall(result: ArrayList<LocalMedia>) {
                    onResultEvent(result)
                }
            })
    }

    override fun checkCropValidity(): Boolean {
        if (PictureSelectionConfig.cropFileEngine != null) {
            val filterSet = HashSet<String>()
            val filters: List<String> = config.skipCropList
            if (filters != null && filters.size > 0) {
                filterSet.addAll(filters)
            }
            return if (SelectedManager.getSelectCount() === 1) {
                val mimeType: String = SelectedManager.getTopResultMimeType()
                val isHasImage: Boolean = PictureMimeType.isHasImage(mimeType)
                if (isHasImage) {
                    if (filterSet.contains(mimeType)) {
                        return false
                    }
                }
                isHasImage
            } else {
                var notSupportCropCount = 0
                for (i in 0 until SelectedManager.getSelectCount()) {
                    val media: LocalMedia = SelectedManager.getSelectedResult().get(i)
                    if (PictureMimeType.isHasImage(media.getMimeType())) {
                        if (filterSet.contains(media.getMimeType())) {
                            notSupportCropCount++
                        }
                    }
                }
                notSupportCropCount != SelectedManager.getSelectCount()
            }
        }
        return false
    }

    override fun checkOldCropValidity(): Boolean {
        if (PictureSelectionConfig.cropEngine != null) {
            val filterSet = HashSet<String>()
            val filters: List<String> = config.skipCropList
            if (filters != null && filters.size > 0) {
                filterSet.addAll(filters)
            }
            return if (SelectedManager.getSelectCount() === 1) {
                val mimeType: String = SelectedManager.getTopResultMimeType()
                val isHasImage: Boolean = PictureMimeType.isHasImage(mimeType)
                if (isHasImage) {
                    if (filterSet.contains(mimeType)) {
                        return false
                    }
                }
                isHasImage
            } else {
                var notSupportCropCount = 0
                for (i in 0 until SelectedManager.getSelectCount()) {
                    val media: LocalMedia = SelectedManager.getSelectedResult().get(i)
                    if (PictureMimeType.isHasImage(media.getMimeType())) {
                        if (filterSet.contains(media.getMimeType())) {
                            notSupportCropCount++
                        }
                    }
                }
                notSupportCropCount != SelectedManager.getSelectCount()
            }
        }
        return false
    }

    override fun checkCompressValidity(): Boolean {
        if (PictureSelectionConfig.compressFileEngine != null) {
            for (i in 0 until SelectedManager.getSelectCount()) {
                val media: LocalMedia = SelectedManager.getSelectedResult().get(i)
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    return true
                }
            }
        }
        return false
    }

    override fun checkOldCompressValidity(): Boolean {
        if (PictureSelectionConfig.compressEngine != null) {
            for (i in 0 until SelectedManager.getSelectCount()) {
                val media: LocalMedia = SelectedManager.getSelectedResult().get(i)
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    return true
                }
            }
        }
        return false
    }

    override fun checkTransformSandboxFile(): Boolean {
        return SdkVersionUtils.isQ() && PictureSelectionConfig.uriToFileTransformEngine != null
    }

    override fun checkOldTransformSandboxFile(): Boolean {
        return SdkVersionUtils.isQ() && PictureSelectionConfig.sandboxFileEngine != null
    }

    override fun checkAddBitmapWatermark(): Boolean {
        return PictureSelectionConfig.onBitmapWatermarkListener != null
    }

    override fun checkVideoThumbnail(): Boolean {
        return PictureSelectionConfig.onVideoThumbnailEventListener != null
    }

    /**
     * 处理视频的缩略图
     *
     * @param result
     */
    private fun videoThumbnail(result: ArrayList<LocalMedia>) {
        val queue: ConcurrentHashMap<String, LocalMedia> = ConcurrentHashMap<String, LocalMedia>()
        for (i in result.indices) {
            val media: LocalMedia = result[i]
            val availablePath: String = media.getAvailablePath()
            if (PictureMimeType.isHasVideo(media.getMimeType()) || PictureMimeType.isUrlHasVideo(
                    availablePath)
            ) {
                queue[availablePath] = media
            }
        }
        if (queue.size == 0) {
            onCallBackResult(result)
        } else {
            for ((key) in queue) {
                PictureSelectionConfig.onVideoThumbnailEventListener.onVideoThumbnail(context,
                    key, object : OnKeyValueResultCallbackListener() {
                        fun onCallback(srcPath: String, resultPath: String?) {
                            val media: LocalMedia? = queue[srcPath]
                            if (media != null) {
                                media.setVideoThumbnailPath(resultPath)
                                queue.remove(srcPath)
                            }
                            if (queue.size == 0) {
                                onCallBackResult(result)
                            }
                        }
                    })
            }
        }
    }

    /**
     * 添加水印
     */
    private fun addBitmapWatermark(result: ArrayList<LocalMedia>) {
        val queue: ConcurrentHashMap<String, LocalMedia> = ConcurrentHashMap<String, LocalMedia>()
        for (i in result.indices) {
            val media: LocalMedia = result[i]
            if (PictureMimeType.isHasAudio(media.getMimeType())) {
                continue
            }
            val availablePath: String = media.getAvailablePath()
            queue[availablePath] = media
        }
        if (queue.size == 0) {
            dispatchWatermarkResult(result)
        } else {
            for ((srcPath, media) in queue) {
                PictureSelectionConfig.onBitmapWatermarkListener.onAddBitmapWatermark(context,
                    srcPath, media.getMimeType(), object : OnKeyValueResultCallbackListener() {
                        fun onCallback(srcPath: String, resultPath: String?) {
                            if (TextUtils.isEmpty(srcPath)) {
                                dispatchWatermarkResult(result)
                            } else {
                                val media: LocalMedia? = queue[srcPath]
                                if (media != null) {
                                    media.setWatermarkPath(resultPath)
                                    queue.remove(srcPath)
                                }
                                if (queue.size == 0) {
                                    dispatchWatermarkResult(result)
                                }
                            }
                        }
                    })
            }
        }
    }

    /**
     * dispatchUriToFileTransformResult
     *
     * @param result
     */
    private fun dispatchUriToFileTransformResult(result: ArrayList<LocalMedia>) {
        showLoading()
        if (checkAddBitmapWatermark()) {
            addBitmapWatermark(result)
        } else if (checkVideoThumbnail()) {
            videoThumbnail(result)
        } else {
            onCallBackResult(result)
        }
    }

    /**
     * dispatchWatermarkResult
     *
     * @param result
     */
    private fun dispatchWatermarkResult(result: ArrayList<LocalMedia>) {
        if (checkVideoThumbnail()) {
            videoThumbnail(result)
        } else {
            onCallBackResult(result)
        }
    }

    /**
     * SDK > 29 把外部资源copy一份至应用沙盒内
     *
     * @param result
     */
    private fun uriToFileTransform29(result: ArrayList<LocalMedia>) {
        showLoading()
        val queue: ConcurrentHashMap<String, LocalMedia> = ConcurrentHashMap<String, LocalMedia>()
        for (i in result.indices) {
            val media: LocalMedia = result[i]
            queue[media.getPath()] = media
        }
        if (queue.size == 0) {
            dispatchUriToFileTransformResult(result)
        } else {
            PictureThreadUtils.executeByIo(object : SimpleTask<ArrayList<LocalMedia?>?>() {
                fun doInBackground(): ArrayList<LocalMedia> {
                    for ((_, media) in queue) {
                        if (config.isCheckOriginalImage || TextUtils.isEmpty(media.getSandboxPath())) {
                            PictureSelectionConfig.uriToFileTransformEngine.onUriToFileAsyncTransform(
                                context,
                                media.getPath(),
                                media.getMimeType(),
                                object : OnKeyValueResultCallbackListener() {
                                    fun onCallback(srcPath: String, resultPath: String?) {
                                        if (TextUtils.isEmpty(srcPath)) {
                                            return
                                        }
                                        val media: LocalMedia? = queue[srcPath]
                                        if (media != null) {
                                            if (TextUtils.isEmpty(media.getSandboxPath())) {
                                                media.setSandboxPath(resultPath)
                                            }
                                            if (config.isCheckOriginalImage) {
                                                media.setOriginalPath(resultPath)
                                                media.setOriginal(!TextUtils.isEmpty(resultPath))
                                            }
                                            queue.remove(srcPath)
                                        }
                                    }
                                })
                        }
                    }
                    return result
                }

                fun onSuccess(result: ArrayList<LocalMedia>) {
                    PictureThreadUtils.cancel(this)
                    dispatchUriToFileTransformResult(result)
                }
            })
        }
    }

    /**
     * SDK > 29 把外部资源copy一份至应用沙盒内
     *
     * @param result
     */
    @Deprecated("")
    private fun copyExternalPathToAppInDirFor29(result: ArrayList<LocalMedia>) {
        showLoading()
        PictureThreadUtils.executeByIo(object : SimpleTask<ArrayList<LocalMedia?>?>() {
            fun doInBackground(): ArrayList<LocalMedia> {
                for (i in result.indices) {
                    val media: LocalMedia = result[i]
                    PictureSelectionConfig.sandboxFileEngine.onStartSandboxFileTransform(context,
                        config.isCheckOriginalImage,
                        i,
                        media,
                        object : OnCallbackIndexListener<LocalMedia?>() {
                            fun onCall(data: LocalMedia, index: Int) {
                                val media: LocalMedia = result[index]
                                media.setSandboxPath(data.getSandboxPath())
                                if (config.isCheckOriginalImage) {
                                    media.setOriginalPath(data.getOriginalPath())
                                    media.setOriginal(!TextUtils.isEmpty(data.getOriginalPath()))
                                }
                            }
                        })
                }
                return result
            }

            fun onSuccess(result: ArrayList<LocalMedia>) {
                PictureThreadUtils.cancel(this)
                dispatchUriToFileTransformResult(result)
            }
        })
    }

    /**
     * 构造原图数据
     *
     * @param result
     */
    private fun mergeOriginalImage(result: ArrayList<LocalMedia>) {
        if (config.isCheckOriginalImage) {
            for (i in result.indices) {
                val media: LocalMedia = result[i]
                media.setOriginal(true)
                media.setOriginalPath(media.getPath())
            }
        }
    }

    /**
     * 返回处理完成后的选择结果
     */
    fun onResultEvent(result: ArrayList<LocalMedia>) {
        if (checkTransformSandboxFile()) {
            uriToFileTransform29(result)
        } else if (checkOldTransformSandboxFile()) {
            copyExternalPathToAppInDirFor29(result)
        } else {
            mergeOriginalImage(result)
            dispatchUriToFileTransformResult(result)
        }
    }

    /**
     * 返回结果
     */
    private fun onCallBackResult(result: ArrayList<LocalMedia>) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            dismissLoading()
            if (config.isActivityResultBack) {
                activity!!.setResult(Activity.RESULT_OK, PictureSelector.putIntentResult(result))
                onSelectFinish(Activity.RESULT_OK, result)
            } else {
                if (PictureSelectionConfig.onResultCallListener != null) {
                    PictureSelectionConfig.onResultCallListener.onResult(result)
                }
            }
            onExitPictureSelector()
        }
    }

    /**
     * set app language
     */
    override fun initAppLanguage() {
        val config: PictureSelectionConfig = PictureSelectionConfig.getInstance()
        if (config.language !== LanguageConfig.UNKNOWN_LANGUAGE) {
            PictureLanguageUtils.setAppLanguage(activity, config.language)
        }
    }

    override fun onRecreateEngine() {
        createImageLoaderEngine()
        createCompressEngine()
        createSandboxFileEngine()
        createLoaderDataEngine()
        createResultCallbackListener()
        createLayoutResourceListener()
    }

    override fun onKeyBackFragmentFinish() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (config.isActivityResultBack) {
                activity!!.setResult(Activity.RESULT_CANCELED)
                onSelectFinish(Activity.RESULT_CANCELED, null)
            } else {
                if (PictureSelectionConfig.onResultCallListener != null) {
                    PictureSelectionConfig.onResultCallListener.onCancel()
                }
            }
            onExitPictureSelector()
        }
    }

    override fun onDestroy() {
        releaseSoundPool()
        super.onDestroy()
    }

    override fun showLoading() {
        try {
            if (ActivityCompatHelper.isDestroy(activity)) {
                return
            }
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismissLoading() {
        try {
            if (ActivityCompatHelper.isDestroy(activity)) {
                return
            }
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        initAppLanguage()
        onRecreateEngine()
        super.onAttach(context)
        if (parentFragment is IBridgePictureBehavior) {
            iBridgePictureBehavior = parentFragment as IBridgePictureBehavior?
        } else if (context is IBridgePictureBehavior) {
            iBridgePictureBehavior = context
        }
    }

    /**
     * setRequestedOrientation
     */
    protected fun setRequestedOrientation() {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        activity!!.requestedOrientation = config.requestedOrientation
    }

    /**
     * back current Fragment
     */
    protected fun onBackCurrentFragment() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (!isStateSaved) {
                if (PictureSelectionConfig.viewLifecycle != null) {
                    PictureSelectionConfig.viewLifecycle.onDestroy(this)
                }
                activity!!.supportFragmentManager.popBackStack()
            }
        }
        val fragments = activity!!.supportFragmentManager.fragments
        for (i in fragments.indices) {
            val fragment = fragments[i]
            if (fragment is PictureCommonFragment) {
                fragment.onFragmentResume()
            }
        }
    }

    /**
     * onSelectFinish
     *
     * @param resultCode
     * @param result
     */
    protected fun onSelectFinish(resultCode: Int, result: ArrayList<LocalMedia>?) {
        if (null != iBridgePictureBehavior) {
            val selectorResult: com.luck.picture.lib.basic.PictureCommonFragment.SelectorResult =
                getResult(resultCode, result)
            iBridgePictureBehavior!!.onSelectFinish(selectorResult)
        }
    }

    /**
     * exit PictureSelector
     */
    protected fun onExitPictureSelector() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (isNormalDefaultEnter) {
                if (PictureSelectionConfig.viewLifecycle != null) {
                    PictureSelectionConfig.viewLifecycle.onDestroy(this)
                }
                activity!!.finish()
            } else {
                val fragments = activity!!.supportFragmentManager.fragments
                for (i in fragments.indices) {
                    val fragment = fragments[i]
                    if (fragment is PictureCommonFragment) {
                        onBackCurrentFragment()
                    }
                }
            }
        }
        PictureSelectionConfig.destroy()
    }

    /**
     * Get the image loading engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createImageLoaderEngine() {
        if (PictureSelectionConfig.imageEngine == null) {
            val baseEngine: PictureSelectorEngine =
                PictureAppMaster.getInstance().getPictureSelectorEngine()
            if (baseEngine != null) {
                PictureSelectionConfig.imageEngine = baseEngine.createImageLoaderEngine()
            }
        }
    }

    /**
     * Get the image loader data engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createLoaderDataEngine() {
        if (PictureSelectionConfig.getInstance().isLoaderDataEngine) {
            if (PictureSelectionConfig.loaderDataEngine == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) PictureSelectionConfig.loaderDataEngine =
                    baseEngine.createLoaderDataEngine()
            }
        }
        if (PictureSelectionConfig.getInstance().isLoaderFactoryEngine) {
            if (PictureSelectionConfig.loaderFactory == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) PictureSelectionConfig.loaderFactory =
                    baseEngine.onCreateLoader()
            }
        }
    }

    /**
     * Get the image compress engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createCompressEngine() {
        if (PictureSelectionConfig.getInstance().isCompressEngine) {
            if (PictureSelectionConfig.compressFileEngine == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) PictureSelectionConfig.compressFileEngine =
                    baseEngine.createCompressFileEngine()
            }
            if (PictureSelectionConfig.compressEngine == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) PictureSelectionConfig.compressEngine =
                    baseEngine.createCompressEngine()
            }
        }
    }

    /**
     * Get the Sandbox engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createSandboxFileEngine() {
        if (PictureSelectionConfig.getInstance().isSandboxFileEngine) {
            if (PictureSelectionConfig.uriToFileTransformEngine == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) PictureSelectionConfig.uriToFileTransformEngine =
                    baseEngine.createUriToFileTransformEngine()
            }
            if (PictureSelectionConfig.sandboxFileEngine == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) PictureSelectionConfig.sandboxFileEngine =
                    baseEngine.createSandboxFileEngine()
            }
        }
    }

    /**
     * Retrieve the result callback listener, provided that the user implements the IApp interface in the Application
     */
    private fun createResultCallbackListener() {
        if (PictureSelectionConfig.getInstance().isResultListenerBack) {
            if (PictureSelectionConfig.onResultCallListener == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) {
                    PictureSelectionConfig.onResultCallListener =
                        baseEngine.getResultCallbackListener()
                }
            }
        }
    }

    /**
     * Retrieve the layout callback listener, provided that the user implements the IApp interface in the Application
     */
    private fun createLayoutResourceListener() {
        if (PictureSelectionConfig.getInstance().isInjectLayoutResource) {
            if (PictureSelectionConfig.onLayoutResourceListener == null) {
                val baseEngine: PictureSelectorEngine =
                    PictureAppMaster.getInstance().getPictureSelectorEngine()
                if (baseEngine != null) {
                    PictureSelectionConfig.onLayoutResourceListener =
                        baseEngine.createLayoutResourceListener()
                }
            }
        }
    }

    /**
     * generate result
     *
     * @param data result
     * @return
     */
    protected fun getResult(
        resultCode: Int,
        data: ArrayList<LocalMedia>?,
    ): com.luck.picture.lib.basic.PictureCommonFragment.SelectorResult {
        return com.luck.picture.lib.basic.PictureCommonFragment.SelectorResult(resultCode,
            if (data != null) PictureSelector.putIntentResult(data) else null)
    }

    /**
     * SelectorResult
     */
    class SelectorResult(var mResultCode: Int, var mResultData: Intent)
    companion object {
        val fragmentTag = PictureCommonFragment::class.java.simpleName
            get() = Companion.field

        /**
         * 根据类型获取相应的Toast文案
         *
         * @param context
         * @param mimeType
         * @param maxSelectNum
         * @return
         */
        @SuppressLint("StringFormatInvalid")
        private fun getTipsMsg(context: Context?, mimeType: String, maxSelectNum: Int): String {
            return if (PictureMimeType.isHasVideo(mimeType)) {
                context!!.getString(R.string.ps_message_video_max_num, maxSelectNum.toString())
            } else if (PictureMimeType.isHasAudio(mimeType)) {
                context!!.getString(R.string.ps_message_audio_max_num, maxSelectNum.toString())
            } else {
                context!!.getString(R.string.ps_message_max_num, maxSelectNum.toString())
            }
        }
    }
}