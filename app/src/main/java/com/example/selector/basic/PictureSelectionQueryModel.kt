package com.example.selector.basic

import android.app.Activity
import android.text.TextUtils
import com.example.selector.config.FileSizeUnit
import com.example.selector.config.PictureConfig
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.interfaces.OnQueryAllAlbumListener
import com.example.selector.interfaces.OnQueryDataResultListener
import com.example.selector.interfaces.OnQueryDataSourceListener
import com.example.selector.loader.IBridgeMediaLoader
import com.example.selector.loader.LocalMediaLoader
import com.example.selector.loader.LocalMediaPageLoader
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

class PictureSelectionQueryModel(selector: PictureSelector, selectMimeType: Int) {
    private val selectionConfig: PictureSelectionConfig
    private val selector: PictureSelector

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @return
     */
    fun isPageStrategy(isPageStrategy: Boolean): PictureSelectionQueryModel {
        selectionConfig.isPageStrategy = isPageStrategy
        return this
    }

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @param pageSize       Maximum number of pages [is preferably no less than 20][PageSize]
     * @return
     */
    fun isPageStrategy(isPageStrategy: Boolean, pageSize: Int): PictureSelectionQueryModel {
        selectionConfig.isPageStrategy = isPageStrategy
        selectionConfig.pageSize =
            if (pageSize < PictureConfig.MIN_PAGE_SIZE) PictureConfig.MAX_PAGE_SIZE else pageSize
        return this
    }

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @param pageSize            Maximum number of pages [is preferably no less than 20][PageSize]
     * @param isFilterInvalidFile Whether to filter invalid files [of the query performance is consumed,Especially on the Q version][Some]
     * @return
     */
    fun isPageStrategy(
        isPageStrategy: Boolean,
        pageSize: Int,
        isFilterInvalidFile: Boolean,
    ): PictureSelectionQueryModel {
        selectionConfig.isPageStrategy = isPageStrategy
        selectionConfig.pageSize =
            if (pageSize < PictureConfig.MIN_PAGE_SIZE) PictureConfig.MAX_PAGE_SIZE else pageSize
        selectionConfig.isFilterInvalidFile = isFilterInvalidFile
        return this
    }

    /**
     * query local data source sort
     * [# DATE_ADDED # _ID][MediaStore.MediaColumns.DATE_MODIFIED]
     *
     *
     * example:
     * MediaStore.MediaColumns.DATE_MODIFIED + " DESC";  or MediaStore.MediaColumns.DATE_MODIFIED + " ASC";
     *
     *
     * @param sortOrder
     * @return
     */
    fun setQuerySortOrder(sortOrder: String): PictureSelectionQueryModel {
        if (!TextUtils.isEmpty(sortOrder)) {
            selectionConfig.sortOrder = sortOrder
        }
        return this
    }

    /**
     * @param isGif Whether to open gif
     * @return
     */
    fun isGif(isGif: Boolean): PictureSelectionQueryModel {
        selectionConfig.isGif = isGif
        return this
    }

    /**
     * @param isWebp Whether to open .webp
     * @return
     */
    fun isWebp(isWebp: Boolean): PictureSelectionQueryModel {
        selectionConfig.isWebp = isWebp
        return this
    }

    /**
     * @param isBmp Whether to open .isBmp
     * @return
     */
    fun isBmp(isBmp: Boolean): PictureSelectionQueryModel {
        selectionConfig.isBmp = isBmp
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter max file size
     * @return
     */
    fun setFilterMaxFileSize(fileKbSize: Long): PictureSelectionQueryModel {
        if (fileKbSize >= FileSizeUnit.MB) {
            selectionConfig.filterMaxFileSize = fileKbSize
        } else {
            selectionConfig.filterMaxFileSize = fileKbSize * FileSizeUnit.KB
        }
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter min file size
     * @return
     */
    fun setFilterMinFileSize(fileKbSize: Long): PictureSelectionQueryModel {
        if (fileKbSize >= FileSizeUnit.MB) {
            selectionConfig.filterMinFileSize = fileKbSize
        } else {
            selectionConfig.filterMinFileSize = fileKbSize * FileSizeUnit.KB
        }
        return this
    }

    /**
     * filter max seconds video
     *
     * @param videoMaxSecond filter video max second
     * @return
     */
    fun setFilterVideoMaxSecond(videoMaxSecond: Int): PictureSelectionQueryModel {
        selectionConfig.filterVideoMaxSecond = videoMaxSecond * 1000
        return this
    }

    /**
     * filter min seconds video
     *
     * @param videoMinSecond filter video min second
     * @return
     */
    fun setFilterVideoMinSecond(videoMinSecond: Int): PictureSelectionQueryModel {
        selectionConfig.filterVideoMinSecond = videoMinSecond * 1000
        return this
    }

    /**
     * build local media Loader
     */
    fun buildMediaLoader(): IBridgeMediaLoader {
        val activity: Activity = selector.activity
            ?: throw NullPointerException("Activity cannot be null")
        val loader: IBridgeMediaLoader =
            if (selectionConfig.isPageStrategy) LocalMediaPageLoader() else LocalMediaLoader()
        loader.initConfig(activity, selectionConfig)
        return loader
    }

    /**
     * obtain album data source
     *
     * @param call
     */
    fun obtainAlbumData(call: OnQueryDataSourceListener<LocalMediaFolder>) {
        val activity: Activity = selector.activity
            ?: throw NullPointerException("Activity cannot be null")
        val loader: IBridgeMediaLoader =
            if (selectionConfig.isPageStrategy) LocalMediaPageLoader() else LocalMediaLoader()
        loader.initConfig(activity, selectionConfig)
        loader.loadAllAlbum(object : OnQueryAllAlbumListener<LocalMediaFolder> {
            override fun onComplete(result: List<LocalMediaFolder>) {
                call.onComplete(result)
            }
        })
    }

    /**
     * obtain data source
     *
     * @param call
     */
    fun obtainMediaData(call: OnQueryDataSourceListener<LocalMedia?>?) {
        val activity: Activity = selector.activity
            ?: throw NullPointerException("Activity cannot be null")
        if (call == null) {
            throw NullPointerException("OnQueryDataSourceListener cannot be null")
        }
        val loader: IBridgeMediaLoader =
            if (selectionConfig.isPageStrategy) LocalMediaPageLoader() else LocalMediaLoader()
        loader.initConfig(activity, selectionConfig)
        loader.loadAllAlbum(object : OnQueryAllAlbumListener<LocalMediaFolder> {
            override fun onComplete(result: List<LocalMediaFolder>) {
                if (result.size > 0) {
                    val all: LocalMediaFolder = result[0]
                    if (selectionConfig.isPageStrategy) {
                        loader.loadPageMediaData(all.bucketId, 1, selectionConfig.pageSize,
                            object : OnQueryDataResultListener<LocalMedia>() {
                                override fun onComplete(
                                    result: ArrayList<LocalMedia>,
                                    isHasMore: Boolean,
                                ) {
                                    call.onComplete(result)
                                }
                            })
                    } else {
                        val data: ArrayList<LocalMedia> = all.data
                        call.onComplete(data)
                    }
                }
            }
        })
    }

    init {
        this.selector = selector
        selectionConfig = PictureSelectionConfig.cleanInstance
        selectionConfig.chooseMode = selectMimeType
    }
}
