package com.example.selector.engine

import com.example.selector.basic.IBridgeLoaderFactory
import com.example.selector.interfaces.OnInjectLayoutResourceListener
import com.example.selector.interfaces.OnResultCallbackListener
import com.luck.picture.lib.entity.LocalMedia

interface PictureSelectorEngine {
    /**
     * Create ImageLoad Engine
     *
     * @return
     */
    fun createImageLoaderEngine(): ImageEngine?

    /**
     * Create compress Engine
     *
     * @return
     */
    fun createCompressEngine(): CompressEngine?

    /**
     * Create compress Engine
     *
     * @return
     */
    fun createCompressFileEngine(): CompressFileEngine?

    /**
     * Create loader data Engine
     *
     * @return
     */
    fun createLoaderDataEngine(): ExtendLoaderEngine?

    /**
     * Create loader data Engine
     *
     * @return
     */
    fun onCreateLoader(): IBridgeLoaderFactory?

    /**
     * Create SandboxFileEngine  Engine
     *
     * @return
     */
    fun createSandboxFileEngine(): SandboxFileEngine?

    /**
     * Create UriToFileTransformEngine  Engine
     *
     * @return
     */
    fun createUriToFileTransformEngine(): UriToFileTransformEngine?

    /**
     * Create LayoutResource  Listener
     *
     * @return
     */
    fun createLayoutResourceListener(): OnInjectLayoutResourceListener?

    /**
     * Create Result Listener
     *
     * @return
     */
    val resultCallbackListener: OnResultCallbackListener<LocalMedia?>?
}
