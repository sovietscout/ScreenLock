package io.sovietscout.screenlock.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import io.sovietscout.screenlock.AppUtils
import io.sovietscout.screenlock.AppUtils.TAG
import io.sovietscout.screenlock.Constants
import io.sovietscout.screenlock.ui.StartServiceActivity

class QuickSettingsTileService: TileService() {

    override fun onClick() {
        super.onClick()

        val serviceIntent = Intent(this, StartServiceActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Constants.RUN_ON_RESUME, false)

        qsTile.state = when (ForegroundService.IS_SERVICE_RUNNING) {
            true -> Tile.STATE_INACTIVE
            false -> Tile.STATE_ACTIVE
        }

        startActivityAndCollapse(serviceIntent)

        qsTile.updateTile()
        Log.v(TAG(), "Tile clicked")
    }

    override fun onStartListening() {
        super.onStartListening()

        if (AppUtils.canDrawOverlays(this)) {
            qsTile.state = when (ForegroundService.IS_SERVICE_RUNNING) {
                true -> Tile.STATE_ACTIVE
                false -> Tile.STATE_INACTIVE
            }
        } else qsTile.state = Tile.STATE_UNAVAILABLE

        qsTile.updateTile()
        Log.v(TAG(), "Tile state set")
    }
}