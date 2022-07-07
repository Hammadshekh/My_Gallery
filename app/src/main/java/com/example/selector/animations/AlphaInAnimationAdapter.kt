package com.example.selector.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class AlphaInAnimationAdapter (
    adapter: RecyclerView.Adapter<*>?,
    private val mFrom: Float = DEFAULT_ALPHA_FROM,
) :
    BaseAnimationAdapter(adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    override fun getAnimators(view: View?): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "alpha", mFrom, 1f))
    }

    companion object {
        private const val DEFAULT_ALPHA_FROM = 0f
    }
}
