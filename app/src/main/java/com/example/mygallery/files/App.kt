package com.example.mygallery.files

import android.app.Application
import android.content.Context
import android.os.Build.VERSION
import android.util.Log
import java.io.File

class App : Application(), IApp, CameraXConfig.Provider, ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        PictureAppMaster.instance.setApp(this)
    }

    val appContext: Context
        get() = this
    val pictureSelectorEngine: PictureSelectorEngine
        get() = PictureSelectorEngineImp()
    val cameraXConfig: CameraXConfig
        get() = CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()

    fun newImageLoader(): ImageLoader {
        val imageLoader: ImageLoader.Builder = Builder(appContext)
        val newBuilder: ComponentRegistry.Builder = ComponentRegistry().newBuilder()
        newBuilder.add(Factory())
        if (VERSION.SDK_INT >= 28) {
            newBuilder.add(Factory())
        } else {
            newBuilder.add(Factory())
        }
        val componentRegistry: ComponentRegistry = newBuilder.build()
        imageLoader.components(componentRegistry)
        imageLoader.memoryCache(Builder(appContext)
            .maxSizePercent(0.25).build())
        imageLoader.diskCache(Builder()
            .directory(File(cacheDir, "image_cache"))
            .maxSizePercent(0.02)
            .build())
        return imageLoader.build()
    }

    companion object {
        private val TAG = App::class.java.simpleName
    }
}
