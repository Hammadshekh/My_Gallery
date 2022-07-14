package com.example.ucrop.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatImageView
import com.example.ucrop.UCropDevelopConfig
import com.example.ucrop.UCropImageEngine
import com.example.ucrop.callback.BitmapLoadCallback
import com.example.ucrop.model.ExifInfo
import com.example.ucrop.utils.BitmapLoadUtils
import com.example.ucrop.utils.FastBitmapDrawable
import com.example.ucrop.utils.FileUtils
import com.example.ucrop.utils.RectUtils
import kotlin.math.atan2

open class TransformImageView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) :
    AppCompatImageView(context!!, attrs, defStyle) {
    val mCurrentImageCorners = FloatArray(RECT_CORNER_POINTS_COORDS)
    val mCurrentImageCenter = FloatArray(RECT_CENTER_POINT_COORDS)
    private val mMatrixValues = FloatArray(MATRIX_VALUES_COUNT)
    var mCurrentImageMatrix = Matrix()
    var mThisWidth = 0
    var mThisHeight = 0
    var mTransformImageListener: TransformImageListener? = null
    private lateinit var mInitialImageCorners: FloatArray
    private lateinit var mInitialImageCenter: FloatArray
    private var mBitmapDecoded = false
    var mBitmapLaidOut = false
    var mMaxBitmapSize = 0
    var imageInputPath: String? = null
        private set
    var imageOutputPath: String? = null
        private set
    var imageInputUri: Uri? = null
        private set
    var imageOutputUri: Uri? = null
        private set
    private var mExifInfo: ExifInfo? = null

    private val mImageInputPath: String? = null
    private  var mImageOutputPath:String? = null

    /**
     * Interface for rotation and scale change notifying.
     */
    interface TransformImageListener {
        fun onLoadComplete()
        fun onLoadFailure(e: Exception)
        fun onRotate(currentAngle: Float)
        fun onScale(currentScale: Float)
    }

    fun setTransformImageListener(transformImageListener: TransformImageListener?) {
        mTransformImageListener = transformImageListener
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType)
        } else {
            Log.w(TAG, "Invalid ScaleType. Only ScaleType.MATRIX can be used")
        }
    }

    /**
     * Setter for [.mMaxBitmapSize] value.
     * Be sure to call it before [.setImageURI] or other image setters.
     *
     * @param maxBitmapSize - max size for both width and height of bitmap that will be used in the view.
     */
