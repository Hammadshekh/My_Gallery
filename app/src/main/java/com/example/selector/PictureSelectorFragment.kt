import android.annotation.SuppressLint
import android.app.Service
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.os.Vibrator
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
import com.example.selector.adapter.PictureImageGridAdapter
import com.example.selector.basic.IPictureSelectorEvent
import com.example.selector.basic.PictureCommonFragment
import com.example.selector.config.InjectResourceSource
import com.example.selector.config.SelectMimeType
import com.example.selector.config.SelectMimeType.ofAudio
import com.example.selector.dialog.AlbumListPopWindow
import com.example.selector.interfaces.OnRecyclerViewPreloadMoreListener
import com.example.selector.widget.*
import com.luck.picture.lib.entity.LocalMedia
import java.io.File
import java.lang.Exception
import java.lang.NullPointerException
import java.util.ArrayList
import java.util.HashSet

class PictureSelectorFragment : PictureCommonFragment(),
    OnRecyclerViewPreloadMoreListener, IPictureSelectorEvent {
    private var mRecycler: RecyclerPreloadView? = null
    private var tvDataEmpty: TextView? = null
    private var titleBar: TitleBar? = null
    private var bottomNarBar: BottomNavBar? = null
    private var completeSelectView: CompleteSelectView? = null
    private var tvCurrentDataTime: TextView? = null
    private var intervalClickTime: Long = 0
    private var allFolderSize = 0
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
    private var mAdapter: PictureImageGridAdapter? = null
    private var albumListPopWindow: AlbumListPopWindow? = null
    private var mDragSelectTouchListener: SlideSelectTouchListener? = null
    override val resourceId: Int
        get() {
            val layoutResourceId: Int = InjectResourceSource.getLayoutResource(getContext(),
                InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE)
            return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
                layoutResourceId
            } else R.layout.ps_fragment_selector
        }

    @SuppressLint("NotifyDataSetChanged")
    fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia) {
        bottomNarBar?.setSelectedChange()
        completeSelectView?.setSelectedChange(false)
        // 刷新列表数据
        if (checkNotifyStrategy(isAddRemove)) {
            mAdapter?.notifyItemPositionChanged(currentMedia.position)
            mRecycler?.postDelayed(Runnable { mAdapter?.notifyDataSetChanged() },
                SELECT_ANIM_DURATION)
        } else {
            mAdapter?.notifyItemPositionChanged(currentMedia.position)
        }
        if (!isAddRemove) {
            sendChangeSubSelectPositionEvent(true)
        }
    }

    fun onFixedSelectedChange(oldLocalMedia: LocalMedia) {
        mAdapter.notifyItemPositionChanged(oldLocalMedia.position)
    }

    fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {
        if (PictureSelectionConfig.selectorStyle.getSelectMainStyle().isSelectNumberStyle()) {
            for (index in 0 until SelectedManager.getSelectCount()) {
                val media: LocalMedia = SelectedManager.getSelectedResult().get(index)
                media.num = index + 1
                if (adapterChange) {
                    mAdapter.notifyItemPositionChanged(media.position)
                }
            }
        }
    }

    fun onCheckOriginalChange() {
        bottomNarBar.setOriginalCheck()
    }

    /**
     * 刷新列表策略
     *
     * @param isAddRemove
     * @return
     */
    private fun checkNotifyStrategy(isAddRemove: Boolean): Boolean {
        var isNotifyAll = false
        if (config.isMaxSelectEnabledMask) {
            if (config.isWithVideoImage) {
                if (config.selectionMode === SelectModeConfig.SINGLE) {
                    // ignore
                } else {
                    isNotifyAll = (SelectedManager.getSelectCount() === config.maxSelectNum
                            || !isAddRemove && SelectedManager.getSelectCount() === config.maxSelectNum - 1)
                }
            } else {
                isNotifyAll =
                    if (SelectedManager.getSelectCount() === 0 || isAddRemove && SelectedManager.getSelectCount() === 1) {
                        // 首次添加或单选，选择数量变为0了，都notifyDataSetChanged
                        true
                    } else {
                        if (PictureMimeType.isHasVideo(SelectedManager.getTopResultMimeType())) {
                            val maxSelectNum: Int =
                                if (config.maxVideoSelectNum > 0) config.maxVideoSelectNum else config.maxSelectNum
                            (SelectedManager.getSelectCount() === maxSelectNum
                                    || !isAddRemove && SelectedManager.getSelectCount() === maxSelectNum - 1)
                        } else {
                            (SelectedManager.getSelectCount() === config.maxSelectNum
                                    || !isAddRemove && SelectedManager.getSelectCount() === config.maxSelectNum - 1)
                        }
                    }
            }
        }
        return isNotifyAll
    }

    fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PictureConfig.EXTRA_ALL_FOLDER_SIZE, allFolderSize)
        outState.putInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
        outState.putInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION,
            mRecycler.getLastVisiblePosition())
        outState.putBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA, mAdapter.isDisplayCamera())
        SelectedManager.setCurrentLocalMediaFolder(SelectedManager.getCurrentLocalMediaFolder())
        SelectedManager.addAlbumDataSource(albumListPopWindow.getAlbumList())
        SelectedManager.addDataSource(mAdapter.getData())
    }

    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    fun onFragmentResume() {
        setRootViewKeyListener(requireView())
    }

    fun reStartSavedInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            allFolderSize = savedInstanceState.getInt(PictureConfig.EXTRA_ALL_FOLDER_SIZE)
            mPage = savedInstanceState.getInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
            currentPosition =
                savedInstanceState.getInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION,
                    currentPosition)
            isDisplayCamera = savedInstanceState.getBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA,
                config.isDisplayCamera)
        } else {
            isDisplayCamera = config.isDisplayCamera
        }
    }

    /**
     * 完成按钮
     */
    private fun initComplete() {
        if (config.selectionMode === SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
            PictureSelectionConfig.selectorStyle.getTitleBarStyle().setHideCancelButton(false)
            titleBar.getTitleCancelView().setVisibility(View.VISIBLE)
            completeSelectView.setVisibility(View.GONE)
        } else {
            completeSelectView.setCompleteSelectViewStyle()
            completeSelectView.setSelectedChange(false)
            val selectMainStyle: SelectMainStyle =
                PictureSelectionConfig.selectorStyle.getSelectMainStyle()
            if (selectMainStyle.isCompleteSelectRelativeTop()) {
                if (completeSelectView.getLayoutParams() is ConstraintLayout.LayoutParams) {
                    (completeSelectView.getLayoutParams() as ConstraintLayout.LayoutParams).topToTop =
                        R.id.title_bar
                    (completeSelectView.getLayoutParams() as ConstraintLayout.LayoutParams).bottomToBottom =
                        R.id.title_bar
                    if (config.isPreviewFullScreenMode) {
                        (completeSelectView
                            .getLayoutParams() as ConstraintLayout.LayoutParams).topMargin =
                            DensityUtil.getStatusBarHeight(getContext())
                    }
                } else if (completeSelectView.getLayoutParams() is RelativeLayout.LayoutParams) {
                    if (config.isPreviewFullScreenMode) {
                        (completeSelectView
                            .getLayoutParams() as RelativeLayout.LayoutParams).topMargin =
                            DensityUtil.getStatusBarHeight(getContext())
                    }
                }
            }
            completeSelectView.setOnClickListener(View.OnClickListener {
                if (config.isEmptyResultReturn && SelectedManager.getSelectCount() === 0) {
                    onExitPictureSelector()
                } else {
                    dispatchTransformResult()
                }
            })
        }
    }

    fun onCreateLoader() {
        if (PictureSelectionConfig.loaderFactory != null) {
            mLoader = PictureSelectionConfig.loaderFactory.onCreateLoader()
            if (mLoader == null) {
                throw NullPointerException("No available " + IBridgeMediaLoader::class.java + " loader found")
            }
        } else {
            mLoader = if (config.isPageStrategy) LocalMediaPageLoader() else LocalMediaLoader()
        }
        mLoader.initConfig(getContext(), config)
    }

    private fun initTitleBar() {
        if (PictureSelectionConfig.selectorStyle.getTitleBarStyle().isHideTitleBar()) {
            titleBar.setVisibility(View.GONE)
        }
        titleBar.setTitleBarStyle()
        titleBar.setOnTitleBarListener(object : OnTitleBarListener() {
            fun onTitleDoubleClick() {
                if (config.isAutomaticTitleRecyclerTop) {
                    val intervalTime = 500
                    if (SystemClock.uptimeMillis() - intervalClickTime < intervalTime && mAdapter.getItemCount() > 0) {
                        mRecycler.scrollToPosition(0)
                    } else {
                        intervalClickTime = SystemClock.uptimeMillis()
                    }
                }
            }

            fun onBackPressed() {
                if (albumListPopWindow.isShowing()) {
                    albumListPopWindow.dismiss()
                } else {
                    onKeyBackFragmentFinish()
                }
            }

            fun onShowAlbumPopWindow(anchor: View?) {
                albumListPopWindow.showAsDropDown(anchor)
            }
        })
    }

    /**
     * initAlbumListPopWindow
     */
    private fun initAlbumListPopWindow() {
        albumListPopWindow = AlbumListPopWindow.buildPopWindow(getContext())
        albumListPopWindow.setOnPopupWindowStatusListener(object : OnPopupWindowStatusListener() {
            fun onShowPopupWindow() {
                if (!config.isOnlySandboxDir) {
                    AnimUtils.rotateArrow(titleBar.getImageArrow(), true)
                }
            }

            fun onDismissPopupWindow() {
                if (!config.isOnlySandboxDir) {
                    AnimUtils.rotateArrow(titleBar.getImageArrow(), false)
                }
            }
        })
        addAlbumPopWindowAction()
    }

    private fun recoverSaveInstanceData() {
        mAdapter.setDisplayCamera(isDisplayCamera)
        setEnterAnimationDuration(0)
        if (config.isOnlySandboxDir) {
            handleInAppDirAllMedia(SelectedManager.getCurrentLocalMediaFolder())
        } else {
            handleRecoverAlbumData(ArrayList<Any?>(SelectedManager.getAlbumDataSource()))
        }
    }

    private fun handleRecoverAlbumData(albumData: List<LocalMediaFolder>) {
        if (ActivityCompatHelper.isDestroy(getActivity())) {
            return
        }
        if (albumData.size > 0) {
            val firstFolder: LocalMediaFolder
            if (SelectedManager.getCurrentLocalMediaFolder() != null) {
                firstFolder = SelectedManager.getCurrentLocalMediaFolder()
            } else {
                firstFolder = albumData[0]
                SelectedManager.setCurrentLocalMediaFolder(firstFolder)
            }
            titleBar.setTitle(firstFolder.getFolderName())
            albumListPopWindow.bindAlbumData(albumData)
            if (config.isPageStrategy) {
                handleFirstPageMedia(ArrayList<Any?>(SelectedManager.getDataSource()), true)
            } else {
                setAdapterData(firstFolder.getData())
            }
        } else {
            showDataNull()
        }
    }

    private fun requestLoadData() {
        mAdapter.setDisplayCamera(isDisplayCamera)
        val isCheckReadStorage =
            if (SdkVersionUtils.isR() && config.isAllFilesAccess) Environment.isExternalStorageManager() else PermissionChecker.isCheckReadStorage(
                getContext())
        if (isCheckReadStorage) {
            beginLoadData()
        } else {
            onPermissionExplainEvent(true, PermissionConfig.READ_WRITE_EXTERNAL_STORAGE)
            if (PictureSelectionConfig.onPermissionsEventListener != null) {
                onApplyPermissionsEvent(PermissionEvent.EVENT_SOURCE_DATA,
                    PermissionConfig.READ_WRITE_EXTERNAL_STORAGE)
            } else {
                PermissionChecker.getInstance().requestPermissions(this,
                    PermissionConfig.READ_WRITE_EXTERNAL_STORAGE,
                    object : PermissionResultCallback() {
                        fun onGranted() {
                            beginLoadData()
                        }

                        fun onDenied() {
                            handlePermissionDenied(PermissionConfig.READ_WRITE_EXTERNAL_STORAGE)
                        }
                    })
            }
        }
    }

    fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String?>?) {
        if (event != PermissionEvent.EVENT_SOURCE_DATA) {
            super.onApplyPermissionsEvent(event, permissionArray)
        } else {
            PictureSelectionConfig.onPermissionsEventListener.requestPermission(this,
                permissionArray,
                object : OnRequestPermissionListener() {
                    fun onCall(permissionArray: Array<String?>?, isResult: Boolean) {
                        if (isResult) {
                            beginLoadData()
                        } else {
                            handlePermissionDenied(permissionArray)
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

    fun handlePermissionSettingResult(permissions: Array<String?>) {
        onPermissionExplainEvent(false, null)
        val isHasCamera =
            permissions.size > 0 && TextUtils.equals(permissions[0], PermissionConfig.CAMERA.get(0))
        val isHasPermissions: Boolean
        isHasPermissions = if (PictureSelectionConfig.onPermissionsEventListener != null) {
            PictureSelectionConfig.onPermissionsEventListener.hasPermissions(this, permissions)
        } else {
            if (isHasCamera) {
                PermissionChecker.isCheckSelfPermission(getContext(), permissions)
            } else {
                if (SdkVersionUtils.isR() && config.isAllFilesAccess) {
                    Environment.isExternalStorageManager()
                } else {
                    PermissionChecker.isCheckSelfPermission(getContext(), permissions)
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
                ToastUtils.showToast(getContext(), getString(R.string.ps_camera))
            } else {
                ToastUtils.showToast(getContext(), getString(R.string.ps_jurisdiction))
                onKeyBackFragmentFinish()
            }
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = arrayOf<String>()
    }

    /**
     * 给AlbumListPopWindow添加事件
     */
    private fun addAlbumPopWindowAction() {
        albumListPopWindow.setOnIBridgeAlbumWidget(object : OnAlbumItemClickListener() {
            fun onItemClick(position: Int, curFolder: LocalMediaFolder) {
                isDisplayCamera =
                    config.isDisplayCamera && curFolder.getBucketId() === PictureConfig.ALL
                mAdapter.setDisplayCamera(isDisplayCamera)
                titleBar.setTitle(curFolder.getFolderName())
                val lastFolder: LocalMediaFolder = SelectedManager.getCurrentLocalMediaFolder()
                val lastBucketId: Long = lastFolder.getBucketId()
                if (config.isPageStrategy) {
                    if (curFolder.getBucketId() !== lastBucketId) {
                        // 1、记录一下上一次相册数据加载到哪了，到时候切回来的时候要续上
                        lastFolder.setData(mAdapter.getData())
                        lastFolder.setCurrentDataPage(mPage)
                        lastFolder.setHasMore(mRecycler.isEnabledLoadMore())

                        // 2、判断当前相册是否请求过，如果请求过则不从MediaStore去拉取了
                        if (curFolder.getData().size() > 0 && !curFolder.isHasMore()) {
                            setAdapterData(curFolder.getData())
                            mPage = curFolder.getCurrentDataPage()
                            mRecycler.setEnabledLoadMore(curFolder.isHasMore())
                            mRecycler.smoothScrollToPosition(0)
                        } else {
                            // 3、从MediaStore拉取数据
                            mPage = 1
                            if (PictureSelectionConfig.loaderDataEngine != null) {
                                PictureSelectionConfig.loaderDataEngine.loadFirstPageMediaData(
                                    getContext(),
                                    curFolder.getBucketId(),
                                    mPage,
                                    config.pageSize,
                                    object : OnQueryDataResultListener<LocalMedia?>() {
                                        fun onComplete(
                                            result: ArrayList<LocalMedia>,
                                            isHasMore: Boolean,
                                        ) {
                                            handleSwitchAlbum(result, isHasMore)
                                        }
                                    })
                            } else {
                                mLoader.loadPageMediaData(curFolder.getBucketId(),
                                    mPage,
                                    config.pageSize,
                                    object : OnQueryDataResultListener<LocalMedia?>() {
                                        fun onComplete(
                                            result: ArrayList<LocalMedia>,
                                            isHasMore: Boolean,
                                        ) {
                                            handleSwitchAlbum(result, isHasMore)
                                        }
                                    })
                            }
                        }
                    }
                } else {
                    // 非分页模式直接导入该相册下的所有资源
                    if (curFolder.getBucketId() !== lastBucketId) {
                        setAdapterData(curFolder.getData())
                        mRecycler.smoothScrollToPosition(0)
                    }
                }
                SelectedManager.setCurrentLocalMediaFolder(curFolder)
                albumListPopWindow.dismiss()
                if (mDragSelectTouchListener != null && config.isFastSlidingSelect) {
                    mDragSelectTouchListener.setRecyclerViewHeaderCount(if (mAdapter.isDisplayCamera()) 1 else 0)
                }
            }
        })
    }

    private fun handleSwitchAlbum(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(getActivity())) {
            return
        }
        mRecycler.setEnabledLoadMore(isHasMore)
        if (result.size == 0) {
            // 如果从MediaStore拉取都没有数据了，adapter里的可能是缓存所以也清除
            mAdapter.getData().clear()
        }
        setAdapterData(result)
        mRecycler.onScrolled(0, 0)
        mRecycler.smoothScrollToPosition(0)
    }

    private fun initBottomNavBar() {
        bottomNarBar.setBottomNavBarStyle()
        bottomNarBar.setOnBottomNavBarListener(object : OnBottomNavBarListener() {
            fun onPreview() {
                onStartPreview(0, true)
            }

            fun onCheckOriginalChange() {
                sendSelectedOriginalChangeEvent()
            }
        })
        bottomNarBar.setSelectedChange()
    }

    fun loadAllAlbumData() {
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine.loadAllAlbumData(getContext(),
                object : OnQueryAllAlbumListener<LocalMediaFolder?>() {
                    fun onComplete(result: List<LocalMediaFolder>) {
                        handleAllAlbumData(result)
                    }
                })
        } else {
            mLoader.loadAllAlbum(object : OnQueryAllAlbumListener<LocalMediaFolder?>() {
                fun onComplete(result: List<LocalMediaFolder>) {
                    handleAllAlbumData(result)
                }
            })
        }
    }

    private fun handleAllAlbumData(result: List<LocalMediaFolder>) {
        if (ActivityCompatHelper.isDestroy(getActivity())) {
            return
        }
        if (result.size > 0) {
            val firstFolder: LocalMediaFolder
            if (SelectedManager.getCurrentLocalMediaFolder() != null) {
                firstFolder = SelectedManager.getCurrentLocalMediaFolder()
            } else {
                firstFolder = result[0]
                SelectedManager.setCurrentLocalMediaFolder(firstFolder)
            }
            titleBar.setTitle(firstFolder.getFolderName())
            albumListPopWindow.bindAlbumData(result)
            if (config.isPageStrategy) {
                loadFirstPageMediaData(firstFolder.getBucketId())
            } else {
                setAdapterData(firstFolder.getData())
            }
        } else {
            showDataNull()
        }
    }

    fun loadFirstPageMediaData(firstBucketId: Long) {
        mRecycler.setEnabledLoadMore(true)
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine.loadFirstPageMediaData(getContext(),
                firstBucketId,
                mPage,
                mPage * config.pageSize,
                object : OnQueryDataResultListener<LocalMedia?>() {
                    fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                        handleFirstPageMedia(result, isHasMore)
                    }
                })
        } else {
            mLoader.loadPageMediaData(firstBucketId, 1, mPage * config.pageSize,
                object : OnQueryDataResultListener<LocalMedia?>() {
                    fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                        handleFirstPageMedia(result, isHasMore)
                    }
                })
        }
    }

    private fun handleFirstPageMedia(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(getActivity())) {
            return
        }
        mRecycler.setEnabledLoadMore(isHasMore)
        if (mRecycler.isEnabledLoadMore() && result.size == 0) {
            // 如果isHasMore为true但result.size() = 0;
            // 那么有可能是开启了某些条件过滤，实际上是还有更多资源的再强制请求
            onRecyclerViewPreloadMore()
        } else {
            setAdapterData(result)
        }
    }

    fun loadOnlyInAppDirectoryAllMediaData() {
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine.loadOnlyInAppDirAllMediaData(getContext(),
                object : OnQueryAlbumListener<LocalMediaFolder?>() {
                    fun onComplete(folder: LocalMediaFolder?) {
                        handleInAppDirAllMedia(folder)
                    }
                })
        } else {
            mLoader.loadOnlyInAppDirAllMedia(object : OnQueryAlbumListener<LocalMediaFolder?>() {
                fun onComplete(folder: LocalMediaFolder?) {
                    handleInAppDirAllMedia(folder)
                }
            })
        }
    }

    private fun handleInAppDirAllMedia(folder: LocalMediaFolder?) {
        if (!ActivityCompatHelper.isDestroy(getActivity())) {
            val sandboxDir: String = config.sandboxDir
            val isNonNull = folder != null
            val folderName = if (isNonNull) folder.getFolderName() else File(sandboxDir).name
            titleBar.setTitle(folderName)
            if (isNonNull) {
                SelectedManager.setCurrentLocalMediaFolder(folder)
                setAdapterData(folder.getData())
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
                mRecycler.setLastVisiblePosition(currentPosition)
            })
        }
    }

    private fun initRecycler(view: View) {
        mRecycler = view.findViewById(R.id.recycler)
        val selectorStyle: PictureSelectorStyle = PictureSelectionConfig.selectorStyle
        val selectMainStyle: SelectMainStyle = selectorStyle.getSelectMainStyle()
        val listBackgroundColor: Int = selectMainStyle.getMainListBackgroundColor()
        if (StyleUtils.checkStyleValidity(listBackgroundColor)) {
            mRecycler.setBackgroundColor(listBackgroundColor)
        } else {
            mRecycler.setBackgroundColor(ContextCompat.getColor(getContext(),
                R.color.ps_color_black))
        }
        val imageSpanCount: Int =
            if (config.imageSpanCount <= 0) PictureConfig.DEFAULT_SPAN_COUNT else config.imageSpanCount
        if (mRecycler.getItemDecorationCount() === 0) {
            if (StyleUtils.checkSizeValidity(selectMainStyle.getAdapterItemSpacingSize())) {
                mRecycler.addItemDecoration(GridSpacingItemDecoration(imageSpanCount,
                    selectMainStyle.getAdapterItemSpacingSize(),
                    selectMainStyle.isAdapterItemIncludeEdge()))
            } else {
                mRecycler.addItemDecoration(GridSpacingItemDecoration(imageSpanCount,
                    DensityUtil.dip2px(view.context, 1),
                    selectMainStyle.isAdapterItemIncludeEdge()))
            }
        }
        mRecycler.setLayoutManager(GridLayoutManager(getContext(), imageSpanCount))
        val itemAnimator: ItemAnimator = mRecycler.getItemAnimator()
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            mRecycler.setItemAnimator(null)
        }
        if (config.isPageStrategy) {
            mRecycler.setReachBottomRow(RecyclerPreloadView.BOTTOM_PRELOAD)
            mRecycler.setOnRecyclerViewPreloadListener(this)
        } else {
            mRecycler.setHasFixedSize(true)
        }
        mAdapter = PictureImageGridAdapter(getContext(), config)
        mAdapter.setDisplayCamera(isDisplayCamera)
        when (config.animationMode) {
            AnimationType.ALPHA_IN_ANIMATION -> mRecycler.setAdapter(AlphaInAnimationAdapter(
                mAdapter))
            AnimationType.SLIDE_IN_BOTTOM_ANIMATION -> mRecycler.setAdapter(
                SlideInBottomAnimationAdapter(mAdapter))
            else -> mRecycler.setAdapter(mAdapter)
        }
        addRecyclerAction()
    }

    private fun addRecyclerAction() {
        mAdapter.setOnItemClickListener(object : OnItemClickListener() {
            fun openCameraClick() {
                if (DoubleUtils.isFastDoubleClick()) {
                    return
                }
                openSelectedCamera()
            }

            fun onSelected(selectedView: View, position: Int, media: LocalMedia?): Int {
                val selectResultCode: Int = confirmSelect(media, selectedView.isSelected)
                if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                    if (PictureSelectionConfig.onSelectAnimListener != null) {
                        val duration: Long =
                            PictureSelectionConfig.onSelectAnimListener.onSelectAnim(selectedView)
                        if (duration > 0) {
                            SELECT_ANIM_DURATION = duration.toInt()
                        }
                    } else {
                        val animation =
                            AnimationUtils.loadAnimation(getContext(), R.anim.ps_anim_modal_in)
                        SELECT_ANIM_DURATION =
                            animation.duration.toInt()
                        selectedView.startAnimation(animation)
                    }
                }
                return selectResultCode
            }

            fun onItemClick(selectedView: View?, position: Int, media: LocalMedia?) {
                if (config.selectionMode === SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
                    SelectedManager.clearSelectResult()
                    val selectResultCode: Int = confirmSelect(media, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        dispatchTransformResult()
                    }
                } else {
                    if (DoubleUtils.isFastDoubleClick()) {
                        return
                    }
                    onStartPreview(position, false)
                }
            }

            fun onItemLongClick(itemView: View?, position: Int) {
                if (mDragSelectTouchListener != null && config.isFastSlidingSelect) {
                    val vibrator =
                        getActivity().getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(50)
                    mDragSelectTouchListener.startSlideSelection(position)
                }
            }
        })
        mRecycler.setOnRecyclerViewScrollStateListener(object :
            OnRecyclerViewScrollStateListener() {
            fun onScrollFast() {
                if (PictureSelectionConfig.imageEngine != null) {
                    PictureSelectionConfig.imageEngine.pauseRequests(getContext())
                }
            }

            fun onScrollSlow() {
                if (PictureSelectionConfig.imageEngine != null) {
                    PictureSelectionConfig.imageEngine.resumeRequests(getContext())
                }
            }
        })
        mRecycler.setOnRecyclerViewScrollListener(object : OnRecyclerViewScrollListener() {
            fun onScrolled(dx: Int, dy: Int) {
                setCurrentMediaCreateTimeText()
            }

            fun onScrollStateChanged(state: Int) {
                if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showCurrentMediaCreateTimeUI()
                } else if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    hideCurrentMediaCreateTimeUI()
                }
            }
        })
        if (config.isFastSlidingSelect) {
            val selectedPosition = HashSet<Int>()
            val slideSelectionHandler = SlideSelectionHandler(object : ISelectionHandler() {
                val selection: HashSet<Int>
                    get() {
                        for (i in 0 until SelectedManager.getSelectCount()) {
                            val media: LocalMedia = SelectedManager.getSelectedResult().get(i)
                            selectedPosition.add(media.position)
                        }
                        return selectedPosition
                    }

                fun changeSelection(
                    start: Int,
                    end: Int,
                    isSelected: Boolean,
                    calledFromOnStart: Boolean,
                ) {
                    val adapterData: ArrayList<LocalMedia> = mAdapter.getData()
                    if (adapterData.size == 0 || start > adapterData.size) {
                        return
                    }
                    val media = adapterData[start]
                    val selectResultCode: Int =
                        confirmSelect(media, SelectedManager.getSelectedResult().contains(media))
                    mDragSelectTouchListener.setActive(selectResultCode != SelectedManager.INVALID)
                }
            })
            mDragSelectTouchListener = SlideSelectTouchListener()
                .setRecyclerViewHeaderCount(if (mAdapter.isDisplayCamera()) 1 else 0)
                .withSelectListener(slideSelectionHandler)
            mRecycler.addOnItemTouchListener(mDragSelectTouchListener)
        }
    }

    /**
     * 显示当前资源时间轴
     */
    private fun setCurrentMediaCreateTimeText() {
        if (config.isDisplayTimeAxis) {
            val position: Int = mRecycler.getFirstVisiblePosition()
            if (position != RecyclerView.NO_POSITION) {
                val data: ArrayList<LocalMedia> = mAdapter.getData()
                if (data.size > position && data[position].dateAddedTime > 0) {
                    tvCurrentDataTime.setText(DateUtils.getDataFormat(getContext(),
                        data[position].dateAddedTime))
                }
            }
        }
    }

    /**
     * 显示当前资源时间轴
     */
    private fun showCurrentMediaCreateTimeUI() {
        if (config.isDisplayTimeAxis && mAdapter.getData().size() > 0) {
            if (tvCurrentDataTime!!.alpha == 0f) {
                tvCurrentDataTime!!.animate().setDuration(150).alphaBy(1.0f).start()
            }
        }
    }

    /**
     * 隐藏当前资源时间轴
     */
    private fun hideCurrentMediaCreateTimeUI() {
        if (config.isDisplayTimeAxis && mAdapter.getData().size() > 0) {
            tvCurrentDataTime!!.animate().setDuration(250).alpha(0.0f).start()
        }
    }

    /**
     * 预览图片
     *
     * @param position        预览图片下标
     * @param isBottomPreview true 底部预览模式 false列表预览模式
     */
    private fun onStartPreview(position: Int, isBottomPreview: Boolean) {
        if (ActivityCompatHelper.checkFragmentNonExits(getActivity(),
                PictureSelectorPreviewFragment.TAG)
        ) {
            val data: ArrayList<LocalMedia>
            val totalNum: Int
            var currentBucketId: Long = 0
            if (isBottomPreview) {
                data = ArrayList<Any?>(SelectedManager.getSelectedResult())
                totalNum = data.size
            } else {
                data = ArrayList<Any?>(mAdapter.getData())
                totalNum = SelectedManager.getCurrentLocalMediaFolder().getFolderTotalNum()
                currentBucketId = SelectedManager.getCurrentLocalMediaFolder().getBucketId()
            }
            if (!isBottomPreview && config.isPreviewZoomEffect) {
                BuildRecycleItemViewParams.generateViewParams(mRecycler,
                    if (config.isPreviewFullScreenMode) 0 else DensityUtil.getStatusBarHeight(
                        getContext()))
            }
            if (PictureSelectionConfig.onPreviewInterceptListener != null) {
                PictureSelectionConfig.onPreviewInterceptListener
                    .onPreview(getContext(),
                        position,
                        totalNum,
                        mPage,
                        currentBucketId,
                        titleBar.getTitleText(),
                        mAdapter.isDisplayCamera(),
                        data,
                        isBottomPreview)
            } else {
                if (ActivityCompatHelper.checkFragmentNonExits(getActivity(),
                        PictureSelectorPreviewFragment.TAG)
                ) {
                    val previewFragment: PictureSelectorPreviewFragment =
                        PictureSelectorPreviewFragment.newInstance()
                    previewFragment.setInternalPreviewData(isBottomPreview,
                        titleBar.getTitleText(),
                        mAdapter.isDisplayCamera(),
                        position,
                        totalNum,
                        mPage,
                        currentBucketId,
                        data)
                    FragmentInjectManager.injectFragment(getActivity(),
                        PictureSelectorPreviewFragment.TAG,
                        previewFragment)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapterData(result: ArrayList<LocalMedia>) {
        // 这个地方有个时间差，主要是解决进场动画和查询数据同时进行导致动画有点卡顿问题，
        // 主要是针对添加PictureSelectorFragment方式下
        val enterAnimationDuration: Long = getEnterAnimationDuration()
        if (enterAnimationDuration > 0) {
            requireView().postDelayed(Runnable { setAdapterDataComplete(result) },
                enterAnimationDuration)
        } else {
            setAdapterDataComplete(result)
        }
    }

    private fun setAdapterDataComplete(result: ArrayList<LocalMedia>) {
        setEnterAnimationDuration(0)
        sendChangeSubSelectPositionEvent(false)
        mAdapter.setDataAndDataSetChanged(result)
        SelectedManager.clearAlbumDataSource()
        SelectedManager.clearDataSource()
        recoveryRecyclerPosition()
        if (mAdapter.isDataEmpty()) {
            showDataNull()
        } else {
            hideDataNull()
        }
    }

    fun onRecyclerViewPreloadMore() {
        if (isMemoryRecycling) {
            // 这里延迟是拍照导致的页面被回收，Fragment的重创会快于相机的onActivityResult的
            requireView().postDelayed(Runnable { loadMoreMediaData() }, 350)
        } else {
            loadMoreMediaData()
        }
    }

    /**
     * 加载更多
     */
    fun loadMoreMediaData() {
        if (mRecycler.isEnabledLoadMore()) {
            mPage++
            val localMediaFolder: LocalMediaFolder = SelectedManager.getCurrentLocalMediaFolder()
            val bucketId: Long = if (localMediaFolder != null) localMediaFolder.getBucketId() else 0
            if (PictureSelectionConfig.loaderDataEngine != null) {
                PictureSelectionConfig.loaderDataEngine.loadMoreMediaData(getContext(),
                    bucketId,
                    mPage,
                    config.pageSize,
                    config.pageSize,
                    object : OnQueryDataResultListener<LocalMedia?>() {
                        fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                            handleMoreMediaData(result, isHasMore)
                        }
                    })
            } else {
                mLoader.loadPageMediaData(bucketId, mPage, config.pageSize,
                    object : OnQueryDataResultListener<LocalMedia?>() {
                        fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                            handleMoreMediaData(result, isHasMore)
                        }
                    })
            }
        }
    }

    private fun handleMoreMediaData(result: MutableList<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(getActivity())) {
            return
        }
        mRecycler.setEnabledLoadMore(isHasMore)
        if (mRecycler.isEnabledLoadMore()) {
            removePageCameraRepeatData(result)
            if (result.size > 0) {
                val positionStart: Int = mAdapter.getData().size()
                mAdapter.getData().addAll(result)
                mAdapter.notifyItemRangeChanged(positionStart, mAdapter.getItemCount())
                hideDataNull()
            } else {
                // 如果没数据这里在强制调用一下上拉加载更多，防止是因为某些条件过滤导致的假为0的情况
                onRecyclerViewPreloadMore()
            }
            if (result.size < PictureConfig.MIN_PAGE_SIZE) {
                // 当数据量过少时强制触发一下上拉加载更多，防止没有自动触发加载更多
                mRecycler.onScrolled(mRecycler.getScrollX(), mRecycler.getScrollY())
            }
        }
    }

    private fun removePageCameraRepeatData(result: MutableList<LocalMedia>) {
        try {
            if (config.isPageStrategy && isCameraCallback) {
                synchronized(LOCK) {
                    val iterator = result.iterator()
                    while (iterator.hasNext()) {
                        if (mAdapter.getData().contains(iterator.next())) {
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

    fun dispatchCameraMediaResult(media: LocalMedia) {
        val exitsTotalNum: Int = albumListPopWindow.getFirstAlbumImageCount()
        if (!isAddSameImp(exitsTotalNum)) {
            mAdapter.getData().add(0, media)
            isCameraCallback = true
        }
        if (config.selectionMode === SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
            SelectedManager.clearSelectResult()
            val selectResultCode: Int = confirmSelect(media, false)
            if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                dispatchTransformResult()
            }
        } else {
            confirmSelect(media, false)
        }
        mAdapter.notifyItemInserted(if (config.isDisplayCamera) 1 else 0)
        mAdapter.notifyItemRangeChanged(if (config.isDisplayCamera) 1 else 0,
            mAdapter.getData().size())
        if (config.isOnlySandboxDir) {
            var currentLocalMediaFolder: LocalMediaFolder? =
                SelectedManager.getCurrentLocalMediaFolder()
            if (currentLocalMediaFolder == null) {
                currentLocalMediaFolder = LocalMediaFolder()
            }
            currentLocalMediaFolder.setBucketId(ValueOf.toLong(media.parentFolderName.hashCode()))
            currentLocalMediaFolder.setFolderName(media.parentFolderName)
            currentLocalMediaFolder.setFirstMimeType(media.mimeType)
            currentLocalMediaFolder.setFirstImagePath(media.path)
            currentLocalMediaFolder.setFolderTotalNum(mAdapter.getData().size())
            currentLocalMediaFolder.setCurrentDataPage(mPage)
            currentLocalMediaFolder.setHasMore(false)
            currentLocalMediaFolder.setData(mAdapter.getData())
            mRecycler.setEnabledLoadMore(false)
            SelectedManager.setCurrentLocalMediaFolder(currentLocalMediaFolder)
        } else {
            mergeFolder(media)
        }
        allFolderSize = 0
        if (mAdapter.getData().size() > 0 || config.isDirectReturnSingle) {
            hideDataNull()
        } else {
            showDataNull()
        }
    }

    /**
     * 拍照出来的合并到相应的专辑目录中去
     *
     * @param media
     */
    private fun mergeFolder(media: LocalMedia) {
        val allFolder: LocalMediaFolder
        val albumList: MutableList<LocalMediaFolder?> = albumListPopWindow.getAlbumList()
        if (albumListPopWindow.getFolderCount() === 0) {
            // 1、没有相册时需要手动创建相机胶卷
            allFolder = LocalMediaFolder()
            val folderName: String
            folderName = if (TextUtils.isEmpty(config.defaultAlbumName)) {
                if (config.chooseMode === SelectMimeType.ofAudio()) getString(R.string.ps_all_audio) else getString(
                    R.string.ps_camera_roll)
            } else {
                config.defaultAlbumName
            }
            allFolder.setFolderName(folderName)
            allFolder.setFirstImagePath("")
            allFolder.setBucketId(PictureConfig.ALL)
            albumList.add(0, allFolder)
        } else {
            // 2、有相册就找到对应的相册把数据加进去
            allFolder = albumListPopWindow.getFolder(0)
        }
        allFolder.setFirstImagePath(media.path)
        allFolder.setFirstMimeType(media.mimeType)
        allFolder.setData(mAdapter.getData())
        allFolder.setBucketId(PictureConfig.ALL)
        allFolder.setFolderTotalNum(if (isAddSameImp(allFolder.getFolderTotalNum())) allFolder.getFolderTotalNum() else allFolder.getFolderTotalNum() + 1)
        if (SelectedManager.getCurrentLocalMediaFolder() == null) {
            SelectedManager.setCurrentLocalMediaFolder(allFolder)
        }
        // 先查找Camera目录，没有找到则创建一个Camera目录
        var cameraFolder: LocalMediaFolder? = null
        for (i in albumList.indices) {
            val exitsFolder: LocalMediaFolder? = albumList[i]
            if (TextUtils.equals(exitsFolder.getFolderName(), media.parentFolderName)) {
                cameraFolder = exitsFolder
                break
            }
        }
        if (cameraFolder == null) {
            // 还没有这个目录，创建一个
            cameraFolder = LocalMediaFolder()
            albumList.add(cameraFolder)
        }
        cameraFolder.setFolderName(media.parentFolderName)
        if (cameraFolder.getBucketId() === -1 || cameraFolder.getBucketId() === 0) {
            cameraFolder.setBucketId(media.bucketId)
        }
        // 分页模式下，切换到Camera目录下时，会直接从MediaStore拉取
        if (config.isPageStrategy) {
            cameraFolder.setHasMore(true)
        } else {
            // 非分页模式数据都是存在目录的data下，所以直接添加进去就行
            if (!isAddSameImp(allFolder.getFolderTotalNum())
                || !TextUtils.isEmpty(config.outPutCameraDir)
                || !TextUtils.isEmpty(config.outPutAudioDir)
            ) {
                cameraFolder.getData().add(0, media)
            }
        }
        cameraFolder.setFolderTotalNum(if (isAddSameImp(allFolder.getFolderTotalNum())) cameraFolder.getFolderTotalNum() else cameraFolder.getFolderTotalNum() + 1)
        cameraFolder.setFirstImagePath(config.cameraPath)
        cameraFolder.setFirstMimeType(media.mimeType)
        albumListPopWindow.bindAlbumData(albumList)
    }

    /**
     * 数量是否一致
     */
    private fun isAddSameImp(totalNum: Int): Boolean {
        return if (totalNum == 0) {
            false
        } else allFolderSize > 0 && allFolderSize < totalNum
    }

    fun onDestroyView() {
        super.onDestroyView()
        if (mDragSelectTouchListener != null) {
            mDragSelectTouchListener.stopAutoScroll()
        }
    }

    /**
     * 显示数据为空提示
     */
    private fun showDataNull() {
        if (SelectedManager.getCurrentLocalMediaFolder() == null
            || SelectedManager.getCurrentLocalMediaFolder().getBucketId() === PictureConfig.ALL
        ) {
            if (tvDataEmpty!!.visibility == View.GONE) {
                tvDataEmpty!!.visibility = View.VISIBLE
            }
            tvDataEmpty!!.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                R.drawable.ps_ic_no_data,
                0,
                0)
            val tips: String =
                if (config.chooseMode === ofAudio()) getString(R.string.ps_audio_empty) else getString(
                    R.string.ps_empty)
            tvDataEmpty!!.text = tips
        }
    }

    /**
     * 隐藏数据为空提示
     */
    private fun hideDataNull() {
        if (tvDataEmpty!!.visibility == View.VISIBLE) {
            tvDataEmpty!!.visibility = View.GONE
        }
    }

  /*  companion object {
        val fragmentTag = PictureSelectorFragment::class.java.simpleName
            get() = Companion.field

        *//**
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
