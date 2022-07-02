package com.example.selector.app

import android.content.Context

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
