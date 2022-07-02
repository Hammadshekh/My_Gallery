package com.example.selector.config

import android.content.pm.ActivityInfo
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.ArrayList

class PictureSelectionConfig : Parcelable {
    var chooseMode = 0
    var isOnlyCamera = false
    var isDirectReturnSingle = false
    var cameraImageFormat: String? = null
    var cameraVideoFormat: String? = null
    var cameraImageFormatForQ: String? = null
    var cameraVideoFormatForQ: String? = null
    var requestedOrientation = 0
    var isCameraAroundState = false
    var selectionMode = 0
    var maxSelectNum = 0
    var minSelectNum = 0
    var maxVideoSelectNum = 0
    var minVideoSelectNum = 0
    var minAudioSelectNum = 0
    var videoQuality = 0
    var filterVideoMaxSecond = 0
    var filterVideoMinSecond = 0
    var selectMaxDurationSecond = 0
    var selectMinDurationSecond = 0
    var recordVideoMaxSecond = 0
    var recordVideoMinSecond = 0
    var imageSpanCount = 0
    var filterMaxFileSize: Long = 0
    var filterMinFileSize: Long = 0
    var selectMaxFileSize: Long = 0
    var selectMinFileSize: Long = 0
    var language = 0
    var isDisplayCamera = false
    var isGif = false
    var isWebp = false
    var isBmp = false
    var isEnablePreviewImage = false
    var isEnablePreviewVideo = false
    var isEnablePreviewAudio = false
    var isPreviewFullScreenMode = false
    var isPreviewZoomEffect = false
    var isOpenClickSound = false
    var isEmptyResultReturn = false
    var isHidePreviewDownload = false
    var isWithVideoImage = false
    var queryOnlyList: List<String>? = null
    var skipCropList: List<String>? = null
    var isCheckOriginalImage = false
    var outPutCameraImageFileName: String? = null
    var outPutCameraVideoFileName: String? = null
    var outPutAudioFileName: String? = null
    var outPutCameraDir: String? = null
    var outPutAudioDir: String? = null
    var sandboxDir: String? = null
    var originalPath: String? = null
    var cameraPath: String? = null
    var sortOrder: String? = null
    var defaultAlbumName: String? = null
    var pageSize = 0
    var isPageStrategy = false
    var isFilterInvalidFile = false
    var isMaxSelectEnabledMask = false
    var animationMode = 0
    var isAutomaticTitleRecyclerTop = false
    var isQuickCapture = false
    var isCameraRotateImage = false
    var isAutoRotating = false
    var isSyncCover = false
    var ofAllCameraType = 0
    var isOnlySandboxDir = false
    var isCameraForegroundService = false
    var isResultListenerBack = false
    var isInjectLayoutResource = false
    var isActivityResultBack = false
    var isCompressEngine = false
    var isLoaderDataEngine = false
    var isLoaderFactoryEngine = false
    var isSandboxFileEngine = false
    var isOriginalControl = false
    var isDisplayTimeAxis = false
    var isFastSlidingSelect = false
    var isSelectZoomAnim = false
    var isAutoVideoPlay = false
    var isLoopAutoPlay = false
    var isFilterSizeDuration = false
    var isAllFilesAccess = false
    var isPageSyncAsCount = false

