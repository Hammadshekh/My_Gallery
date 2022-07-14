package com.example.selector

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.mygallery.R
import com.example.mygallery.adapter.PicturePreviewAdapter
import com.example.selector.adapter.PreviewGalleryAdapter
import com.example.selector.adapter.holder.BasePreviewHolder
import com.example.selector.adapter.holder.PreviewVideoHolder
import com.example.selector.basic.PictureCommonFragment
import com.example.selector.basic.PictureMediaScannerConnection
import com.example.selector.config.*
import com.example.selector.decoration.HorizontalItemDecoration
import com.example.selector.decoration.WrapContentLinearLayoutManager
import com.example.selector.dialog.PictureCommonDialog
import com.example.selector.entity.MediaExtraInfo
import com.example.selector.interfaces.OnCallbackListener
import com.example.selector.interfaces.OnItemClickListener
import com.example.selector.interfaces.OnQueryDataResultListener
import com.example.selector.loader.IBridgeMediaLoader
import com.example.selector.loader.LocalMediaLoader
import com.example.selector.loader.LocalMediaPageLoader
import com.example.selector.magical.BuildRecycleItemViewParams
import com.example.selector.magical.MagicalView
import com.example.selector.magical.OnMagicalViewCallback
import com.example.selector.magical.ViewParams
import com.example.selector.manager.SelectedManager
import com.example.selector.style.PictureWindowAnimationStyle
import com.example.selector.style.SelectMainStyle
import com.example.selector.utils.*
import com.example.selector.widget.*
import com.luck.picture.lib.entity.LocalMedia
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import java.util.Collections

class PictureSelectorPreviewFragment : PictureCommonFragment() {
    private var mData: ArrayList<LocalMedia> = ArrayList<LocalMedia>()
    private var magicalView: MagicalView? = null
    var viewPager2: ViewPager2? = null
    var viewPageAdapter: PicturePreviewAdapter? = null
    var bottomNarBar: PreviewBottomNavBar? = null
    var titleBar: PreviewTitleBar? = null

    /**
     * if there more
     */
    private var isHasMore = true
    private var curPosition = 0
    private var isInternalBottomPreview = false
    private var isSaveInstanceState = false

    /**
     * 当前相册
     */
    private var currentAlbum: String? = null

    /**
     * 是否显示了拍照入口
     */
    private var isShowCamera = false

    /**
     * 是否外部预览进来
     */
    private var isExternalPreview = false

    /**
     * 外部预览是否支持删除
     */
    private var isDisplayDelete = false
    private var isAnimationStart = false
    private var totalNum = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var mBucketId: Long = -1
    private var tvSelected: TextView? = null
    private var tvSelectedWord: TextView? = null
    private var selectClickArea: View? = null
    private var completeSelectView: CompleteSelectView? = null
    private var needScaleBig = true
    private var needScaleSmall = false
    private var mGalleryRecycle: RecyclerView? = null
    private var mGalleryAdapter: PreviewGalleryAdapter? = null
    protected var mAnimViews: List<View> = ArrayList()

    // private var mAnimViews: MutableCollection<View> = ArrayList()

    /**
     * 内部预览
     *
     * @param isBottomPreview 是否顶部预览进来的
     * @param currentAlbum    当前预览的目录
     * @param isShowCamera    是否有显示拍照图标
     * @param position        预览下标
     * @param totalNum        当前预览总数
     * @param page            当前页码
     * @param currentBucketId 当前相册目录id
     * @param data            预览数据源
     */
    fun setInternalPreviewData(
        isBottomPreview: Boolean, currentAlbumName: String?, isShowCamera: Boolean,
        position: Int, totalNum: Int, page: Int, currentBucketId: Long,
        data: ArrayList<LocalMedia>,
    ) {
        this.mPage = page
        mBucketId = currentBucketId
        mData = data
        this.totalNum = totalNum
        curPosition = position
        currentAlbum = currentAlbumName
        this.isShowCamera = isShowCamera
        isInternalBottomPreview = isBottomPreview
    }

    /**
     * 外部预览
     *
     * @param position        预览下标
     * @param totalNum        当前预览总数
     * @param data            预览数据源
     * @param isDisplayDelete 是否显示删除按钮
     */
    fun setExternalPreviewData(
        position: Int,
        totalNum: Int,
        data: ArrayList<LocalMedia>,
        isDisplayDelete: Boolean,
    ) {
        mData = data
        this.totalNum = totalNum
        curPosition = position
        this.isDisplayDelete = isDisplayDelete
        isExternalPreview = true
    }

