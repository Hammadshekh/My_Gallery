package com.example.selector.utils

import android.content.Context
import android.net.Uri
import com.example.camerax.utils.FileUtils.isContent
import com.example.compress.Checker.Companion.isContent
import com.example.selector.basic.PictureContentResolver
import com.example.selector.config.PictureMimeType
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

object SandboxTransformUtils {
    /**
     * 把外部目录下的图片拷贝至沙盒内
     *
     * @param ctx
     * @param url
     * @param mineType
     * @param customFileName
     * @return
     */
    /**
     * 把外部目录下的图片拷贝至沙盒内
     *
     * @param ctx
     * @param url
     * @param mineType
     * @return
     */
    @JvmOverloads
    fun copyPathToSandbox(
        ctx: Context?,
        url: String?,
        mineType: String?,
        customFileName: String? = "",
    ): String? {
        try {
            val inputStream: InputStream
            val sandboxPath = PictureFileUtils.createFilePath(ctx!!, "", mineType, customFileName)
            inputStream = if (PictureMimeType.isContent(url)) ({
                PictureContentResolver.getContentResolverOpenInputStream(ctx, Uri.parse(url))
            })!! else {
                FileInputStream(url)
            }
            val copyFileSuccess =
                PictureFileUtils.writeFileFromIS(inputStream, FileOutputStream(sandboxPath))
            if (copyFileSuccess) {
                return sandboxPath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