    protected constructor(`in`: Parcel) {
        chooseMode = `in`.readInt()
        isOnlyCamera = `in`.readByte().toInt() != 0
        isDirectReturnSingle = `in`.readByte().toInt() != 0
        cameraImageFormat = `in`.readString()
        cameraVideoFormat = `in`.readString()
        cameraImageFormatForQ = `in`.readString()
        cameraVideoFormatForQ = `in`.readString()
        requestedOrientation = `in`.readInt()
        isCameraAroundState = `in`.readByte().toInt() != 0
        selectionMode = `in`.readInt()
        maxSelectNum = `in`.readInt()
        minSelectNum = `in`.readInt()
        maxVideoSelectNum = `in`.readInt()
        minVideoSelectNum = `in`.readInt()
        minAudioSelectNum = `in`.readInt()
        videoQuality = `in`.readInt()
        filterVideoMaxSecond = `in`.readInt()
        filterVideoMinSecond = `in`.readInt()
        selectMaxDurationSecond = `in`.readInt()
        selectMinDurationSecond = `in`.readInt()
        recordVideoMaxSecond = `in`.readInt()
        recordVideoMinSecond = `in`.readInt()
        imageSpanCount = `in`.readInt()
        filterMaxFileSize = `in`.readLong()
        filterMinFileSize = `in`.readLong()
        selectMaxFileSize = `in`.readLong()
        selectMinFileSize = `in`.readLong()
        language = `in`.readInt()
        isDisplayCamera = `in`.readByte().toInt() != 0
        isGif = `in`.readByte().toInt() != 0
        isWebp = `in`.readByte().toInt() != 0
        isBmp = `in`.readByte().toInt() != 0
        isEnablePreviewImage = `in`.readByte().toInt() != 0
        isEnablePreviewVideo = `in`.readByte().toInt() != 0
        isEnablePreviewAudio = `in`.readByte().toInt() != 0
        isPreviewFullScreenMode = `in`.readByte().toInt() != 0
        isPreviewZoomEffect = `in`.readByte().toInt() != 0
        isOpenClickSound = `in`.readByte().toInt() != 0
        isEmptyResultReturn = `in`.readByte().toInt() != 0
        isHidePreviewDownload = `in`.readByte().toInt() != 0
        isWithVideoImage = `in`.readByte().toInt() != 0
        queryOnlyList = `in`.createStringArrayList()
        skipCropList = `in`.createStringArrayList()
        isCheckOriginalImage = `in`.readByte().toInt() != 0
        outPutCameraImageFileName = `in`.readString()
        outPutCameraVideoFileName = `in`.readString()
        outPutAudioFileName = `in`.readString()
        outPutCameraDir = `in`.readString()
        outPutAudioDir = `in`.readString()
        sandboxDir = `in`.readString()
        originalPath = `in`.readString()
        cameraPath = `in`.readString()
        sortOrder = `in`.readString()
        defaultAlbumName = `in`.readString()
        pageSize = `in`.readInt()
        isPageStrategy = `in`.readByte().toInt() != 0
        isFilterInvalidFile = `in`.readByte().toInt() != 0
        isMaxSelectEnabledMask = `in`.readByte().toInt() != 0
        animationMode = `in`.readInt()
        isAutomaticTitleRecyclerTop = `in`.readByte().toInt() != 0
        isQuickCapture = `in`.readByte().toInt() != 0
        isCameraRotateImage = `in`.readByte().toInt() != 0
        isAutoRotating = `in`.readByte().toInt() != 0
        isSyncCover = `in`.readByte().toInt() != 0
        ofAllCameraType = `in`.readInt()
        isOnlySandboxDir = `in`.readByte().toInt() != 0
        isCameraForegroundService = `in`.readByte().toInt() != 0
        isResultListenerBack = `in`.readByte().toInt() != 0
        isInjectLayoutResource = `in`.readByte().toInt() != 0
        isActivityResultBack = `in`.readByte().toInt() != 0
        isCompressEngine = `in`.readByte().toInt() != 0
        isLoaderDataEngine = `in`.readByte().toInt() != 0
        isLoaderFactoryEngine = `in`.readByte().toInt() != 0
        isSandboxFileEngine = `in`.readByte().toInt() != 0
        isOriginalControl = `in`.readByte().toInt() != 0
        isDisplayTimeAxis = `in`.readByte().toInt() != 0
        isFastSlidingSelect = `in`.readByte().toInt() != 0
        isSelectZoomAnim = `in`.readByte().toInt() != 0
        isAutoVideoPlay = `in`.readByte().toInt() != 0
        isLoopAutoPlay = `in`.readByte().toInt() != 0
        isFilterSizeDuration = `in`.readByte().toInt() != 0
        isAllFilesAccess = `in`.readByte().toInt() != 0
        isPageSyncAsCount = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(chooseMode)
        dest.writeByte((if (isOnlyCamera) 1 else 0).toByte())
        dest.writeByte((if (isDirectReturnSingle) 1 else 0).toByte())
        dest.writeString(cameraImageFormat)
        dest.writeString(cameraVideoFormat)
        dest.writeString(cameraImageFormatForQ)
        dest.writeString(cameraVideoFormatForQ)
        dest.writeInt(requestedOrientation)
        dest.writeByte((if (isCameraAroundState) 1 else 0).toByte())
        dest.writeInt(selectionMode)
        dest.writeInt(maxSelectNum)
        dest.writeInt(minSelectNum)
        dest.writeInt(maxVideoSelectNum)
        dest.writeInt(minVideoSelectNum)
        dest.writeInt(minAudioSelectNum)
        dest.writeInt(videoQuality)
        dest.writeInt(filterVideoMaxSecond)
        dest.writeInt(filterVideoMinSecond)
        dest.writeInt(selectMaxDurationSecond)
        dest.writeInt(selectMinDurationSecond)
        dest.writeInt(recordVideoMaxSecond)
        dest.writeInt(recordVideoMinSecond)
        dest.writeInt(imageSpanCount)
        dest.writeLong(filterMaxFileSize)
        dest.writeLong(filterMinFileSize)
        dest.writeLong(selectMaxFileSize)
        dest.writeLong(selectMinFileSize)
        dest.writeInt(language)
        dest.writeByte((if (isDisplayCamera) 1 else 0).toByte())
        dest.writeByte((if (isGif) 1 else 0).toByte())
        dest.writeByte((if (isWebp) 1 else 0).toByte())
        dest.writeByte((if (isBmp) 1 else 0).toByte())
        dest.writeByte((if (isEnablePreviewImage) 1 else 0).toByte())
        dest.writeByte((if (isEnablePreviewVideo) 1 else 0).toByte())
        dest.writeByte((if (isEnablePreviewAudio) 1 else 0).toByte())
        dest.writeByte((if (isPreviewFullScreenMode) 1 else 0).toByte())
        dest.writeByte((if (isPreviewZoomEffect) 1 else 0).toByte())
        dest.writeByte((if (isOpenClickSound) 1 else 0).toByte())
        dest.writeByte((if (isEmptyResultReturn) 1 else 0).toByte())
        dest.writeByte((if (isHidePreviewDownload) 1 else 0).toByte())
        dest.writeByte((if (isWithVideoImage) 1 else 0).toByte())
        dest.writeStringList(queryOnlyList)
        dest.writeStringList(skipCropList)
        dest.writeByte((if (isCheckOriginalImage) 1 else 0).toByte())
        dest.writeString(outPutCameraImageFileName)
        dest.writeString(outPutCameraVideoFileName)
        dest.writeString(outPutAudioFileName)
        dest.writeString(outPutCameraDir)
        dest.writeString(outPutAudioDir)
        dest.writeString(sandboxDir)
        dest.writeString(originalPath)
        dest.writeString(cameraPath)
        dest.writeString(sortOrder)
        dest.writeString(defaultAlbumName)
        dest.writeInt(pageSize)
        dest.writeByte((if (isPageStrategy) 1 else 0).toByte())
        dest.writeByte((if (isFilterInvalidFile) 1 else 0).toByte())
        dest.writeByte((if (isMaxSelectEnabledMask) 1 else 0).toByte())
        dest.writeInt(animationMode)
        dest.writeByte((if (isAutomaticTitleRecyclerTop) 1 else 0).toByte())
        dest.writeByte((if (isQuickCapture) 1 else 0).toByte())
        dest.writeByte((if (isCameraRotateImage) 1 else 0).toByte())
        dest.writeByte((if (isAutoRotating) 1 else 0).toByte())
        dest.writeByte((if (isSyncCover) 1 else 0).toByte())
        dest.writeInt(ofAllCameraType)
        dest.writeByte((if (isOnlySandboxDir) 1 else 0).toByte())
        dest.writeByte((if (isCameraForegroundService) 1 else 0).toByte())
        dest.writeByte((if (isResultListenerBack) 1 else 0).toByte())
        dest.writeByte((if (isInjectLayoutResource) 1 else 0).toByte())
        dest.writeByte((if (isActivityResultBack) 1 else 0).toByte())
        dest.writeByte((if (isCompressEngine) 1 else 0).toByte())
        dest.writeByte((if (isLoaderDataEngine) 1 else 0).toByte())
        dest.writeByte((if (isLoaderFactoryEngine) 1 else 0).toByte())
        dest.writeByte((if (isSandboxFileEngine) 1 else 0).toByte())
        dest.writeByte((if (isOriginalControl) 1 else 0).toByte())
        dest.writeByte((if (isDisplayTimeAxis) 1 else 0).toByte())
        dest.writeByte((if (isFastSlidingSelect) 1 else 0).toByte())
        dest.writeByte((if (isSelectZoomAnim) 1 else 0).toByte())
        dest.writeByte((if (isAutoVideoPlay) 1 else 0).toByte())
        dest.writeByte((if (isLoopAutoPlay) 1 else 0).toByte())
        dest.writeByte((if (isFilterSizeDuration) 1 else 0).toByte())
        dest.writeByte((if (isAllFilesAccess) 1 else 0).toByte())
        dest.writeByte((if (isPageSyncAsCount) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun initDefaultValue() {
        chooseMode = SelectMimeType.ofImage()
        isOnlyCamera = false
        selectionMode = SelectModeConfig.MULTIPLE
        selectorStyle = PictureSelectorStyle()
        maxSelectNum = 9
        minSelectNum = 0
        maxVideoSelectNum = 1
        minVideoSelectNum = 0
        minAudioSelectNum = 0
        videoQuality = VideoQuality.VIDEO_QUALITY_HIGH
        language = LanguageConfig.UNKNOWN_LANGUAGE
        filterVideoMaxSecond = 0
        filterVideoMinSecond = 0
        selectMaxDurationSecond = 0
        selectMinDurationSecond = 0
        filterMaxFileSize = 0
        filterMinFileSize = 0
        selectMaxFileSize = 0
        selectMinFileSize = 0
        recordVideoMaxSecond = 60
        recordVideoMinSecond = 0
        imageSpanCount = PictureConfig.DEFAULT_SPAN_COUNT
        isCameraAroundState = false
        isWithVideoImage = false
        isDisplayCamera = true
        isGif = false
        isWebp = true
        isBmp = true
        isCheckOriginalImage = false
        isDirectReturnSingle = false
        isEnablePreviewImage = true
        isEnablePreviewVideo = true
        isEnablePreviewAudio = true
        isHidePreviewDownload = false
        isOpenClickSound = false
        isEmptyResultReturn = false
        cameraImageFormat = PictureMimeType.JPEG
        cameraVideoFormat = PictureMimeType.MP4
        cameraImageFormatForQ = PictureMimeType.MIME_TYPE_IMAGE
        cameraVideoFormatForQ = PictureMimeType.MIME_TYPE_VIDEO
        outPutCameraImageFileName = ""
        outPutCameraVideoFileName = ""
        outPutAudioFileName = ""
        queryOnlyList = ArrayList()
        outPutCameraDir = ""
        outPutAudioDir = ""
        sandboxDir = ""
        originalPath = ""
        cameraPath = ""
        pageSize = PictureConfig.MAX_PAGE_SIZE
        isPageStrategy = true
        isFilterInvalidFile = false
        isMaxSelectEnabledMask = false
        animationMode = -1
        isAutomaticTitleRecyclerTop = true
        isQuickCapture = true
        isCameraRotateImage = true
        isAutoRotating = true
        isSyncCover = !SdkVersionUtils.isQ()
        ofAllCameraType = SelectMimeType.ofAll()
        isOnlySandboxDir = false
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        isCameraForegroundService = false
        isResultListenerBack = true
        isActivityResultBack = false
        isCompressEngine = false
        isLoaderDataEngine = false
        isLoaderFactoryEngine = false
        isSandboxFileEngine = false
        isPreviewFullScreenMode = true
        isPreviewZoomEffect = chooseMode != SelectMimeType.ofAudio()
        isOriginalControl = false
        isInjectLayoutResource = false
        isDisplayTimeAxis = true
        isFastSlidingSelect = false
        skipCropList = ArrayList()
        sortOrder = ""
        isSelectZoomAnim = true
        defaultAlbumName = ""
        isAutoVideoPlay = false
        isLoopAutoPlay = false
        isFilterSizeDuration = true
        isAllFilesAccess = false
        isPageSyncAsCount = false
    }

    constructor() {}

    companion object {
        var imageEngine: ImageEngine? = null
        var compressEngine: CompressEngine? = null
        var compressFileEngine: CompressFileEngine? = null
        var cropEngine: CropEngine? = null
        var cropFileEngine: CropFileEngine? = null
        var sandboxFileEngine: SandboxFileEngine? = null
        var uriToFileTransformEngine: UriToFileTransformEngine? = null
        var loaderDataEngine: ExtendLoaderEngine? = null
        var selectorStyle: PictureSelectorStyle? = null
        var onCameraInterceptListener: OnCameraInterceptListener? = null
        var onSelectLimitTipsListener: OnSelectLimitTipsListener? = null
        var onResultCallListener: OnResultCallbackListener<LocalMedia>? = null
        var onExternalPreviewEventListener: OnExternalPreviewEventListener? = null
        var onEditMediaEventListener: OnMediaEditInterceptListener? = null
        var onPermissionsEventListener: OnPermissionsInterceptListener? = null
        var onLayoutResourceListener: OnInjectLayoutResourceListener? = null
        var onPreviewInterceptListener: OnPreviewInterceptListener? = null
        var onSelectFilterListener: OnSelectFilterListener? = null
        var onPermissionDescriptionListener: OnPermissionDescriptionListener? = null
        var onPermissionDeniedListener: OnPermissionDeniedListener? = null
        var onRecordAudioListener: OnRecordAudioInterceptListener? = null
        var onQueryFilterListener: OnQueryFilterListener? = null
        var onBitmapWatermarkListener: OnBitmapWatermarkEventListener? = null
        var onVideoThumbnailEventListener: OnVideoThumbnailEventListener? = null
        var viewLifecycle: IBridgeViewLifecycle? = null
        var loaderFactory: IBridgeLoaderFactory? = null
        var interpolatorFactory: InterpolatorFactory? = null
        val CREATOR: Creator<PictureSelectionConfig> = object : Creator<PictureSelectionConfig?> {
            override fun createFromParcel(`in`: Parcel): PictureSelectionConfig? {
                return PictureSelectionConfig(`in`)
            }

            override fun newArray(size: Int): Array<PictureSelectionConfig?> {
                return arrayOfNulls(size)
            }
        }
        val cleanInstance: PictureSelectionConfig?
            get() {
                val selectionSpec = instance
                selectionSpec!!.initDefaultValue()
                return selectionSpec
            }

        @Volatile
        private var mInstance: PictureSelectionConfig? = null
        val instance: PictureSelectionConfig?
            get() {
                if (mInstance == null) {
                    synchronized(PictureSelectionConfig::class.java) {
                        if (mInstance == null) {
                            mInstance = PictureSelectionConfig()
                            mInstance!!.initDefaultValue()
                        }
                    }
                }
                return mInstance
            }

        /**
         * 释放监听器
         */
        fun destroy() {
            imageEngine = null
            compressEngine = null
            compressFileEngine = null
            cropEngine = null
            cropFileEngine = null
            sandboxFileEngine = null
            uriToFileTransformEngine = null
            loaderDataEngine = null
            onResultCallListener = null
            onCameraInterceptListener = null
            onExternalPreviewEventListener = null
            onEditMediaEventListener = null
            onPermissionsEventListener = null
            onLayoutResourceListener = null
            onPreviewInterceptListener = null
            onSelectLimitTipsListener = null
            onSelectFilterListener = null
            onPermissionDescriptionListener = null
            onPermissionDeniedListener = null
            onRecordAudioListener = null
            onQueryFilterListener = null
            onBitmapWatermarkListener = null
            onVideoThumbnailEventListener = null
            viewLifecycle = null
            loaderFactory = null
            interpolatorFactory = null
            PictureThreadUtils.cancel(PictureThreadUtils.getIoPool())
            SelectedManager.clearSelectResult()
            BuildRecycleItemViewParams.clear()
            LocalMedia.destroyPool()
            SelectedManager.setCurrentLocalMediaFolder(null)
        }
    }
}