    override val resourceId: Int
        get() {
            val layoutResourceId: Int = InjectResourceSource.getLayoutResource(requireContext(),
                InjectResourceSource.PREVIEW_LAYOUT_RESOURCE)
            return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
                layoutResourceId
            } else R.layout.ps_fragment_preview
        }

    override fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia) {
        // 更新TitleBar和BottomNarBar选择态
        tvSelected?.isSelected = SelectedManager.selectedResult.contains(currentMedia)
        bottomNarBar?.setSelectedChange()
        completeSelectView?.setSelectedChange(true)
        notifySelectNumberStyle(currentMedia)
        notifyPreviewGalleryData(isAddRemove, currentMedia)
    }

    override fun onCheckOriginalChange() {
        bottomNarBar?.setOriginalCheck()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reStartSavedInstance(savedInstanceState)
        isSaveInstanceState = savedInstanceState != null
        screenWidth = context?.let { DensityUtil.getRealScreenWidth(it) }!!
        screenHeight = context?.let { DensityUtil.getScreenHeight(it) }!!
        titleBar = view.findViewById(R.id.title_bar)
        tvSelected = view.findViewById(R.id.ps_tv_selected)
        tvSelectedWord = view.findViewById(R.id.ps_tv_selected_word)
        selectClickArea = view.findViewById(R.id.select_click_area)
        completeSelectView = view.findViewById(R.id.ps_complete_select)
        magicalView = view.findViewById(R.id.magical)
        viewPager2 = ViewPager2(requireContext())
        bottomNarBar = view.findViewById(R.id.bottom_nar_bar)
        magicalView!!.setMagicalContent(viewPager2)
        setMagicalViewBackgroundColor()
        addAminViews(titleBar,
            tvSelected,
            tvSelectedWord,
            selectClickArea,
            completeSelectView,
            bottomNarBar)
        onCreateLoader()
        initTitleBar()
        initViewPagerData(mData)
        if (isExternalPreview) {
            externalPreviewStyle()
        } else {
            initBottomNavBar()
            initPreviewSelectGallery(view as ViewGroup)
            initComplete()
        }
        iniMagicalView()
    }

    /**
     * addAminViews
     *
     * @param views
     */
    fun addAminViews(vararg views: View?) {
        Collections.addAll(mAnimViews, views)
        ///
    }

    private fun setMagicalViewBackgroundColor() {
        val mainStyle: SelectMainStyle = PictureSelectionConfig.selectorStyle.selectMainStyle!!
        if (StyleUtils.checkStyleValidity(mainStyle.previewBackgroundColor)) {
            magicalView?.setBackgroundColor(mainStyle.previewBackgroundColor)
        } else {
            if (config.chooseMode == SelectMimeType.ofAudio() || mData.size > 0 && PictureMimeType.isHasAudio(
                    mData[0].mimeType)
            ) {
                magicalView?.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.ps_color_white))
            } else {
                magicalView?.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.ps_color_black))
            }
        }
    }

    override fun reStartSavedInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt(PictureConfig.EXTRA_CURRENT_PAGE, 1)
            mBucketId = savedInstanceState.getLong(PictureConfig.EXTRA_CURRENT_BUCKET_ID, -1)
            curPosition =
                savedInstanceState.getInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, curPosition)
            isShowCamera =
                savedInstanceState.getBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA, isShowCamera)
            totalNum =
                savedInstanceState.getInt(PictureConfig.EXTRA_PREVIEW_CURRENT_ALBUM_TOTAL, totalNum)
            isExternalPreview = savedInstanceState.getBoolean(PictureConfig.EXTRA_EXTERNAL_PREVIEW,
                isExternalPreview)
            isDisplayDelete =
                savedInstanceState.getBoolean(PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE,
                    isDisplayDelete)
            isInternalBottomPreview =
                savedInstanceState.getBoolean(PictureConfig.EXTRA_BOTTOM_PREVIEW,
                    isInternalBottomPreview)
            currentAlbum = savedInstanceState.getString(PictureConfig.EXTRA_CURRENT_ALBUM_NAME, "")
            if (mData!!.size == 0) {
                mData!!.addAll(ArrayList(SelectedManager.getSelectedPreviewResult()))
            }
        }
    }

    override fun onKeyBackFragmentFinish() {
        onKeyDownBackToMin()
    }

    /**
     * 设置MagicalView
     */
    private fun iniMagicalView() {
        if (isHasMagicalEffect) {
            setMagicalViewAction()
            val alpha = if (isSaveInstanceState) 1.0f else 0.0f
            magicalView?.setBackgroundAlpha(alpha)
            for (i in mAnimViews.indices) {
                if (mAnimViews[i] is TitleBar) {
                    continue
                }
                mAnimViews[i].alpha = alpha
            }
        } else {
            magicalView?.setBackgroundAlpha(1.0f)
        }
    }

    private val isHasMagicalEffect: Boolean
        get() = !isInternalBottomPreview && config.isPreviewZoomEffect

    /**
     * 设置MagicalView监听器
     */
    private fun setMagicalViewAction() {
        magicalView?.setOnMojitoViewCallback(object : OnMagicalViewCallback {
            override fun onBeginBackMinAnim() {
                val currentHolder: BasePreviewHolder = viewPageAdapter?.getCurrentHolder(
                    viewPager2!!.currentItem) ?: return
                if (currentHolder.coverImageView?.visibility == View.GONE) {
                    currentHolder.coverImageView?.visibility = View.VISIBLE
                }
                if (currentHolder is PreviewVideoHolder) {
                    val videoHolder: PreviewVideoHolder = currentHolder as PreviewVideoHolder
                    if (videoHolder.ivPlayButton.visibility == View.VISIBLE) {
                        videoHolder.ivPlayButton.visibility = View.GONE
                    }
                }
            }

            override fun onBeginMagicalAnimComplete(
                mojitoView: MagicalView?,
                showImmediately: Boolean,
            ) {
                val currentHolder: BasePreviewHolder = viewPageAdapter?.getCurrentHolder(
                    viewPager2!!.currentItem) ?: return
                val media: LocalMedia? = mData!![viewPager2!!.currentItem]
                val realWidth: Int
                val realHeight: Int
                if (media?.isCut() == true && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
                    realWidth = media.cropImageWidth
                    realHeight = media.cropImageHeight
                } else {
                    realWidth = media?.width!!
                    realHeight = media.height
                }
                if (MediaUtils.isLongImage(realWidth, realHeight)) {
                    currentHolder.coverImageView?.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    currentHolder.coverImageView?.scaleType = ImageView.ScaleType.FIT_CENTER
                }
                if (currentHolder is PreviewVideoHolder) {
                    val videoHolder: PreviewVideoHolder = currentHolder as PreviewVideoHolder
                    if (config.isAutoVideoPlay) {
                        startAutoVideoPlay(viewPager2!!.currentItem)
                    } else {
                        if (videoHolder.ivPlayButton.visibility == View.GONE) {
                            videoHolder.ivPlayButton.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onBackgroundAlpha(alpha: Float) {
                for (i in mAnimViews.indices) {
                    if (mAnimViews[i] is TitleBar) {
                        continue
                    }
                    mAnimViews[i].alpha = alpha
                }
            }

            override fun onMagicalViewFinish() {
                if (isExternalPreview && isNormalDefaultEnter && isHasMagicalEffect) {
                    onExitPictureSelector()
                } else {
                    onBackCurrentFragment()
                }
            }

            override fun onBeginBackMinMagicalFinish(isResetSize: Boolean) {
                val itemViewParams: ViewParams =
                    BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)
                        ?: return
                val currentHolder: BasePreviewHolder = viewPageAdapter?.getCurrentHolder(
                    viewPager2!!.currentItem) ?: return
                currentHolder.coverImageView?.layoutParams?.width = itemViewParams.width
                currentHolder.coverImageView?.layoutParams?.height = itemViewParams.height
                currentHolder.coverImageView?.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
        outState.putLong(PictureConfig.EXTRA_CURRENT_BUCKET_ID, mBucketId)
        outState.putInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, curPosition)
        outState.putInt(PictureConfig.EXTRA_PREVIEW_CURRENT_ALBUM_TOTAL, totalNum)
        outState.putBoolean(PictureConfig.EXTRA_EXTERNAL_PREVIEW, isExternalPreview)
        outState.putBoolean(PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE, isDisplayDelete)
        outState.putBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA, isShowCamera)
        outState.putBoolean(PictureConfig.EXTRA_BOTTOM_PREVIEW, isInternalBottomPreview)
        outState.putString(PictureConfig.EXTRA_CURRENT_ALBUM_NAME, currentAlbum)
        SelectedManager.addSelectedPreviewResult(mData)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (isHasMagicalEffect) {
            // config.isPreviewZoomEffect模式下使用缩放动画
            return null
        }
        val windowAnimationStyle: PictureWindowAnimationStyle =
            PictureSelectionConfig.selectorStyle?.windowAnimationStyle!!
        return if (windowAnimationStyle.activityPreviewEnterAnimation != 0 && windowAnimationStyle.activityPreviewExitAnimation != 0) {
            val loadAnimation = AnimationUtils.loadAnimation(requireActivity(),
                if (enter) windowAnimationStyle.activityPreviewEnterAnimation else windowAnimationStyle.activityPreviewExitAnimation)
            if (enter) {
                onEnterFragment()
            } else {
                onExitFragment()
            }
            loadAnimation
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {
        if (PictureSelectionConfig.selectorStyle?.selectMainStyle
                ?.isPreviewSelectNumberStyle == true
        ) {
            if (PictureSelectionConfig.selectorStyle?.selectMainStyle?.isSelectNumberStyle == true) {
                for (index in 0 until SelectedManager.selectCount) {
                    val media: LocalMedia = SelectedManager.selectedResult[index]
                    media.num = index + 1
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isHasMagicalEffect && mData.size > curPosition) {
            val media: LocalMedia = mData[curPosition]
            val size = getRealSizeFromMedia(media)
            val viewParams: ViewParams? =
                BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)
            if (viewParams == null || size[0] == 0 || size[1] == 0) {
                magicalView?.setViewParams(0, 0, 0, 0, size[0], size[1])
                magicalView?.resetStartNormal(size[0], size[1], false)
            } else {
                magicalView?.setViewParams(viewParams.left,
                    viewParams.top,
                    viewParams.width,
                    viewParams.height,
                    size[0],
                    size[1])
                magicalView?.resetStart()
            }
        }
    }

    override fun onCreateLoader() {
        if (isExternalPreview) {
            return
        }
        if (PictureSelectionConfig.loaderFactory != null) {
            mLoader = PictureSelectionConfig.loaderFactory!!.onCreateLoader()
            if (mLoader == null) {
                throw NullPointerException("No available " + IBridgeMediaLoader::class.java + " loader found")
            }
        } else {
            mLoader = if (config.isPageStrategy) LocalMediaPageLoader() else LocalMediaLoader()
        }
        mLoader!!.initConfig(requireContext(), config)
    }

    /**
     * 加载更多
     */
    private fun loadMoreData() {
        mPage++
        if (PictureSelectionConfig.loaderDataEngine != null) {
            PictureSelectionConfig.loaderDataEngine!!.loadMoreMediaData(requireContext(),
                mBucketId,
                mPage,
                config.pageSize,
                config.pageSize,
                object : OnQueryDataResultListener<LocalMedia>() {
                    override fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                        handleMoreData(result, isHasMore)
                    }
                })
        } else {
            mLoader?.loadPageMediaData(mBucketId,
                mPage,
                config.pageSize,
                object : OnQueryDataResultListener<LocalMedia>() {
                    override fun onComplete(result: ArrayList<LocalMedia>, isHasMore: Boolean) {
                        handleMoreData(result, isHasMore)
                    }
                })
        }
    }

    private fun handleMoreData(result: Collection<LocalMedia>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(requireActivity())) {
            return
        }
        this@PictureSelectorPreviewFragment.isHasMore = isHasMore
        if (isHasMore) {
            if (result.isNotEmpty()) {
                val oldStartPosition = mData.size
                mData.addAll(result)
                val itemCount = mData.size
                viewPageAdapter?.notifyItemRangeChanged(oldStartPosition, itemCount)
            } else {
                loadMoreData()
            }
        }
    }

    private fun initComplete() {
        val selectMainStyle: SelectMainStyle? =
            PictureSelectionConfig.selectorStyle?.selectMainStyle
        if (selectMainStyle?.previewSelectBackground?.let { StyleUtils.checkStyleValidity(it) } == true) {
            selectMainStyle.previewSelectBackground.let { tvSelected!!.setBackgroundResource(it) }
        } else if (selectMainStyle?.selectBackground?.let { StyleUtils.checkStyleValidity(it) } == true) {
            selectMainStyle.selectBackground.let { tvSelected!!.setBackgroundResource(it) }
        }
        if (StyleUtils.checkTextValidity(selectMainStyle?.previewSelectText)) {
            tvSelectedWord?.text = (selectMainStyle?.previewSelectText)
        } else {
            tvSelectedWord!!.text = ""
        }
        if (selectMainStyle?.previewSelectTextSize?.let { StyleUtils.checkSizeValidity(it) } == true) {
            tvSelectedWord!!.textSize = selectMainStyle.previewSelectTextSize.toFloat()
        }
        if (selectMainStyle?.previewSelectTextColor?.let { StyleUtils.checkStyleValidity(it) } == true) {
            selectMainStyle.previewSelectTextColor.let { tvSelectedWord?.setTextColor(it) }
        }
        if (selectMainStyle?.previewSelectMarginRight?.let { StyleUtils.checkSizeValidity(it) } == true) {
            if (tvSelected!!.layoutParams is ConstraintLayout.LayoutParams) {
                if (tvSelected!!.layoutParams is ConstraintLayout.LayoutParams) {
                    val layoutParams = tvSelected!!.layoutParams as ConstraintLayout.LayoutParams
                    layoutParams.rightMargin = selectMainStyle.previewSelectMarginRight
                }
            } else if (tvSelected!!.layoutParams is RelativeLayout.LayoutParams) {
                val layoutParams = tvSelected!!.layoutParams as RelativeLayout.LayoutParams
                layoutParams.rightMargin = selectMainStyle.previewSelectMarginRight
            }
        }
        completeSelectView?.setCompleteSelectViewStyle()
        completeSelectView?.setSelectedChange(true)
        if (selectMainStyle?.isCompleteSelectRelativeTop == true) {
            if (completeSelectView?.layoutParams is ConstraintLayout.LayoutParams) {
                (completeSelectView
                    ?.layoutParams as ConstraintLayout.LayoutParams).topToTop = R.id.title_bar
                (completeSelectView
                    ?.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.title_bar
                if (config.isPreviewFullScreenMode) {
                    (completeSelectView!!
                        .layoutParams as ConstraintLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                }
            } else if (completeSelectView?.layoutParams is RelativeLayout.LayoutParams) {
                if (config.isPreviewFullScreenMode) {
                    (completeSelectView
                        ?.layoutParams as RelativeLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                }
            }
        }
        if (selectMainStyle?.isPreviewSelectRelativeBottom == true) {
            if (tvSelected!!.layoutParams is ConstraintLayout.LayoutParams) {
                (tvSelected!!
                    .layoutParams as ConstraintLayout.LayoutParams).topToTop =
                    R.id.bottom_nar_bar
                (tvSelected!!
                    .layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.bottom_nar_bar
                (tvSelectedWord
                    ?.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                    R.id.bottom_nar_bar
                (tvSelectedWord
                    ?.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.bottom_nar_bar
                (selectClickArea
                    ?.layoutParams as ConstraintLayout.LayoutParams).topToTop =
                    R.id.bottom_nar_bar
                (selectClickArea
                    ?.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.bottom_nar_bar
            }
        } else {
            if (config.isPreviewFullScreenMode) {
                if (tvSelectedWord!!.layoutParams is ConstraintLayout.LayoutParams) {
                    (tvSelectedWord!!
                        .layoutParams as ConstraintLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                } else if (tvSelectedWord!!.layoutParams is RelativeLayout.LayoutParams) {
                    (tvSelectedWord!!
                        .layoutParams as RelativeLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                }
            }
        }
        completeSelectView?.setOnClickListener(View.OnClickListener {
            var isComplete: Boolean? = null
            if (selectMainStyle != null) {
                if (selectMainStyle.isCompleteSelectRelativeTop && SelectedManager.selectCount == 0) {
                    isComplete = (confirmSelect(mData[viewPager2?.currentItem!!], false)
                            == SelectedManager.ADD_SUCCESS)
                } else {
                    isComplete = SelectedManager.selectCount > 0
                }
            }
            if (config.isEmptyResultReturn && SelectedManager.selectCount == 0) {
                onExitPictureSelector()
            } else {
                if (isComplete == true) {
                    dispatchTransformResult()
                }
            }
        })
    }

    private fun initTitleBar() {
        if (PictureSelectionConfig.selectorStyle?.titleBarStyle?.isHideTitleBar == true) {
            titleBar?.visibility = View.GONE
        }
        titleBar?.setTitleBarStyle()
        titleBar?.setOnTitleBarListener(object : TitleBar.OnTitleBarListener() {
            override fun onBackPressed() {
                if (isExternalPreview) {
                    if (config.isPreviewZoomEffect) {
                        magicalView?.backToMin()
                    } else {
                        handleExternalPreviewBack()
                    }
                } else {
                    if (!isInternalBottomPreview && config.isPreviewZoomEffect) {
                        magicalView?.backToMin()
                    } else {
                        onBackCurrentFragment()
                    }
                }
            }
        })
        titleBar?.setTitle((curPosition + 1).toString() + "/" + totalNum)
        titleBar?.imageDelete?.setOnClickListener(View.OnClickListener { deletePreview() })
        selectClickArea!!.setOnClickListener {
            if (isExternalPreview) {
                deletePreview()
            } else {
                val currentMedia: LocalMedia = mData[viewPager2!!.currentItem]
                val selectResultCode: Int =
                    confirmSelect(currentMedia, tvSelected!!.isSelected)
                if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                    tvSelected!!.startAnimation(AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.ps_anim_modal_in))
                }
            }
        }
        tvSelected!!.setOnClickListener { selectClickArea!!.performClick() }
    }

    private fun initPreviewSelectGallery(group: ViewGroup) {
        val selectMainStyle: SelectMainStyle? =
            PictureSelectionConfig.selectorStyle?.selectMainStyle
        if (selectMainStyle?.isPreviewDisplaySelectGallery == true) {
            mGalleryRecycle = RecyclerView(requireContext())
            if (StyleUtils.checkStyleValidity(selectMainStyle.adapterPreviewGalleryBackgroundResource)) {
                mGalleryRecycle!!.setBackgroundResource(selectMainStyle.adapterPreviewGalleryBackgroundResource)
            } else {
                mGalleryRecycle!!.setBackgroundResource(R.drawable.ps_preview_gallery_bg)
            }
            group.addView(mGalleryRecycle)
            val layoutParams = mGalleryRecycle!!.layoutParams
            if (layoutParams is ConstraintLayout.LayoutParams) {
                val params = layoutParams
                params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                params.bottomToTop = R.id.bottom_nar_bar
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
            val layoutManager: WrapContentLinearLayoutManager =
                object : WrapContentLinearLayoutManager(requireContext()) {
                    override fun smoothScrollToPosition(
                        recyclerView: RecyclerView,
                        state: RecyclerView.State?,
                        position: Int,
                    ) {
                        super.smoothScrollToPosition(recyclerView, state, position)
                        val smoothScroller: LinearSmoothScroller =
                            object : LinearSmoothScroller(recyclerView.context) {
                                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                                    return 300f / displayMetrics.densityDpi
                                }
                            }
                        smoothScroller.targetPosition = position
                        startSmoothScroll(smoothScroller)
                    }
                }
            val itemAnimator = mGalleryRecycle!!.itemAnimator
            if (itemAnimator != null) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            if (mGalleryRecycle!!.itemDecorationCount == 0) {
                mGalleryRecycle!!.addItemDecoration(HorizontalItemDecoration(Int.MAX_VALUE,
                    DensityUtil.dip2px(requireContext(), 6f)))
            }
            layoutManager.orientation = WrapContentLinearLayoutManager.HORIZONTAL
            mGalleryRecycle!!.layoutManager = layoutManager
            if (SelectedManager.selectCount > 0) {
                mGalleryRecycle!!.layoutAnimation = AnimationUtils
                    .loadLayoutAnimation(requireContext(), R.anim.ps_anim_layout_fall_enter)
            }
            mGalleryAdapter =
                PreviewGalleryAdapter(isInternalBottomPreview, SelectedManager.selectedResult)
            notifyGallerySelectMedia(mData[curPosition])
            mGalleryRecycle!!.adapter = mGalleryAdapter
            mGalleryAdapter!!.setItemClickListener(object :
                PreviewGalleryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, media: LocalMedia?, v: View?) {
                    val albumName: String =
                        if (TextUtils.isEmpty(config.defaultAlbumName)) getString(R.string.ps_camera_roll) else config.defaultAlbumName.toString()
                    if (isInternalBottomPreview || TextUtils.equals(currentAlbum, albumName)
                        || TextUtils.equals(media?.parentFolderName, currentAlbum)
                    ) {
                        val newPosition =
                            if (isInternalBottomPreview) position else if (isShowCamera) media?.position!! - 1 else media?.position
                        if (newPosition == viewPager2!!.currentItem && media?.isChecked == true) {
                            return
                        }
                        if (viewPager2!!.adapter != null) {
                            // 这里清空一下重新设置，发现频繁调用setCurrentItem会出现页面闪现之前图片
                            viewPager2!!.adapter = null
                            viewPager2!!.adapter = viewPageAdapter
                        }
                        if (newPosition != null) {
                            viewPager2!!.setCurrentItem(newPosition, false)
                        }
                        notifyGallerySelectMedia(media)
                        viewPager2!!.post {
                            if (config.isPreviewZoomEffect) {
                                if (newPosition != null) {
                                    viewPageAdapter?.setVideoPlayButtonUI(newPosition)
                                }
                            }
                        }
                    }
                }
            })
            if (SelectedManager.selectCount > 0) {
                mGalleryRecycle!!.visibility = View.VISIBLE
            } else {
                mGalleryRecycle!!.visibility = View.INVISIBLE
            }
            addAminViews(mGalleryRecycle)
            val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun isLongPressDragEnabled(): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ): Int {
                    viewHolder.itemView.alpha = 0.7f
                    return makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    try {
                        //得到item原来的position
                        val fromPosition: Int = viewHolder.absoluteAdapterPosition
                        //得到目标position
                        val toPosition: Int = target.absoluteAdapterPosition
                        if (fromPosition < toPosition) {
                            for (i in fromPosition until toPosition) {
                                Collections.swap(mGalleryAdapter!!.data, i, i + 1)
                                Collections.swap(SelectedManager.selectedResult, i, i + 1)
                                if (isInternalBottomPreview) {
                                    Collections.swap(mData, i, i + 1)
                                }
                            }
                        } else {
                            for (i in fromPosition downTo toPosition + 1) {
                                Collections.swap(mGalleryAdapter!!.data, i, i - 1)
                                Collections.swap(SelectedManager.selectedResult, i, i - 1)
                                if (isInternalBottomPreview) {
                                    Collections.swap(mData, i, i - 1)
                                }
                            }
                        }
                        mGalleryAdapter!!.notifyItemMoved(fromPosition, toPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return true
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean,
                ) {
                    if (needScaleBig) {
                        needScaleBig = false
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.0f, 1.1f),
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.0f, 1.1f))
                        animatorSet.duration = 50
                        animatorSet.interpolator = LinearInterpolator()
                        animatorSet.start()
                        animatorSet.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                needScaleSmall = true
                            }
                        })
                    }
                    super.onChildDraw(c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive)
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int,
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun getAnimationDuration(
                    recyclerView: RecyclerView,
                    animationType: Int,
                    animateDx: Float,
                    animateDy: Float,
                ): Long {
                    return super.getAnimationDuration(recyclerView,
                        animationType,
                        animateDx,
                        animateDy)
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ) {
                    viewHolder.itemView.alpha = 1.0f
                    if (needScaleSmall) {
                        needScaleSmall = false
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.1f, 1.0f),
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.1f, 1.0f))
                        animatorSet.interpolator = LinearInterpolator()
                        animatorSet.duration = 50
                        animatorSet.start()
                        animatorSet.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                needScaleBig = true
                            }
                        })
                    }
                    super.clearView(recyclerView, viewHolder)
                    mGalleryAdapter!!.notifyItemChanged(viewHolder.absoluteAdapterPosition)
                    if (isInternalBottomPreview) {
                        val position: Int = mGalleryAdapter!!.lastCheckPosition
                        if (viewPager2!!.currentItem != position && position != RecyclerView.NO_POSITION) {
                            if (viewPager2!!.adapter != null) {
                                viewPager2!!.adapter = null
                                viewPager2!!.adapter = viewPageAdapter
                            }
                            viewPager2!!.setCurrentItem(position, false)
                        }
                    }
                    if (PictureSelectionConfig.selectorStyle?.selectMainStyle?.isSelectNumberStyle == true
                    ) {
                        if (!ActivityCompatHelper.isDestroy(requireActivity())) {
                            val fragments: List<Fragment> =
                                requireActivity().supportFragmentManager.fragments
                            for (i in fragments.indices) {
                                val fragment = fragments[i]
                                if (fragment is PictureCommonFragment) {
                                    (fragment as PictureCommonFragment).sendChangeSubSelectPositionEvent(
                                        true)
                                }
                            }
                        }
                    }
                }
            })
            mItemTouchHelper.attachToRecyclerView(mGalleryRecycle)
            mGalleryAdapter?.setItemLongClickListener(object :
                PreviewGalleryAdapter.OnItemLongClickListener {
                override fun onItemLongClick(
                    holder: RecyclerView.ViewHolder?,
                    position: Int,
                    v: View?,
                ) {
                    val vibrator =
                        requireActivity().getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(50)
                    if (mGalleryAdapter?.itemCount != config.maxSelectNum) {
                        holder?.let { mItemTouchHelper.startDrag(it) }
                        return
                    }
                    if (holder?.layoutPosition != mGalleryAdapter?.itemCount!! - 1) {
                        holder?.let { mItemTouchHelper.startDrag(it) }
                    }
                }
            })
        }
    }

    /**
     * 刷新画廊数据选中状态
     *
     * @param currentMedia
     */
    private fun notifyGallerySelectMedia(currentMedia: LocalMedia?) {
        if (mGalleryAdapter != null && PictureSelectionConfig.selectorStyle
                ?.selectMainStyle?.isPreviewDisplaySelectGallery == true
        ) {
            currentMedia?.let { mGalleryAdapter?.isSelectMedia(it) }
        }
    }

    /**
     * 刷新画廊数据
     */
    private fun notifyPreviewGalleryData(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        if (mGalleryAdapter != null && PictureSelectionConfig.selectorStyle
                ?.selectMainStyle?.isPreviewDisplaySelectGallery == true
        ) {

            if (mGalleryRecycle!!.visibility == View.INVISIBLE) {
                mGalleryRecycle!!.visibility = View.VISIBLE
            }
            if (isAddRemove) {
                if (config.selectionMode == SelectModeConfig.SINGLE) {
                    mGalleryAdapter?.clear()
                }
                mGalleryAdapter?.addGalleryData(currentMedia!!)
                mGalleryRecycle!!.smoothScrollToPosition(mGalleryAdapter!!.itemCount - 1)
            } else {
                mGalleryAdapter?.removeGalleryData(currentMedia!!)
                if (SelectedManager.selectCount == 0) {
                    mGalleryRecycle!!.visibility = View.INVISIBLE
                }
            }
        }
    }

    /**
     * 调用了startPreview预览逻辑
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun deletePreview() {
        if (isDisplayDelete) {
            if (PictureSelectionConfig.onExternalPreviewEventListener != null) {
                PictureSelectionConfig.onExternalPreviewEventListener!!.onPreviewDelete(viewPager2!!.currentItem)
                val currentItem = viewPager2!!.currentItem
                mData.removeAt(currentItem)
                if (mData.size == 0) {
                    handleExternalPreviewBack()
                    return
                }
                titleBar?.setTitle(getString(R.string.ps_preview_image_num,
                    curPosition + 1, mData.size))
                totalNum = mData.size
                curPosition = currentItem
                if (viewPager2!!.adapter != null) {
                    viewPager2!!.adapter = null
                    viewPager2!!.adapter = viewPageAdapter
                }
                viewPager2!!.setCurrentItem(curPosition, false)
            }
        }
    }

    // Handling external preview return handling
    private fun handleExternalPreviewBack() {
        if (!ActivityCompatHelper.isDestroy(requireActivity())) {
            if (config.isPreviewFullScreenMode) {
                hideFullScreenStatusBar()
            }
            onExitPictureSelector()
        }
    }

    override fun onExitFragment() {
        if (config.isPreviewFullScreenMode) {
            hideFullScreenStatusBar()
        }
    }

    private fun initBottomNavBar() {
        bottomNarBar?.setBottomNavBarStyle()
        bottomNarBar?.setSelectedChange()
        bottomNarBar?.setOnBottomNavBarListener(object : BottomNavBar.OnBottomNavBarListener() {
            override fun onEditImage() {
                if (PictureSelectionConfig.onEditMediaEventListener != null) {
                    val media: LocalMedia = mData[viewPager2!!.currentItem]
                    PictureSelectionConfig.onEditMediaEventListener!!
                        .onStartMediaEdit(this@PictureSelectorPreviewFragment, media,
                            Crop.REQUEST_EDIT_CROP)
                }
            }

            override fun onCheckOriginalChange() {
                sendSelectedOriginalChangeEvent()
            }

            override fun onFirstCheckOriginalSelectedChange() {
                val currentItem = viewPager2!!.currentItem
                if (mData.size > currentItem) {
                    val media: LocalMedia = mData[currentItem]
                    media.let { confirmSelect(it, false) }
                }
            }
        })
    }

    /**
     * 外部预览的样式
     */
    private fun externalPreviewStyle() {
        titleBar?.imageDelete?.visibility = if (isDisplayDelete) View.VISIBLE else View.GONE
        tvSelected?.visibility = View.GONE
        bottomNarBar?.visibility = View.GONE
        completeSelectView?.visibility = View.GONE
    }

    private fun createAdapter(): PicturePreviewAdapter {
        return PicturePreviewAdapter()
    }

    val adapter: PicturePreviewAdapter?
        get() = viewPageAdapter

    private fun initViewPagerData(data: ArrayList<LocalMedia>) {
        viewPageAdapter = createAdapter()
        viewPageAdapter?.setData(data)
        viewPageAdapter?.setOnPreviewEventListener(MyOnPreviewEventListener())
        viewPager2!!.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager2!!.adapter = viewPageAdapter
        SelectedManager.clearPreviewData()
        if (data.size == 0 || curPosition > data.size) {
            onKeyBackFragmentFinish()
            return
        }
        val media: LocalMedia = data[curPosition]
        bottomNarBar?.isDisplayEditor(media.mimeType?.let { PictureMimeType.isHasVideo(it) } == true
                || PictureMimeType.isHasAudio(media.mimeType))
        tvSelected!!.isSelected =
            SelectedManager.selectedResult.contains(data[viewPager2!!.currentItem])
        viewPager2!!.registerOnPageChangeCallback(pageChangeCallback)
        viewPager2!!.setPageTransformer(MarginPageTransformer(DensityUtil.dip2px(requireContext(),
            3f)))
        viewPager2!!.setCurrentItem(curPosition, false)
        sendChangeSubSelectPositionEvent(false)
        notifySelectNumberStyle(data[curPosition])
        startZoomEffect(media)
    }

    /**
     * 启动预览缩放特效
     */
    private fun startZoomEffect(media: LocalMedia?) {
        if (isSaveInstanceState || isInternalBottomPreview) {
            return
        }
        if (PictureMimeType.isHasAudio(media?.mimeType)) {
            return
        }
        if (config.isPreviewZoomEffect) {
            viewPager2!!.post { viewPageAdapter?.setCoverScaleType(curPosition) }
            val size =
                getRealSizeFromMedia(media,
                    !media?.let { PictureMimeType.isHasHttp(it.availablePath) }!!)
            magicalView?.changeRealScreenHeight(size[0], size[1], false)
            val viewParams: ViewParams =
                BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)!!
            if (size[0] == 0 && size[1] == 0) {
                magicalView?.startNormal(size[0], size[1], false)
                magicalView?.setBackgroundAlpha(1.0f)
                for (i in mAnimViews.indices) {
                    mAnimViews[i].alpha = 1.0f
                }
            } else {
                magicalView?.setViewParams(viewParams.left,
                    viewParams.top,
                    viewParams.width,
                    viewParams.height,
                    size[0],
                    size[1])
                magicalView?.start(false)
            }
            ObjectAnimator.ofFloat(viewPager2, "alpha", 0.0f, 1.0f).setDuration(50).start()
        }
    }

    /**
     * ViewPageAdapter回调事件处理
     */
    inner class MyOnPreviewEventListener : BasePreviewHolder.OnPreviewEventListener {
        override fun onBackPressed() {
            if (config.isPreviewFullScreenMode) {
                previewFullScreenMode()
            } else {
                if (isExternalPreview) {
                    if (config.isPreviewZoomEffect) {
                        magicalView?.backToMin()
                    } else {
                        handleExternalPreviewBack()
                    }
                } else {
                    if (!isInternalBottomPreview && config.isPreviewZoomEffect) {
                        magicalView?.backToMin()
                    } else {
                        onBackCurrentFragment()
                    }
                }
            }
        }

        override fun onPreviewVideoTitle(videoName: String?) {
            if (TextUtils.isEmpty(videoName)) {
                titleBar?.setTitle((curPosition + 1).toString() + "/" + totalNum)
            } else {
                titleBar?.setTitle(videoName)
            }
        }

        override fun onLongPressDownload(media: LocalMedia?) {
            if (config.isHidePreviewDownload) {
                return
            }
            if (isExternalPreview) {
                onExternalLongPressDownload(media!!)
            }
        }
    }

    /**
     * 回到初始位置
     */
    private fun onKeyDownBackToMin() {
        if (!ActivityCompatHelper.isDestroy(requireActivity())) {
            if (isExternalPreview) {
                if (config.isPreviewZoomEffect) {
                    magicalView?.backToMin()
                } else {
                    onExitPictureSelector()
                }
            } else if (isInternalBottomPreview) {
                onBackCurrentFragment()
            } else if (config.isPreviewZoomEffect) {
                magicalView?.backToMin()
            } else {
                onBackCurrentFragment()
            }
        }
    }

    /**
     * 预览全屏模式
     */
    private fun previewFullScreenMode() {
        if (isAnimationStart) {
            return
        }
        val isAnimInit = titleBar?.getTranslationY() == 0.0f
        val set = AnimatorSet()
        val titleBarForm: Float = if (isAnimInit) 0f else (-titleBar?.height!!).toFloat()
        val titleBarTo: Float = if (isAnimInit) (-titleBar?.height!!).toFloat() else 0f
        val alphaForm = if (isAnimInit) 1.0f else 0.0f
        val alphaTo = if (isAnimInit) 0.0f else 1.0f
        for (i in mAnimViews.indices) {
            val view = mAnimViews[i]
            set.playTogether(ObjectAnimator.ofFloat(view, "alpha", alphaForm, alphaTo))
            if (view is TitleBar) {
                set.playTogether(ObjectAnimator.ofFloat(view,
                    "translationY",
                    titleBarForm,
                    titleBarTo))
            }
        }
        set.duration = 350
        set.start()
        isAnimationStart = true
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isAnimationStart = false
            }
        })
        if (isAnimInit) {
            showFullScreenStatusBar()
        } else {
            hideFullScreenStatusBar()
        }
    }

    /**
     * 全屏模式
     */
    private fun showFullScreenStatusBar() {
        for (i in mAnimViews.indices) {
            mAnimViews[i].isEnabled = false
        }
        bottomNarBar?.editor?.isEnabled = false
    }

    /**
     * 隐藏全屏模式
     */
    private fun hideFullScreenStatusBar() {
        for (i in mAnimViews.indices) {
            mAnimViews[i].isEnabled = true
        }
        bottomNarBar?.editor?.isEnabled = true
    }

    /**
     * 外部预览长按下载
     *
     * @param media
     */
    private fun onExternalLongPressDownload(media: LocalMedia) {
        if (PictureSelectionConfig.onExternalPreviewEventListener != null) {
            if (!PictureSelectionConfig.onExternalPreviewEventListener!!.onLongPressDownload(media)) {
                val content: String
                if (PictureMimeType.isHasAudio(media.mimeType)
                    || PictureMimeType.isUrlHasAudio(media.availablePath)
                ) {
                    content = getString(R.string.ps_prompt_audio_content)
                } else if (media.mimeType?.let { PictureMimeType.isHasVideo(it) } == true
                    || PictureMimeType.isUrlHasVideo(media.availablePath)
                ) {
                    content = getString(R.string.ps_prompt_video_content)
                } else {
                    content = getString(R.string.ps_prompt_image_content)
                }
                val dialog: PictureCommonDialog = PictureCommonDialog.showDialog(context,
                    getString(R.string.ps_prompt),
                    content)
                dialog.setOnDialogEventListener(object : PictureCommonDialog.OnDialogEventListener {
                    override fun onConfirm() {
                        val path: String = media.availablePath
                        if (PictureMimeType.isHasHttp(path)) {
                            showLoading()
                        }
                        media.mimeType?.let {
                            DownloadFileUtils.saveLocalFile(requireContext(),
                                path,
                                it,
                                object : OnCallbackListener<String?> {
                                    override fun onCall(realPath: String?) {
                                        dismissLoading()
                                        if (TextUtils.isEmpty(realPath)) {
                                            val errorMsg: String = when {
                                                PictureMimeType.isHasAudio(media.mimeType) -> {
                                                    getString(R.string.ps_save_audio_error)
                                                }
                                                PictureMimeType.isHasVideo(media.mimeType!!) -> {
                                                    getString(R.string.ps_save_video_error)
                                                }
                                                else -> {
                                                    getString(R.string.ps_save_image_error)
                                                }
                                            }
                                            ToastUtils.showToast(requireContext(), errorMsg)
                                        } else {
                                            requireActivity().let { it1 ->
                                                realPath?.let { it2 ->
                                                    PictureMediaScannerConnection(it1,
                                                        it2)
                                                }
                                            }
                                            ToastUtils.showToast(requireContext(),
                                                getString(R.string.ps_save_success) + "\n" + realPath)
                                        }
                                    }
                                })
                        }
                    }
                })
            }
        }
    }

    private val pageChangeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            if (mData.size > position) {
                val currentMedia: LocalMedia =
                    if (positionOffsetPixels < screenWidth / 2) mData!![position] else mData!![position + 1]
                tvSelected!!.isSelected = isSelected(currentMedia)
                notifyGallerySelectMedia(currentMedia)
                notifySelectNumberStyle(currentMedia)
            }
        }

        override fun onPageSelected(position: Int) {
            curPosition = position
            titleBar!!.setTitle((curPosition + 1).toString() + "/" + totalNum)
            if (mData.size > position) {
                val currentMedia: LocalMedia = mData[position]
                notifySelectNumberStyle(currentMedia)
                if (isHasMagicalEffect) {
                    changeMagicalViewParams(position)
                }
                if (config.isPreviewZoomEffect) {
                    if (isInternalBottomPreview && config.isAutoVideoPlay) {
                        startAutoVideoPlay(position)
                    } else {
                        viewPageAdapter?.setVideoPlayButtonUI(position)
                    }
                } else {
                    if (config.isAutoVideoPlay) {
                        startAutoVideoPlay(position)
                    }
                }
                notifyGallerySelectMedia(currentMedia)
                bottomNarBar?.isDisplayEditor(currentMedia.mimeType?.let {
                    PictureMimeType.isHasVideo(it)
                } == true
                        || PictureMimeType.isHasAudio(currentMedia.mimeType))
                if (!isExternalPreview && !isInternalBottomPreview && !config.isOnlySandboxDir) {
                    if (config.isPageStrategy) {
                        if (isHasMore) {
                            if (position == viewPageAdapter?.itemCount!! - 1 - PictureConfig.MIN_PAGE_SIZE
                                || position == viewPageAdapter?.itemCount!! - 1
                            ) {
                                loadMoreData()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 自动播放视频
     *
     * @param position
     */
    private fun startAutoVideoPlay(position: Int) {
        viewPager2!!.post { viewPageAdapter?.startAutoVideoPlay(position) }
    }

    /**
     * 更新MagicalView ViewParams 参数
     *
     * @param position
     */
    private fun changeMagicalViewParams(position: Int) {
        val media: LocalMedia = mData[position]
        val size = getRealSizeFromMedia(media)
        setMagicalViewParams(size[0], size[1], position)
    }

    /**
     * setMagicalViewParams
     *
     * @param imageWidth
     * @param imageHeight
     * @param position
     */
    private fun setMagicalViewParams(imageWidth: Int, imageHeight: Int, position: Int) {
        magicalView?.changeRealScreenHeight(imageWidth, imageHeight, true)
        val viewParams: ViewParams =
            BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) position + 1 else position)!!
        if (imageWidth == 0 || imageHeight == 0) {
            magicalView?.setViewParams(0, 0, 0, 0, imageWidth, imageHeight)
        } else {
            magicalView?.setViewParams(viewParams.left,
                viewParams.top,
                viewParams.width,
                viewParams.height,
                imageWidth,
                imageHeight)
        }
    }

    /**
     * 获取Media的真实大小
     *
     * @param media
     */
    private fun getRealSizeFromMedia(media: LocalMedia?): IntArray {
        return getRealSizeFromMedia(media, false)
    }

    /**
     * 获取Media的真实大小
     *
     * @param media
     * @param resize
     */
    private fun getRealSizeFromMedia(media: LocalMedia?, resize: Boolean): IntArray {
        var realWidth: Int? = null
        var realHeight: Int? = null
        if (media != null) {
            if (MediaUtils.isLongImage(media.width, media.height)) {
                realWidth = screenWidth
                realHeight = screenHeight
            } else {
                realWidth = media.width
                realHeight = media.height
                if (resize) {
                    if (realWidth <= 0 || realHeight <= 0 || realWidth > realHeight) {
                        val extraInfo: MediaExtraInfo =
                            if (media.mimeType?.let { PictureMimeType.isHasVideo(it) } == true) {
                                MediaUtils.getVideoSize(requireContext(), media.availablePath)
                            } else {
                                MediaUtils.getImageSize(requireContext(), media.availablePath)
                            }
                        if (extraInfo.width > 0) {
                            realWidth = extraInfo.width
                            media.width = (realWidth)
                        }
                        if (extraInfo.height > 0) {
                            realHeight = extraInfo.height
                            media.height = (realHeight)
                        }
                    }
                }
            }
        }
        if (media != null) {
            if (media.isCut() && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
                realWidth = media.cropImageWidth
                realHeight = media.cropImageHeight
            }
        }
        return realWidth?.let { realHeight?.let { it1 -> intArrayOf(it, it1) } }!!
    }

    // Sort selections by number
    fun notifySelectNumberStyle(currentMedia: LocalMedia?) {
        if (PictureSelectionConfig.selectorStyle?.selectMainStyle?.isPreviewSelectNumberStyle == true
        ) {
            if (PictureSelectionConfig.selectorStyle?.selectMainStyle?.isSelectNumberStyle == true) {
                tvSelected!!.text = ""
                for (i in 0 until SelectedManager.selectCount) {
                    val media: LocalMedia = SelectedManager.selectedResult[i]
                    if (TextUtils.equals(media.path, currentMedia?.path)
                        || media.id == currentMedia?.id
                    ) {
                        currentMedia?.num = (media.num)
                        media.position = (currentMedia?.position!!)
                        tvSelected?.text = ValueOf.toString(currentMedia.num)
                    }
                }
            }
        }
    }

    /**
     * 当前图片是否选中
     *
     * @param media
     * @return
     */
    private fun isSelected(media: LocalMedia?): Boolean {
        return SelectedManager.selectedResult.contains(media)
    }

    override fun onEditMedia(data: Intent) {
        if (mData.size > viewPager2!!.currentItem) {
            val currentMedia: LocalMedia = mData[viewPager2!!.currentItem]
            val output: Uri = Crop.getOutput(data)!!
            currentMedia.cutPath = (output.path)
            currentMedia.cropImageWidth = (Crop.getOutputImageWidth(data))
            currentMedia.cropImageHeight = (Crop.getOutputImageHeight(data))
            currentMedia.cropOffsetX = (Crop.getOutputImageOffsetX(data))
            currentMedia.cropOffsetY = (Crop.getOutputImageOffsetY(data))
            currentMedia.cropResultAspectRatio = (Crop.getOutputCropAspectRatio(data))
            currentMedia.setCut(!TextUtils.isEmpty(currentMedia.cutPath))
            currentMedia.customData = (Crop.getOutputCustomExtraData(data))
            currentMedia.setEditorImage(currentMedia.isCut())
            currentMedia.sandboxPath = (currentMedia.cutPath)
            if (SelectedManager.selectedResult.contains(currentMedia)) {
                val exitsMedia: LocalMedia = currentMedia.compareLocalMedia!!
                exitsMedia.cutPath = (currentMedia.cutPath)
                exitsMedia.setCut(currentMedia.isCut())
                exitsMedia.setEditorImage(currentMedia.isEditorImage())
                exitsMedia.customData = (currentMedia.customData)
                exitsMedia.sandboxPath = (currentMedia.cutPath)
                exitsMedia.cropImageWidth = (Crop.getOutputImageWidth(data))
                exitsMedia.cropImageHeight = (Crop.getOutputImageHeight(data))
                exitsMedia.cropOffsetX = (Crop.getOutputImageOffsetX(data))
                exitsMedia.cropOffsetY = (Crop.getOutputImageOffsetY(data))
                exitsMedia.cropResultAspectRatio = (Crop.getOutputCropAspectRatio(data))
                sendFixedSelectedChangeEvent(currentMedia)
            } else {
                confirmSelect(currentMedia, false)
            }
            viewPageAdapter?.notifyItemChanged(viewPager2!!.currentItem)
            notifyGallerySelectMedia(currentMedia)
        }
    }

    override fun onDestroy() {
        viewPageAdapter?.destroy()
        if (viewPager2 != null) {
            viewPager2!!.unregisterOnPageChangeCallback(pageChangeCallback)
        }
        super.onDestroy()
    }

    companion object {
        val fragmentTag = PictureSelectorPreviewFragment::class.java.simpleName
            get() = field ?: " PictureSelectorPreviewFragment"

        fun newInstance(): PictureSelectorPreviewFragment {
            val fragment = PictureSelectorPreviewFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}
