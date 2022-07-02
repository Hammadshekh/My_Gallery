package com.example.selector.interfaces

import com.example.selector.entity.LocalMedia

interface OnQueryFilterListener {
    /**
     * You need to filter out what doesn't meet the standards
     *
     * @return the boolean result
     */
    fun onFilter(media: LocalMedia?): Boolean
}