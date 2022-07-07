package com.example.selector.adapter.holder

import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.mygallery.R
import com.example.selector.config.PictureMimeType
import com.example.selector.config.PictureSelectionConfig
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.luck.picture.lib.entity.LocalMedia
import java.io.File

class PreviewVideoHolder(itemView: View) : BasePreviewHolder(itemView) {
    var ivPlayButton: ImageView = itemView.findViewById(R.id.iv_play_video)
    var mPlayerView: StyledPlayerView = itemView.findViewById(R.id.playerView)
    var progress: ProgressBar? = null
    var videoPlayer: View? = null
    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        setScaleDisplaySize(media)
        ivPlayButton.setOnClickListener { startPlay() }
        itemView.setOnClickListener {
            if (mPreviewEventListener != null) {
                mPreviewEventListener!!.onBackPressed()
            }
        }
    }

    private fun startPlay() {
        val player: Player = mPlayerView.player!!
        val path: String = media?.availablePath!!
        progress?.visibility = View.VISIBLE
        ivPlayButton.visibility = View.GONE
        mPreviewEventListener!!.onPreviewVideoTitle(media?.fileName)
        val mediaItem: MediaItem = when {
            PictureMimeType.isContent(path) -> {
                MediaItem.fromUri(Uri.parse(path))
            }
            PictureMimeType.isHasHttp(path) -> {
                MediaItem.fromUri(path)
            }
            else -> {
                MediaItem.fromUri(Uri.fromFile(File(path)))
            }
        }
        player.repeatMode = if (config.isLoopAutoPlay) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun setScaleDisplaySize(media: LocalMedia) {
        media.let { super.setScaleDisplaySize(it) }
        if (!config.isPreviewZoomEffect && screenWidth < screenHeight) {
            val playerLayoutParams = mPlayerView.layoutParams as FrameLayout.LayoutParams
            playerLayoutParams.width = screenWidth
            playerLayoutParams.height = screenAppInHeight
            playerLayoutParams.gravity = Gravity.CENTER
        }
    }

    private val mPlayerListener: Player.Listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            playerDefaultUI()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    playerIngUI()
                }
                Player.STATE_BUFFERING -> {
                    progress?.visibility = View.VISIBLE
                }
                Player.STATE_ENDED -> {
                    playerDefaultUI()
                }
                Player.STATE_IDLE -> {
                    TODO()
                }
            }
        }
    }

    private fun playerDefaultUI() {
        ivPlayButton.visibility = View.VISIBLE
        progress?.visibility = View.GONE
        coverImageView?.visibility = View.VISIBLE
        mPlayerView.visibility = View.GONE
        if (mPreviewEventListener != null) {
            mPreviewEventListener!!.onPreviewVideoTitle(null)
        }
    }

    private fun playerIngUI() {
        if (progress?.visibility == View.VISIBLE) {
            progress?.visibility = View.GONE

        }
        if (ivPlayButton.visibility == View.VISIBLE) {
            ivPlayButton.visibility = View.GONE
        }
        if (coverImageView?.visibility == View.VISIBLE) {
            coverImageView?.visibility = View.GONE
        }
        if (mPlayerView.visibility == View.GONE) {
            mPlayerView.visibility = View.VISIBLE
        }
    }
    override fun onViewAttachedToWindow() {
        if (PictureSelectionConfig. videoPlayerEngine != null) {
            PictureSelectionConfig.videoPlayerEngine!!.onPlayerAttachedToWindow(videoPlayer)
            PictureSelectionConfig.videoPlayerEngine!!.addPlayListener(mPlayerListener)
        }
    }

    override fun onViewDetachedFromWindow() {
        val player: Player = mPlayerView.player!!
        player.removeListener(mPlayerListener)
        player.release()
        mPlayerView.player = null
        playerDefaultUI()
    }

    /**
     *
    freed VideoView
     */
    fun releaseVideo() {
        val player: Player? = mPlayerView.player
        if (player != null) {
            player.removeListener(mPlayerListener)
            player.release()
        }
    }

    init {
        progress = itemView.findViewById(R.id.progress)
        mPlayerView.useController = false
        val config: PictureSelectionConfig = PictureSelectionConfig.instance!!
        ivPlayButton.visibility = if (config.isPreviewZoomEffect) View.GONE else View.VISIBLE
    }
}
