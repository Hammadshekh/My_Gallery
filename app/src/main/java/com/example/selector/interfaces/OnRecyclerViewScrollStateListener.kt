package com.example.selector.interfaces

interface OnRecyclerViewScrollStateListener {
    /**
     * RecyclerView Scroll Fast
     */
    fun onScrollFast()

    /**
     * RecyclerView Scroll Slow
     */
    fun onScrollSlow()
}