package com.example.selector.utils

import java.util.*

object SortUtils {
    /**
     * Sort by the number of files
     *
     * @param imageFolders
     */
    fun sortFolder(imageFolders: List<LocalMediaFolder?>?) {
        Collections.sort(imageFolders, Comparator<T> { lhs: T, rhs: T ->
            if (lhs.getData() == null || rhs.getData() == null) {
                return@sort 0
            }
            val lSize: Int = lhs.getFolderTotalNum()
            val rSize: Int = rhs.getFolderTotalNum()
            Integer.compare(rSize, lSize)
        })
    }

    /**
     * Sort by the add Time of files
     *
     * @param list
     */
    fun sortLocalMediaAddedTime(list: List<LocalMedia?>?) {
        Collections.sort(list, Comparator<T> { lhs: T, rhs: T ->
            val lAddedTime: Long = lhs.getDateAddedTime()
            val rAddedTime: Long = rhs.getDateAddedTime()
            java.lang.Long.compare(rAddedTime, lAddedTime)
        })
    }
}
