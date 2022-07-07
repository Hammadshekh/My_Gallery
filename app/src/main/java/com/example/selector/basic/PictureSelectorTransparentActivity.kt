package com.example.selector.basic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mygallery.R
import com.example.selector.PictureOnlyCameraFragment
import com.example.selector.PictureSelectorPreviewFragment
import com.example.selector.PictureSelectorSystemFragment
import com.example.selector.config.PictureConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.immersive.ImmersiveManager
import com.example.selector.manager.SelectedManager
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.PictureFileUtils.TAG
import com.example.selector.utils.StyleUtils
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

class PictureSelectorTransparentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        setContentView(R.layout.ps_empty)
        if (isExternalPreview) {
            // TODO ignore
        } else {
            setActivitySize()
        }
        setupFragment()
    }

    private val isExternalPreview: Boolean
        get() {
            val modeTypeSource = intent.getIntExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE, 0)
            return modeTypeSource == PictureConfig.MODE_TYPE_EXTERNAL_PREVIEW_SOURCE
        }

    private fun immersive() {
        if (PictureSelectionConfig.selectorStyle == null) {
            PictureSelectionConfig.instance
        }
        val mainStyle: SelectMainStyle = PictureSelectionConfig.selectorStyle?.selectMainStyle!!
        var statusBarColor: Int = mainStyle.statusBarColor
        var navigationBarColor: Int = mainStyle.navigationBarColor
        val isDarkStatusBarBlack: Boolean = mainStyle.isDarkStatusBarBlack
        if (!StyleUtils.checkStyleValidity(statusBarColor)) {
            statusBarColor = ContextCompat.getColor(this, R.color.ps_color_grey)
        }
        if (!StyleUtils.checkStyleValidity(navigationBarColor)) {
            navigationBarColor = ContextCompat.getColor(this, R.color.ps_color_grey)
        }
        ImmersiveManager.immersiveAboveAPI23(this,
            statusBarColor,
            navigationBarColor,
            isDarkStatusBarBlack)
    }

    private fun setupFragment() {
        val fragmentTag: String
        val targetFragment: Fragment
        when (intent.getIntExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE, 0)) {
            PictureConfig.MODE_TYPE_SYSTEM_SOURCE -> {
                fragmentTag = TAG
                targetFragment = PictureSelectorSystemFragment.newInstance()
            }
            PictureConfig.MODE_TYPE_EXTERNAL_PREVIEW_SOURCE -> {
                fragmentTag = TAG
                targetFragment = PictureSelectorPreviewFragment.newInstance()
                val position = intent.getIntExtra(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, 0)
                val previewResult: ArrayList<LocalMedia> = SelectedManager.getSelectedPreviewResult()
                val previewData: ArrayList<LocalMedia> = ArrayList(previewResult)
                val isDisplayDelete = intent
                    .getBooleanExtra(PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE, false)
                (targetFragment ).setExternalPreviewData(position,
                    previewData.size,
                    previewData,
                    isDisplayDelete)
            }
            else -> {
                fragmentTag = TAG
                targetFragment = PictureOnlyCameraFragment.newInstance()
            }
        }
        val supportFragmentManager = supportFragmentManager
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        FragmentInjectManager.injectSystemRoomFragment(supportFragmentManager,
            fragmentTag,
            targetFragment)
    }

    @SuppressLint("RtlHardcoded")
    private fun setActivitySize() {
        val window = window
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.ps_anim_fade_out)
    }
}
