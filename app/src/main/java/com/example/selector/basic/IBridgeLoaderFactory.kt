package com.example.selector.basic

import com.example.selector.loader.IBridgeMediaLoader

interface IBridgeLoaderFactory {
    /**
     * CreateLoader
     */
    fun onCreateLoader(): IBridgeMediaLoader?
}