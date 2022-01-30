package io.sovietscout.screenlock.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.sovietscout.screenlock.AppUtils.TAG
import io.sovietscout.screenlock.Overlay
import io.sovietscout.screenlock.R
import org.greenrobot.eventbus.EventBus

private const val notificationChannelID = "screen_lock_notification_channel"
private const val actionStopService = "ACTION_STOP_SERVICE"

class ForegroundService : Service() {
    private lateinit var overlay: Overlay

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        overlay = Overlay(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If intent action matches, remove notification and kill service
        if ((intent != null) && (intent.action == actionStopService)) {
            stopForeground(true)
            stopSelf()

            return START_NOT_STICKY
        }

        createNotificationChannel()

        val stopSelf = Intent(this, ForegroundService::class.java).apply { action = actionStopService }
        val pStopSelf = PendingIntent.getForegroundService(
            this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle(resources.getString(R.string.notification_title))
            .setContentText(resources.getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pStopSelf)
            .build()

        startForeground(1, notification)
        overlay.start()
        IS_SERVICE_RUNNING = true

        EventBus.getDefault().post(true)
        Log.v(TAG(), "Notification shown")

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        overlay.stop()
        IS_SERVICE_RUNNING = false

        EventBus.getDefault().post(false)
        Log.v(TAG(), "Service stopped")

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            notificationChannelID,
            "Foreground Service",
            NotificationManager.IMPORTANCE_MIN)

        val notificationManger = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManger.createNotificationChannel(notificationChannel)

        Log.v(TAG(), "Notification channel created")
    }

    companion object { var IS_SERVICE_RUNNING = false }
}