package com.example.selector.basic

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

interface IBridgeViewLifecycle {
    /**
     * onViewCreated
     *
     * @param fragment
     * @param view
     * @param savedInstanceState
     */
    fun onViewCreated(fragment: Fragment?, view: View?, savedInstanceState: Bundle?)

    /**
     * onDestroy
     *
     * @param fragment
     */
    fun onDestroy(fragment: Fragment?)
}
