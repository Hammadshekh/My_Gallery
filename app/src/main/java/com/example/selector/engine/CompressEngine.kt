package com.example.selector.engine

import android.content.Context
import java.util.ArrayList

interface CompressEngine {
    /**
     * Custom compression engine
     *
     *
     * Users can implement this interface, and then access their own compression framework to plug
     * the compressed path into the [LocalMedia] object;
     *
     *
     *
     *
     *
     * 1、LocalMedia media = new LocalMedia();
     * media.setCompressed(true);
     * media.setCompressPath("Your compressed path");
     *
     *
     *
     * 2、listener.onCall( "you result" );
     *
     *
     * @param context
     * @param list
     * @param listener
     */
    fun onStartCompress(
        context: Context?,
        list: ArrayList<LocalMedia?>?,
        listener: OnCallbackListener<ArrayList<LocalMedia?>?>?,
    )
}
