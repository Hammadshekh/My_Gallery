package com.example.selector.interfaces

import android.content.Context
import com.example.selector.config.PictureSelectionConfig

interface OnSelectLimitTipsListener {
    /**
     * Custom limit tips
     *
     * @param context
     * @param config    PictureSelectionConfig
     * @param limitType Use [SelectLimitType]
     * @return If true is returned, the user needs to customize the implementation prompt content，
     * Otherwise, use the system default prompt
     */
    fun onSelectLimitTips(
        context: Context?,
        config: PictureSelectionConfig?,
        limitType: Int,
    ): Boolean
}