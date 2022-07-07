import android.annotation.SuppressLint
import android.app.Service
import android.os.*
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.mygallery.R
import com.example.selector.PictureSelectorPreviewFragment
import com.example.selector.adapter.PictureImageGridAdapter
import com.example.selector.animations.AlphaInAnimationAdapter
import com.example.selector.animations.AnimationType
import com.example.selector.animations.SlideInBottomAnimationAdapter
import com.example.selector.basic.FragmentInjectManager
import com.example.selector.basic.IPictureSelectorEvent
import com.example.selector.basic.PictureCommonFragment
import com.example.selector.config.*
import com.example.selector.config.SelectMimeType.ofAudio
import com.example.selector.decoration.GridSpacingItemDecoration
import com.example.selector.dialog.AlbumListPopWindow
import com.example.selector.entity.LocalMediaFolder
import com.example.selector.interfaces.*
import com.example.selector.loader.IBridgeMediaLoader
import com.example.selector.loader.LocalMediaLoader
import com.example.selector.loader.LocalMediaPageLoader
import com.example.selector.magical.BuildRecycleItemViewParams
import com.example.selector.manager.SelectedManager
import com.example.selector.permissions.PermissionChecker
import com.example.selector.permissions.PermissionChecker.Companion.isCheckSelfPermission
import com.example.selector.permissions.PermissionConfig
import com.example.selector.permissions.PermissionConfig.READ_WRITE_EXTERNAL_STORAGE
import com.example.selector.permissions.PermissionResultCallback
import com.example.selector.style.PictureSelectorStyle
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.*
import com.example.selector.widget.*
import com.luck.picture.lib.entity.LocalMedia
import java.io.File
import java.lang.Exception
import java.lang.NullPointerException
import java.util.HashSet
import kotlin.collections.ArrayList

