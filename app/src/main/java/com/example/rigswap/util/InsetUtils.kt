package com.example.rigswap.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.rigswap.R

object InsetUtils {
    /**
     * Applies system status bar and display cutout (notch/camera hole) insets
     * to the top of the given view (e.g., top action bar), ensuring the header
     * and title text remain strictly inside the safe screen bounds on every phone.
     */
    fun applyTopSystemBarInsets(view: View) {
        val initialPaddingTop = (view.getTag(R.id.tag_initial_padding_top) as? Int) ?: run {
            val pad = view.paddingTop
            view.setTag(R.id.tag_initial_padding_top, pad)
            pad
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = initialPaddingTop + insets.top)
            windowInsets
        }

        if (view.isAttachedToWindow) {
            view.requestApplyInsets()
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    v.requestApplyInsets()
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }
}
