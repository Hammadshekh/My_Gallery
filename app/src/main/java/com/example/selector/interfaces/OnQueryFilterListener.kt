package com.example.selector.interfaces

import com.luck.picture.lib.entity.LocalMedia

interface OnQueryFilterListener {
    /**
     * You need to filter out what doesn't meet the standards
     *
     * @return the boolean result
     */
    fun onFilter(media: LocalMedia): Boolean
}