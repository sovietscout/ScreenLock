package io.sovietscout.screenlock.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import io.sovietscout.screenlock.*
import io.sovietscout.screenlock.AppUtils.TAG
import io.sovietscout.screenlock.service.ForegroundService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {
    private lateinit var switchAB: SwitchMaterial
    private lateinit var dynamicShortcut: ShortcutInfoCompat

    private val intentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Result code returned is always 0. Manually checking permission status on return

        if (AppUtils.canDrawOverlays(this)) {
            AppUtils.startForegroundService(this)
            refreshPreferenceFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        // If we cannot draw overlays, show Alert Dialog asking for permissions
        if (!AppUtils.canDrawOverlays(this)) showDrawOverlaysAD()

        // Dynamic shortcuts
        if (ShortcutManagerCompat.getDynamicShortcuts(this).isEmpty()) generateShortcut()
        dynamicShortcut = ShortcutManagerCompat.getDynamicShortcuts(this)[0]

        supportFragmentManager.beginTransaction().replace(R.id.preferenceScreenFL, PreferenceScreenFragment()).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        switchAB = menu.findItem(R.id.switch_ab).actionView as SwitchMaterial

        switchAB.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                when (AppUtils.canDrawOverlays(this)) {
                    true -> AppUtils.startForegroundService(this)
                    false -> {
                        switchAB.isChecked = false
                        showDrawOverlaysAD()
                    }
                }
            } else AppUtils.stopForegroundService(this)
        }

        switchAB.isChecked = ForegroundService.IS_SERVICE_RUNNING

        return true
    }

    private fun showDrawOverlaysAD() = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogDarkStyle))
        .setCancelable(true)
        .setTitle(R.string.menuAD_title)
        .setMessage(R.string.menuAD_text)
        .setPositiveButton(R.string.menuAD_pos) { _, _ ->
            Toast.makeText(this, "Find 'Screen Lock' and enable 'Allow display over other apps'",
                Toast.LENGTH_LONG).show()

            permissionActivityLaunch()
        }
        .create()
        .show()

    fun permissionActivityLaunch() = intentLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))

    private fun generateShortcut() {
        val serviceIntent = Intent(this, StartServiceActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .putExtra(Constants.RUN_ON_RESUME, true)

        val shortcutInfo = ShortcutInfoCompat.Builder(this, "service-shortcut")
            .setIntent(serviceIntent)
            .setShortLabel("Toggle Overlay")
            .setLongLabel("Toggle Screen Lock Overlay")
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_lock))
            .build()

        ShortcutManagerCompat.setDynamicShortcuts(this, listOf(shortcutInfo))
        Log.v(TAG(), "Shortcut generated")
    }

    fun addShortcut() {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) ShortcutManagerCompat.requestPinShortcut(
            this, dynamicShortcut, null)
        else Toast.makeText(this, "Pinning shortcuts not supported", Toast.LENGTH_LONG).show()
    }

    fun refreshPreferenceFragment() =
        supportFragmentManager.beginTransaction().replace(R.id.preferenceScreenFL, PreferenceScreenFragment()).commit()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Boolean?) { if (this::switchAB.isInitialized) switchAB.isChecked = event!! }

    override fun onResume() {
        super.onResume()

        // Start overlay on app resume
        if (AppUtils.canDrawOverlays(this)
            and Settings(this).showOnAppStart
            and StartServiceActivity.RUN_ON_RESUME
            and !ForegroundService.IS_SERVICE_RUNNING
        ) AppUtils.startForegroundService(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}