package io.sovietscout.screenlock.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.sovietscout.screenlock.AppUtils
import io.sovietscout.screenlock.R


class PreferenceScreenFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

        // Add shortcut
        (findPreference<Preference>("add_shortcut_pref"))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                (activity as MainActivity).addShortcut()
                true
            }

        // Reset to defaults
        (findPreference<Preference>("reset_to_defaults_pref"))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                preferenceManager.sharedPreferences.edit().clear().apply()
                PreferenceManager.setDefaultValues(this.context, R.xml.prefs, true)

                (activity as MainActivity).refreshPreferenceFragment()
                true
            }

        // Permission
        val permissionPref = findPreference<Preference>("permission_pref")!!
        if (!AppUtils.canDrawOverlays(this.context!!)) permissionPref.isEnabled = true
        permissionPref.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                true
            }

        // Position
        val positionPref = findPreference<ListPreference>(resources.getString(R.string.button_position_key))!!
        positionPref.summary = positionPref.entry
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        val positionPref = findPreference<ListPreference>(resources.getString(R.string.button_position_key))!!
        positionPref.summary = positionPref.entry
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}