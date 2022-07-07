package com.example.selector.app

import android.content.Context
import com.example.selector.engine.PictureSelectorEngine

interface IApp {
    /**
     * Application
     *
     * @return
     */
    val appContext: Context?

    /**
     * PictureSelectorEngine
     *
     * @return
     */
    val pictureSelectorEngine: PictureSelectorEngine?
}
