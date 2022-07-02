package com.example.mygallery.files

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.io.IOException

/*class VideoRequestHandler : RequestHandler() {
    var SCHEME_VIDEO = "video"
    fun canHandleRequest(data: Request): Boolean {
        val scheme: String = data.uri.getScheme()
        return SCHEME_VIDEO == scheme
    }

    @Throws(IOException::class)
    fun load(request: Request, networkPolicy: Int): Result? {
        val uri: Uri = request.uri
        val path = uri.path
        if (!TextUtils.isEmpty(path)) {
            val bm =
                ThumbnailUtils.createVideoThumbnail(path!!, MediaStore.Images.Thumbnails.MINI_KIND)
            return Result(bm, Picasso.LoadedFrom.DISK)
        }
        return null
    }
}*/
