package com.example.selector.basic

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.PictureSelectorPreviewFragment
import com.example.selector.config.PictureConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.engine.ImageEngine
import com.example.selector.interfaces.OnExternalPreviewEventListener
import com.example.selector.interfaces.OnInjectLayoutResourceListener
import com.example.selector.magical.BuildRecycleItemViewParams
import com.example.selector.manager.SelectedManager
import com.example.selector.style.PictureSelectorStyle
import com.example.selector.style.PictureWindowAnimationStyle
import com.example.selector.utils.ActivityCompatHelper
import com.example.selector.utils.DensityUtil
import com.example.selector.utils.DoubleUtils
import com.example.selector.utils.PictureFileUtils.TAG
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

class PictureSelectionPreviewModel(selector: PictureSelector) {
    private val selectionConfig: PictureSelectionConfig
    private val selector: PictureSelector

    /**
     * Image Load the engine
     *
     * @param engine Image Load the engine
     *
     *
     * [
](https://github.com/LuckSiege/PictureSelector/blob/version_component/app/src/main/java/com/luck/pictureselector/GlideEngine.java) *
     * @return
     */
    fun setImageEngine(engine: ImageEngine): PictureSelectionPreviewModel {
        PictureSelectionConfig.imageEngine = engine
        return this
    }

    /**
     * PictureSelector theme style settings
     *
     * @param uiStyle
     *
     *
     * Use [                It consists of the following parts and can be set separately][PictureSelectorStyle]
     * [com.luck.picture.lib.style.TitleBarStyle]
     * [com.luck.picture.lib.style.AlbumWindowStyle]
     * [com.luck.picture.lib.style.SelectMainStyle]
     * [com.luck.picture.lib.style.BottomNavBarStyle]
     * [com.luck.picture.lib.style.PictureWindowAnimationStyle]
     *
     *
     * @return PictureSelectorStyle
     */
    fun setSelectorUIStyle(uiStyle: PictureSelectorStyle?): PictureSelectionPreviewModel {
        if (uiStyle != null) {
            PictureSelectionConfig.selectorStyle = uiStyle
        }
        return this
    }

    /**
     * Set App Language
     *
     * @param language [LanguageConfig]
     * @return PictureSelectionModel
     */
    fun setLanguage(language: Int): PictureSelectionPreviewModel {
        selectionConfig.language = language
        return this
    }

    /**
     * Intercept custom inject layout events, Users can implement their own layout
     * on the premise that the view ID must be consistent
     *
     * @param listener
     * @return
     */
    fun setInjectLayoutResourceListener(listener: OnInjectLayoutResourceListener?): PictureSelectionPreviewModel {
        selectionConfig.isInjectLayoutResource = listener != null
        PictureSelectionConfig.onLayoutResourceListener = listener
        return this
    }

    /**
     * View lifecycle listener
     *
     * @param viewLifecycle
     * @return
     */
    fun setAttachViewLifecycle(viewLifecycle: IBridgeViewLifecycle): PictureSelectionPreviewModel {
        PictureSelectionConfig.viewLifecycle = viewLifecycle
        return this
    }

    /**
     * Preview Full Screen Mode
     *
     * @param isFullScreenModel
     * @return
     */
    fun isPreviewFullScreenMode(isFullScreenModel: Boolean): PictureSelectionPreviewModel {
        selectionConfig.isPreviewFullScreenMode = isFullScreenModel
        return this
    }

    /**
     * Preview Zoom Effect Mode
     *
     * @param isPreviewZoomEffect
     * @param rv
     */
    fun isPreviewZoomEffect(
        isPreviewZoomEffect: Boolean,
        rv: RecyclerView?,
    ): PictureSelectionPreviewModel {
        return isPreviewZoomEffect(isPreviewZoomEffect, selectionConfig.isPreviewFullScreenMode, rv)
    }

    /**
     * Preview Zoom Effect Mode
     *
     * @param isPreviewZoomEffect
     * @param isFullScreenModel
     * @param rv
     */
    fun isPreviewZoomEffect(
        isPreviewZoomEffect: Boolean,
        isFullScreenModel: Boolean,
        rv: RecyclerView?,
    ): PictureSelectionPreviewModel {
        if (selectionConfig.chooseMode === SelectMimeType.ofAudio()) {
            selectionConfig.isPreviewZoomEffect = false
        } else {
            if (isPreviewZoomEffect && rv == null) {
                throw NullPointerException("isPreviewZoomEffect mode, external must be passed into " + RecyclerView::class.java)
            }
            if (isPreviewZoomEffect) {
                if (isFullScreenModel) {
                    if (rv != null) {
                        BuildRecycleItemViewParams.generateViewParams(rv, 0)
                    }
                } else {
                    selector.activity?.let { DensityUtil.getStatusBarHeight(it) }?.let {
                        if (rv != null) {
                            BuildRecycleItemViewParams.generateViewParams(rv,
                                it)
                        }
                    }
                }
            }
            selectionConfig.isPreviewZoomEffect = isPreviewZoomEffect
        }
        return this
    }

