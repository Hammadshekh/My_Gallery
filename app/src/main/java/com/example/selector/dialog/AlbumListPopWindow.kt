package com.example.selector.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.selector.adapter.PictureAlbumAdapter
import com.example.selector.config.PictureConfig
import com.example.selector.decoration.WrapContentLinearLayoutManager
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.interfaces.OnAlbumItemClickListener
import com.example.selector.manager.SelectedManager
import com.example.selector.utils.DensityUtil
import com.example.selector.utils.SdkVersionUtils
import com.luck.picture.lib.entity.LocalMedia

class AlbumListPopWindow(private val mContext: Context?) : PopupWindow() {
    private var windMask: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var isDismiss = false
    private var windowMaxHeight = 0
    private var mAdapter: PictureAlbumAdapter? = null
    private fun initViews() {
        windowMaxHeight = ((mContext?.let { DensityUtil.getScreenHeight(it) }!! * 0.6).toInt())
        mRecyclerView = contentView.findViewById(R.id.folder_list)
        windMask = contentView.findViewById(R.id.rootViewBg)
        mRecyclerView!!.layoutManager = WrapContentLinearLayoutManager(mContext)
        mAdapter = PictureAlbumAdapter()
        mRecyclerView!!.adapter = mAdapter
        windMask!!.setOnClickListener { dismiss() }
        contentView.findViewById<View>(R.id.rootView).setOnClickListener {
            if (SdkVersionUtils.isMinM) {
                dismiss()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun bindAlbumData(list: List<LocalMediaFolder?>) {
        mAdapter!!.bindAlbumData(list)
        mAdapter!!.notifyDataSetChanged()
        val lp = mRecyclerView!!.layoutParams
        lp.height =
            if (list.size > ALBUM_MAX_COUNT) windowMaxHeight else ViewGroup.LayoutParams.WRAP_CONTENT
    }

    val albumList: List<Any>
        get() = mAdapter!!.getAlbumList()

    fun getFolder(position: Int): LocalMediaFolder? {
        return if (mAdapter?.getAlbumList()?.isNotEmpty() == true
            && position < mAdapter!!.getAlbumList().size
        ) mAdapter!!.getAlbumList()[position] else null
    }

    val firstAlbumImageCount: Int?
        get() = if (folderCount > 0) getFolder(0)?.folderTotalNum else 0
    val folderCount: Int
        get() = mAdapter!!.getAlbumList()?.size!!

    /**
     * 专辑列表桥接类
     *
     * @param listener
     */
    fun setOnIBridgeAlbumWidget(listener: OnAlbumItemClickListener?) {
        mAdapter?.setOnIBridgeAlbumWidget(listener)
    }

    override fun showAsDropDown(anchor: View) {
        if (albumList.isEmpty()) {
            return
        }
        if (SdkVersionUtils.isN) {
            val location = IntArray(2)
            anchor.getLocationInWindow(location)
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] + anchor.height)
        } else {
            super.showAsDropDown(anchor)
        }
        isDismiss = false
        if (windowStatusListener != null) {
            windowStatusListener!!.onShowPopupWindow()
        }
        windMask!!.animate().alpha(1f).setDuration(250).setStartDelay(250).start()
        changeSelectedAlbumStyle()
    }

    /**
     *
    set selected state
     */
    private fun changeSelectedAlbumStyle() {
        val folders: List<LocalMediaFolder>? = mAdapter!!.getAlbumList()
        if (folders != null) {
            for (i in folders.indices) {
                val folder: LocalMediaFolder = folders[i]
                folder.isSelectTag = (false)
                mAdapter!!.notifyItemChanged(i)
                for (j in 0 until SelectedManager.selectCount) {
                    val media: LocalMedia = SelectedManager.selectedResult[j]
                    if (TextUtils.equals(folder.folderName, media.parentFolderName)
                        || folder.bucketId.equals(PictureConfig.ALL)
                    ) {
                        folder.isSelectTag=(true)
                        mAdapter!!.notifyItemChanged(i)
                        break
                    }
                }
            }
        }
    }

    override fun dismiss() {
        if (isDismiss) {
            return
        }
        windMask!!.alpha = 0f
        if (windowStatusListener != null) {
            windowStatusListener!!.onDismissPopupWindow()
        }
        isDismiss = true
        windMask!!.post {
            super@AlbumListPopWindow.dismiss()
            isDismiss = false
        }
    }

    /**
     * AlbumListPopWindow 弹出与消失状态监听
     *
     * @param listener
     */
    fun setOnPopupWindowStatusListener(listener: OnPopupWindowStatusListener?) {
        windowStatusListener = listener
    }

    private var windowStatusListener: OnPopupWindowStatusListener? = null

    interface OnPopupWindowStatusListener {
        fun onShowPopupWindow()
        fun onDismissPopupWindow()
    }

    companion object {
        private const val ALBUM_MAX_COUNT = 8
        fun buildPopWindow(context: Context?): AlbumListPopWindow {
            return AlbumListPopWindow(context)
        }
    }

    init {
        contentView = LayoutInflater.from(mContext).inflate(R.layout.ps_window_folder, null)
        width = RelativeLayout.LayoutParams.MATCH_PARENT
        height = RelativeLayout.LayoutParams.WRAP_CONTENT
        animationStyle = R.style.PictureThemeWindowStyle
        isFocusable = true
        isOutsideTouchable = true
        update()
        initViews()
    }
}
