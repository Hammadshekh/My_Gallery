package com.example.selector.app

import android.content.Context

class PictureAppMaster private constructor() : IApp {
    override val appContext: Context?
        get() = if (app == null) {
            null
        } else app.getAppContext()
    override val pictureSelectorEngine: PictureSelectorEngine?
        get() {
            return if (app == null) {
                null
            } else app.getPictureSelectorEngine()
        }
    var app: IApp? = null

    companion object {
        private var mInstance: PictureAppMaster? = null
        val instance: PictureAppMaster?
            get() {
                if (mInstance == null) {
                    synchronized(PictureAppMaster::class.java) {
                        if (mInstance == null) {
                            mInstance = PictureAppMaster()
                        }
                    }
                }
                return mInstance
            }
    }
}
