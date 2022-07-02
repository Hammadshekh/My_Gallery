package com.example.ucrop

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.R
import com.example.ucrop.decoration.GridSpacingItemDecoration
import com.example.ucrop.model.AspectRatio
import com.example.ucrop.model.CustomIntentKey
import com.example.ucrop.statusbar.ImmersiveManager
import com.example.ucrop.utils.DensityUtil
import com.example.ucrop.utils.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashMap

class UCropMultipleActivity : AppCompatActivity(), UCropFragmentCallback {
    private var mToolbarTitle: String? = null
    private var mToolbarTitleSize = 0

    // Enables dynamic coloring
    private var mToolbarColor = 0
    private var mStatusBarColor = 0

    @DrawableRes
    private var mToolbarCancelDrawable = 0

    @DrawableRes
    private var mToolbarCropDrawable = 0
    private var mToolbarWidgetColor = 0
    private var mShowLoader = false
    private val fragments: MutableList<UCropFragment> = ArrayList()
    private var uCropCurrentFragment: UCropFragment? = null
    private var currentFragmentPosition = 0
    private var uCropSupportList: ArrayList<String>? = null
    private var uCropNotSupportList: ArrayList<String>? = null
    private val uCropTotalQueue = LinkedHashMap<String?, JSONObject?>()
    private var outputCropFileName: String? = null
    private var galleryAdapter: UCropGalleryAdapter? = null
    private var isForbidCropGifWebp = false
    private var aspectRatioList: ArrayList<AspectRatio>? = null
    private val filterSet = HashSet<String>()

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        setContentView(R.layout.ucrop_activity_multiple)
        setupViews(intent)
        initCropFragments()
    }

    private fun immersive() {
        val intent = intent
        val isDarkStatusBarBlack =
            intent.getBooleanExtra(UCrop.Options.EXTRA_DARK_STATUS_BAR_BLACK, false)
        mStatusBarColor = intent.getIntExtra(UCrop.Options.EXTRA_STATUS_BAR_COLOR,
            ContextCompat.getColor(this, R.color.ucrop_color_statusbar))
        ImmersiveManager.immersiveAboveAPI23(this,
            mStatusBarColor,
            mStatusBarColor,
            isDarkStatusBarBlack)
    }

    private fun initCropFragments() {
        val totalCropData = intent.extras!!.getStringArrayList(UCrop.EXTRA_CROP_TOTAL_DATA_SOURCE)
        require(!(totalCropData == null || totalCropData.size <= 0)) { "Missing required parameters, count cannot be less than 1" }
        uCropSupportList = ArrayList()
        uCropNotSupportList = ArrayList()
        for (i in totalCropData.indices) {
            val path = totalCropData[i]
            uCropTotalQueue[path] = JSONObject()
            val realPath =
                if (FileUtils.isContent(path)) FileUtils.getPath(this, Uri.parse(path)) else path
            val mimeType = getPathToMimeType(path)
            if (realPath?.let { FileUtils.isUrlHasVideo(it) } == true || FileUtils.isHasVideo(
                    mimeType) || FileUtils.isHasAudio(
                    mimeType)
            ) {
                // not crop type
                uCropNotSupportList!!.add(path)
            } else {
                uCropSupportList!!.add(path)
                val extras = intent.extras
                val inputUri =
                    if (FileUtils.isContent(path) || FileUtils.isHasHttp(path)) Uri.parse(path) else Uri.fromFile(
                        File(path))
                val postfix: String = FileUtils.getPostfixDefaultJPEG(this@UCropMultipleActivity,
                    isForbidCropGifWebp, inputUri)
                val fileName: String =
                    if (TextUtils.isEmpty(outputCropFileName)) FileUtils.getCreateFileName("CROP_")
                        .toString() + postfix else FileUtils.getCreateFileName()
                        .toString() + "_" + outputCropFileName
                val destinationUri = Uri.fromFile(File(
                    sandboxPathDir, fileName))
                extras!!.putParcelable(UCrop.EXTRA_INPUT_URI, inputUri)
                extras.putParcelable(UCrop.EXTRA_OUTPUT_URI, destinationUri)
                val aspectRatio: AspectRatio? =
                    if (aspectRatioList != null && aspectRatioList!!.size > i) aspectRatioList!![i] else null
                extras.putFloat(UCrop.EXTRA_ASPECT_RATIO_X,
                    (aspectRatio?.getAspectRatioX() ?: -1) as Float)
                extras.putFloat(UCrop.EXTRA_ASPECT_RATIO_Y,
                    (aspectRatio?.getAspectRatioY() ?: -1) as Float)
                val uCropFragment = UCropFragment.newInstance(extras)
                fragments.add(uCropFragment)
            }
        }
        require(uCropSupportList!!.size != 0) { "No clipping data sources are available" }
        setGalleryAdapter()
        val uCropFragment = fragments[cropSupportPosition]
        switchCropFragment(uCropFragment, cropSupportPosition)
        galleryAdapter?.setCurrentSelectPosition(cropSupportPosition)
    }


    private val cropSupportPosition: Int
        private get() {
            var position = 0
            val skipCropMimeType =
                intent.extras!!.getStringArrayList(UCrop.Options.EXTRA_SKIP_CROP_MIME_TYPE)
            if (skipCropMimeType != null && skipCropMimeType.size > 0) {
                position = -1
                filterSet.addAll(skipCropMimeType)
                for (i in uCropSupportList!!.indices) {
                    val path = uCropSupportList!![i]
                    val mimeType = getPathToMimeType(path)
                    position++
                    if (!filterSet.contains(mimeType)) {
                        break
                    }
                }
                if (position == -1 || position > fragments.size) {
                    position = 0
                }
            }
            return position
        }


    private fun getPathToMimeType(path: String): String {
        val mimeType: String? = if (FileUtils.isContent(path)) {
            FileUtils.getMimeTypeFromMediaContentUri(this, Uri.parse(path))
        } else {
            FileUtils.getMimeTypeFromMediaContentUri(this, Uri.fromFile(File(path)))
        }
        return mimeType!!
    }

    private fun switchCropFragment(targetFragment: UCropFragment, position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        if (!targetFragment.isAdded) {
            if (uCropCurrentFragment != null) {
                transaction.hide(uCropCurrentFragment!!)
            }
            transaction.add(R.id.fragment_container,
                targetFragment,
                UCropFragment.TAG.toString() + "-" + position)
        } else {
            transaction.hide(uCropCurrentFragment!!).show(targetFragment)
            targetFragment.fragmentReVisible()
        }
        currentFragmentPosition = position
        uCropCurrentFragment = targetFragment
        transaction.commitAllowingStateLoss()
    }

    private fun setGalleryAdapter() {
        val galleryRecycle = findViewById<RecyclerView>(R.id.recycler_gallery)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        galleryRecycle.layoutManager = layoutManager
        if (galleryRecycle.itemDecorationCount == 0) {
            galleryRecycle.addItemDecoration(GridSpacingItemDecoration(Int.MAX_VALUE,
                DensityUtil.dip2px(this, 6F), true))
        }
        val animation = AnimationUtils
            .loadLayoutAnimation(this, R.anim.ucrop_layout_animation_fall_down)
        galleryRecycle.layoutAnimation = animation
        val galleryBarBackground = intent.getIntExtra(UCrop.Options.EXTRA_GALLERY_BAR_BACKGROUND,
            R.drawable.ucrop_gallery_bg)
        galleryRecycle.setBackgroundResource(galleryBarBackground)
        galleryAdapter = UCropGalleryAdapter(uCropSupportList)
        galleryAdapter!!.setOnItemClickListener(object : UCropGalleryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View?) {
                val path = uCropSupportList!![position]
                val mimeType = getPathToMimeType(path)
                if (filterSet.contains(mimeType)) {
                    Toast.makeText(applicationContext,
                        getString(R.string.ucrop_not_crop), Toast.LENGTH_SHORT).show()
                    return
                }
                if (galleryAdapter?.getCurrentSelectPosition() === position) {
                    return
                }
                galleryAdapter!!.notifyItemChanged(galleryAdapter!!.getCurrentSelectPosition())
                galleryAdapter!!.setCurrentSelectPosition(position)
                galleryAdapter!!.notifyItemChanged(position)
                val uCropFragment = fragments[position]
                switchCropFragment(uCropFragment, position)
            }
        })
        galleryRecycle.adapter = galleryAdapter
    }

    private val sandboxPathDir: String
        get() {
            val customFile: File
            val outputDir = intent.getStringExtra(UCrop.Options.EXTRA_CROP_OUTPUT_DIR)
            customFile = if (outputDir == null || "" == outputDir) {
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath, "Sandbox")
            } else {
                File(outputDir)
            }
            if (!customFile.exists()) {
                customFile.mkdirs()
            }
            return customFile.absolutePath + File.separator
        }

    private fun setupViews(intent: Intent) {
        aspectRatioList =
            getIntent().getParcelableArrayListExtra<AspectRatio>(UCrop.Options.EXTRA_MULTIPLE_ASPECT_RATIO)
        isForbidCropGifWebp =
            intent.getBooleanExtra(UCrop.Options.EXTRA_CROP_FORBID_GIF_WEBP, false)
        outputCropFileName = intent.getStringExtra(UCrop.Options.EXTRA_CROP_OUTPUT_FILE_NAME)
        mStatusBarColor = intent.getIntExtra(UCrop.Options.EXTRA_STATUS_BAR_COLOR,
            ContextCompat.getColor(this, R.color.ucrop_color_statusbar))
        mToolbarColor = intent.getIntExtra(UCrop.Options.EXTRA_TOOL_BAR_COLOR,
            ContextCompat.getColor(this, R.color.ucrop_color_toolbar))
        mToolbarWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_COLOR_TOOLBAR,
            ContextCompat.getColor(this, R.color.ucrop_color_toolbar_widget))
        mToolbarCancelDrawable =
            intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE,
                R.drawable.ucrop_ic_cross)
        mToolbarCropDrawable = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CROP_DRAWABLE,
            R.drawable.ucrop_ic_done)
        mToolbarTitle = intent.getStringExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_TOOLBAR)
        mToolbarTitleSize =
            intent.getIntExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_SIZE_TOOLBAR, 18)
        mToolbarTitle =
            if (mToolbarTitle != null) mToolbarTitle else resources.getString(R.string.ucrop_label_edit_photo)
        ColorFilter()
    }

    private fun setupAppBar() {
        setStatusBarColor(mStatusBarColor)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // Set all of the Toolbar coloring
        toolbar.setBackgroundColor(mToolbarColor)
        toolbar.setTitleTextColor(mToolbarWidgetColor)
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.setTextColor(mToolbarWidgetColor)
        toolbarTitle.text = mToolbarTitle
        toolbarTitle.textSize = mToolbarTitleSize.toFloat()

        // Color buttons inside the Toolbar
        val stateButtonDrawable = AppCompatResources.getDrawable(this, mToolbarCancelDrawable)!!
            .mutate()
        val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            mToolbarWidgetColor,
            BlendModeCompat.SRC_ATOP)
        stateButtonDrawable.colorFilter = colorFilter
        toolbar.navigationIcon = stateButtonDrawable
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(@ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = color
            }
        }
    }

    override fun loadingProgress(showLoader: Boolean) {
        mShowLoader = showLoader
        supportInvalidateOptionsMenu()
    }

    override fun onCropFinish(result: UCropFragment.UCropResult?) {
        when (result?.mResultCode) {
            RESULT_OK -> {
                val realPosition = currentFragmentPosition + uCropNotSupportList!!.size
                val realTotalSize = uCropNotSupportList!!.size + uCropSupportList!!.size - 1
                mergeCropResult(result.mResultData)
                if (realPosition == realTotalSize) {
                    onCropCompleteFinish()
                } else {
                    var nextFragmentPosition = currentFragmentPosition + 1
                    var path = uCropSupportList!![nextFragmentPosition]
                    var mimeType = getPathToMimeType(path)
                    var isCropCompleteFinish = false
                    while (filterSet.contains(mimeType)) {
                        if (nextFragmentPosition == realTotalSize) {
                            isCropCompleteFinish = true
                            break
                        } else {
                            nextFragmentPosition += 1
                            path = uCropSupportList!![nextFragmentPosition]
                            mimeType = getPathToMimeType(path)
                        }
                    }
                    if (isCropCompleteFinish) {
                        onCropCompleteFinish()
                    } else {
                        val uCropFragment = fragments[nextFragmentPosition]
                        switchCropFragment(uCropFragment, nextFragmentPosition)
                        galleryAdapter!!.notifyItemChanged(galleryAdapter!!.getCurrentSelectPosition())
                        galleryAdapter!!.setCurrentSelectPosition(nextFragmentPosition)
                        galleryAdapter!!.notifyItemChanged(galleryAdapter!!.getCurrentSelectPosition())
                    }
                }
            }
            UCrop.RESULT_ERROR -> handleCropError(result.mResultData)
        }
    }


    private fun onCropCompleteFinish() {
        val array = JSONArray()
        /*    for ((_, object) in uCropTotalQueue) {
                array.put(`object`)
            }*/
        val intent = Intent()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, array.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * merge crop result
     *
     * @param intent*/


    private fun mergeCropResult(intent: Intent) {
        try {
            val key = intent.getStringExtra(UCrop.EXTRA_CROP_INPUT_ORIGINAL)
            val uCropObject = uCropTotalQueue[key]
            val output = UCrop.getOutput(intent)
            uCropObject!!.put(CustomIntentKey.EXTRA_OUT_PUT_PATH,
                if (output != null) output.path else "")
            uCropObject.put(CustomIntentKey.EXTRA_IMAGE_WIDTH, UCrop.getOutputImageWidth(intent))
            uCropObject.put(CustomIntentKey.EXTRA_IMAGE_HEIGHT, UCrop.getOutputImageHeight(intent))
            uCropObject.put(CustomIntentKey.EXTRA_OFFSET_X, UCrop.getOutputImageOffsetX(intent))
            uCropObject.put(CustomIntentKey.EXTRA_OFFSET_Y, UCrop.getOutputImageOffsetY(intent))
            uCropObject.put(CustomIntentKey.EXTRA_ASPECT_RATIO,
                UCrop.getOutputCropAspectRatio(intent))
            uCropTotalQueue[key] = uCropObject
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Toast.makeText(this@UCropMultipleActivity, cropError.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@UCropMultipleActivity, "Unexpected error", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        UCropDevelopConfig.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ucrop_menu_activity, menu)

        // Change crop & loader menu icons color to match the rest of the UI colors
        val menuItemLoader = menu.findItem(R.id.menu_loader)
        val menuItemLoaderIcon = menuItemLoader.icon
        if (menuItemLoaderIcon != null) {
            try {
                menuItemLoaderIcon.mutate()
                val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    mToolbarWidgetColor,
                    BlendModeCompat.SRC_ATOP)
                menuItemLoaderIcon.colorFilter = colorFilter
                menuItemLoader.icon = menuItemLoaderIcon
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            (menuItemLoader.icon as Animatable).start()
        }
        val menuItemCrop = menu.findItem(R.id.menu_crop)
        val menuItemCropIcon = ContextCompat.getDrawable(this, mToolbarCropDrawable)
        if (menuItemCropIcon != null) {
            menuItemCropIcon.mutate()
            val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                mToolbarWidgetColor,
                BlendModeCompat.SRC_ATOP)
            menuItemCropIcon.colorFilter = colorFilter
            menuItemCrop.icon = menuItemCropIcon
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_crop).isVisible = !mShowLoader
        menu.findItem(R.id.menu_loader).isVisible = mShowLoader
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_crop) {
            if (uCropCurrentFragment != null && uCropCurrentFragment!!.isAdded) {
                uCropCurrentFragment!!.cropAndSaveImage()
            }
        } else if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
