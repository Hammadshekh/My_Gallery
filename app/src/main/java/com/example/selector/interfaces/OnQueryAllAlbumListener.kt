package com.example.selector.interfaces

interface OnQueryAllAlbumListener<T> {
    fun onComplete(result: List<T>)
}