/*    var maxBitmapSize: Int
        get() {
            if (mMaxBitmapSize <= 0) {
                mMaxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(context)
            }
            return mMaxBitmapSize
        }
        set(maxBitmapSize) {
            mMaxBitmapSize = maxBitmapSize
        }*/

    override fun setImageBitmap(bitmap: Bitmap) {
        setImageDrawable(FastBitmapDrawable(bitmap))
    }

    val exifInfo: ExifInfo?
        get() = mExifInfo

    /**
     * This method takes an Uri as a parameter, then calls method to decode it into Bitmap with specified size.
     *
     * @param imageUri - image Uri
     */
    fun setImageUri(imageUri: Uri, outputUri: Uri?) {
        useDefaultLoaderCrop(imageUri, outputUri)
    }

    /**
     * use uCrop custom loader
     *
     * @param imageUri
     * @param outputUri
     */
    @Deprecated("")
    private fun useCustomLoaderCrop(imageUri: Uri, outputUri: Uri?) {
        val maxImageSize: IntArray = BitmapLoadUtils.getMaxImageSize(context, imageUri)
        UCropDevelopConfig.imageEngine?.loadImage(context, imageUri,
            maxImageSize[0], maxImageSize[1], object : UCropImageEngine.OnCallbackListener<Bitmap?> {
                override fun onCall(bitmap: Bitmap?) {
                    if (bitmap == null) {
                        useDefaultLoaderCrop(imageUri, outputUri)
                    } else {
                        var copyBitmap = bitmap.copy(bitmap.config, true)
                        val exifOrientation: Int =
                            BitmapLoadUtils.getExifOrientation(context, imageUri)
                        val exifDegrees: Int = BitmapLoadUtils.exifToDegrees(exifOrientation)
                        val exifTranslation: Int =
                            BitmapLoadUtils.exifToTranslation(exifOrientation)
                        val exifInfo = ExifInfo(exifOrientation, exifDegrees, exifTranslation)
                        val matrix = Matrix()
                        if (exifDegrees != 0) {
                            matrix.preRotate(exifDegrees.toFloat())
                        }
                        if (exifTranslation != 1) {
                            matrix.postScale(exifTranslation.toFloat(), 1f)
                        }
                        if (!matrix.isIdentity) {
                            copyBitmap = BitmapLoadUtils.transformBitmap(copyBitmap, matrix)
                        }
                        setBitmapLoadedResult(copyBitmap, exifInfo, imageUri, outputUri)
                    }
                }
            })
    }

    /**
     * use uCrop default loader
     *
     * @param imageUri
     * @param outputUri
     */
    private fun useDefaultLoaderCrop(imageUri: Uri, outputUri: Uri?) {
        val maxBitmapSize = mMaxBitmapSize
        BitmapLoadUtils.decodeBitmapInBackground(context,
            imageUri,
            outputUri,
            maxBitmapSize,
            maxBitmapSize,
            object : BitmapLoadCallback {
                override fun onBitmapLoaded(
                    bitmap: Bitmap,
                    exifInfo: ExifInfo,
                    imageInputUri: Uri,
                    imageOutputUri: Uri?,
                ) {
                    setBitmapLoadedResult(bitmap, exifInfo, imageInputUri, imageOutputUri)
                }

                override fun onFailure(bitmapWorkerException: Exception) {
                    Log.e(TAG, "onFailure: setImageUri", bitmapWorkerException)
                    if (mTransformImageListener != null) {
                        mTransformImageListener!!.onLoadFailure(bitmapWorkerException)
                    }
                }
            })
    }

    /**
     * bitmap loader complete
     *
     * @param bitmap
     * @param exifInfo
     * @param imageInputUri
     * @param imageOutputUri
     */
    fun setBitmapLoadedResult(
        bitmap: Bitmap,
        exifInfo: ExifInfo,
        imageInputUri: Uri,
        imageOutputUri: Uri?,
    ) {
        this.imageInputUri = imageInputUri
        this.imageOutputUri = imageOutputUri
        imageInputPath =
            if (FileUtils.isContent(imageInputUri.toString())) imageInputUri.toString() else imageInputUri.path
        imageOutputPath =
            if (imageOutputUri != null) if (FileUtils.isContent(imageOutputUri.toString())) imageOutputUri.toString() else imageOutputUri.path else null
        mExifInfo = exifInfo
        mBitmapDecoded = true
        setImageBitmap(bitmap)
    }

    /**
     * @return - current image scale value.
     * [1.0f - for original image, 2.0f - for 200% scaled image, etc.]
     */
