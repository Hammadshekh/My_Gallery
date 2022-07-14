package com.example.selector.manager

import android.content.Context
import android.os.Environment
import com.example.selector.basic.PictureMediaScannerConnection
import com.example.selector.config.SelectMimeType
import com.example.selector.interfaces.OnCallbackListener
import com.example.selector.threads.PictureThreadUtils
import java.io.File

object PictureCacheManager {
    /**
     * set empty PictureSelector Cache
     */
    /**
     * set empty PictureSelector Cache
     */
    @JvmOverloads
    fun deleteCacheDirFile(cacheDir: String?, listener: OnCallbackListener<String?>? = null) {
        val cacheFileDir = File(cacheDir)
        val files = cacheFileDir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    val isResult = file.delete()
                    if (isResult) {
                        listener?.onCall(file.absolutePath)
                    }
                }
            }
        }
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     * @param type    image or video ...
     */
    fun deleteCacheRefreshDirFile(context: Context, type: Int) {
        deleteCacheDirFile(context, type, true, null)
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     * @param type    image or video ...
     */
    fun deleteCacheDirFile(context: Context, type: Int) {
        deleteCacheDirFile(context, type, false, null)
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     * @param type    image or video ...
     */
    fun deleteCacheDirFile(context: Context, type: Int, listener: OnCallbackListener<String>?) {
        deleteCacheDirFile(context, type, false, listener)
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     * @param type    image or video ...
     */
    private fun deleteCacheDirFile(
        context: Context,
        type: Int,
        isRefresh: Boolean,
        listener: OnCallbackListener<String>?,
    ) {
        val cutDir =
            context.getExternalFilesDir(if (type == SelectMimeType.ofImage()) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES)
        if (cutDir != null) {
            val files = cutDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        val isResult = file.delete()
                        if (isResult) {
                            if (isRefresh) {
                                PictureThreadUtils.runOnUiThread(Runnable {
                                    PictureMediaScannerConnection(context,
                                        file.absolutePath)
                                })
                            } else {
                                listener?.onCall(file.absolutePath)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     */
    fun deleteAllCacheDirFile(context: Context) {
        deleteAllCacheDirFile(context, false, null)
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     */
    fun deleteAllCacheDirFile(context: Context, listener: OnCallbackListener<String>?) {
        deleteAllCacheDirFile(context, false, listener)
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     */
    fun deleteAllCacheDirRefreshFile(context: Context) {
        deleteAllCacheDirFile(context, true, null)
    }

    /**
     * set empty PictureSelector Cache
     *
     * @param context
     */
    private fun deleteAllCacheDirFile(
        context: Context,
        isRefresh: Boolean,
        listener: OnCallbackListener<String>?,
    ) {
        val dirPictures = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (dirPictures != null) {
            val files = dirPictures.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        val isResult = file.delete()
                        if (isResult) {
                            if (isRefresh) {
                                PictureThreadUtils.runOnUiThread(Runnable {
                                    PictureMediaScannerConnection(context,
                                        file.absolutePath)
                                })
                            } else {
                                listener?.onCall(file.absolutePath)
                            }
                        }
                    }
                }
            }
        }
        val dirMovies = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (dirMovies != null) {
            val files = dirMovies.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        val isResult = file.delete()
                        if (isResult) {
                            if (isRefresh) {
                                PictureThreadUtils.runOnUiThread(Runnable {
                                    PictureMediaScannerConnection(context,
                                        file.absolutePath)
                                })
                            } else {
                                listener?.onCall(file.absolutePath)
                            }
                        }
                    }
                }
            }
        }
        val dirMusic = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (dirMusic != null) {
            val files = dirMusic.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        val isResult = file.delete()
                        if (isResult) {
                            if (isRefresh) {
                                PictureThreadUtils.runOnUiThread(Runnable {
                                    PictureMediaScannerConnection(context,
                                        file.absolutePath)
                                })
                            } else {
                                listener?.onCall(file.absolutePath)
                            }
                        }
                    }
                }
            }
        }
    }
}