class PictureSelectorFragment : PictureCommonFragment(),
    OnRecyclerViewPreloadMoreListener, IPictureSelectorEvent {
    private lateinit var mRecycler: RecyclerPreloadView
    private lateinit var tvDataEmpty: TextView
    private lateinit var titleBar: TitleBar
    private lateinit var bottomNarBar: BottomNavBar
    private lateinit var completeSelectView: CompleteSelectView
    private lateinit var tvCurrentDataTime: TextView
    private var intervalClickTime: Long = 0
    private var allFolderSize = 0
    private val LOCK = Any()
    private var SELECT_ANIM_DURATION = 135
    private var currentPosition = -1

    /**
     * Use camera to callback
     */
    private var isCameraCallback = false

    /**
     * memory recycling
     */
    private var isMemoryRecycling = false
    private var isDisplayCamera = false
    private lateinit var mAdapter: PictureImageGridAdapter
    private lateinit var albumListPopWindow: AlbumListPopWindow
    private lateinit var mDragSelectTouchListener: SlideSelectTouchListener
    override val resourceId: Int
        get() {
            val layoutResourceId: Int = InjectResourceSource.getLayoutResource(context,
                InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE)
            return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
                layoutResourceId
            } else R.layout.ps_fragment_selector
        }

    override fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        bottomNarBar?.setSelectedChange()
        completeSelectView?.setSelectedChange(false)
        // 刷新列表数据
        if (checkNotifyStrategy(isAddRemove)) {
            currentMedia?.position?.let { mAdapter?.notifyItemPositionChanged(it) }
            mRecycler?.postDelayed({ mAdapter?.notifyDataSetChanged() },
                SELECT_ANIM_DURATION.toLong())
        } else {
            currentMedia?.position?.let { mAdapter?.notifyItemPositionChanged(it) }
        }
        if (!isAddRemove) {
            sendChangeSubSelectPositionEvent(true)
        }
    }

    override fun onFixedSelectedChange(oldLocalMedia: LocalMedia?) {
        if (oldLocalMedia != null) {
            mAdapter?.notifyItemPositionChanged(oldLocalMedia.position)
        }
    }

    override fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {
        if (PictureSelectionConfig.selectorStyle!!.selectMainStyle!!.isSelectNumberStyle) {
            for (index in 0 until SelectedManager.selectCount) {
                val media: LocalMedia = SelectedManager.getSelectedResult()[index]
                media.num = index + 1
                if (adapterChange) {
                    mAdapter.notifyItemPositionChanged(media.position)
                }
            }
        }
    }

    override fun onCheckOriginalChange() {
        bottomNarBar?.setOriginalCheck()
    }

    /**
     * 刷新列表策略
     *
     * @param isAddRemove
     * @return
     */
    private fun checkNotifyStrategy(isAddRemove: Boolean): Boolean {
        var isNotifyAll = false
        if (config!!.isMaxSelectEnabledMask) {
            if (config!!.isWithVideoImage) {
                if (config!!.selectionMode == SelectModeConfig.SINGLE) {
                    // ignore
                } else {
                    isNotifyAll = (SelectedManager.selectCount == config!!.maxSelectNum
                            || !isAddRemove && SelectedManager.selectCount == config!!.maxSelectNum - 1)
                }
            } else {
                isNotifyAll =
                    if (SelectedManager.selectCount == 0 || isAddRemove && SelectedManager.selectCount == 1) {
                        // 首次添加或单选，选择数量变为0了，都notifyDataSetChanged
                        true
                    } else {
                        if (PictureMimeType.isHasVideo(SelectedManager.topResultMimeType)) {
                            val maxSelectNum: Int =
                                if (config!!.maxVideoSelectNum > 0) config!!.maxVideoSelectNum else config!!.maxSelectNum
                            (SelectedManager.selectCount == maxSelectNum
                                    || !isAddRemove && SelectedManager.selectCount == maxSelectNum - 1)
                        } else {
                            (SelectedManager.selectCount == config?.maxSelectNum
                                    || !isAddRemove && SelectedManager.selectCount == config?.maxSelectNum!! - 1)
                        }
                    }
            }
        }
        return isNotifyAll
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PictureConfig.EXTRA_ALL_FOLDER_SIZE, allFolderSize)
        outState.putInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
        mRecycler.let {
            outState.putInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION,
                it.lastVisiblePosition)
        }
        mAdapter.isDisplayCamera.let {
            outState.putBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA,
                it)
        }
        SelectedManager.currentLocalMediaFolder = (SelectedManager.currentLocalMediaFolder)
        SelectedManager.addAlbumDataSource(albumListPopWindow.albumList as List<LocalMediaFolder>?)
        SelectedManager.addDataSource(mAdapter.data)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reStartSavedInstance(savedInstanceState)
        isMemoryRecycling = savedInstanceState != null
        tvDataEmpty = view.findViewById(R.id.tv_data_empty)
        completeSelectView = view.findViewById(R.id.ps_complete_select)
        titleBar = view.findViewById(R.id.title_bar)
        bottomNarBar = view.findViewById(R.id.bottom_nar_bar)
        tvCurrentDataTime = view.findViewById(R.id.tv_current_data_time)
        onCreateLoader()
        initAlbumListPopWindow()
        initTitleBar()
        initComplete()
        initRecycler(view)
        initBottomNavBar()
        if (isMemoryRecycling) {
            recoverSaveInstanceData()
        } else {
            requestLoadData()
        }
    }

    override fun onFragmentResume() {
        setRootViewKeyListener(requireView())
    }

    override fun reStartSavedInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            allFolderSize = savedInstanceState.getInt(PictureConfig.EXTRA_ALL_FOLDER_SIZE)
            mPage = savedInstanceState.getInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
            currentPosition =
                savedInstanceState.getInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION,
                    currentPosition)
            isDisplayCamera = config?.isDisplayCamera?.let {
                savedInstanceState.getBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA,
                    it)
            } == true
        } else {
            isDisplayCamera = config?.isDisplayCamera == true == true
        }
    }

    /**
     * 完成按钮
     */
    private fun initComplete() =
        if (config?.selectionMode == SelectModeConfig.SINGLE && config?.isDirectReturnSingle == true) {
            PictureSelectionConfig.selectorStyle?.titleBarStyle?.isHideCancelButton = false
            titleBar?.titleCancelView?.visibility = View.VISIBLE
            completeSelectView?.visibility = View.GONE
        } else {
            completeSelectView?.setCompleteSelectViewStyle()
            completeSelectView?.setSelectedChange(false)
            val selectMainStyle: SelectMainStyle =
                PictureSelectionConfig.selectorStyle?.selectMainStyle!!
            if (selectMainStyle.isCompleteSelectRelativeTop) {
                if (completeSelectView?.layoutParams is ConstraintLayout.LayoutParams) {
                    (completeSelectView?.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                        R.id.title_bar
                    (completeSelectView?.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                        R.id.title_bar
                    if (config?.isPreviewFullScreenMode == true) {
                        (completeSelectView!!
                            .getLayoutParams() as ConstraintLayout.LayoutParams).topMargin =
                            context?.let { DensityUtil.getStatusBarHeight(it) }!!
                    }
                } else if (completeSelectView?.layoutParams is RelativeLayout.LayoutParams) {
                    if (config.isPreviewFullScreenMode) {
                        (completeSelectView
                            .layoutParams as RelativeLayout.LayoutParams).topMargin =
                            context?.let { DensityUtil.getStatusBarHeight(it) }!!
                    }
                }
            }
            completeSelectView.setOnClickListener {
                if (config?.isEmptyResultReturn && SelectedManager.selectCount == 0) {
                    onExitPictureSelector()
                } else {
                    dispatchTransformResult()
                }
            }
        }

    override fun onCreateLoader() {
        if (PictureSelectionConfig.loaderFactory != null) {
            mLoader = PictureSelectionConfig.loaderFactory!!.onCreateLoader()
            if (mLoader == null) {
                throw NullPointerException("No available " + IBridgeMediaLoader::class.java + " loader found")
            }
        } else {
            mLoader =
                if (config.isPageStrategy) LocalMediaPageLoader() else LocalMediaLoader()
        }
        mLoader?.initConfig(context, config)
    }

    private fun initTitleBar() {
        if (PictureSelectionConfig.selectorStyle?.titleBarStyle?.isHideTitleBar == true) {
            titleBar.visibility = View.GONE
        }
        titleBar.setTitleBarStyle()
        titleBar.setOnTitleBarListener(object : TitleBar.OnTitleBarListener() {
            override fun onTitleDoubleClick() {
                if (config.isAutomaticTitleRecyclerTop) {
                    val intervalTime = 500
                    if (SystemClock.uptimeMillis() - intervalClickTime < intervalTime && mAdapter.itemCount > 0) {
                        mRecycler.scrollToPosition(0)
                    } else {
                        intervalClickTime = SystemClock.uptimeMillis()
                    }
                }
            }

            override fun onBackPressed() {
                if (albumListPopWindow.isShowing) {
                    albumListPopWindow.dismiss()
                } else {
                    onKeyBackFragmentFinish()
                }
            }

            override fun onShowAlbumPopWindow(anchor: View?) {
                anchor?.let { albumListPopWindow.showAsDropDown(it) }
            }
        })
    }

    /**
     * initAlbumListPopWindow
     */
    private fun initAlbumListPopWindow() {
        albumListPopWindow = AlbumListPopWindow.buildPopWindow(context)
        albumListPopWindow.setOnPopupWindowStatusListener(object :
            AlbumListPopWindow.OnPopupWindowStatusListener {
            override fun onShowPopupWindow() {
                if (!config.isOnlySandboxDir) {
                    AnimUtils.rotateArrow(titleBar.imageArrow, true)
                }
            }

            override fun onDismissPopupWindow() {
                if (!config.isOnlySandboxDir) {
                    AnimUtils.rotateArrow(titleBar.imageArrow, false)
                }
            }
        })
        addAlbumPopWindowAction()
    }

    private fun recoverSaveInstanceData() {
        mAdapter?.isDisplayCamera
        enterAnimationDuration = 0
        if (this.config.isOnlySandboxDir) {
            handleInAppDirAllMedia(SelectedManager.currentLocalMediaFolder)
        } else {
            handleRecoverAlbumData(ArrayList(SelectedManager.albumDataSource))
        }
    }

    private fun handleRecoverAlbumData(albumData: ArrayList<LocalMediaFolder>) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        if (albumData.isNotEmpty()) {
            val firstFolder: LocalMediaFolder
            if (SelectedManager.currentLocalMediaFolder != null) {
                firstFolder = SelectedManager.currentLocalMediaFolder!!
            } else {
                firstFolder = albumData[0]
                SelectedManager.currentLocalMediaFolder = (firstFolder)
            }
            titleBar.setTitle(firstFolder.getFolderName())
            albumListPopWindow.bindAlbumData(albumData)
            if (config.isPageStrategy) {
                handleFirstPageMedia(ArrayList(SelectedManager.dataSource), true)
            } else {
                setAdapterData(firstFolder.getData())
            }
        } else {
            showDataNull()
        }
    }

    private fun requestLoadData() {
        mAdapter?.isDisplayCamera
        val isCheckReadStorage =
            if (SdkVersionUtils.isR() && config!!.isAllFilesAccess) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                TODO("VERSION.SDK_INT < R")
            } else context?.let {
                PermissionChecker.isCheckReadStorage(
                    it)
            }
        if (isCheckReadStorage == true) {
            beginLoadData()
        } else {
            onPermissionExplainEvent(true, READ_WRITE_EXTERNAL_STORAGE)
            if (PictureSelectionConfig.onPermissionsEventListener != null) {
                onApplyPermissionsEvent(PermissionEvent.EVENT_SOURCE_DATA,
                    READ_WRITE_EXTERNAL_STORAGE)
            } else {
                PermissionChecker.instance?.requestPermissions(this,
                    READ_WRITE_EXTERNAL_STORAGE,
                    object : PermissionResultCallback {
                        override fun onGranted() {
                            beginLoadData()
                        }

                        override fun onDenied() {
                            handlePermissionDenied(READ_WRITE_EXTERNAL_STORAGE)
                        }
                    })
            }
        }
    }

    override fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String>) {
        if (event != PermissionEvent.EVENT_SOURCE_DATA) {
            super.onApplyPermissionsEvent(event, permissionArray)
        } else {
            PictureSelectionConfig.onPermissionsEventListener?.requestPermission(this,
                permissionArray,
                object : OnRequestPermissionListener {
                    override fun onCall(permissionArray: Array<String>, isResult: Boolean) {
                        if (isResult) {
                            beginLoadData()
                        } else {
                            permissionArray.let { handlePermissionDenied(it) }
                        }
                    }
                })
        }
    }

    /**
     * 开始获取数据
     */
    private fun beginLoadData() {
        onPermissionExplainEvent(false, null)
        if (config.isOnlySandboxDir) {
            loadOnlyInAppDirectoryAllMediaData()
        } else {
            loadAllAlbumData()
        }
    }

    override fun handlePermissionSettingResult(permissions: Array<String>) {
        onPermissionExplainEvent(false, null)
        val isHasCamera = permissions.isNotEmpty() && TextUtils.equals(permissions[0],
            PermissionConfig.CAMERA[0])
        val isHasPermissions: Boolean = if (PictureSelectionConfig.onPermissionsEventListener != null) {
            PictureSelectionConfig.onPermissionsEventListener!!.hasPermissions(this, permissions)
        } else {
            if (isHasCamera) {
                isCheckSelfPermission(requireContext(), permissions)
            } else {
                if (SdkVersionUtils.isR() && config.isAllFilesAccess) {
                    Environment.isExternalStorageManager()
                } else {
                    isCheckSelfPermission(requireContext(), permissions)
                }
            }
        }
        if (isHasPermissions) {
            if (isHasCamera) {
                openSelectedCamera()
            } else {
                beginLoadData()
            }
        } else {
            if (isHasCamera) {
                ToastUtils.showToast(requireContext(), getString(R.string.ps_camera))
            } else {
                ToastUtils.showToast(requireContext(), getString(R.string.ps_jurisdiction))
                onKeyBackFragmentFinish()
            }
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = arrayOf()
    }

    /**
     * 给AlbumListPopWindow添加事件
     */
    private fun addAlbumPopWindowAction() {
        albumListPopWindow.setOnIBridgeAlbumWidget(object : OnAlbumItemClickListener {
            override fun onItemClick(position: Int, curFolder: LocalMediaFolder?) {
                if (curFolder != null) {
                    isDisplayCamera =
                        config.isDisplayCamera == true && curFolder.bucketId.equals(PictureConfig.ALL)
                }
                mAdapter.isDisplayCamera
                titleBar?.setTitle(curFolder?.getFolderName())
                val lastFolder: LocalMediaFolder = SelectedManager.currentLocalMediaFolder!!
                val lastBucketId: Long = lastFolder.bucketId
                if (config?.isPageStrategy) {
                    if (curFolder?.bucketId != lastBucketId) {
                        lastFolder.setData(mAdapter.data)
                        lastFolder.currentDataPage = (mPage)
                        lastFolder.isHasMore = (mRecycler.isEnabledLoadMore)

                        if (curFolder != null) {
                            if (curFolder.data?.size!! > 0 && !curFolder.isHasMore) {
                                curFolder.data.let {
                                    if (it != null) {
                                        setAdapterData(it)
                                    }
                                }
                                mPage = curFolder.currentDataPage
                                mRecycler.isEnabledLoadMore = curFolder.isHasMore
                                mRecycler.smoothScrollToPosition(0)
                            } else {
                                // 3、从MediaStore拉取数据
                                mPage = 1
                                if (PictureSelectionConfig.loaderDataEngine != null) {
                                    config.pageSize.let {
                                        PictureSelectionConfig.loaderDataEngine!!.loadFirstPageMediaData(
                                            requireContext(),
                                            curFolder.bucketId,
                                            mPage,
                                            it,
                                            object : OnQueryDataResultListener<LocalMedia>() {
                                                override fun onComplete(
                                                    result: ArrayList<LocalMedia>,
                                                    isHasMore: Boolean,
                                                ) {
                                                    handleSwitchAlbum(result, isHasMore)
                                                }
                                            })
                                    }
                                } else {
                                    config.pageSize.let {
                                        mLoader?.loadPageMediaData(curFolder.bucketId,
                                            mPage,
                                            it,
                                            object : OnQueryDataResultListener<LocalMedia>() {
                                                override fun onComplete(
                                                    result: ArrayList<LocalMedia>,
                                                    isHasMore: Boolean,
                                                ) {
                                                    handleSwitchAlbum(result, isHasMore)
                                                }
                                            })
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (curFolder != null) {
                        if (curFolder.bucketId != lastBucketId) {
                            curFolder?.data?.let { setAdapterData(it) }
                            mRecycler.smoothScrollToPosition(0)
                        }
                    }
                }
                SelectedManager.currentLocalMediaFolder = (curFolder)
                albumListPopWindow.dismiss()
                if (config.isFastSlidingSelect) {
                    mDragSelectTouchListener.setRecyclerViewHeaderCount(if (mAdapter.isDisplayCamera) 1 else 0)
                }
            }
        })
    }

    private fun handleSwitchAlbum(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(getActivity())) {
            return
        }
        mRecycler.isEnabledLoadMore = isHasMore
        if (result.size == 0) {
            // 如果从MediaStore拉取都没有数据了，adapter里的可能是缓存所以也清除
            mAdapter.data.clear()
        }
        setAdapterData(result)
        mRecycler.onScrolled(0, 0)
        mRecycler.smoothScrollToPosition(0)
    }

    private fun initBottomNavBar() {
        bottomNarBar.setBottomNavBarStyle()
        bottomNarBar.setOnBottomNavBarListener(object : BottomNavBar.OnBottomNavBarListener() {
            override fun onPreview() {
                onStartPreview(0, true)
            }

            override fun onCheckOriginalChange() {
                sendSelectedOriginalChangeEvent()
            }
        })
        bottomNarBar?.setSelectedChange()
    }

    override fun loadAllAlbumData() {
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine!!.loadAllAlbumData(context,
                object : OnQueryAllAlbumListener<LocalMediaFolder> {
                    override fun onComplete(result: List<LocalMediaFolder>?) {
                        handleAllAlbumData(result!!)
                    }
                })
        } else {
            mLoader?.loadAllAlbum(object : OnQueryAllAlbumListener<LocalMediaFolder> {
                override fun onComplete(result: List<LocalMediaFolder>?) {
                    handleAllAlbumData(result!!)
                }
            })
        }
    }

    private fun handleAllAlbumData(result: List<LocalMediaFolder>) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        if (result.isNotEmpty()) {
            val firstFolder: LocalMediaFolder
            if (SelectedManager.currentLocalMediaFolder != null) {
                firstFolder = SelectedManager.currentLocalMediaFolder!!
            } else {
                firstFolder = result[0]
                SelectedManager.currentLocalMediaFolder = (firstFolder)
            }
            titleBar.setTitle(firstFolder.getFolderName())
            albumListPopWindow.bindAlbumData(result)
            if (config.isPageStrategy) {
                loadFirstPageMediaData(firstFolder.bucketId)
            } else {
                setAdapterData(firstFolder.getData())
            }
        } else {
            showDataNull()
        }
    }

    override fun loadFirstPageMediaData(firstBucketId: Long) {
        mRecycler?.isEnabledLoadMore = true
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine!!.loadFirstPageMediaData(context,
                firstBucketId,
                mPage,
                mPage * (config.pageSize),
                object : OnQueryDataResultListener<LocalMedia>() {
                    override fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                        handleSwitchAlbum(result, isHasMore)
                    }
                })
        } else {
            mLoader?.loadPageMediaData(firstBucketId, 1, mPage * config.pageSize,
                object : OnQueryDataResultListener<LocalMedia>() {
                    override fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                        handleFirstPageMedia(result, isHasMore)
                    }
                })
        }
    }

    private fun handleFirstPageMedia(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        mRecycler!!.isEnabledLoadMore = isHasMore
        if (mRecycler!!.isEnabledLoadMore && result.size == 0) {
            // 如果isHasMore为true但result.size() = 0;
            // 那么有可能是开启了某些条件过滤，实际上是还有更多资源的再强制请求
            onRecyclerViewPreloadMore()
        } else {
            setAdapterData(result)
        }
    }

    override fun loadOnlyInAppDirectoryAllMediaData() {
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine!!.loadOnlyInAppDirAllMediaData(context,
                object : OnQueryAlbumListener<LocalMediaFolder> {
                    override fun onComplete(result: LocalMediaFolder) {
                        handleInAppDirAllMedia(result)
                    }
                })
        } else {
            mLoader?.loadOnlyInAppDirAllMedia(object : OnQueryAlbumListener<LocalMediaFolder> {


                override fun onComplete(result: LocalMediaFolder) {
                    handleInAppDirAllMedia(result)
                }
            })
        }
    }

    private fun handleInAppDirAllMedia(folder: LocalMediaFolder?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val sandboxDir: String = config?.sandboxDir.toString()
            val isNonNull = folder != null
            val folderName = if (isNonNull) folder?.getFolderName() else File(sandboxDir).name
            titleBar.setTitle(folderName)
            if (isNonNull) {
                SelectedManager.currentLocalMediaFolder = (folder)
                folder?.data?.let { setAdapterData(it) }
            } else {
                showDataNull()
            }
        }
    }

    /**
     * 内存不足时，恢复RecyclerView定位位置
     */
    private fun recoveryRecyclerPosition() {
        if (currentPosition > 0) {
            mRecycler.post(Runnable {
                mRecycler.scrollToPosition(currentPosition)
                mRecycler.lastVisiblePosition = currentPosition
            })
        }
    }

    private fun initRecycler(view: View) {
        mRecycler = view.findViewById(R.id.recycler)
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle!!
        val selectMainStyle: SelectMainStyle = selectorStyle.selectMainStyle!!
        val listBackgroundColor: Int = selectMainStyle.mainListBackgroundColor
        if (StyleUtils.checkStyleValidity(listBackgroundColor)) {
            mRecycler.setBackgroundColor(listBackgroundColor)
        } else {
            context?.let {
                ContextCompat.getColor(it,
                    R.color.ps_color_black)
            }?.let { mRecycler.setBackgroundColor(it) }
        }
        val imageSpanCount: Int =
            if (config.imageSpanCount <= 0) PictureConfig.DEFAULT_SPAN_COUNT else config.imageSpanCount
        if (mRecycler.itemDecorationCount == 0) {
            if (StyleUtils.checkSizeValidity(selectMainStyle.adapterItemSpacingSize)) {
                mRecycler.addItemDecoration(GridSpacingItemDecoration(imageSpanCount,
                    selectMainStyle.adapterItemSpacingSize,
                    selectMainStyle.isAdapterItemIncludeEdge))
            } else {
                mRecycler.addItemDecoration(GridSpacingItemDecoration(imageSpanCount,
                    DensityUtil.dip2px(view.context, 1f),
                    selectMainStyle.isAdapterItemIncludeEdge))
            }
        }
        mRecycler.layoutManager = GridLayoutManager(context, imageSpanCount)
        val itemAnimator: ItemAnimator = mRecycler.itemAnimator!!
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mRecycler.itemAnimator = null
        if (config.isPageStrategy) {
            mRecycler.setReachBottomRow(RecyclerPreloadView.BOTTOM_PRELOAD)
            mRecycler.setOnRecyclerViewPreloadListener(this)
        } else {
            mRecycler.setHasFixedSize(true)
        }
        mAdapter = PictureImageGridAdapter(requireContext(), config)
        mAdapter.isDisplayCamera
        when (config.animationMode) {
            AnimationType.ALPHA_IN_ANIMATION -> mRecycler.adapter = AlphaInAnimationAdapter(
                mAdapter)
            AnimationType.SLIDE_IN_BOTTOM_ANIMATION -> mRecycler.adapter =
                SlideInBottomAnimationAdapter(mAdapter)
            else -> mRecycler.adapter = mAdapter
        }
        addRecyclerAction()
    }

    private fun addRecyclerAction() {
        mAdapter.setOnItemClickListener(object : PictureImageGridAdapter.OnItemClickListener {
            override fun openCameraClick() {
                if (DoubleUtils.isFastDoubleClick) {
                    return
                }
                openSelectedCamera()
            }

            override fun onSelected(selectedView: View?, position: Int, media: LocalMedia?): Int {
                val selectResultCode = selectedView?.let { confirmSelect(media!!, it.isSelected) }
                if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                    if (PictureSelectionConfig.onSelectAnimListener != null) {
                        val duration =
                            PictureSelectionConfig.onSelectAnimListener!!.onSelectAnim(selectedView)
                        if (duration > 0) {
                           SELECT_ANIM_DURATION = duration.toInt()
                        }
                    } else {
                        val animation = AnimationUtils.loadAnimation(
                            context, R.anim.ps_anim_modal_in)
                        SELECT_ANIM_DURATION = animation.duration.toInt()
                        selectedView?.startAnimation(animation)
                    }
                }
                return selectResultCode!!
            }

            override fun onItemClick(selectedView: View?, position: Int, media: LocalMedia?) {
                if (config.selectionMode == SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
                    SelectedManager.clearSelectResult()
                    val selectResultCode = confirmSelect(media!!, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        dispatchTransformResult()
                    }
                } else {
                    if (DoubleUtils.isFastDoubleClick) {
                        return
                    }
                    onStartPreview(position, false)
                }
            }

            override fun onItemLongClick(itemView: View?, position: Int) {
                if (config.isFastSlidingSelect) {
                    val vibrator = activity!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(50)
                    mDragSelectTouchListener.startSlideSelection(position)
                }
            }

          /*  fun onItemClick(v: View?, position: Int) {
                TODO("Not yet implemented")
            }*/
        })
        mRecycler.setOnRecyclerViewScrollStateListener(object :
            OnRecyclerViewScrollStateListener {
            override fun onScrollFast() {
                if (PictureSelectionConfig.imageEngine != null) {
                    PictureSelectionConfig.imageEngine!!.pauseRequests(context)
                }
            }

            override fun onScrollSlow() {
                if (PictureSelectionConfig.imageEngine != null) {
                    PictureSelectionConfig.imageEngine!!.resumeRequests(context)
                }
            }
        })
        mRecycler.setOnRecyclerViewScrollListener(object : OnRecyclerViewScrollListener {
            override fun onScrolled(dx: Int, dy: Int) {
                setCurrentMediaCreateTimeText()
            }

            override fun onScrollStateChanged(state: Int) {
                if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showCurrentMediaCreateTimeUI()
                } else if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    hideCurrentMediaCreateTimeUI()
                }
            }
        })
        if (config.isFastSlidingSelect) {
            val selectedPosition = HashSet<Int>()
            val slideSelectionHandler = SlideSelectionHandler(object : SlideSelectionHandler.ISelectionHandler {
                override val selection: HashSet<Int>
                    get() {
                        for (i in 0 until SelectedManager.selectCount) {
                            val media = SelectedManager.getSelectedResult()[i]
                            selectedPosition.add(media.position)
                        }
                        return selectedPosition
                    }

                override fun changeSelection(
                    start: Int,
                    end: Int,
                    isSelected: Boolean,
                    calledFromOnStart: Boolean,
                ) {
                    val adapterData: java.util.ArrayList<LocalMedia> = mAdapter.data
                    if (adapterData.size == 0 || start > adapterData.size) {
                        return
                    }
                    val media = adapterData[start]
                    val selectResultCode =
                        confirmSelect(media, SelectedManager.getSelectedResult().contains(media))
                    mDragSelectTouchListener.setActive(selectResultCode != SelectedManager.INVALID)
                }
            })
            mDragSelectTouchListener = SlideSelectTouchListener()
                .setRecyclerViewHeaderCount(if (mAdapter.isDisplayCamera) 1 else 0)
                .withSelectListener(slideSelectionHandler)
            mRecycler.addOnItemTouchListener(mDragSelectTouchListener)
        }
    }

    /**
     * 显示当前资源时间轴
     */
    private fun setCurrentMediaCreateTimeText() {
        if (config.isDisplayTimeAxis) {
            val position: Int = mRecycler.firstVisiblePosition
            if (position != RecyclerView.NO_POSITION) {
                val data: ArrayList<LocalMedia> = mAdapter.data
                if (data.size > position && data[position].dateAddedTime > 0) {
                    tvCurrentDataTime.text = DateUtils.getDataFormat(requireContext(),
                        data[position].dateAddedTime)
                }
            }
        }
    }

    /**
     * 显示当前资源时间轴
     */
    private fun showCurrentMediaCreateTimeUI() {
        if (config.isDisplayTimeAxis && mAdapter.data.size > 0) {
            if (tvCurrentDataTime.alpha == 0f) {
                tvCurrentDataTime.animate().setDuration(150).alphaBy(1.0f).start()
            }
        }
    }

    /**
     * 隐藏当前资源时间轴
     */
    private fun hideCurrentMediaCreateTimeUI() {
        if (config.isDisplayTimeAxis && mAdapter.data.size > 0) {
            tvCurrentDataTime.animate().setDuration(250).alpha(0.0f).start()
        }
    }

    /**
     * 预览图片
     *
     * @param position        预览图片下标
     * @param isBottomPreview true 底部预览模式 false列表预览模式
     */
    private fun onStartPreview(position: Int, isBottomPreview: Boolean) {
        if (activity?.let {
                ActivityCompatHelper.checkFragmentNonExits(it,
                    PictureSelectorPreviewFragment.fragmentTag)
            } == true
        ) {
            val data: ArrayList<LocalMedia?>?
            val totalNum: Int
            var currentBucketId: Long = 0
            if (isBottomPreview) {
                data = ArrayList(SelectedManager.selectedResult)
                totalNum = data.size
            } else {
                data = ArrayList(mAdapter.data)
                totalNum = SelectedManager.currentLocalMediaFolder?.folderTotalNum!!
                currentBucketId = SelectedManager.currentLocalMediaFolder?.bucketId!!
            }
            if (!isBottomPreview && config.isPreviewZoomEffect) {
                BuildRecycleItemViewParams.generateViewParams(mRecycler,
                    if (config.isPreviewFullScreenMode) 0 else DensityUtil.getStatusBarHeight(
                        requireContext()))
            }
            if (PictureSelectionConfig.onPreviewInterceptListener != null) {
                PictureSelectionConfig.onPreviewInterceptListener!!
                    .onPreview(requireContext(),
                        position,
                        totalNum,
                        mPage,
                        currentBucketId,
                        titleBar.titleText,
                        mAdapter.isDisplayCamera,
                        data,
                        isBottomPreview)
            } else {
                if (ActivityCompatHelper.checkFragmentNonExits(requireActivity(),
                        PictureSelectorPreviewFragment.fragmentTag)
                ) {
                    val previewFragment: PictureSelectorPreviewFragment =
                        PictureSelectorPreviewFragment.newInstance()
                    previewFragment.setInternalPreviewData(isBottomPreview,
                        titleBar.titleText,
                        mAdapter.isDisplayCamera,
                        position,
                        totalNum,
                        mPage,
                        currentBucketId,
                        data)
                    FragmentInjectManager.injectFragment(requireActivity(),
                        PictureSelectorPreviewFragment.fragmentTag,
                        previewFragment)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapterData(result: ArrayList<LocalMedia>) {
        val enterAnimationDuration: Long = enterAnimationDuration
        if (enterAnimationDuration > 0) {
            requireView().postDelayed( { setAdapterDataComplete(result) },
                enterAnimationDuration)
        } else {
            setAdapterDataComplete(result)
        }
    }

    private fun setAdapterDataComplete(result: ArrayList<LocalMedia>) {
        enterAnimationDuration = 0
        sendChangeSubSelectPositionEvent(false)
        mAdapter.setDataAndDataSetChanged(result)
        SelectedManager.clearAlbumDataSource()
        SelectedManager.clearDataSource()
        recoveryRecyclerPosition()
        if (mAdapter.isDataEmpty) {
            showDataNull()
        } else {
            hideDataNull()
        }
    }

    override fun onRecyclerViewPreloadMore() {
        if (isMemoryRecycling) {
            requireView().postDelayed( { loadMoreMediaData() }, 350)
        } else {
            loadMoreMediaData()
        }
    }

    override fun loadMoreMediaData() {
        if (mRecycler.isEnabledLoadMore) {
            mPage++
            val localMediaFolder: LocalMediaFolder = SelectedManager.currentLocalMediaFolder!!
            val bucketId: Long = localMediaFolder.bucketId
            if (PictureSelectionConfig.loaderDataEngine != null) {
                PictureSelectionConfig.loaderDataEngine!!.loadMoreMediaData(requireContext(),
                    bucketId,
                    mPage,
                    config.pageSize,
                    config.pageSize,
                    object : OnQueryDataResultListener<LocalMedia>() {
                        override fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                            handleMoreMediaData(result, isHasMore)
                        }
                    })
            } else {
                mLoader?.loadPageMediaData(bucketId, mPage, config.pageSize,
                    object : OnQueryDataResultListener<LocalMedia>() {
                        override fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                            handleMoreMediaData(result, isHasMore)
                        }
                    })
            }
        }
    }

    private fun handleMoreMediaData(result: MutableList<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        mRecycler.isEnabledLoadMore = isHasMore
        if (mRecycler.isEnabledLoadMore) {
            removePageCameraRepeatData(result)
            if (result.size > 0) {
                val positionStart: Int = mAdapter.data.size
                mAdapter.data.addAll(result)
                mAdapter.notifyItemRangeChanged(positionStart, mAdapter.itemCount)
                hideDataNull()
            } else {
                onRecyclerViewPreloadMore()
            }
            if (result.size < PictureConfig.MIN_PAGE_SIZE) {
                mRecycler.onScrolled(mRecycler.scrollX, mRecycler.scrollY)
            }
        }
    }

    private fun removePageCameraRepeatData(result: MutableList<LocalMedia>) {
        try {
            if (config.isPageStrategy && isCameraCallback) {
                synchronized(LOCK) {
                    val iterator = result.iterator()
                    while (iterator.hasNext()) {
                        if (mAdapter.data.contains(iterator.next())) {
                            iterator.remove()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isCameraCallback = false
        }
    }

    override fun dispatchCameraMediaResult(media: LocalMedia?) {
        val exitsTotalNum: Int = albumListPopWindow.firstAlbumImageCount!!
        if (!isAddSameImp(exitsTotalNum)) {
            mAdapter.data.add(0, media!!)
            isCameraCallback = true
        }
        if (config.selectionMode == SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
            SelectedManager.clearSelectResult()
            val selectResultCode: Int = confirmSelect(media!!, false)
            if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                dispatchTransformResult()
            }
        } else {
            confirmSelect(media!!, false)
        }
        mAdapter.notifyItemInserted(if (config.isDisplayCamera) 1 else 0)
        mAdapter.notifyItemRangeChanged(if (config.isDisplayCamera) 1 else 0,
            mAdapter.data.size)
        if (config.isOnlySandboxDir) {
            var currentLocalMediaFolder: LocalMediaFolder? =
                SelectedManager.currentLocalMediaFolder
            if (currentLocalMediaFolder == null) {
                currentLocalMediaFolder = LocalMediaFolder()
            }
            currentLocalMediaFolder.bucketId = (ValueOf.toLong(media.parentFolderName.hashCode()))
            currentLocalMediaFolder.folderName = (media.parentFolderName)
            currentLocalMediaFolder.firstMimeType = (media.mimeType)
            currentLocalMediaFolder.firstImagePath = (media.path)
            currentLocalMediaFolder.folderTotalNum = (mAdapter.data.size)
            currentLocalMediaFolder.currentDataPage = (mPage)
            currentLocalMediaFolder.isHasMore = (false)
            currentLocalMediaFolder.data = (mAdapter.data)
            mRecycler.isEnabledLoadMore = false
            SelectedManager.currentLocalMediaFolder = (currentLocalMediaFolder)
        } else {
            mergeFolder(media)
        }
        allFolderSize = 0
        if (mAdapter.data.size > 0 || config.isDirectReturnSingle) {
            hideDataNull()
        } else {
            showDataNull()
        }
    }


    private fun mergeFolder(media: LocalMedia) {
        val allFolder: LocalMediaFolder
        val albumList: ArrayList<LocalMediaFolder?> = albumListPopWindow.albumList as ArrayList<LocalMediaFolder?>
        if (albumListPopWindow.folderCount == 0) {
            // 1、没有相册时需要手动创建相机胶卷
            allFolder = LocalMediaFolder()
            val folderName: String = if (TextUtils.isEmpty(config.defaultAlbumName)) {
                if (config.chooseMode == SelectMimeType.ofAudio()) getString(R.string.ps_all_audio) else getString(
                    R.string.ps_camera_roll)
            } else ({
                config.defaultAlbumName
            }).toString()
            allFolder.setFolderName(folderName)
            allFolder.firstImagePath = ""
            allFolder.bucketId = (PictureConfig.ALL.toLong())
            albumList?.add(0, allFolder)
        } else {
            // 2、有相册就找到对应的相册把数据加进去
            allFolder = albumListPopWindow.getFolder(0)!!
        }
        allFolder.firstImagePath = (media.path)
        allFolder.firstMimeType = (media.mimeType)
        allFolder.data = (mAdapter.data)
        allFolder.bucketId = (PictureConfig.ALL.toLong())
        allFolder.folderTotalNum =
            (if (isAddSameImp(allFolder.folderTotalNum)) allFolder.folderTotalNum else allFolder.folderTotalNum + 1)
        if (SelectedManager.currentLocalMediaFolder == null) {
            SelectedManager.currentLocalMediaFolder = allFolder
        }
        // 先查找Camera目录，没有找到则创建一个Camera目录
        var cameraFolder: LocalMediaFolder? = null
        if (albumList != null) {
            for (i in albumList.indices) {
                val exitsFolder: LocalMediaFolder = albumList[i] as LocalMediaFolder
                if (TextUtils.equals(exitsFolder.getFolderName(), media.parentFolderName)) {
                    cameraFolder = exitsFolder
                    break
                }
            }
        }
        if (cameraFolder == null) {
            // 还没有这个目录，创建一个
            cameraFolder = LocalMediaFolder()
            albumList?.add(cameraFolder)
        }
        cameraFolder.setFolderName(media.parentFolderName)
        if (cameraFolder.bucketId.equals(-1) || cameraFolder.bucketId.equals(0)) {
            cameraFolder.bucketId = (media.bucketId)
        }
        if (config.isPageStrategy) {
            cameraFolder.isHasMore = (true)
        } else {
            if (!isAddSameImp(allFolder.folderTotalNum)
                || !TextUtils.isEmpty(config.outPutCameraDir)
                || !TextUtils.isEmpty(config.outPutAudioDir)
            ) {
                cameraFolder.getData().add(0, media)
            }
        }
        cameraFolder.folderTotalNum = (if (isAddSameImp(allFolder.folderTotalNum)) cameraFolder.folderTotalNum else cameraFolder.folderTotalNum + 1)
        cameraFolder.firstImagePath = (config.cameraPath)
        cameraFolder.firstMimeType = (media.mimeType)
        albumList?.let { albumListPopWindow.bindAlbumData(it) }
    }

    /**
     * 数量是否一致
     */
    private fun isAddSameImp(totalNum: Int): Boolean {
        return if (totalNum == 0) {
            false
        } else allFolderSize in 1 until totalNum
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDragSelectTouchListener.stopAutoScroll()
    }

    /**
     * 显示数据为空提示
     */
    private fun showDataNull() {
        if (SelectedManager.currentLocalMediaFolder == null
            || SelectedManager.currentLocalMediaFolder!!.bucketId.equals(PictureConfig.ALL)
        ) {
            if (tvDataEmpty.visibility == View.GONE) {
                tvDataEmpty.visibility = View.VISIBLE
            }
            tvDataEmpty.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                R.drawable.ps_ic_no_data,
                0,
                0)
            val tips: String =
                if (config.chooseMode == ofAudio()) getString(R.string.ps_audio_empty) else getString(
                    R.string.ps_empty)
            tvDataEmpty.text = tips
        }
    }

    /**
     * 隐藏数据为空提示
     */
    private fun hideDataNull() {
        if (tvDataEmpty.visibility == View.VISIBLE) {
            tvDataEmpty.visibility = View.GONE
        }
    }

    /*  companion object {
          val fragmentTag = PictureSelectorFragment::class.java.simpleName
              get() = Companion.field

          */
    /**
     * 这个时间对应的是R.anim.ps_anim_modal_in里面的
     *//*
        private var SELECT_ANIM_DURATION = 135
        private val LOCK = Any()
        fun newInstance(): PictureSelectorFragment {
            val fragment = PictureSelectorFragment()
            fragment.setArguments(Bundle())
            return fragment
        }
    }*/
}