/*    val currentScale: Float
        get() = getMatrixScale(mCurrentImageMatrix)*/

    /**
     * This method calculates scale value for given Matrix object.
     */
    fun getMatrixScale(matrix: Matrix): Float {
        return Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X).toDouble(), 2.0)
                + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y).toDouble(), 2.0))
            .toFloat()
    }

    /**
     * @return - current image rotation angle.
     */
 /*   val currentAngle: Float
        get() = getMatrixAngle(mCurrentImageMatrix)*/

    /**
     * This method calculates rotation angle for given Matrix object.
     */
  /*  fun getMatrixAngle(matrix: Matrix): Float {
        return (-(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(),
            getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()) * (180 / Math.PI))).toFloat()
    }*/

    override fun setImageMatrix(matrix: Matrix) {
        super.setImageMatrix(matrix)
        mCurrentImageMatrix.set(matrix)
        updateCurrentImagePoints()
    }

    val viewBitmap: Bitmap?
        get() = if (drawable == null || drawable !is FastBitmapDrawable) {
            null
        } else {
            (drawable as FastBitmapDrawable).getBitmap()
        }

    /**
     * This method translates current image.
     *
     * @param deltaX - horizontal shift
     * @param deltaY - vertical shift
     */
    fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            mCurrentImageMatrix.postTranslate(deltaX, deltaY)
            imageMatrix = mCurrentImageMatrix
        }
    }

    fun getCurrentScale(): Float {
        return getMatrixScale(mCurrentImageMatrix)
    }

     fun getCurrentAngle(): Float {
        return getMatrixAngle(mCurrentImageMatrix)
    }
     @JvmName("getImageInputPath1")
     fun getImageInputPath(): String? {
        return mImageInputPath
    }
     @JvmName("getViewBitmap1")
     fun getViewBitmap(): Bitmap? {
        return if (drawable == null || drawable !is FastBitmapDrawable) {
            null
        } else {
            (drawable as FastBitmapDrawable).getBitmap()
        }
    }

     @JvmName("getImageOutputPath1")
     fun getImageOutputPath(): String? {
        return mImageOutputPath
    }

     @JvmName("getExifInfo1")
     fun getExifInfo(): ExifInfo? {
        return mExifInfo
    }
     private fun getMatrixAngle(matrix: Matrix): Float {
        return (-(atan2(getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(),
            getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()) * (180 / Math.PI))).toFloat()
    }
    /**
     * This method scales current image.
     *
     * @param deltaScale - scale value
     * @param px         - scale center X
     * @param py         - scale center Y
     */
    open fun postScale(deltaScale: Float, px: Float, py: Float) {
        if (deltaScale != 0f) {
            mCurrentImageMatrix.postScale(deltaScale, deltaScale, px, py)
            imageMatrix = mCurrentImageMatrix
            if (mTransformImageListener != null) {
                mTransformImageListener!!.onScale(getMatrixScale(mCurrentImageMatrix))
            }
        }
    }

    /**
     * This method rotates current image.
     *
     * @param deltaAngle - rotation angle
     * @param px         - rotation center X
     * @param py         - rotation center Y
     */
    fun postRotate(deltaAngle: Float, px: Float, py: Float) {
        if (deltaAngle != 0f) {
            mCurrentImageMatrix.postRotate(deltaAngle, px, py)
            imageMatrix = mCurrentImageMatrix
            if (mTransformImageListener != null) {
                mTransformImageListener!!.onRotate(getMatrixAngle(mCurrentImageMatrix))
            }
        }
    }

    open fun init() {
        scaleType = ScaleType.MATRIX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        super.onLayout(changed, left, top, right, bottom)
        if (changed || mBitmapDecoded && !mBitmapLaidOut) {
            left = paddingLeft
            top = paddingTop
            right = width - paddingRight
            bottom = height - paddingBottom
            mThisWidth = right - left
            mThisHeight = bottom - top
            onImageLaidOut()
        }
    }

    /**
     * When image is laid out [.mInitialImageCenter] and [.mInitialImageCenter]
     * must be set.
     */
     open fun onImageLaidOut() {
        val drawable = drawable ?: return
        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()
        Log.d(TAG, String.format("Image size: [%d:%d]",
            w.toInt(), h.toInt()))
        val initialImageRect = RectF(0F, 0F, w, h)
        mInitialImageCorners = RectUtils.getCornersFromRect(initialImageRect)
        mInitialImageCenter = RectUtils.getCenterFromRect(initialImageRect)
        mBitmapLaidOut = true
        if (mTransformImageListener != null) {
            mTransformImageListener!!.onLoadComplete()
        }
    }

    /**
     * This method returns Matrix value for given index.
     *
     * @param matrix     - valid Matrix object
     * @param valueIndex - index of needed value. See [Matrix.MSCALE_X] and others.
     * @return - matrix value for index
     */
    private fun getMatrixValue(
        matrix: Matrix,
        @IntRange(from = 0,
            to = MATRIX_VALUES_COUNT.toLong()) valueIndex: Int,
    ): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[valueIndex]
    }

    /**
     * This method logs given matrix X, Y, scale, and angle values.
     * Can be used for debug.
     */
    private fun printMatrix(logPrefix: String, matrix: Matrix) {
        val x = getMatrixValue(matrix, Matrix.MTRANS_X)
        val y = getMatrixValue(matrix, Matrix.MTRANS_Y)
        val rScale = getMatrixScale(matrix)
        val rAngle = getMatrixAngle(matrix)
        Log.d(TAG,
            "$logPrefix: matrix: { x: $x, y: $y, scale: $rScale, angle: $rAngle }")
    }

    /**
     * This method updates current image corners and center points that are stored in
     * [.mCurrentImageCorners] and [.mCurrentImageCenter] arrays.
     * Those are used for several calculations.
     */
    private fun updateCurrentImagePoints() {
        mCurrentImageMatrix.mapPoints(mCurrentImageCorners, mInitialImageCorners)
        mCurrentImageMatrix.mapPoints(mCurrentImageCenter, mInitialImageCenter)
    }

    companion object {
        private const val TAG = "TransformImageView"
        private const val RECT_CORNER_POINTS_COORDS = 8
        private const val RECT_CENTER_POINT_COORDS = 2
        private const val MATRIX_VALUES_COUNT = 9
    }

    init {
        init()
    }
}
