package com.example.ucrop

import android.graphics.ColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R

class UCropGalleryAdapter(private val list: List<String>?) :
    RecyclerView.Adapter<UCropGalleryAdapter.ViewHolder>() {
    var currentSelectPosition = 0

    @JvmName("setCurrentSelectPosition1")
    fun setCurrentSelectPosition(currentSelectPosition: Int) {
        this.currentSelectPosition = currentSelectPosition
    }

    @JvmName("getCurrentSelectPosition1")
    fun getCurrentSelectPosition(): Int {
        return currentSelectPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.ucrop_gallery_adapter_item,
                parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = list!![position]
        if (UCropDevelopConfig.imageEngine != null) {
            UCropDevelopConfig.imageEngine!!.loadImage(holder.itemView.context, path, holder.mIvPhoto)
        }
        val colorFilter: ColorFilter?
        if (currentSelectPosition == position) {
            holder.mViewCurrentSelect.visibility = View.VISIBLE
            colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.ucrop_color_80),
                    BlendModeCompat.SRC_ATOP)
        } else {
            colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.ucrop_color_20),
                    BlendModeCompat.SRC_ATOP)
            holder.mViewCurrentSelect.visibility = View.GONE
        }
        holder.mIvPhoto.colorFilter = colorFilter
        holder.itemView.setOnClickListener { v ->
            if (listener != null) {
                listener!!.onItemClick(holder.absoluteAdapterPosition, v)
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mIvPhoto: ImageView = view.findViewById(R.id.iv_photo)
        var mViewCurrentSelect: View = view.findViewById(R.id.view_current_select)

    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View?)
    }
}
