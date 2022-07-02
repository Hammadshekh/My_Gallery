package com.example.selector.engine

import android.content.Context

interface ExtendLoaderEngine {
    /**
     * load all album list data
     *
     *
     * Users can implement some interfaces to access their own query data,
     * provided that they comply with the [LocalMediaFolder] standard
     *
     *
     *
     *
     * query.onComplete(List<LocalMediaFolder> result);
    </LocalMediaFolder> *
     *
     * @param context
     * @param query
     */
    fun loadAllAlbumData(context: Context?, query: OnQueryAllAlbumListener<LocalMediaFolder?>?)

    /**
     * load resources in the specified directory
     *
     *
     * Users can implement some interfaces to access their own query data,
     * provided that they comply with the [LocalMediaFolder] standard
     *
     *
     *
     *
     * query.onComplete(LocalMediaFolder result);
     *
     *
     * @param context
     * @param query
     */
    fun loadOnlyInAppDirAllMediaData(
        context: Context?,
        query: OnQueryAlbumListener<LocalMediaFolder?>?,
    )

    /**
     * load the first item of data in the album list
     * [PictureSelectionConfig] Valid only in isPageStrategy mode
     *
     *
     * Users can implement some interfaces to access their own query data,
     * provided that they comply with the [LocalMedia] standard
     *
     *
     *
     * query.onComplete(List<LocalMedia> result, int currentPage, boolean isHasMore);
    </LocalMedia> *
     *
     *
     *
     * isHasMore; Whether there is more data needs to be controlled by developers
     *
     *
     * @param context
     * @param bucketId Album ID
     * @param page     first page
     * @param pageSize How many entries per page
     * @param query
     */
    fun loadFirstPageMediaData(
        context: Context?, bucketId: Long, page: Int, pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia?>?,
    )

    /**
     * load the first item of data in the album list
     * [PictureSelectionConfig] Valid only in isPageStrategy mode
     *
     *
     *
     *
     * Users can implement some interfaces to access their own query data,
     * provided that they comply with the [LocalMedia] standard
     *
     * query.onComplete(List<LocalMedia> result, int currentPage, boolean isHasMore);
     *
     *
     *
     *
     * currentPage; Represents the current page number
     * isHasMore; Whether there is more data needs to be controlled by developers
     *
     *
     * @param context
     * @param bucketId Album ID
     * @param page     Current page number
     * @param limit    query limit
     * @param pageSize How many entries per page
     * @param query
    </LocalMedia> */
    fun loadMoreMediaData(
        context: Context?, bucketId: Long, page: Int, limit: Int, pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia?>?,
    )
}
