package io.sovietscout.screenlock

import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.sovietscout.screenlock.service.ForegroundService
import io.sovietscout.screenlock.ui.MainActivity


object AppUtils {

    fun startForegroundService(context: Context) = context.startForegroundService(
        Intent(context, ForegroundService::class.java))
    fun stopForegroundService(context: Context) = context.stopService(
        Intent(context, ForegroundService::class.java))

    fun openMainActivity(context: Context) {
        // Will take 5 seconds to open from service after home button pressed

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun canDrawOverlays(context: Context) = Settings.canDrawOverlays(context)

    inline fun <reified T> T.TAG(): String = T::class.java.simpleName

}