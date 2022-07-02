package com.example.selector.adapter.holder

import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import java.io.File

class PreviewVideoHolder(itemView: View) : BasePreviewHolder(itemView) {
    var ivPlayButton: ImageView
    var mPlayerView: StyledPlayerView
    var progress: ProgressBar
    override fun bindData(media: LocalMedia?, position: Int) {
        super.bindData(media, position)
        setScaleDisplaySize(media)
        ivPlayButton.setOnClickListener { startPlay() }
        itemView.setOnClickListener {
            if (mPreviewEventListener != null) {
                mPreviewEventListener!!.onBackPressed()
            }
        }
    }

    fun startPlay() {
        val player: Player = mPlayerView.getPlayer()
        if (player != null) {
            val path: String = media.getAvailablePath()
            progress.visibility = View.VISIBLE
            ivPlayButton.visibility = View.GONE
            mPreviewEventListener!!.onPreviewVideoTitle(media.getFileName())
            val mediaItem: MediaItem
            mediaItem = if (PictureMimeType.isContent(path)) {
                MediaItem.fromUri(Uri.parse(path))
            } else if (PictureMimeType.isHasHttp(path)) {
                MediaItem.fromUri(path)
            } else {
                MediaItem.fromUri(Uri.fromFile(File(path)))
            }
            player.setRepeatMode(if (config.isLoopAutoPlay) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    override fun setScaleDisplaySize(media: LocalMedia?) {
        super.setScaleDisplaySize(media)
        if (!config.isPreviewZoomEffect && screenWidth < screenHeight) {
            val playerLayoutParams = mPlayerView.getLayoutParams() as FrameLayout.LayoutParams
            playerLayoutParams.width = screenWidth
            playerLayoutParams.height = screenAppInHeight
            playerLayoutParams.gravity = Gravity.CENTER
        }
    }

    private val mPlayerListener: Player.Listener = object : Listener() {
        fun onPlayerError(error: PlaybackException) {
            playerDefaultUI()
        }

        fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                playerIngUI()
            } else if (playbackState == Player.STATE_BUFFERING) {
                progress.visibility = View.VISIBLE
            } else if (playbackState == Player.STATE_ENDED) {
                playerDefaultUI()
            }
        }
    }

    private fun playerDefaultUI() {
        ivPlayButton.visibility = View.VISIBLE
        progress.visibility = View.GONE
        coverImageView.setVisibility(View.VISIBLE)
        mPlayerView.setVisibility(View.GONE)
        if (mPreviewEventListener != null) {
            mPreviewEventListener!!.onPreviewVideoTitle(null)
        }
    }

    private fun playerIngUI() {
        if (progress.visibility == View.VISIBLE) {
            progress.visibility = View.GONE
        }
        if (ivPlayButton.visibility == View.VISIBLE) {
            ivPlayButton.visibility = View.GONE
        }
        if (coverImageView.getVisibility() === View.VISIBLE) {
            coverImageView.setVisibility(View.GONE)
        }
        if (mPlayerView.getVisibility() === View.GONE) {
            mPlayerView.setVisibility(View.VISIBLE)
        }
    }

    override fun onViewAttachedToWindow() {
        val player: Player = Builder(itemView.context).build()
        mPlayerView.setPlayer(player)
        player.addListener(mPlayerListener)
    }

    override fun onViewDetachedFromWindow() {
        val player: Player = mPlayerView.getPlayer()
        if (player != null) {
            player.removeListener(mPlayerListener)
            player.release()
            mPlayerView.setPlayer(null)
            playerDefaultUI()
        }
    }

    /**
     * 释放VideoView
     */
    fun releaseVideo() {
        val player: Player = mPlayerView.getPlayer()
        if (player != null) {
            player.removeListener(mPlayerListener)
            player.release()
        }
    }

    init {
        ivPlayButton = itemView.findViewById(R.id.iv_play_video)
        mPlayerView = itemView.findViewById(R.id.playerView)
        progress = itemView.findViewById(R.id.progress)
        mPlayerView.setUseController(false)
        val config: PictureSelectionConfig = PictureSelectionConfig.getInstance()
        ivPlayButton.visibility = if (config.isPreviewZoomEffect) View.GONE else View.VISIBLE
    }
}