    /**
     * Whether to play video automatically when previewing
     *
     * @param isAutoPlay
     * @return
     */
    fun isAutoVideoPlay(isAutoPlay: Boolean): PictureSelectionPreviewModel {
        selectionConfig.isAutoVideoPlay = isAutoPlay
        return this
    }

    /**
     * loop video
     *
     * @param isLoopAutoPlay
     * @return
     */
    fun isLoopAutoVideoPlay(isLoopAutoPlay: Boolean): PictureSelectionPreviewModel {
        selectionConfig.isLoopAutoPlay = isLoopAutoPlay
        return this
    }

    /**
     * Intercept external preview click events, and users can implement their own preview framework
     *
     * @param listener
     * @return
     */
    fun setExternalPreviewEventListener(listener: OnExternalPreviewEventListener): PictureSelectionPreviewModel {
        PictureSelectionConfig.onExternalPreviewEventListener = listener
        return this
    }

    /**
     * @param isHidePreviewDownload Previews do not show downloads
     * @return
     */
    fun isHidePreviewDownload(isHidePreviewDownload: Boolean): PictureSelectionPreviewModel {
        selectionConfig.isHidePreviewDownload = isHidePreviewDownload
        return this
    }

    /**
     * preview LocalMedia
     *
     * @param currentPosition current position
     * @param isDisplayDelete if visible delete
     * @param list            preview data
     */
    fun startFragmentPreview(
        currentPosition: Int,
        isDisplayDelete: Boolean,
        list: ArrayList<LocalMedia?>?,
    ) {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            if (PictureSelectionConfig.imageEngine == null && selectionConfig.chooseMode != SelectMimeType.ofAudio()) {
                throw NullPointerException("imageEngine is null,Please implement ImageEngine")
            }
            if (list == null || list.size == 0) {
                throw NullPointerException("preview data is null")
            }
            var fragmentManager: FragmentManager? = null
            if (activity is AppCompatActivity) {
                fragmentManager = activity.supportFragmentManager
            } else if (activity is FragmentActivity) {
                fragmentManager = activity.supportFragmentManager
            }
            if (fragmentManager == null) {
                throw NullPointerException("FragmentManager cannot be null")
            }
            if (ActivityCompatHelper.checkFragmentNonExits(activity as FragmentActivity, TAG)
            ) {
                val fragment: PictureSelectorPreviewFragment =
                    PictureSelectorPreviewFragment.newInstance()
                val previewData: ArrayList<LocalMedia> = ArrayList(list)
                fragment.setExternalPreviewData(currentPosition,
                    previewData.size,
                    previewData,
                    isDisplayDelete)
                FragmentInjectManager.injectSystemRoomFragment(fragmentManager, TAG,
                    fragment)
            }
        }
    }

    /**
     * preview LocalMedia
     *
     * @param currentPosition current position
     * @param isDisplayDelete if visible delete
     * @param list            preview data
     */
    fun startActivityPreview(
        currentPosition: Int,
        isDisplayDelete: Boolean,
        list: ArrayList<LocalMedia>,
    ) {
        if (!DoubleUtils.isFastDoubleClick) {
            val activity: Activity = selector.activity
                ?: throw NullPointerException("Activity cannot be null")
            if (PictureSelectionConfig.imageEngine == null && selectionConfig.chooseMode != SelectMimeType.ofAudio()) {
                throw NullPointerException("imageEngine is null,Please implement ImageEngine")
            }
            if (list.size == 0) {
                throw NullPointerException("preview data is null")
            }
            val intent = Intent(activity, PictureSelectorTransparentActivity::class.java)
            SelectedManager.addSelectedPreviewResult(list)
            intent.putExtra(PictureConfig.EXTRA_EXTERNAL_PREVIEW, true)
            intent.putExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE,
                PictureConfig.MODE_TYPE_EXTERNAL_PREVIEW_SOURCE)
            intent.putExtra(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, currentPosition)
            intent.putExtra(PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE, isDisplayDelete)
            val fragment: Fragment = selector.fragment!!
            fragment.startActivity(intent)
            if (selectionConfig.isPreviewZoomEffect) {
                activity.overridePendingTransition(R.anim.ps_anim_fade_in, R.anim.ps_anim_fade_in)
            } else {
                val windowAnimationStyle: PictureWindowAnimationStyle =
                    PictureSelectionConfig.selectorStyle?.windowAnimationStyle!!
                activity.overridePendingTransition(windowAnimationStyle.activityEnterAnimation,
                    R.anim.ps_anim_fade_in)
            }
        }
    }

    init {
        this.selector = selector
        selectionConfig = PictureSelectionConfig.cleanInstance
    }
}
