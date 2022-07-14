package com.example.selector.utils

import android.content.Context
import android.net.Uri
import com.example.selector.basic.PictureContentResolver
import com.example.selector.config.PictureMimeType
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

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

    fun copyPathToSandbox(
        ctx: Context,
        url: String?,
        mineType: String,
        customFileName: String? = "",
    ): String? {
        try {
            val sandboxPath =
                customFileName?.let { PictureFileUtils.createFilePath(ctx, "", mineType, it) }
            val inputStream: () -> InputStream? = if (url?.let { PictureMimeType.isContent(it) } == true) ({
                PictureContentResolver.getContentResolverOpenInputStream(ctx, Uri.parse(url))
            }) else {
            {
                FileInputStream(url)
            }
            }
            val copyFileSuccess =
                PictureFileUtils.writeFileFromIS(inputStream, FileOutputStream(sandboxPath))
            if (copyFileSuccess) {
                return sandboxPath.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
