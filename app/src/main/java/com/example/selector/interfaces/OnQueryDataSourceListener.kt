package com.example.selector.interfaces

interface OnQueryDataSourceListener<T> {
    /**
     * Query data source
     *
     * @param result The data source
     */
    fun onComplete(result: List<T>?)
}