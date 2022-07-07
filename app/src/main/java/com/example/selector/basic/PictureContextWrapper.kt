package com.example.selector.basic

import android.content.Context
import android.content.ContextWrapper
import com.example.selector.config.LanguageConfig
import com.example.selector.utils.PictureLanguageUtils

class PictureContextWrapper(base: Context?) : ContextWrapper(base) {
    override fun getSystemService(name: String): Any {
        return if (AUDIO_SERVICE == name) {
            applicationContext.getSystemService(name)
        } else super.getSystemService(name)
    }

    companion object {
        fun wrap(context: Context?, language: Int): ContextWrapper {
            if (language != LanguageConfig.UNKNOWN_LANGUAGE) {
                context?.let { PictureLanguageUtils.setAppLanguage(it, language) }
            }
            return PictureContextWrapper(context)
        }
    }
}
