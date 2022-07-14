package com.example.selector.utils

import com.example.selector.entity.LocalMediaFolder
import com.luck.picture.lib.entity.LocalMedia
import java.util.*

object SortUtils {
    /**
     * Sort by the number of files
     *
     * @param imageFolders
     */
    fun sortFolder(imageFolders: List<LocalMediaFolder>) {
        Collections.sort(imageFolders, Comparator { lhs, rhs ->
            if (lhs.data == null || rhs.data == null) {
                return@Comparator 0
            }
            val lSize: Int = lhs.folderTotalNum
            val rSize: Int = rhs.folderTotalNum
            rSize.compareTo(lSize)
        })
    }

    /**
     * Sort by the add Time of files
     *
     * @param list
     */
    fun sortLocalMediaAddedTime(list: List<LocalMedia>) {
        Collections.sort(list) { lhs: LocalMedia, rhs: LocalMedia ->
            val lAddedTime = lhs.dateAddedTime
            val rAddedTime = rhs.dateAddedTime
            rAddedTime.compareTo(lAddedTime)
        }
    }
}

