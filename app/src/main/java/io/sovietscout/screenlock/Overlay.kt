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

        Log.v(Constants.TAG, "Overlay initialised")
    }

    fun start() {
        try {
            if ((mView.windowToken == null) && (mView.parent == null)) {
                mWindowManager.addView(mView, mWMLPUtils.getLP())

                alertDialog.setOnShowListener { lockBtnFullAlpha() }
                alertDialog.setOnDismissListener { if (isLocked) lockBtnFadeOut() }

                lockBtn.setOnClickListener { toggleLock() }
                lockBtn.setOnLongClickListener {
                    alertDialog.show()
                    return@setOnLongClickListener true
                }

                mView.setOnClickListener { if (isLocked) lockBtnFadeIn() }

                Log.v(Constants.TAG, "Overlay started")
            }
        } catch (exception: Exception) { Log.e(Constants.TAG, "Exception at Overlay#start()", exception) }
    }

    fun stop() {
        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)

            // (mView.parent as ViewGroup).removeAllViews() // Impossible cast
            mWindowManager.removeView(mView)
            mView.invalidate()

            Log.v(Constants.TAG, "Overlay stopped")
        } catch (exception: Exception) { Log.e("Screen Lock", "Exception at Overlay#stop()", exception) }
    }

    private fun toggleLock() {
        // Toggle lock state

        if (mWMLPUtils.lockState == Constants.SCREEN_STATE_UNLOCKED) {
            // Lock state
            mWMLPUtils.lockState = Constants.SCREEN_STATE_LOCKED
            updateParams()

            lockBtn.icon = context.getDrawable(R.drawable.ic_lock)
            lockBtnFadeOut()
        } else {
            // Unlock state
            mWMLPUtils.lockState = Constants.SCREEN_STATE_UNLOCKED
            updateParams()

            lockBtn.icon = context.getDrawable(R.drawable.ic_unlock)
            lockBtnFullAlpha()
        }

        Log.v(Constants.TAG, "Lock state toggled: ${if (isLocked) "Locked" else "Unlocked"}")
    }

    private fun updateParams() = mWindowManager.updateViewLayout(mView, mWMLPUtils.getLP())

    private fun updateSettings() {
        mWMLPUtils.updateSettings()
        updateParams()
    }







    private fun lockBtnFadeOut() {
        // Fade out animation
        lockBtn.animate().apply {
            interpolator = LinearInterpolator()
            duration = 200      // 0.2 seconds
            startDelay = 2000   // 2 seconds
            alpha(0f)
            withEndAction { lockBtn.visibility = View.INVISIBLE }
            start()
        }
    }

    private fun lockBtnFadeIn() {
        // Fade in animation
        lockBtnFullAlpha()
        lockBtn.animate().apply {
            startDelay = 0
            withEndAction { lockBtnFadeOut() }
            start()
        }
    }

    private fun lockBtnFullAlpha() {
        lockBtn.apply {
            visibility = View.VISIBLE
            clearAnimation()
            animate()?.cancel()
            alpha = 1f
        }
    }
}