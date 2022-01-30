package io.sovietscout.screenlock

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.SharedPreferences
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import io.sovietscout.screenlock.AppUtils.TAG


class Overlay(private val context: Context) {

    private val isLocked: Boolean
        get() = mWMLPUtils.lockState == Constants.SCREEN_STATE_LOCKED

    private val options = arrayOf(context.getString(R.string.buttonAD_wake_lock))
    private val optionValues = booleanArrayOf(false)

    private val mView: View
    private val mWindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val lockBtn: MaterialButton
    private val linearLayout: LinearLayout
    private val alertDialog: AlertDialog

    private val mWMLPUtils: WMLPUtils
    private val btnAnim = LockBtnAnim()
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val sharedPreferencesListener: SharedPreferences.OnSharedPreferenceChangeListener
            = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> updateSettings() }

    init {
        @SuppressLint("InflateParams")
        mView = layoutInflater.inflate(R.layout.lock_overlay, null)

        lockBtn = mView.findViewById(R.id.lockBtn)
        linearLayout = mView.findViewById(R.id.relativeLayout)

        mWMLPUtils = WMLPUtils(context, lockBtn, linearLayout)

        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.DialogDarkStyle))

        builder.setTitle(R.string.buttonAD_title)
        builder.setPositiveButton(R.string.buttonAD_pos
        ) { _, _ -> }
        builder.setNegativeButton(R.string.buttonAD_neg
        ) { _, _ -> AppUtils.stopForegroundService(context) }
        builder.setNeutralButton(R.string.buttonAD_neu
        ) { _, _ -> AppUtils.openMainActivity(context) }

        builder.setMultiChoiceItems(options, optionValues
        ) { _, which, isChecked ->
            mWMLPUtils.keepScreenOn(isChecked)
            optionValues[which] = isChecked
            updateParams()
        }

        alertDialog = builder.create()
        alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

        Log.v(TAG(), "Overlay initialised")
    }

    fun start() {
        try {
            if ((mView.windowToken == null) && (mView.parent == null)) {
                mWindowManager.addView(mView, mWMLPUtils.getLP())

                alertDialog.setOnShowListener { btnAnim.fullAlpha() }
                alertDialog.setOnDismissListener { if (isLocked) btnAnim.fadeOut() }

                lockBtn.setOnClickListener { toggleLock() }
                lockBtn.setOnLongClickListener {
                    alertDialog.show()
                    return@setOnLongClickListener true
                }

                mView.setOnClickListener { if (isLocked) btnAnim.fadeIn() }

                Log.v(TAG(), "Overlay started")
            }
        } catch (exception: Exception) { Log.e(TAG(), "Exception at Overlay#start()", exception) }
    }

    fun stop() {
        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)

            // (mView.parent as ViewGroup).removeAllViews() // Impossible cast
            mWindowManager.removeView(mView)
            mView.invalidate()

            Log.v(TAG(), "Overlay stopped")
        } catch (exception: Exception) { Log.e(TAG(), "Exception at Overlay#stop()", exception) }
    }

    private fun toggleLock() {
        // Toggle lock state

        when (mWMLPUtils.lockState) {
            // Locked
            Constants.SCREEN_STATE_UNLOCKED -> {
                mWMLPUtils.lockState = Constants.SCREEN_STATE_LOCKED
                updateParams()

                lockBtn.icon = context.getDrawable(R.drawable.ic_lock)
                btnAnim.fadeOut()
            }
            // Unlocked
            Constants.SCREEN_STATE_LOCKED -> {
                mWMLPUtils.lockState = Constants.SCREEN_STATE_UNLOCKED
                updateParams()

                lockBtn.icon = context.getDrawable(R.drawable.ic_unlock)
                btnAnim.fullAlpha()
            }
        }

        Log.v(TAG(), "Lock state toggled: ${if (isLocked) "Locked" else "Unlocked"}")
    }

    private fun updateParams() = mWindowManager.updateViewLayout(mView, mWMLPUtils.getLP())

    private fun updateSettings() {
        mWMLPUtils.updateSettings()
        updateParams()
    }

    private inner class LockBtnAnim() {
        fun fadeOut() {
            lockBtn.animate().apply {
                interpolator = LinearInterpolator()
                duration = 200
                startDelay = 2000
                alpha(0f)
                withEndAction { lockBtn.visibility = View.INVISIBLE }
                start()
            }
        }

        fun fadeIn() {
            fullAlpha()
            lockBtn.animate().apply {
                startDelay = 0
                withEndAction { fadeOut() }
                start()
            }
        }

        fun fullAlpha() {
            lockBtn.apply {
                visibility = View.VISIBLE
                clearAnimation()
                animate()?.cancel()
                alpha = 1f
            }
        }
    }
}