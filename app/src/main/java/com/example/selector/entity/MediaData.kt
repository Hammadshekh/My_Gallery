package com.example.selector.entity

import com.luck.picture.lib.entity.LocalMedia
import java.util.*

class MediaData {
    /**
     * Is there more
     */
    var isHasNextMore = false

    /**
     * data
     */
    var data: ArrayList<LocalMedia>? = null

    constructor() : super() {}
    constructor(isHasNextMore: Boolean, data: ArrayList<LocalMedia>) : super() {
        this.isHasNextMore = isHasNextMore
        this.data = data
    }
}