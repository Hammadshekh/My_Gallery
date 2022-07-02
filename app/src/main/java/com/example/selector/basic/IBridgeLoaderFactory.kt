package com.example.selector.basic

interface IBridgeLoaderFactory {
    /**
     * CreateLoader
     */
    fun onCreateLoader(): IBridgeMediaLoader?
}