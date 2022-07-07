package com.example.selector.basic

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mygallery.R
import com.example.selector.config.LanguageConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.PictureSelectionConfig.Companion.instance
import com.example.selector.immersive.ImmersiveManager
import com.example.selector.style.PictureWindowAnimationStyle
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.PictureFileUtils.TAG
import com.example.selector.utils.PictureLanguageUtils
import com.example.selector.utils.StyleUtils

class PictureSelectorSupporterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        setContentView(R.layout.ps_activity_container)
        setupFragment()
    }

    private fun immersive() {
        val mainStyle: SelectMainStyle? = PictureSelectionConfig.selectorStyle?.selectMainStyle
        var statusBarColor: Int = mainStyle?.statusBarColor!!
        var navigationBarColor: Int? = mainStyle.navigationBarColor
        val isDarkStatusBarBlack: Boolean = mainStyle.isDarkStatusBarBlack
        if (!StyleUtils.checkStyleValidity(statusBarColor)) {
            statusBarColor = ContextCompat.getColor(this, R.color.ps_color_grey)
        }
        if (navigationBarColor?.let { StyleUtils.checkStyleValidity(it) } == true) {
            navigationBarColor = ContextCompat.getColor(this, R.color.ps_color_grey)
        }
        if (navigationBarColor != null) {
            ImmersiveManager.immersiveAboveAPI23(this,
                statusBarColor,
                navigationBarColor,
                isDarkStatusBarBlack)
        }
    }

    private fun setupFragment() {
        FragmentInjectManager.injectFragment(this,TAG,
            instance)
    }

    /**
     * set app language
     */
    private fun initAppLanguage() {
        val config: PictureSelectionConfig = instance!!
        if (config.language != LanguageConfig.UNKNOWN_LANGUAGE && !config.isOnlyCamera) {
            PictureLanguageUtils.setAppLanguage(this, config.language)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initAppLanguage()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(PictureContextWrapper.wrap(newBase,
            instance!!.language))
    }

    override fun finish() {
        super.finish()
        val windowAnimationStyle: PictureWindowAnimationStyle? =
            PictureSelectionConfig.selectorStyle?.windowAnimationStyle
        windowAnimationStyle?.activityExitAnimation?.let { overridePendingTransition(0, it) }
    }
}