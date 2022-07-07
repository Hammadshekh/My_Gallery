package com.example.selector.app

import android.content.Context
import com.example.selector.engine.PictureSelectorEngine

class PictureAppMaster private constructor() : IApp {
    override val appContext: Context?
        get() = if (app == null) {
            null
        } else app?.appContext
    override val pictureSelectorEngine: PictureSelectorEngine?
        get() {
            return app?.pictureSelectorEngine
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
