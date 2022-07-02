package com.example.selector.engine

import android.content.Context

interface SandboxFileEngine {
    /**
     * Custom Sandbox File engine
     *
     *
     * Users can implement this interface, and then access their own sandbox framework to plug
     * the sandbox path into the [LocalMedia] object;
     *
     *
     *
     *
     *
     * 1、LocalMedia media = new LocalMedia();
     * media.setSandboxPath("Your sandbox path");
     *
     *
     *
     * 2、listener.onCall( "you result" );
     *
     *
     * @param context              context
     * @param isOriginalImage The original drawing needs to be processed
     * @param index                The location of the resource in the result queue
     * @param media                LocalMedia
     * @param listener
     */
    fun onStartSandboxFileTransform(
        context: Context?, isOriginalImage: Boolean,
        index: Int, media: LocalMedia?,
        listener: OnCallbackIndexListener<LocalMedia?>?,
    )
}
