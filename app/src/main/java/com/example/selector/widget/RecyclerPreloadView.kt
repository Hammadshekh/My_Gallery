package com.example.selector.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selector.interfaces.OnRecyclerViewPreloadMoreListener
import com.example.selector.interfaces.OnRecyclerViewScrollListener
import com.example.selector.interfaces.OnRecyclerViewScrollStateListener
import java.lang.RuntimeException
import kotlin.math.abs

class RecyclerPreloadView : RecyclerView {
    private var isInTheBottom = false
    /**
     * Whether to load more
     */
    /**
     * Whether to load more
     *
     * @param isEnabledLoadMore
     */
    var isEnabledLoadMore = false

    /**
     * Gets the first visible position index
     *
     * @return
     */
    var firstVisiblePosition = 0
        private set

    /**
     * Gets the last visible position index
     *
     * @return
     */
    var lastVisiblePosition = 0

    /**
     * reachBottomRow = 1;(default)
     * mean : when the lastVisibleRow is lastRow , call the onReachBottom();
     * reachBottomRow = 2;
     * mean : when the lastVisibleRow is Penultimate Row , call the onReachBottom();
     * And so on
     */
    private var reachBottomRow = BOTTOM_DEFAULT

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs,
        defStyle) {
    }

    fun setReachBottomRow(reachBottomRow: Int) {
        var reachBottomRow = reachBottomRow
        if (reachBottomRow < 1) reachBottomRow = 1
        this.reachBottomRow = reachBottomRow
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val layoutManager = layoutManager
            ?: throw RuntimeException("LayoutManager is null,Please check it!")
        setLayoutManagerPosition(layoutManager)
        if (onRecyclerViewPreloadListener != null) {
            if (isEnabledLoadMore) {
                val adapter = adapter ?: throw RuntimeException("Adapter is null,Please check it!")
                var isReachBottom = false
                if (layoutManager is GridLayoutManager) {
                    val rowCount = adapter.itemCount / layoutManager.spanCount
                    val lastVisibleRowPosition =
                        layoutManager.findLastVisibleItemPosition() / layoutManager.spanCount
                    isReachBottom = lastVisibleRowPosition >= rowCount - reachBottomRow
                }
                if (!isReachBottom) {
                    isInTheBottom = false
                } else if (!isInTheBottom) {
                    onRecyclerViewPreloadListener!!.onRecyclerViewPreloadMore()
                    if (dy > 0) {
                        isInTheBottom = true
                    }
                } else {
                    // 属于首次进入屏幕未滑动且内容未超过一屏，用于确保分页数设置过小导致内容不足二次上拉加载...
                    if (dy == 0) {
                        isInTheBottom = false
                    }
                }
            }
        }
        onRecyclerViewScrollListener?.onScrolled(dx, dy)
        if (onRecyclerViewScrollStateListener != null) {
            if (abs(dy) < LIMIT) {
                onRecyclerViewScrollStateListener!!.onScrollSlow()
            } else {
                onRecyclerViewScrollStateListener!!.onScrollFast()
            }
        }
    }

    private fun setLayoutManagerPosition(layoutManager: LayoutManager?) {
        if (layoutManager is GridLayoutManager) {
            val gridLayoutManager = layoutManager
            firstVisiblePosition = gridLayoutManager.findFirstVisibleItemPosition()
            lastVisiblePosition = gridLayoutManager.findLastVisibleItemPosition()
        } else if (layoutManager is LinearLayoutManager) {
            val linearLayoutManager = layoutManager
            firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition()
            lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_DRAGGING) {
            setLayoutManagerPosition(layoutManager)
        }
        onRecyclerViewScrollListener?.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            onRecyclerViewScrollStateListener?.onScrollSlow()
        }
    }

    private var onRecyclerViewPreloadListener: OnRecyclerViewPreloadMoreListener? = null
    fun setOnRecyclerViewPreloadListener(listener: OnRecyclerViewPreloadMoreListener?) {
        onRecyclerViewPreloadListener = listener
    }

    private var onRecyclerViewScrollStateListener: OnRecyclerViewScrollStateListener? = null
    fun setOnRecyclerViewScrollStateListener(listener: OnRecyclerViewScrollStateListener?) {
        onRecyclerViewScrollStateListener = listener
    }

    private var onRecyclerViewScrollListener: OnRecyclerViewScrollListener? = null
    fun setOnRecyclerViewScrollListener(listener: OnRecyclerViewScrollListener?) {
        onRecyclerViewScrollListener = listener
    }

    companion object {
        private val TAG = RecyclerPreloadView::class.java.simpleName
        private const val BOTTOM_DEFAULT = 1
        const val BOTTOM_PRELOAD = 2
        private const val LIMIT = 150
    }
}
