package io.sovietscout.screenlock

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings(private val context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val buttonPosition
        get() = sharedPreferences.getString(context.getString(R.string.button_position_key), "TL")!!

    val buttonSize
        get() = sharedPreferences.getInt(context.getString(R.string.button_size_key), 48)

    val buttonEdgeOffset
        get() = sharedPreferences.getInt(context.getString(R.string.button_edge_offset), 0)


    val showOnAppStart
        get() = sharedPreferences.getBoolean(context.getString(R.string.behaviour_show_on_app_start), true)

    val keepScreenOn
        get() = sharedPreferences.getBoolean(context.getString(R.string.behaviour_keep_screen_on), true)
}