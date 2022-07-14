package com.example.selector.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.config.InjectResourceSource
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.interfaces.OnAlbumItemClickListener
import com.example.selector.manager.SelectedManager
import com.example.selector.style.AlbumWindowStyle
import com.example.selector.style.PictureSelectorStyle
import com.example.ucrop.utils.FileUtils.isHasAudio
import java.util.*

class PictureAlbumAdapter : RecyclerView.Adapter<PictureAlbumAdapter.ViewHolder>() {
    private var albumList: List<LocalMediaFolder>? = null
    fun bindAlbumData(albumList: List<LocalMediaFolder>) {
        this.albumList = ArrayList(albumList)
    }

    fun getAlbumList(): List<LocalMediaFolder> {
        return if (albumList != null) albumList!! else ArrayList<LocalMediaFolder>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResourceId: Int = InjectResourceSource.getLayoutResource(parent.context,
            InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE)
        val itemView = LayoutInflater.from(parent.context)
            .inflate(if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_album_folder_item,
                parent,
                false)
        return ViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder: LocalMediaFolder = albumList!![position]
        val name: String = folder.folderName.toString()
        val imageNum: Int = folder.folderTotalNum
        val imagePath: String = folder.firstImagePath.toString()
        holder.tvSelectTag.visibility = if (folder.isSelectTag) View.VISIBLE else View.INVISIBLE
        val currentLocalMediaFolder: LocalMediaFolder? = SelectedManager.currentLocalMediaFolder
        holder.itemView.isSelected = (folder.bucketId == currentLocalMediaFolder?.bucketId)
        val firstMimeType: String = folder.firstMimeType.toString()
        if (isHasAudio(firstMimeType)) {
            holder.ivFirstImage.setImageResource(R.drawable.ps_audio_placeholder)
        } else {
            if (PictureSelectionConfig.imageEngine != null) {
                PictureSelectionConfig.imageEngine!!.loadAlbumCover(holder.itemView.context,
                    imagePath, holder.ivFirstImage)
            }
        }
        val context = holder.itemView.context
        holder.tvFolderName.text = context.getString(R.string.ps_camera_roll_num, name, imageNum)
        holder.itemView.setOnClickListener(View.OnClickListener {
            if (onAlbumItemClickListener == null) {
                return@OnClickListener
            }
            onAlbumItemClickListener!!.onItemClick(position, folder)
        })
    }

    override fun getItemCount(): Int {
        return albumList!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivFirstImage: ImageView = itemView.findViewById(R.id.first_image)
        var tvFolderName: TextView = itemView.findViewById(R.id.tv_folder_name)
        var tvSelectTag: TextView

        init {
            tvSelectTag = itemView.findViewById(R.id.tv_select_tag)
            val selectorStyle: PictureSelectorStyle? = PictureSelectionConfig.selectorStyle
            val albumWindowStyle: AlbumWindowStyle = selectorStyle?.albumWindowStyle!!
            val itemBackground: Int = albumWindowStyle.albumAdapterItemBackground
            if (itemBackground != 0) {
                itemView.setBackgroundResource(itemBackground)
            }
            val itemSelectStyle: Int = albumWindowStyle.albumAdapterItemSelectStyle
            if (itemSelectStyle != 0) {
                tvSelectTag.setBackgroundResource(itemSelectStyle)
            }
            val titleColor: Int = albumWindowStyle.albumAdapterItemTitleColor
            if (titleColor != 0) {
                tvFolderName.setTextColor(titleColor)
            }
            val titleSize: Int = albumWindowStyle.albumAdapterItemTitleSize
            if (titleSize > 0) {
                tvFolderName.textSize = titleSize.toFloat()
            }
        }
    }

    private var onAlbumItemClickListener: OnAlbumItemClickListener? = null

    /**
     *Album list bridge class
     *
     * @param listener
     */
    fun setOnIBridgeAlbumWidget(listener: OnAlbumItemClickListener?) {
        onAlbumItemClickListener = listener
    }
}
