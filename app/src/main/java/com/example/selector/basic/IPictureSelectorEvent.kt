package com.example.selector.basic

interface IPictureSelectorEvent {
    /**
     *
    Get album directory
     */
    fun loadAllAlbumData()

    /**
     * 获取首页资源
     */
    fun loadFirstPageMediaData(firstBucketId: Long)

    /**
     * 加载应用沙盒内的资源
     */
    fun loadOnlyInAppDirectoryAllMediaData()

    /**
     * 加载更多
     */
    fun loadMoreMediaData()
}
