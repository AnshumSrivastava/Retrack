package com.example.retrack.ui.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class LiveCounterBadgeView(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var badgeView: View? = null
    private var textView: TextView? = null

    fun showOrUpdate(currentCount: Int, dailyLimit: Int) {
        if (badgeView == null) {
            createBadgeView()
        }
        textView?.text = "📹 $currentCount / $dailyLimit watched"
    }

    private fun createBadgeView() {
        val isAccessibilityContext = context is AccessibilityService
        val canDraw = isAccessibilityContext || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context))
        if (!canDraw) return

        val tv = TextView(context).apply {
            text = "📹 0 / 20 watched"
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(32, 16, 32, 16)

            val backgroundDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#CC121212")) // Translucent dark
                cornerRadius = 48f
                setStroke(2, Color.parseColor("#44FFFFFF"))
            }
            background = backgroundDrawable
        }
        this.textView = tv

        val layoutType = if (isAccessibilityContext) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 32
            y = 120
        }

        try {
            windowManager.addView(tv, params)
            badgeView = tv
        } catch (e: Exception) {
            e.printStackTrace()
            badgeView = null
            textView = null
        }
    }

    fun hide() {
        badgeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        badgeView = null
        textView = null
    }
}
