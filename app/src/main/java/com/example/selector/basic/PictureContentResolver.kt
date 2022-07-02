package com.example.selector.basic

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

object PictureContentResolver {
    /**
     * ContentResolver openInputStream
     *
     * @param context
     * @param uri
     * @return
     */
    fun getContentResolverOpenInputStream(context: Context, uri: Uri?): InputStream? {
        try {
            return context.contentResolver.openInputStream(uri!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * ContentResolver OutputStream
     *
     * @param context
     * @param uri
     * @return
     */
    fun getContentResolverOpenOutputStream(context: Context, uri: Uri?): OutputStream? {
        try {
            return context.contentResolver.openOutputStream(uri!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
