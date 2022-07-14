package com.example.selector.engine

import androidx.fragment.app.Fragment
import com.luck.picture.lib.entity.LocalMedia
import java.util.ArrayList

interface CropEngine {
    /**
     * Custom crop image engine
     *
     *
     * Users can implement this interface, and then access their own crop framework to plug
     * the crop path into the [LocalMedia] object;
     *
     *
     * 1、If Activity start crop use context;
     * activity.startActivityForResult([Crop.REQUEST_CROP])
     *
     *
     * 2、If Fragment start crop use fragment;
     * fragment.startActivityForResult([Crop.REQUEST_CROP])
     *
     *
     * 3、If you implement your own clipping function, you need to assign the following values in
     * Intent.putExtra [CustomIntentKey]
     *
     *
     *
     * @param fragment          Fragment
     * @param currentLocalMedia current crop data
     * @param dataSource        crop data
     * @param requestCode       Activity result code or fragment result code
     */
    fun onStartCrop(
        fragment: Fragment?,
        currentLocalMedia: LocalMedia?,
        dataSource: ArrayList<LocalMedia>,
        requestCode: Int,
    )
}
