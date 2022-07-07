package com.example.selector.interfaces

import java.util.*

open class OnQueryDataResultListener<T> {
    /**
     * Query to complete The callback listener
     *
     * @param result        The data source
     * @param isHasMore   Is there more
     */
    open fun onComplete(result: ArrayList<T>, isHasMore: Boolean) {}
}
