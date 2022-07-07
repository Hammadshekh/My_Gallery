package com.example.selector.basic

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.selector.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import java.lang.ref.WeakReference
import java.util.*

class PictureSelector private constructor(activity: Activity?, fragment: Fragment? = null) {
    private val mActivity: WeakReference<Activity?> = WeakReference(activity)
    private val mFragment: WeakReference<Fragment?>?

    private constructor(fragment: Fragment) : this(fragment.activity, fragment) {}

    /**
     * @param chooseMode Select the type of images you want，all or images or video or audio
     * @return LocalMedia PictureSelectionModel
     * Use [SelectMimeType]
     */
    fun openGallery(chooseMode: Int): PictureSelectionModel {
        return PictureSelectionModel(this, chooseMode)
    }

    /**
     * @param chooseMode only use camera，images or video or audio
     * @return LocalMedia PictureSelectionModel
     * Use [SelectMimeType]
     */
    fun openCamera(chooseMode: Int): PictureSelectionCameraModel {
        return PictureSelectionCameraModel(this, chooseMode)
    }

    /**
     * @param chooseMode Select the type of images you want，all or images or video or audio
     * @return LocalMedia PictureSelectionSystemModel
     * Use [SelectMimeType]
     *
     *
     * openSystemGallery mode only supports some APIs
     *
     */
    fun openSystemGallery(chooseMode: Int): PictureSelectionSystemModel {
        return PictureSelectionSystemModel(this, chooseMode)
    }

    /**
     * @param selectMimeType query the type of images you want，all or images or video or audio
     * @return LocalMedia PictureSelectionQueryModel
     * Use [SelectMimeType]
     *
     *
     * only query [LocalMedia] data source
     *
     */
    fun dataSource(selectMimeType: Int): PictureSelectionQueryModel {
        return PictureSelectionQueryModel(this, selectMimeType)
    }

    /**
     * Preview mode to preview images or videos or audio
     *
     * @return
     */
    fun openPreview(): PictureSelectionPreviewModel {
        return PictureSelectionPreviewModel(this)
    }

    /**
     * @return Activity.
     */
    val activity: Activity?
        get() = mActivity.get()

    /**
     * @return Fragment.
     */
    val fragment: Fragment?
        get() = mFragment?.get()

    companion object {
        /**
         * Start PictureSelector for context.
         *
         * @param context
         * @return PictureSelector instance.
         */
        fun create(context: Context?): PictureSelector {
            return PictureSelector(context as Activity?)
        }

        /**
         * Start PictureSelector for Activity.
         *
         * @param activity
         * @return PictureSelector instance.
         */
        fun create(activity: Activity?): PictureSelector {
            return PictureSelector(activity)
        }

        /**
         * Start PictureSelector for Fragment.
         *
         * @param fragment
         * @return PictureSelector instance.
         */
        fun create(fragment: Fragment): PictureSelector {
            return PictureSelector(fragment)
        }

        /**
         * set result
         *
         * @param data result
         * @return
         */
        fun putIntentResult(data: ArrayList<LocalMedia?>?): Intent {
            return Intent().putParcelableArrayListExtra(PictureConfig.EXTRA_RESULT_SELECTION, data)
        }

        /**
         * @param intent
         * @return get Selector  LocalMedia
         */
        fun obtainSelectorList(intent: Intent?): ArrayList<LocalMedia> {
            if (intent == null) {
                return ArrayList<LocalMedia>()
            }
            return intent.getParcelableArrayListExtra<LocalMedia>(PictureConfig.EXTRA_RESULT_SELECTION) as ArrayList<LocalMedia>
        }
    }

    init {
        mFragment = WeakReference(fragment)
    }
}
