package com.example.selector.interfaces

import android.content.Context

interface OnInjectLayoutResourceListener {
    /**
     * inject custom layout resource id
     *
     *
     * The layout ID must be the same as
     * [R.layout.ps_fragment_selector]
     * [R.layout.ps_fragment_preview]
     * [R.layout.ps_item_grid_image]
     * [R.layout.ps_item_grid_video]
     * [R.layout.ps_item_grid_audio]
     * [R.layout.ps_album_folder_item]
     * [R.layout.ps_preview_image]
     * [R.layout.ps_preview_video]
     *
     *
     * The layout can be overloaded to implement differences on the UI, but the view ID cannot be changed
     *
     *
     * @param context
     * @param resourceSource [InjectResourceSource]
     * @return
     */
    fun getLayoutResourceId(context: Context?, resourceSource: Int): Int
}
