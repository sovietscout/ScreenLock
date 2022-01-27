package io.sovietscout.screenlock.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import io.sovietscout.screenlock.AppUtils
import io.sovietscout.screenlock.Constants
import io.sovietscout.screenlock.ui.StartServiceActivity

class QuickSettingsTileService: TileService() {

    override fun onClick() {
        super.onClick()

        val serviceIntent = Intent(this, StartServiceActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        startActivityAndCollapse(serviceIntent)

        qsTile.state = when (qsTile.state) {
            Tile.STATE_ACTIVE -> Tile.STATE_INACTIVE
            Tile.STATE_INACTIVE -> Tile.STATE_ACTIVE
            else -> Tile.STATE_UNAVAILABLE
        }

        qsTile.updateTile()
        Log.v(Constants.TAG, "Tile clicked")
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
        Log.v(Constants.TAG, "Tile state set")
    }

}