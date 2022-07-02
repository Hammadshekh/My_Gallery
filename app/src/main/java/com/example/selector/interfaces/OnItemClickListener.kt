package com.example.selector.interfaces

import android.view.View

interface OnItemClickListener {
    /**
     * Item click event
     *
     * @param v
     * @param position
     */
    fun onItemClick(v: View?, position: Int)
}