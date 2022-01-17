package io.sovietscout.screenlock.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.sovietscout.screenlock.Constants
import io.sovietscout.screenlock.Overlay
import io.sovietscout.screenlock.R
import org.greenrobot.eventbus.EventBus

private const val notificationChannelID = "screen_lock_notification_channel"

class ForegroundService : Service() {
    private lateinit var overlay: Overlay

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        IS_SERVICE_RUNNING = true
        overlay = Overlay(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If intent action matches, remove notification and kill service
        if ((intent != null) && (intent.action == "ACTION_STOP_SERVICE")) {
            stopForeground(true)
            stopSelf()

            return START_NOT_STICKY
        }

        createNotificationChannel()

        val stopSelf = Intent(this, ForegroundService::class.java).apply { action = "ACTION_STOP_SERVICE" }
        val pStopSelf = PendingIntent.getForegroundService(
            this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle(resources.getString(R.string.notification_title))
            .setContentText(resources.getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pStopSelf)
            .build()

        startForeground(1, notification)
        overlay.start()

        Log.v(Constants.TAG, "Notification shown")

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        overlay.stop()
        IS_SERVICE_RUNNING = false

        EventBus.getDefault().post("The end is nigh. Close ye gates!")
        Log.v(Constants.TAG, "Service stopped")

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channelName = "Background Service"
        val notificationChannel = NotificationChannel(
            notificationChannelID, channelName, NotificationManager.IMPORTANCE_MIN)

        val notificationManger = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManger.createNotificationChannel(notificationChannel)

        Log.v(Constants.TAG, "Notification channel created")
    }

    companion object {
        var IS_SERVICE_RUNNING = false
    }
}