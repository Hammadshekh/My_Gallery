package com.example.selector.engine

import android.content.Context
import android.net.Uri
import java.util.ArrayList

interface CompressFileEngine {
    /**
     * Custom compression engine
     *
     *
     * Users can implement this interface, and then access their own compression framework to plug
     * the compressed path into the [LocalMedia] object;
     *
     *
     * @param context
     * @param source
     */
    fun onStartCompress(
        context: Context?,
        source: ArrayList<Uri?>?,
        call: OnKeyValueResultCallbackListener?,
    )
}