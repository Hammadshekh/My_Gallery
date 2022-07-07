package com.example.selector.manager

import com.example.selector.entity.LocalMediaFolder
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

object SelectedManager {
    const val INVALID = -1
    const val ADD_SUCCESS = 0
    const val REMOVE = 1
    const val SUCCESS = 200

    /**
     * selected result
     */
    val selectedResult: ArrayList<LocalMedia> = ArrayList<LocalMedia>()
    @Synchronized
    fun addSelectResult(media: LocalMedia) {
        selectedResult.add(media)
    }

    @Synchronized
    fun addAllSelectResult(result: ArrayList<LocalMedia>) {
        selectedResult.addAll(result)
    }

    val selectCount: Int
        get() = selectedResult.size
    val topResultMimeType: String
        get() = if (selectedResult.size > 0) selectedResult[0].mimeType.toString() else ""

    @Synchronized
    fun clearSelectResult() {
        if (selectedResult.size > 0) {
            selectedResult.clear()
        }
    }

    /**
     * selected preview result
     */
    private val selectedPreviewResult: ArrayList<LocalMedia> = ArrayList<LocalMedia>()
    fun getSelectedPreviewResult(): ArrayList<LocalMedia> {
        return selectedPreviewResult
    }

    fun addSelectedPreviewResult(list: ArrayList<LocalMedia>?) {
        clearPreviewData()
        selectedPreviewResult.addAll(list!!)
    }

    fun clearPreviewData() {
        if (selectedPreviewResult.size > 0) {
            selectedPreviewResult.clear()
        }
    }

    /**
     * all data source
     */
    val dataSource: ArrayList<LocalMedia> = ArrayList<LocalMedia>()

    fun addDataSource(list: ArrayList<LocalMedia>?) {
        if (list != null) {
            clearDataSource()
            dataSource.addAll(list)
        }
    }

    fun clearDataSource() {
        if (dataSource.size > 0) {
            dataSource.clear()
        }
    }

    /**
     * all album data source
     */
    val albumDataSource: ArrayList<LocalMediaFolder> = ArrayList<LocalMediaFolder>()

    fun addAlbumDataSource(list: List<LocalMediaFolder>?) {
        if (list != null) {
            clearAlbumDataSource()
            albumDataSource.addAll(list)
        }
    }

    fun clearAlbumDataSource() {
        if (albumDataSource.size > 0) {
            albumDataSource.clear()
        }
    }

    /**
     * current selected album
     */
    var currentLocalMediaFolder: LocalMediaFolder? = null
}
