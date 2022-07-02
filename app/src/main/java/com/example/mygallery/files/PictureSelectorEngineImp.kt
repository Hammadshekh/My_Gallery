package com.example.mygallery.files

import android.content.Context
import android.util.Log
import java.util.ArrayList

class PictureSelectorEngineImp : PictureSelectorEngine {
    /**
     * 重新创建[ImageEngine]引擎
     *
     * @return
     */
    fun createImageLoaderEngine(): ImageEngine {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致ImageEngine被回收
        return GlideEngine.createGlideEngine()
    }

    /**
     * 重新创建[CompressEngine]引擎
     *
     * @return
     */
    fun createCompressEngine(): CompressEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致CompressEngine被回收
        return null
    }

    /**
     * 重新创建[CompressEngine]引擎
     *
     * @return
     */
    fun createCompressFileEngine(): CompressFileEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致CompressFileEngine被回收
        return null
    }

    /**
     * 重新创建[ExtendLoaderEngine]引擎
     *
     * @return
     */
    fun createLoaderDataEngine(): ExtendLoaderEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致ExtendLoaderEngine被回收
        return null
    }

    /**
     * 重新创建[IBridgeMediaLoader]引擎
     * @return
     */
    fun onCreateLoader(): IBridgeLoaderFactory? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致IBridgeLoaderFactory被回收
        return null
    }

    /**
     * 重新创建[SandboxFileEngine]引擎
     *
     * @return
     */
    fun createSandboxFileEngine(): SandboxFileEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致SandboxFileEngine被回收
        return null
    }

    /**
     * 重新创建[UriToFileTransformEngine]引擎
     *
     * @return
     */
    fun createUriToFileTransformEngine(): UriToFileTransformEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致UriToFileTransformEngine被回收
        return null
    }

    /**
     * 如果出现内存不足导致OnInjectLayoutResourceListener被回收，需要重新引入自定义布局
     *
     * @return
     */
    fun createLayoutResourceListener(): OnInjectLayoutResourceListener {
        return object : OnInjectLayoutResourceListener() {
            fun getLayoutResourceId(context: Context?, resourceSource: Int): Int {
                return when (resourceSource) {
                    InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE -> R.layout.ps_custom_fragment_selector
                    InjectResourceSource.PREVIEW_LAYOUT_RESOURCE -> R.layout.ps_custom_fragment_preview
                    InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE -> R.layout.ps_custom_item_grid_image
                    InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE -> R.layout.ps_custom_item_grid_video
                    InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE -> R.layout.ps_custom_item_grid_audio
                    InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE -> R.layout.ps_custom_album_folder_item
                    InjectResourceSource.PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE -> R.layout.ps_custom_preview_image
                    InjectResourceSource.PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE -> R.layout.ps_custom_preview_video
                    InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE -> R.layout.ps_custom_preview_gallery_item
                    else -> 0
                }
            }
        }
    }

    // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致OnResultCallbackListener被回收
    // 可以在这里进行一些补救措施，通过广播或其他方式将结果推送到相应页面，防止结果丢失的情况
    val resultCallbackListener: OnResultCallbackListener<LocalMedia>
        get() = object : OnResultCallbackListener<LocalMedia?>() {
            fun onResult(result: ArrayList<LocalMedia?>) {
                // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致OnResultCallbackListener被回收
                // 可以在这里进行一些补救措施，通过广播或其他方式将结果推送到相应页面，防止结果丢失的情况
                Log.i(TAG, "onResult:" + result.size)
            }

            fun onCancel() {
                Log.i(TAG, "PictureSelector onCancel")
            }
        }

    companion object {
        private val TAG = PictureSelectorEngineImp::class.java.simpleName
    }
}
