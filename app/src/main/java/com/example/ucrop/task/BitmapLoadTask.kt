package com.example.ucrop.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.example.ucrop.OkHttpClientStore
import com.example.ucrop.callback.BitmapLoadCallback
import com.example.ucrop.model.ExifInfo
import com.example.ucrop.utils.BitmapLoadUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource
import okio.Okio
import okio.Sink
import okio.sink
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.lang.ref.WeakReference

class BitmapLoadTask(
    context: Context,
    inputUri: Uri, outputUri: Uri?,
    requiredWidth: Int, requiredHeight: Int,
    loadCallback: BitmapLoadCallback,
) : AsyncTask<Void?, Void?, BitmapLoadTask.BitmapWorkerResult>() {
    private val mContext: WeakReference<Context> = WeakReference(context)
    private var mInputUri: Uri?
    private val mOutputUri: Uri?
    private val mRequiredWidth: Int
    private val mRequiredHeight: Int
    private val mBitmapLoadCallback: BitmapLoadCallback

    class BitmapWorkerResult {
        var mBitmapResult: Bitmap? = null
        var mExifInfo: ExifInfo? = null
        var mBitmapWorkerException: Exception? = null

        constructor(bitmapResult: Bitmap, exifInfo: ExifInfo) {
            mBitmapResult = bitmapResult
            mExifInfo = exifInfo
        }

        constructor(bitmapWorkerException: Exception) {
            mBitmapWorkerException = bitmapWorkerException
        }
    }

     override fun doInBackground(vararg p0: Void?): BitmapWorkerResult? {
        val context = mContext.get()
            ?: return BitmapWorkerResult(NullPointerException("context is null"))
        if (mInputUri == null) {
            return BitmapWorkerResult(NullPointerException("Input Uri cannot be null"))
        }
        try {
            processInputUri()
        } catch (e: NullPointerException) {
            return BitmapWorkerResult(e)
        } catch (e: IOException) {
            return BitmapWorkerResult(e)
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            val stream = context.contentResolver.openInputStream(
                mInputUri!!)
            BitmapFactory.decodeStream(stream, null, options)
            options.inSampleSize = BitmapLoadUtils.computeSize(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        options.inJustDecodeBounds = false
        var decodeSampledBitmap: Bitmap? = null
        var decodeAttemptSuccess = false
        while (!decodeAttemptSuccess) {
            try {
                val stream = context.contentResolver.openInputStream(
                    mInputUri!!)
                try {
                    decodeSampledBitmap = BitmapFactory.decodeStream(stream, null, options)
                    if (options.outWidth == -1 || options.outHeight == -1) {
                        return BitmapWorkerResult(IllegalArgumentException(
                            "Bounds for bitmap could not be retrieved from the Uri: [$mInputUri]"))
                    }
                } finally {
                    BitmapLoadUtils.close(stream)
                }
                if (BitmapLoadUtils.checkSize(decodeSampledBitmap, options)) continue
                decodeAttemptSuccess = true
            } catch (error: OutOfMemoryError) {
                Log.e(TAG, "doInBackground: BitmapFactory.decodeFileDescriptor: ", error)
                options.inSampleSize *= 2
            } catch (e: IOException) {
                Log.e(TAG, "doInBackground: ImageDecoder.createSource: ", e)
                return BitmapWorkerResult(IllegalArgumentException("Bitmap could not be decoded from the Uri: [$mInputUri]",
                    e))
            }
        }
        if (decodeSampledBitmap == null) {
            return BitmapWorkerResult(IllegalArgumentException("Bitmap could not be decoded from the Uri: [$mInputUri]"))
        }
        val exifOrientation: Int = BitmapLoadUtils.getExifOrientation(context, mInputUri!!)
        val exifDegrees: Int = BitmapLoadUtils.exifToDegrees(exifOrientation)
        val exifTranslation: Int = BitmapLoadUtils.exifToTranslation(exifOrientation)
        val exifInfo = ExifInfo(exifOrientation, exifDegrees, exifTranslation)
        val matrix = Matrix()
        if (exifDegrees != 0) {
            matrix.preRotate(exifDegrees.toFloat())
        }
        if (exifTranslation != 1) {
            matrix.postScale(exifTranslation.toFloat(), 1f)
        }
        return if (!matrix.isIdentity) {
            BitmapWorkerResult(BitmapLoadUtils.transformBitmap(decodeSampledBitmap, matrix),
                exifInfo)
        } else BitmapWorkerResult(decodeSampledBitmap, exifInfo)
    }

    @Throws(NullPointerException::class, IOException::class)
    private fun processInputUri() {
        val inputUriScheme = mInputUri!!.scheme
        Log.d(TAG, "Uri scheme: $inputUriScheme")
        if ("http" == inputUriScheme || "https" == inputUriScheme) {
            try {
                downloadFile(mInputUri!!, mOutputUri)
            } catch (e: NullPointerException) {
                Log.e(TAG, "Downloading failed", e)
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Downloading failed", e)
                throw e
            }
        } else if ("file" != inputUriScheme && "content" != inputUriScheme) {
            Log.e(TAG, "Invalid Uri scheme $inputUriScheme")
            throw IllegalArgumentException("Invalid Uri scheme$inputUriScheme")
        }
    }

    @Throws(NullPointerException::class, IOException::class)
    private fun downloadFile(inputUri: Uri, outputUri: Uri?) {
        Log.d(TAG, "downloadFile")
        if (outputUri == null) {
            throw NullPointerException("Output Uri is null - cannot download image")
        }
        val context = mContext.get() ?: throw NullPointerException("Context is null")
        val client: OkHttpClient = OkHttpClientStore.INSTANCE.client!!
        var source: BufferedSource? = null
        var sink: Sink? = null
        var response: Response? = null
        try {
            val request: Request = Request.Builder()
                .url(inputUri.toString())
                .build()
            response = client.newCall(request).execute()
            source = response.body?.source()
            val outputStream = context.contentResolver.openOutputStream(outputUri)
            if (outputStream != null) {
                sink = outputStream.sink()
                source?.readAll(sink)
            } else {
                throw NullPointerException("OutputStream for given output Uri is null")
            }
        } finally {
            BitmapLoadUtils.close(source)
            BitmapLoadUtils.close(sink)
            if (response != null) {
                BitmapLoadUtils.close(response.body)
            }
            client.dispatcher.cancelAll()

            // swap uris, because input image was downloaded to the output destination
            // (cropped image will override it later)
            mInputUri = mOutputUri
        }
    }

    override fun onPostExecute(result: BitmapWorkerResult) {
        if (result.mBitmapWorkerException == null) {
            result.mBitmapResult?.let {
                mInputUri?.let { it1 ->
                    mBitmapLoadCallback.onBitmapLoaded(it,
                        result.mExifInfo,
                        it1,
                        mOutputUri)
                }
            }
        } else {
            mBitmapLoadCallback.onFailure(result.mBitmapWorkerException!!)
        }
    }

    companion object {
        private const val TAG = "BitmapWorkerTask"
    }

    init {
        mInputUri = inputUri
        mOutputUri = outputUri
        mRequiredWidth = requiredWidth
        mRequiredHeight = requiredHeight
        mBitmapLoadCallback = loadCallback
    }
}
