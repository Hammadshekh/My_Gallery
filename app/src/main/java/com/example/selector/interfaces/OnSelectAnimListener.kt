package com.example.selector.interfaces

import android.view.View

interface OnSelectAnimListener {
    /**
     * onSelectAnim
     *
     * @param view
     * @return anim duration
     */
    fun onSelectAnim(view: View?): Long
}