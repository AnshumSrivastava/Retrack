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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class HabitReplacementModalView(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var modalView: View? = null

    fun show(motivationalMsg: String, habits: List<String>) {
        if (modalView != null) return

        val isAccessibilityContext = context is AccessibilityService
        val canDraw = isAccessibilityContext || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context))
        if (!canDraw) return

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#EE121212")) // Dark full-screen backdrop
            }
            background = bg
        }

        val titleTv = TextView(context).apply {
            text = "🛑 DAILY REELS/SHORTS LIMIT REACHED"
            setTextColor(Color.parseColor("#FF5252"))
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val msgTv = TextView(context).apply {
            text = motivationalMsg
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        val subTitleTv = TextView(context).apply {
            text = "IT'S TIME TO:"
            setTextColor(Color.parseColor("#FFD700"))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        container.addView(titleTv)
        container.addView(msgTv)
        container.addView(subTitleTv)

        val selectedHabit = habits.randomOrNull() ?: "Read a book"
        val habitChip = TextView(context).apply {
            text = "✨ $selectedHabit"
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)
            val chipBg = GradientDrawable().apply {
                setColor(Color.parseColor("#33FFFFFF"))
                cornerRadius = 32f
            }
            background = chipBg
        }
        container.addView(habitChip)

        val dismissBtn = Button(context).apply {
            text = "I'LL TAKE A BREAK"
            setPadding(32, 16, 32, 16)
            setOnClickListener {
                hide()
            }
        }
        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 64
        }
        container.addView(dismissBtn, btnParams)

        val layoutType = if (isAccessibilityContext) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager.addView(container, params)
            modalView = container
        } catch (e: Exception) {
            e.printStackTrace()
            modalView = null
        }
    }

    fun hide() {
        modalView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        modalView = null
    }
}
