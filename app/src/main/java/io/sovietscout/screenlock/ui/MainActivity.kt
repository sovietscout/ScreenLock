package io.sovietscout.screenlock.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import io.sovietscout.screenlock.*
import io.sovietscout.screenlock.service.ForegroundService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {
    private lateinit var switchAB: SwitchMaterial
    private lateinit var shortcutService: ShortcutInfoCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forced dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        // If we cannot draw overlays, show Alert Dialog asking for permissions
        if (AppUtils.canDrawOverlays(this)) {
            if (Settings(this).showOnAppStart && !ForegroundService.IS_SERVICE_RUNNING)
                AppUtils.startForegroundService(this)
        } else showDrawOverlaysAD()

        // Dynamic shortcuts
        val dynamicShortcuts = ShortcutManagerCompat.getDynamicShortcuts(this)
        if (dynamicShortcuts.isEmpty()) generateShortcut()
        shortcutService = dynamicShortcuts[0]

        supportFragmentManager.beginTransaction().replace(R.id.preferenceScreenFL, PreferenceScreenFragment()).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem: MenuItem = menu!!.findItem(R.id.switch_ab)
        switchAB = menuItem.actionView as SwitchMaterial

        switchAB.isChecked = ForegroundService.IS_SERVICE_RUNNING
        switchAB.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                if (AppUtils.canDrawOverlays(this)) AppUtils.startForegroundService(this)
                else {
                    switchAB.isChecked = false
                    showDrawOverlaysAD()
                }
            } else AppUtils.stopForegroundService(this)
        }

        return true
    }

    private fun showDrawOverlaysAD() = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogDarkStyle))
        .setCancelable(true)
        .setTitle(R.string.menuAD_title)
        .setMessage(R.string.menuAD_text)
        .setPositiveButton(R.string.menuAD_pos) { _, _ ->
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            Toast.makeText(
                // Might also be called 'Appear on top'
                this, "Find 'Screen Lock' and enable 'Allow display over other apps'", Toast.LENGTH_LONG).show()
        }
        .create()
        .show()

    private fun generateShortcut() {
        val serviceIntent = Intent(this, StartServiceActivity::class.java)
            .setAction(Intent.ACTION_MAIN)

        val shortcutInfo = ShortcutInfoCompat.Builder(this, "service-shortcut")
            .setIntent(serviceIntent)
            .setShortLabel("Toggle Overlay")
            .setLongLabel("Toggle Screen Lock Overlay")
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_lock))
            .build()

        ShortcutManagerCompat.setDynamicShortcuts(this, listOf(shortcutInfo))
    }

    fun addShortcut() {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
            ShortcutManagerCompat.requestPinShortcut(this, shortcutService, null)
        } else Toast.makeText(this, "Pinning shortcuts not supported", Toast.LENGTH_LONG).show()
    }

    fun refreshPreferenceFragment() =
        supportFragmentManager.beginTransaction().replace(R.id.preferenceScreenFL, PreferenceScreenFragment()).commit()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String) { switchAB.isChecked = false }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}