package com.example.selector.engine

import android.content.Context
import com.example.selector.interfaces.OnKeyValueResultCallbackListener

interface UriToFileTransformEngine {
    /**
     * Custom Sandbox File engine
     *
     *
     * Users can implement this interface, and then access their own sandbox framework to plug
     * the sandbox path into the [LocalMedia] object;
     *
     *
     *
     * This is an asynchronous thread callback
     *
     *
     * @param context  context
     * @param srcPath
     * @param mineType
     */
    fun onUriToFileAsyncTransform(
        context: Context?,
        srcPath: String?,
        mineType: String?,
        call: OnKeyValueResultCallbackListener?,
    )
}