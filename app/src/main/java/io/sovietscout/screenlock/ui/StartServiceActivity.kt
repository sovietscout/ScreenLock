package io.sovietscout.screenlock.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.sovietscout.screenlock.AppUtils
import io.sovietscout.screenlock.Constants
import io.sovietscout.screenlock.service.ForegroundService

class StartServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RUN_ON_RESUME = intent.getBooleanExtra(Constants.RUN_ON_RESUME, true)

        if (!ForegroundService.IS_SERVICE_RUNNING)
            AppUtils.startForegroundService(this)
        else
            AppUtils.stopForegroundService(this)

        finish()
    }

    companion object { var RUN_ON_RESUME = true }
}