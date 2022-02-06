package io.sovietscout.screenlock

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.view.setMargins
import com.google.android.material.button.MaterialButton

class WMLPUtils(private val context: Context, private val lockBtn: MaterialButton, private val linearLayout: LinearLayout)
{
    private val lockedLayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT)

    private val unlockedLayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT)

    private var mLP: Array<WindowManager.LayoutParams> = arrayOf(lockedLayoutParams, unlockedLayoutParams)

    var lockState: Int = Constants.SCREEN_STATE_UNLOCKED
    private val settings = Settings(context)

    init {
        // Support Display Cutouts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mLP.forEach {
                it.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        updateSettings()
    }

    fun getLP(): WindowManager.LayoutParams =
        if (lockState == Constants.SCREEN_STATE_LOCKED) lockedLayoutParams
        else unlockedLayoutParams

    fun keepScreenOn(on: Boolean) {
        when (settings.keepScreenOn) {
            true -> {
                if (on) mLP.forEach { it.flags += WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON }
                else    mLP.forEach { it.flags -= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON }
            }

            false -> {
                if (on) lockedLayoutParams.flags += WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                else    lockedLayoutParams.flags -= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

                unlockedLayoutParams.flags -= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            }
        }
    }

    fun updateSettings() {
        buttonPosition(settings.buttonPosition)
        buttonSize(settings.buttonSize)
        buttonEdgeOffset(settings.buttonEdgeOffset)
    }

    private fun buttonPosition(position: String) {
        val gravity = when (position) {
            "TL" -> Gravity.TOP or Gravity.START
            "TR" -> Gravity.TOP or Gravity.END
            else -> Gravity.TOP
        }

        if (linearLayout.layoutParams != null) {
            val linearLayoutParams = linearLayout.layoutParams as WindowManager.LayoutParams
            linearLayoutParams.gravity = gravity
            linearLayout.layoutParams = linearLayoutParams
        }

        linearLayout.gravity = gravity
        // mLP.forEach {it.gravity = gravity }
        unlockedLayoutParams.gravity = gravity

        // TODO: Button jumps to top-left when button position is set to top-right on view layout update
    }

    private fun buttonSize(size: Int) {
        val sizeInPX = (size + 36).toPx()

        val layoutParams = lockBtn.layoutParams
        layoutParams.height = sizeInPX
        layoutParams.width = sizeInPX

        lockBtn.layoutParams = layoutParams
        lockBtn.iconSize = sizeInPX / 2
    }

    private fun buttonEdgeOffset(offset: Int) {
        val offsetInPX = offset.toPx()

        val layoutParams = lockBtn.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(offsetInPX)
        lockBtn.layoutParams = layoutParams

        /*
        mLP.forEach {
            it.x = offsetInPX
            it.y = offsetInPX
        }
         */
    }

    private fun Int.toPx() = (this * context.resources.displayMetrics.density + 0.5f).toInt()
}