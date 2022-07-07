package com.example.selector.magical

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

object BuildRecycleItemViewParams {
    private val viewParams: MutableList<ViewParams> = ArrayList<ViewParams>()
    fun clear() {
        if (viewParams.size > 0) {
            viewParams.clear()
        }
    }

    fun getItemViewParams(position: Int): ViewParams? {
        return if (viewParams.size > position) viewParams[position] else null
    }

    fun generateViewParams(recyclerView: RecyclerView, statusBarHeight: Int) {
        val views: MutableList<View?> = ArrayList()
        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {
            val view = recyclerView.getChildAt(i) ?: continue
            views.add(view)
        }
        val layoutManager = recyclerView.layoutManager as GridLayoutManager? ?: return
        val firstPos: Int
        var lastPos: Int
        val totalCount = layoutManager.itemCount
        firstPos = layoutManager.findFirstVisibleItemPosition()
        lastPos = layoutManager.findLastVisibleItemPosition()
        lastPos = if (lastPos > totalCount) totalCount - 1 else lastPos
        fillPlaceHolder(views, totalCount, firstPos, lastPos)
        viewParams.clear()
        for (i in views.indices) {
            val view = views[i]
            val viewParam = ViewParams()
            if (view == null) {
                viewParam.left = 0
                viewParam.top = 0
                viewParam.width = 0
                viewParam.height = 0
            } else {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                viewParam.left = location[0]
                viewParam.top = location[1] - statusBarHeight
                viewParam.width = view.width
                viewParam.height = view.height
            }
            viewParams.add(viewParam)
        }
    }

    private fun fillPlaceHolder(
        originImageList: MutableList<View?>,
        totalCount: Int,
        firstPos: Int,
        lastPos: Int,
    ) {
        if (firstPos > 0) {
            for (i in firstPos downTo 1) {
                originImageList.add(0, null)
            }
        }
        if (lastPos < totalCount) {
            for (i in totalCount - 1 - lastPos downTo 1) {
                originImageList.add(null)
            }
        }
    }
}
