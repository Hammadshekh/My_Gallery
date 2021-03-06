package com.example.selector.language

import com.example.selector.config.LanguageConfig
import java.util.*

object LocaleTransform {
    fun getLanguage(language: Int): Locale {
        return when (language) {
            LanguageConfig.ENGLISH ->                 // 英语-美国
                Locale.ENGLISH
            LanguageConfig.TRADITIONAL_CHINESE ->                 // 繁体中文
                Locale.TRADITIONAL_CHINESE
            LanguageConfig.KOREA ->                 // 韩语
                Locale.KOREA
            LanguageConfig.GERMANY ->                 // 德语
                Locale.GERMANY
            LanguageConfig.FRANCE ->                 // 法语
                Locale.FRANCE
            LanguageConfig.JAPAN ->                 // 日语
                Locale.JAPAN
            LanguageConfig.VIETNAM ->                 // 越南语
                Locale("vi")
            LanguageConfig.SPANISH ->                 // 西班牙语
                Locale("es", "ES")
            LanguageConfig.PORTUGAL ->                 // 葡萄牙语
                Locale("pt", "PT")
            LanguageConfig.AR ->                 // 阿拉伯语
                Locale("ar", "AE")
            LanguageConfig.RU ->                 // 俄语
                Locale("ru", "rRU")
            else ->                 // 简体中文
                Locale.CHINESE
        }
    }
}
