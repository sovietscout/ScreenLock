package io.sovietscout.screenlock.ui

import android.os.Bundle
import androidx.preference.*
import io.sovietscout.screenlock.AppUtils
import io.sovietscout.screenlock.R


class PreferenceScreenFragment: PreferenceFragmentCompat() {
    private lateinit var mainActivity: MainActivity

    private lateinit var addShortcut: Preference
    private lateinit var resetToDefaults: Preference
    private lateinit var grantPermission: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)

        mainActivity = activity as MainActivity

        addShortcut = findPreference("add_shortcut_pref")!!
        resetToDefaults = findPreference("reset_to_defaults_pref")!!
        grantPermission = findPreference("permission_pref")!!

        if (!AppUtils.canDrawOverlays(this.context!!)) grantPermission.isEnabled = true
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference) {
            addShortcut -> { mainActivity.addShortcutToHomeScreen() }
            grantPermission -> { mainActivity.permissionActivityLaunch() }
            resetToDefaults -> {
                preferenceManager.sharedPreferences!!.edit().clear().apply()
                PreferenceManager.setDefaultValues(this.context!!, R.xml.prefs, true)

                mainActivity.refreshPreferenceFragment()
            }
        }

        return true
    }
}