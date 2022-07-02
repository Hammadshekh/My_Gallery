package com.example.selector.interfaces

import com.example.selector.entity.LocalMediaFolder

interface OnAlbumItemClickListener {
    /**
     * 专辑列表点击事件
     *
     * @param position  下标
     * @param curFolder 当前相册
     */
    fun onItemClick(position: Int, curFolder: LocalMediaFolder?)
}