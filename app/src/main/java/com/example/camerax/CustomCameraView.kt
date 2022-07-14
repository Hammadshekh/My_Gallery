package com.example.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.core.VideoCapture.ERROR_MUXER
import androidx.camera.core.VideoCapture.ERROR_RECORDING_TOO_SHORT
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.ExperimentalVideo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.example.camerax.listener.*
import com.example.camerax.permissions.PermissionChecker
import com.example.camerax.permissions.PermissionResultCallback
import com.example.camerax.permissions.SimpleXPermissionUtil
import com.example.camerax.utils.CameraUtils
import com.example.camerax.utils.DensityUtil
import com.example.camerax.utils.FileUtils
import com.example.camerax.utils.SimpleXSpUtils
import com.example.camerax.widget.CaptureLayout
import com.example.camerax.widget.FocusImageView
import com.example.mygallery.R
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.math.abs


/**
 * @author：luck
 * @date：2020-01-04 13:41
 * @describe：Custom Camera View
 */
class CustomCameraView : RelativeLayout,
    CameraXOrientationEventListener.OnOrientationChangedListener {
    private var typeFlash = TYPE_FLASH_OFF
    private var mCameraPreviewView: PreviewView? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private var mImageCapture: ImageCapture? = null
    private var mImageAnalyzer: ImageAnalysis? = null
    // private var mVideoCapture: androidx.camera.core.VideoCapture? = null

    private var mVideoCapture: VideoCapture? = null
    private var displayId = -1

    /**
     * 相机模式
     */
    private var buttonFeatures = 0

    /**
     * 自定义拍照输出路径
     */
    private var outPutCameraDir: String? = null

    /**
     * 自定义拍照文件名
     */
    private var outPutCameraFileName: String? = null

    /**
     * 设置每秒的录制帧数
     */
    private var videoFrameRate = 0

    /**
     * 设置编码比特率。
     */
    private var videoBitRate = 0

    /**
     * 视频录制最小时长
     */
    private var recordVideoMinSecond = 0

    /**
     * 是否显示录制时间
     */
    private var isDisplayRecordTime = false

    /**
     * 图片文件类型
     */
    private var imageFormat: String? = null
    private var imageFormatForQ: String? = null

    /**
     * 视频文件类型
     */
    private var videoFormat: String? = null
    private var videoFormatForQ: String? = null

    /**
     * 相机模式
     */
    private var useCameraCases: Int = LifecycleCameraController.IMAGE_CAPTURE

    /**
     * 摄像头方向
     */
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    /**
     * 手指点击对焦
     */
    private var isManualFocus = false

    /**
     * 双击可放大缩小
     */
    private var isZoomPreview = false

    /**
     * 是否自动纠偏
     */
    private var isAutoRotation = false
    private var recordTime: Long = 0

    /**
     * 回调监听
     */
    private var mCameraListener: CameraListener? = null
    private var mOnClickListener: ClickListener? = null
    private var mImageCallbackListener: ImageCallbackListener? = null
    private var mImagePreview: ImageView? = null
    private var mImagePreviewBg: View? = null
    private var mSwitchCamera: ImageView? = null
    private var mFlashLamp: ImageView? = null
    private var tvCurrentTime: TextView? = null
    private var mCaptureLayout: CaptureLayout? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mTextureView: TextureView? = null
    private var displayManager: DisplayManager? = null
    private lateinit var displayListener: DisplayListener
    private var orientationEventListener: CameraXOrientationEventListener? = null
    private var mCameraInfo: CameraInfo? = null
    private var mCameraControl: CameraControl? = null
    private var focusImageView: FocusImageView? = null
    private var mainExecutor: Executor? = null
    private var activity: Activity? = null
    private val isImageCaptureEnabled: Boolean
        get() = useCameraCases == LifecycleCameraController.IMAGE_CAPTURE

    constructor(context: Context?) : super(context) {
        initView()
        this.mVideoCapture = mVideoCapture
    }

    constructor(context: Context?, attrs: AttributeSet?, mVideoCapture: VideoCapture) : super(
        context,
        attrs) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.picture_camera_view, this)
        activity = context as Activity
        setBackgroundColor(ContextCompat.getColor(context, R.color.picture_color_black))
        mCameraPreviewView = findViewById(R.id.cameraPreviewView)
        mTextureView = findViewById(R.id.video_play_preview)
        focusImageView = findViewById(R.id.focus_view)
        mImagePreview = findViewById(R.id.cover_preview)
        mImagePreviewBg = findViewById(R.id.cover_preview_bg)
        mSwitchCamera = findViewById(R.id.image_switch)
        mFlashLamp = findViewById(R.id.image_flash)
        mCaptureLayout = findViewById(R.id.capture_layout)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        mSwitchCamera!!.setImageResource(R.drawable.picture_ic_camera)
        displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayListener = DisplayListener()
        displayManager!!.registerDisplayListener(displayListener, null)
        mainExecutor = ContextCompat.getMainExecutor(context)
        mCameraPreviewView!!.post(Runnable {
            displayId = mCameraPreviewView!!.getDisplay().displayId
        })
        mFlashLamp!!.setOnClickListener(OnClickListener { v: View? ->
            typeFlash++
            if (typeFlash > 0x023) {
                typeFlash = TYPE_FLASH_AUTO
            }
            setFlashMode()
        })
        mSwitchCamera!!.setOnClickListener(OnClickListener { toggleCamera() })
        mCaptureLayout!!.setCaptureListener(object : CaptureListener {
            override fun takePictures() {
                if (!mCameraProvider!!.isBound(mImageCapture!!)) {
                    bindCameraImageUseCases()
                }
                useCameraCases = LifecycleCameraController.IMAGE_CAPTURE
                mCaptureLayout!!.setButtonCaptureEnabled(false)
                mSwitchCamera!!.visibility = INVISIBLE
                mFlashLamp!!.visibility = INVISIBLE
                tvCurrentTime!!.visibility = GONE
                val metadata = ImageCapture.Metadata()
                metadata.isReversedHorizontal = isReversedHorizontal
                val fileOptions: ImageCapture.OutputFileOptions
                val cameraFile: File
                cameraFile = if (isSaveExternal) {
                    FileUtils.createTempFile(context, false)
                } else {
                    FileUtils.createCameraFile(context, CameraUtils.TYPE_IMAGE,
                        outPutCameraFileName!!, imageFormat!!, outPutCameraDir!!)
                }
                fileOptions = ImageCapture.OutputFileOptions.Builder(cameraFile)
                    .setMetadata(metadata).build()
                mImageCapture!!.takePicture(fileOptions, mainExecutor!!,
                    MyImageResultCallback(this@CustomCameraView, mImagePreview!!, mImagePreviewBg!!,
                        mCaptureLayout, mImageCallbackListener!!, mCameraListener))
            }

            @SuppressLint("RestrictedApi")
            override fun recordStart() {
                if (!mVideoCapture?.let { mCameraProvider?.isBound(it) }!!) {
                    bindCameraVideoUseCases()
                }
                @ExperimentalVideo
                useCameraCases = LifecycleCameraController.VIDEO_CAPTURE
                mSwitchCamera!!.visibility = INVISIBLE
                mFlashLamp!!.visibility = INVISIBLE
                tvCurrentTime!!.visibility = if (isDisplayRecordTime) VISIBLE else GONE
                val fileOptions: androidx.camera.core.VideoCapture.OutputFileOptions
                val cameraFile: File? = if (isSaveExternal) {
                    FileUtils.createTempFile(context, true)
                } else {
                    outPutCameraFileName?.let {
                        videoFormat?.let { it1 ->
                            outPutCameraDir?.let { it2 ->
                                FileUtils.createCameraFile(context, CameraUtils.TYPE_VIDEO,
                                    it, it1, it2)
                            }
                        }
                    }
                }
                fileOptions = VideoCapture.OutputFileOptions.Builder(cameraFile!!)
                    .build()
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mVideoCapture!!.startRecording(fileOptions, mainExecutor!!,
                    object : VideoCapture.OnVideoSavedCallback {
                        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                            val minSecond =
                                if (recordVideoMinSecond <= 0) CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO.toLong() else recordVideoMinSecond.toLong()
                            if (recordTime < minSecond || outputFileResults.savedUri == null) {
                                return
                            }
                            val savedUri: Uri = outputFileResults.savedUri!!
                            SimpleCameraX.putOutputUri(activity!!.intent, savedUri)
                            val outPutPath =
                                if (FileUtils.isContent(savedUri.toString())) savedUri.toString() else savedUri.path!!
                            mTextureView!!.visibility = VISIBLE
                            tvCurrentTime!!.visibility = GONE
                            if (mTextureView!!.isAvailable) {
                                startVideoPlay(outPutPath)
                            } else {
                                mTextureView!!.surfaceTextureListener = surfaceTextureListener
                            }
                        }

                        override fun onError(
                            videoCaptureError: Int, message: String,
                            cause: Throwable?,
                        ) {
                            if (mCameraListener != null) {
                                if (videoCaptureError == ERROR_RECORDING_TOO_SHORT || videoCaptureError == ERROR_MUXER) {
                                    recordShort(0)
                                } else {
                                    mCameraListener!!.onError(videoCaptureError, message, cause)
                                }
                            }
                        }
                    })
            }

            override fun changeTime(duration: Long) {
                if (isDisplayRecordTime && tvCurrentTime!!.visibility == VISIBLE) {
                    val format = String.format(Locale.getDefault(),
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(
                            duration)))
                    if (!TextUtils.equals(format, tvCurrentTime!!.text)) {
                        tvCurrentTime!!.text = format
                    }
                    if (TextUtils.equals("00:00", tvCurrentTime!!.text)) {
                        tvCurrentTime!!.visibility = GONE
                    }
                }
            }

            @SuppressLint("RestrictedApi")
            override fun recordShort(time: Long) {
                recordTime = time
                mSwitchCamera!!.visibility = VISIBLE
                mFlashLamp!!.visibility = VISIBLE
                tvCurrentTime!!.visibility = GONE
                mCaptureLayout!!.resetCaptureLayout()
                mCaptureLayout!!.setTextWithAnimation(context.getString(R.string.picture_recording_time_is_short))
                mVideoCapture?.stopRecording()
            }

            @SuppressLint("RestrictedApi")
            override fun recordEnd(time: Long) {
                recordTime = time
                try {
                    mVideoCapture!!.stopRecording()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun recordZoom(zoom: Float) {}
            override fun recordError() {
                mCameraListener?.onError(0, "An unknown error", null)
            }
        })
        mCaptureLayout!!.setTypeListener(object : TypeListener {
            override fun cancel() {
                onCancelMedia()
            }

            override fun confirm() {
                var outputPath: String = SimpleCameraX.getOutputPath(activity!!.intent)
                if (isSaveExternal) {
                    outputPath = isMergeExternalStorageState(activity, outputPath)
                } else {
                    // 对前置镜头导致的镜像进行一个纠正
                    if (isImageCaptureEnabled && isReversedHorizontal) {
                        val cameraFile: File =
                            outPutCameraFileName?.let {
                                outPutCameraDir?.let { it1 ->
                                    imageFormat?.let { it2 ->
                                        FileUtils.createCameraFile(context, CameraUtils.TYPE_IMAGE,
                                            it, it2, it1)
                                    }
                                }
                            }!!
                        if (FileUtils.copyPath(activity!!, outputPath, cameraFile.absolutePath)) {
                            outputPath = cameraFile.absolutePath
                        }
                    }
                }
                if (isImageCaptureEnabled) {
                    mImagePreview!!.visibility = INVISIBLE
                    mImagePreviewBg!!.alpha = 0f
                    mCameraListener?.onPictureSuccess(outputPath)
                } else {
                    stopVideoPlay()
                    mCameraListener?.onRecordSuccess(outputPath)
                }
            }
        })
        mCaptureLayout!!.setLeftClickListener(object : ClickListener {
            override fun onClick() {
                mOnClickListener?.onClick()
            }
        })
    }

    private fun isMergeExternalStorageState(activity: Activity?, outputPath: String): String {
        var outputPath = outputPath
        try {
            // 对前置镜头导致的镜像进行一个纠正
            if (isImageCaptureEnabled && isReversedHorizontal) {
                val tempFile: File = activity?.let { FileUtils.createTempFile(it, false) }!!
                if (activity.let { FileUtils.copyPath(it, outputPath, tempFile.absolutePath) }) {
                    outputPath = tempFile.absolutePath
                }
            }
            // 当用户未设置存储路径时，相片默认是存在外部公共目录下
            val externalSavedUri: Uri? = if (isImageCaptureEnabled) {
                val contentValues: ContentValues =
                    outPutCameraFileName?.let {
                        imageFormatForQ?.let { it1 ->
                            CameraUtils.buildImageContentValues(it,
                                it1)
                        }
                    }!!
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
            } else {
                val contentValues: ContentValues =
                    outPutCameraFileName?.let {
                        videoFormatForQ?.let { it1 ->
                            CameraUtils.buildVideoContentValues(it,
                                it1)
                        }
                    }!!
                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
            }
            if (externalSavedUri == null) {
                return outputPath
            }
            val outputStream = context.contentResolver.openOutputStream(externalSavedUri)
            val isWriteFileSuccess: Boolean =
                outputStream?.let {
                    FileUtils.writeFileFromIS(FileInputStream(outputPath),
                        it)
                } == true
            if (isWriteFileSuccess) {
                FileUtils.deleteFile(context, outputPath)
                SimpleCameraX.putOutputUri(activity!!.intent, externalSavedUri)
                return externalSavedUri.toString()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return outputPath
    }

    private val isSaveExternal: Boolean
        private get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && TextUtils.isEmpty(
            outPutCameraDir)
    private val isReversedHorizontal: Boolean
        private get() = lensFacing == CameraSelector.LENS_FACING_FRONT

    /**
     * 用户针对相机的一些参数配制
     *
     * @param intent
     */
    fun setCameraConfig(intent: Intent) {
        val extras = intent.extras
        val isCameraAroundState =
            extras!!.getBoolean(SimpleCameraX.EXTRA_CAMERA_AROUND_STATE, false)
        buttonFeatures =
            extras.getInt(SimpleCameraX.EXTRA_CAMERA_MODE, CustomCameraConfig.BUTTON_STATE_BOTH)
        lensFacing =
            if (isCameraAroundState) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        outPutCameraDir = extras.getString(SimpleCameraX.EXTRA_OUTPUT_PATH_DIR)
        outPutCameraFileName = extras.getString(SimpleCameraX.EXTRA_CAMERA_FILE_NAME)
        videoFrameRate = extras.getInt(SimpleCameraX.EXTRA_VIDEO_FRAME_RATE)
        videoBitRate = extras.getInt(SimpleCameraX.EXTRA_VIDEO_BIT_RATE)
        isManualFocus = extras.getBoolean(SimpleCameraX.EXTRA_MANUAL_FOCUS)
        isZoomPreview = extras.getBoolean(SimpleCameraX.EXTRA_ZOOM_PREVIEW)
        isAutoRotation = extras.getBoolean(SimpleCameraX.EXTRA_AUTO_ROTATION)
        val recordVideoMaxSecond = extras.getInt(SimpleCameraX.EXTRA_RECORD_VIDEO_MAX_SECOND,
            CustomCameraConfig.DEFAULT_MAX_RECORD_VIDEO)
        recordVideoMinSecond = extras.getInt(SimpleCameraX.EXTRA_RECORD_VIDEO_MIN_SECOND,
            CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO)
        imageFormat = extras.getString(SimpleCameraX.EXTRA_CAMERA_IMAGE_FORMAT, CameraUtils.JPEG)
        imageFormatForQ = extras.getString(SimpleCameraX.EXTRA_CAMERA_IMAGE_FORMAT_FOR_Q,
            CameraUtils.MIME_TYPE_IMAGE)
        videoFormat = extras.getString(SimpleCameraX.EXTRA_CAMERA_VIDEO_FORMAT, CameraUtils.MP4)
        videoFormatForQ = extras.getString(SimpleCameraX.EXTRA_CAMERA_VIDEO_FORMAT_FOR_Q,
            CameraUtils.MIME_TYPE_VIDEO)
        val captureLoadingColor =
            extras.getInt(SimpleCameraX.EXTRA_CAPTURE_LOADING_COLOR, -0x828201)
        isDisplayRecordTime =
            extras.getBoolean(SimpleCameraX.EXTRA_DISPLAY_RECORD_CHANGE_TIME, false)
        mCaptureLayout!!.setButtonFeatures(buttonFeatures)
        if (recordVideoMaxSecond > 0) {
            setRecordVideoMaxTime(recordVideoMaxSecond)
        }
        if (recordVideoMinSecond > 0) {
            setRecordVideoMinTime(recordVideoMinSecond)
        }
        val format = String.format(Locale.getDefault(),
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(recordVideoMaxSecond.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(recordVideoMaxSecond.toLong())
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(
                recordVideoMaxSecond.toLong())))
        tvCurrentTime!!.text = format
        if (isAutoRotation && buttonFeatures != CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER) {
            orientationEventListener = CameraXOrientationEventListener(context, this)
            startCheckOrientation()
        }
        setCaptureLoadingColor(captureLoadingColor)
        setProgressColor(captureLoadingColor)
        val isCheckSelfPermission: Boolean = PermissionChecker.checkSelfPermission(context, arrayOf(
            Manifest.permission.CAMERA))
        if (isCheckSelfPermission) {
            buildUseCameraCases()
        } else {
            if (CustomCameraConfig.explainListener != null) {
                if (!SimpleXSpUtils.getBoolean(context, Manifest.permission.CAMERA, false)) {
                    CustomCameraConfig.explainListener!!
                        .onPermissionDescription(context, this, Manifest.permission.CAMERA)
                }
            }
            PermissionChecker.getInstance()!!.requestPermissions(activity!!,
                arrayOf(Manifest.permission.CAMERA),
                object : PermissionResultCallback {
                    override fun onGranted() {
                        buildUseCameraCases()
                        if (CustomCameraConfig.explainListener != null) {
                            CustomCameraConfig.explainListener!!.onDismiss(this@CustomCameraView)
                        }
                    }

                    override fun onDenied() {
                        if (CustomCameraConfig.deniedListener != null) {
                            SimpleXSpUtils.putBoolean(context, Manifest.permission.CAMERA, true)
                            CustomCameraConfig.deniedListener!!.onDenied(context,
                                Manifest.permission.CAMERA,
                                PermissionChecker.PERMISSION_SETTING_CODE)
                            if (CustomCameraConfig.explainListener != null) {
                                CustomCameraConfig.explainListener!!.onDismiss(this@CustomCameraView)
                            }
                        } else {
                            SimpleXPermissionUtil.goIntentSetting(activity!!,
                                PermissionChecker.PERMISSION_SETTING_CODE)
                        }
                    }
                })
        }
    }

    /**
     * 检测手机方向
     */
    private fun startCheckOrientation() {
        orientationEventListener?.star()
    }

    /**
     * 停止检测手机方向
     */
    fun stopCheckOrientation() {
        orientationEventListener?.stop()
    }

    private val targetRotation: Int
        get() = mImageCapture!!.getTargetRotation()

    override fun onOrientationChanged(orientation: Int) {
        mImageCapture?.setTargetRotation(orientation)
        mImageAnalyzer?.setTargetRotation(orientation)
    }

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private inner class DisplayListener : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == this@CustomCameraView.displayId) {
                mImageCapture?.targetRotation = mCameraPreviewView!!.display.rotation
                mImageAnalyzer?.targetRotation = mCameraPreviewView!!.display.rotation
            }
        }
    }

    /**
     * 开始打开相机预览
     */
    fun buildUseCameraCases() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(
                context)
        cameraProviderFuture.addListener(Runnable {
            try {
                mCameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    /**
     * 初始相机预览模式
     */
    private fun bindCameraUseCases() {
        when (buttonFeatures) {
            CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE -> bindCameraImageUseCases()
            CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER -> bindCameraVideoUseCases()
            else -> bindCameraWithUserCases()
        }
    }

    /**
     * bindCameraWithUserCases
     */
    private fun bindCameraWithUserCases() {
        try {
            val cameraSelector: CameraSelector =
                CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // Preview
            val preview: Preview = Preview.Builder()
                .setTargetRotation(mCameraPreviewView!!.display.rotation)
                .build()
            // ImageCapture
            buildImageCapture()
            // VideoCapture
            buildVideoCapture()
            val useCase: UseCaseGroup.Builder = UseCaseGroup.Builder()
            useCase.addUseCase(preview)
            mImageCapture?.let { useCase.addUseCase(it) }
            mVideoCapture?.let { useCase.addUseCase(it) }
            val useCaseGroup: UseCaseGroup = useCase.build()
            // Must unbind the use-cases before rebinding them
            mCameraProvider?.unbindAll()
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera: Camera = (mCameraProvider!!.bindToLifecycle(context as LifecycleOwner,
                cameraSelector,
                useCaseGroup) ?:
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView!!.getSurfaceProvider())) as Camera
            // setFlashMode
            setFlashMode()
            mCameraInfo = camera.cameraInfo
            mCameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * bindCameraImageUseCases
     */
    private fun bindCameraImageUseCases() {
        try {
            val screenAspectRatio =
                aspectRatio(DensityUtil.getScreenWidth(context), DensityUtil.getScreenHeight(
                    context))
            val rotation: Int = mCameraPreviewView!!.getDisplay().rotation
            val cameraSelector: CameraSelector =
                CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // Preview
            val preview: Preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // ImageCapture
            buildImageCapture()

            // ImageAnalysis
            mImageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // Must unbind the use-cases before rebinding them
            mCameraProvider!!.unbindAll()
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera: Camera = mCameraProvider!!.bindToLifecycle(context as LifecycleOwner,
                cameraSelector,
                preview,
                mImageCapture,
                mImageAnalyzer)
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView!!.getSurfaceProvider())
            // setFlashMode
            setFlashMode()
            mCameraInfo = camera.cameraInfo
            mCameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * bindCameraVideoUseCases
     */
    private fun bindCameraVideoUseCases() {
        try {
            val cameraSelector: CameraSelector =
                CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // Preview
            val preview: Preview = Preview.Builder()
                .setTargetRotation(mCameraPreviewView!!.display.rotation)
                .build()
            buildVideoCapture()
            // Must unbind the use-cases before rebinding them
            mCameraProvider?.unbindAll()
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera: Camera = mCameraProvider!!.bindToLifecycle(context as LifecycleOwner,
                cameraSelector,
                preview,
                mVideoCapture)
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView!!.surfaceProvider)
            mCameraInfo = camera.cameraInfo
            mCameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildImageCapture() {
        val screenAspectRatio =
            aspectRatio(DensityUtil.getScreenWidth(context), DensityUtil.getScreenHeight(
                context))
        mImageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(mCameraPreviewView!!.display.rotation)
            .build()
    }

    @SuppressLint("RestrictedApi")
    private fun buildVideoCapture() {
        val videoBuilder = androidx.camera.core.VideoCapture.Builder()
        videoBuilder.setTargetRotation(mCameraPreviewView!!.display.rotation)
        if (videoFrameRate > 0) {
            videoBuilder.setVideoFrameRate(videoFrameRate)
        }
        if (videoBitRate > 0) {
            videoBuilder.setBitRate(videoBitRate)
        }
        mVideoCapture = videoBuilder.build()
    }

    private fun initCameraPreviewListener() {
        val zoomState: LiveData<ZoomState?> = mCameraInfo!!.zoomState
        val cameraXPreviewViewTouchListener = CameraXPreviewViewTouchListener(
            context)
        cameraXPreviewViewTouchListener.setCustomTouchListener(object :
            CameraXPreviewViewTouchListener.CustomTouchListener {
            override fun zoom(delta: Float) {
                if (isZoomPreview) {
                    if (zoomState.value != null) {
                        val currentZoomRatio: Float = zoomState.value!!.zoomRatio
                        mCameraControl!!.setZoomRatio(currentZoomRatio * delta)
                    }
                }
            }

            override fun click(x: Float, y: Float) {
                if (isManualFocus) {
                    val factory: MeteringPointFactory = mCameraPreviewView!!.meteringPointFactory
                    val point: MeteringPoint = factory.createPoint(x, y)
                    val action: FocusMeteringAction =
                        FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                    if (mCameraInfo!!.isFocusMeteringSupported(action)) {
                        mCameraControl!!.cancelFocusAndMetering()
                        focusImageView!!.setDisappear(false)
                        focusImageView!!.startFocus(Point(x.toInt(), y.toInt()))
                        val future: ListenableFuture<FocusMeteringResult> =
                            mCameraControl!!.startFocusAndMetering(action)
                        future.addListener(Runnable {
                            try {
                                val result: FocusMeteringResult = future.get()
                                focusImageView!!.setDisappear(true)
                                if (result.isFocusSuccessful) {
                                    focusImageView!!.onFocusSuccess()
                                } else {
                                    focusImageView!!.onFocusFailed()
                                }
                            } catch (ignored: Exception) {
                            }
                        }, mainExecutor)
                    }
                }
            }

            override fun doubleClick(x: Float, y: Float) {
                if (isZoomPreview) {
                    if (zoomState.value != null) {
                        val currentZoomRatio: Float = zoomState.value!!.zoomRatio
                        val minZoomRatio: Float = zoomState.value!!.minZoomRatio
                        if (currentZoomRatio > minZoomRatio) {
                            mCameraControl!!.setLinearZoom(0f)
                        } else {
                            mCameraControl!!.setLinearZoom(0.5f)
                        }
                    }
                }
            }
        })
        mCameraPreviewView!!.setOnTouchListener(cameraXPreviewViewTouchListener)
    }

    /**
     * [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     * [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *
     * Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     * of preview ratio to one of the provided values.
     *
     * @param width  - preview width
     * @param height - preview height
     * @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val aspect = width.coerceAtLeast(height).toDouble()
        val previewRatio = aspect / width.coerceAtMost(height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
    }

    /**
     * 拍照回调
     */
    private class MyImageResultCallback(
        cameraView: CustomCameraView,
        imagePreview: ImageView,
        imagePreviewBg: View,
        captureLayout: CaptureLayout?,
        imageCallbackListener: ImageCallbackListener,
        cameraListener: CameraListener?,
    ) :
        ImageCapture.OnImageSavedCallback {
        private val mImagePreviewReference: WeakReference<ImageView>
        private val mImagePreviewBgReference: WeakReference<View>
        private val mCaptureLayoutReference: WeakReference<CaptureLayout?>
        private val mImageCallbackListenerReference: WeakReference<ImageCallbackListener>
        private val mCameraListenerReference: WeakReference<CameraListener?>
        private val mCameraViewLayoutReference: WeakReference<CustomCameraView> =
            WeakReference(cameraView)

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri: Uri = outputFileResults.savedUri!!
            val customCameraView = mCameraViewLayoutReference.get()
            customCameraView?.stopCheckOrientation()
            val mImagePreview = mImagePreviewReference.get()
            if (mImagePreview != null) {
                val context = mImagePreview.context
                SimpleCameraX.putOutputUri((context as Activity).intent, savedUri)
                mImagePreview.visibility = VISIBLE
                if (customCameraView != null && customCameraView.isAutoRotation) {
                    val targetRotation = customCameraView.targetRotation
                    // The aspect ratio of the picture taken from this angle is taller, so use the ScaleType.FIT_CENTER zoom mode
                    if (targetRotation == Surface.ROTATION_90 || targetRotation == Surface.ROTATION_270) {
                        mImagePreview.adjustViewBounds = true
                        val mImagePreviewBackground = mImagePreviewBgReference.get()
                        mImagePreviewBackground?.animate()?.alpha(1f)?.setDuration(220)?.start()
                    } else {
                        mImagePreview.adjustViewBounds = false
                        mImagePreview.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                }
                val imageCallbackListener: ImageCallbackListener? =
                    mImageCallbackListenerReference.get()
                if (imageCallbackListener != null) {
                    val outPutCameraPath =
                        if (FileUtils.isContent(savedUri.toString())) savedUri.toString() else savedUri.path!!
                    imageCallbackListener.onLoadImage(outPutCameraPath, mImagePreview)
                }
            }
            val captureLayout: CaptureLayout? = mCaptureLayoutReference.get()
            if (captureLayout != null) {
                captureLayout.setButtonCaptureEnabled(true)
                captureLayout.startTypeBtnAnimator()
            }
        }

        override fun onError(exception: ImageCaptureException) {
            if (mCaptureLayoutReference.get() != null) {
                mCaptureLayoutReference.get()!!.setButtonCaptureEnabled(true)
            }
            if (mCameraListenerReference.get() != null) {
                mCameraListenerReference.get()!!.onError(exception.getImageCaptureError(),
                    exception.message, exception.cause)
            }
        }

        init {
            mImagePreviewReference = WeakReference(imagePreview)
            mImagePreviewBgReference = WeakReference(imagePreviewBg)
            mCaptureLayoutReference = WeakReference<CaptureLayout?>(captureLayout)
            mImageCallbackListenerReference =
                WeakReference<ImageCallbackListener>(imageCallbackListener)
            mCameraListenerReference = WeakReference<CameraListener?>(cameraListener)
        }
    }

    private val surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            val outputPath: String = SimpleCameraX.getOutputPath(activity!!.intent)
            startVideoPlay(outputPath)
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int,
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    fun setCameraListener(cameraListener: CameraListener?) {
        mCameraListener = cameraListener
    }

    // Set the maximum duration of recorded video in seconds
    fun setRecordVideoMaxTime(maxDurationTime: Int) {
        mCaptureLayout!!.setDuration(maxDurationTime)
    }

    /**
     * 设置录制视频最小时长 秒
     */
    fun setRecordVideoMinTime(minDurationTime: Int) {
        mCaptureLayout!!.setMinDuration(minDurationTime)
    }

    /**
     * 设置拍照时loading色值
     *
     * @param color
     */
    fun setCaptureLoadingColor(color: Int) {
        mCaptureLayout!!.setCaptureLoadingColor(color)
    }

    /**
     * 设置录像时loading色值
     *
     * @param color
     */
    fun setProgressColor(color: Int) {
        mCaptureLayout!!.setProgressColor(color)
    }

    /**
     * 切换前后摄像头
     */
    fun toggleCamera() {
        lensFacing =
            if (CameraSelector.LENS_FACING_FRONT == lensFacing) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
        bindCameraUseCases()
    }

    /**
     * 闪光灯模式
     */
    private fun setFlashMode() {
        if (mImageCapture == null) {
            return
        }
        when (typeFlash) {
            TYPE_FLASH_AUTO -> {
                mFlashLamp!!.setImageResource(R.drawable.picture_ic_flash_auto)
                mImageCapture!!.setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            }
            TYPE_FLASH_ON -> {
                mFlashLamp!!.setImageResource(R.drawable.picture_ic_flash_on)
                mImageCapture!!.setFlashMode(ImageCapture.FLASH_MODE_ON)
            }
            TYPE_FLASH_OFF -> {
                mFlashLamp!!.setImageResource(R.drawable.picture_ic_flash_off)
                mImageCapture!!.setFlashMode(ImageCapture.FLASH_MODE_OFF)
            }
        }
    }

    /**
     * 关闭相机界面按钮
     *
     * @param clickListener
     */
    fun setOnCancelClickListener(clickListener: ClickListener?) {
        mOnClickListener = clickListener
    }

    fun setImageCallbackListener(mImageCallbackListener: ImageCallbackListener?) {
        this.mImageCallbackListener = mImageCallbackListener
    }

    /**
     * 重置状态
     */
    @SuppressLint("RestrictedApi")
    private fun resetState() {
        if (isImageCaptureEnabled) {
            mImagePreview!!.visibility = INVISIBLE
            mImagePreviewBg!!.alpha = 0f
        } else {
            try {
                mVideoCapture?.stopRecording()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mSwitchCamera!!.visibility = VISIBLE
        mFlashLamp!!.visibility = VISIBLE
        mCaptureLayout!!.resetCaptureLayout()
    }

    /**
     * 开始循环播放视频
     *
     * @param url
     */
    private fun startVideoPlay(url: String) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer()
            } else {
                mMediaPlayer!!.reset()
            }
            if (FileUtils.isContent(url)) {
                mMediaPlayer!!.setDataSource(context, Uri.parse(url))
            } else {
                mMediaPlayer!!.setDataSource(url)
            }
            mMediaPlayer!!.setSurface(Surface(mTextureView!!.surfaceTexture))
            mMediaPlayer!!.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setOnVideoSizeChangedListener { mp, width, height ->
                updateVideoViewSize(mMediaPlayer!!.videoWidth.toFloat(),
                    mMediaPlayer!!.videoHeight.toFloat())
            }
            mMediaPlayer!!.setOnPreparedListener { mMediaPlayer!!.start() }
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * updateVideoViewSize
     *
     * @param videoWidth
     * @param videoHeight
     */
    private fun updateVideoViewSize(videoWidth: Float, videoHeight: Float) {
        if (videoWidth > videoHeight) {
            val height = (videoHeight / videoWidth * width).toInt()
            val videoViewParam = LayoutParams(LayoutParams.MATCH_PARENT, height)
            videoViewParam.addRule(CENTER_IN_PARENT, TRUE)
            mTextureView!!.layoutParams = videoViewParam
        }
    }

    //Cancel shooting related
    fun onCancelMedia() {
        val outputPath: String = SimpleCameraX.getOutputPath(activity!!.intent)
        FileUtils.deleteFile(context, outputPath)
        stopVideoPlay()
        resetState()
        startCheckOrientation()
    }

    // stop video playback
    private fun stopVideoPlay() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        mTextureView!!.visibility = GONE
    }

    /**
     * onConfigurationChanged
     *
     * @param newConfig
     */
    public override fun onConfigurationChanged(newConfig: Configuration) {
        buildUseCameraCases()
    }

    /**
     * onDestroy
     */
    fun onDestroy() {
        displayManager!!.unregisterDisplayListener(displayListener)
        stopCheckOrientation()
        focusImageView!!.destroy()
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /**
         * 闪关灯状态
         */
        private const val TYPE_FLASH_AUTO = 0x021
        private const val TYPE_FLASH_ON = 0x022
        private const val TYPE_FLASH_OFF = 0x023
    }
}