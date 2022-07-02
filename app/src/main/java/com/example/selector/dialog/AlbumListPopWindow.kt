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

class AlbumListPopWindow(private val mContext: Context?) : PopupWindow() {
    private var windMask: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var isDismiss = false
    private var windowMaxHeight = 0
    private var mAdapter: PictureAlbumAdapter? = null
    private fun initViews() {
        windowMaxHeight = (DensityUtil.getScreenHeight(mContext) * 0.6)
        mRecyclerView = contentView.findViewById(R.id.folder_list)
        windMask = contentView.findViewById(R.id.rootViewBg)
        mRecyclerView.setLayoutManager(WrapContentLinearLayoutManager(mContext))
        mAdapter = PictureAlbumAdapter()
        mRecyclerView.setAdapter(mAdapter)
        windMask.setOnClickListener(View.OnClickListener { dismiss() })
        contentView.findViewById<View>(R.id.rootView).setOnClickListener {
            if (SdkVersionUtils.isMinM()) {
                dismiss()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun bindAlbumData(list: List<LocalMediaFolder?>) {
        mAdapter.bindAlbumData(list)
        mAdapter.notifyDataSetChanged()
        val lp = mRecyclerView!!.layoutParams
        lp.height =
            if (list.size > ALBUM_MAX_COUNT) windowMaxHeight else ViewGroup.LayoutParams.WRAP_CONTENT
    }

    val albumList: List<Any>?
        get() = mAdapter.getAlbumList()

    fun getFolder(position: Int): LocalMediaFolder? {
        return if (mAdapter.getAlbumList().size() > 0
            && position < mAdapter.getAlbumList().size()
        ) mAdapter.getAlbumList().get(position) else null
    }

    val firstAlbumImageCount: Int
        get() = if (folderCount > 0) getFolder(0).getFolderTotalNum() else 0
    val folderCount: Int
        get() = mAdapter.getAlbumList().size()

    /**
     * 专辑列表桥接类
     *
     * @param listener
     */
    fun setOnIBridgeAlbumWidget(listener: OnAlbumItemClickListener?) {
        mAdapter.setOnIBridgeAlbumWidget(listener)
    }

    override fun showAsDropDown(anchor: View) {
        if (albumList == null || albumList!!.size == 0) {
            return
        }
        if (SdkVersionUtils.isN()) {
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
     * 设置选中状态
     */
    fun changeSelectedAlbumStyle() {
        val folders: List<LocalMediaFolder> = mAdapter.getAlbumList()
        for (i in folders.indices) {
            val folder: LocalMediaFolder = folders[i]
            folder.setSelectTag(false)
            mAdapter.notifyItemChanged(i)
            for (j in 0 until SelectedManager.getSelectCount()) {
                val media: LocalMedia = SelectedManager.getSelectedResult().get(j)
                if (TextUtils.equals(folder.getFolderName(), media.getParentFolderName())
                    || folder.getBucketId() === PictureConfig.ALL
                ) {
                    folder.setSelectTag(true)
                    mAdapter.notifyItemChanged(i)
                    break
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
