package com.example.selector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mygallery.R
import com.example.selector.config.PictureSelectionConfig
import com.example.selector.config.SelectMimeType
import com.example.selector.utils.SdkVersionUtils
import com.example.ucrop.BuildConfig.LIBRARY_PACKAGE_NAME

class ForegroundService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        isForegroundServiceIng = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        isForegroundServiceIng = false
        stopForeground(true)
        super.onDestroy()
    }

    /**
     * 创建前台通知Notification
     *
     * @return
     */
    private fun createForegroundNotification(): Notification {
        var importance = 0
        if (SdkVersionUtils.isMaxN) {
            importance = NotificationManager.IMPORTANCE_HIGH
        }
        if (SdkVersionUtils.isO) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.lightColor = Color.BLUE
            channel.canBypassDnd()
            channel.setBypassDnd(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val config: PictureSelectionConfig = PictureSelectionConfig.instance!!
        val contentText =
            if (config.chooseMode == SelectMimeType.ofAudio()) getString(R.string.ps_use_sound) else getString(
                R.string.ps_use_camera)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ps_ic_trans_1px)
            .setContentTitle(appName)
            .setContentText(contentText)
            .setOngoing(true)
            .build()
    }

    private val appName: String
         get() {
            try {
                val packageInfo = packageManager.getPackageInfo(
                    packageName, 0)
                return packageInfo.applicationInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

    companion object {
        private val CHANNEL_ID: String =
            LIBRARY_PACKAGE_NAME + "." + ForegroundService::class.java.name
        private val CHANNEL_NAME: String = LIBRARY_PACKAGE_NAME
        private const val NOTIFICATION_ID = 1
        private var isForegroundServiceIng = false

        /**
         * start foreground service
         *
         * @param context
         */
        fun startForegroundService(context: Context) {
            try {
                if (!isForegroundServiceIng && PictureSelectionConfig.instance!!.isCameraForegroundService) {
                    val intent = Intent(context, ForegroundService::class.java)
                    if (SdkVersionUtils.isO) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * stop foreground service
         *
         * @param context
         */
        fun stopService(context: Context) {
            try {
                if (isForegroundServiceIng) {
                    val foregroundService = Intent(context, ForegroundService::class.java)
                    context.stopService(foregroundService)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
