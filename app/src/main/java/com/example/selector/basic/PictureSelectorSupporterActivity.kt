package com.example.selector.basic

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat

class PictureSelectorSupporterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        setContentView(R.layout.ps_activity_container)
        setupFragment()
    }

    private fun immersive() {
        val mainStyle: SelectMainStyle = PictureSelectionConfig.selectorStyle.getSelectMainStyle()
        var statusBarColor: Int = mainStyle.getStatusBarColor()
        var navigationBarColor: Int = mainStyle.getNavigationBarColor()
        val isDarkStatusBarBlack: Boolean = mainStyle.isDarkStatusBarBlack()
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
        FragmentInjectManager.injectFragment(this, PictureSelectorFragment.TAG,
            PictureSelectorFragment.newInstance())
    }

    /**
     * set app language
     */
    fun initAppLanguage() {
        val config: PictureSelectionConfig = PictureSelectionConfig.getInstance()
        if (config.language !== LanguageConfig.UNKNOWN_LANGUAGE && !config.isOnlyCamera) {
            PictureLanguageUtils.setAppLanguage(this, config.language)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initAppLanguage()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(PictureContextWrapper.wrap(newBase,
            PictureSelectionConfig.getInstance().language))
    }

    override fun finish() {
        super.finish()
        val windowAnimationStyle: PictureWindowAnimationStyle =
            PictureSelectionConfig.selectorStyle.getWindowAnimationStyle()
        overridePendingTransition(0, windowAnimationStyle.activityExitAnimation)
    }
}