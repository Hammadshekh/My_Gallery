package com.example.selector.animations

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

abstract class BaseAnimationAdapter(private val wrappedAdapter: RecyclerView.Adapter<*>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mDuration = 250
    private var mInterpolator: Interpolator = LinearInterpolator()
    private var mLastPosition = -1
    private var isFirstOnly = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return wrappedAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
        wrappedAdapter.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
        wrappedAdapter.unregisterAdapterDataObserver(observer)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        wrappedAdapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        wrappedAdapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        wrappedAdapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        wrappedAdapter.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        wrappedAdapter.onBindViewHolder(holder, position)
        val adapterPosition = holder.adapterPosition
        if (!isFirstOnly || adapterPosition > mLastPosition) {
            for (anim in getAnimators(holder.itemView)) {
                anim.setDuration(mDuration.toLong()).start()
                anim.interpolator = mInterpolator
            }
            mLastPosition = adapterPosition
        } else {
            ViewHelper.clear(holder.itemView)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        wrappedAdapter.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return wrappedAdapter.itemCount
    }

    fun setDuration(duration: Int) {
        mDuration = duration
    }

    fun setInterpolator(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    fun setStartPosition(start: Int) {
        mLastPosition = start
    }

    abstract fun getAnimators(view: View?): Array<Animator>
    fun setFirstOnly(firstOnly: Boolean) {
        isFirstOnly = firstOnly
    }

    override fun getItemViewType(position: Int): Int {
        return wrappedAdapter.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return wrappedAdapter.getItemId(position)
    }
}

