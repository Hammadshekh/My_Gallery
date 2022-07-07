package com.example.selector.interfaces

import com.luck.picture.lib.entity.LocalMedia

interface OnExternalPreviewEventListener {
    /**
     * 删除图片
     *
     * @param position 删除的下标
     */
    fun onPreviewDelete(position: Int)

    /**
     * 长按下载
     *
     * @param media 资源
     * @return false 自己实现下载逻辑；默认true
     */
    fun onLongPressDownload(media: LocalMedia): Boolean
}
