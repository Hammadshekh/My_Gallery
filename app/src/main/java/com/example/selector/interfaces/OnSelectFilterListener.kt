package com.example.selector.interfaces

import com.luck.picture.lib.entity.LocalMedia

interface OnSelectFilterListener {
    /**
     * You need to filter out the content that does not meet the selection criteria
     *
     * @param media current select [LocalMedia]
     * @return the boolean result
     */
    fun onSelectFilter(media: LocalMedia): Boolean
}
