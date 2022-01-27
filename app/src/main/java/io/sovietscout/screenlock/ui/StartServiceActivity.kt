package io.sovietscout.screenlock.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.sovietscout.screenlock.AppUtils
import io.sovietscout.screenlock.service.ForegroundService

class StartServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!ForegroundService.IS_SERVICE_RUNNING)
            AppUtils.startForegroundService(this)
        else
            AppUtils.stopForegroundService(this)

        finish()
    }
}