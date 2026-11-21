package com.example.retrack.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.retrack.data.db.RetrackDbHelper
import com.example.retrack.data.repository.RepositoryCommunicatorImpl
import com.example.retrack.data.repository.SettingsRepository
import com.example.retrack.data.repository.UsageRepository
import com.example.retrack.ui.overlay.HabitReplacementModalView
import com.example.retrack.ui.overlay.LiveCounterBadgeView

class RetrackAccessibilityService : AccessibilityService() {

    companion object {
        var instance: RetrackAccessibilityService? = null
            private set

        var communicatorInstance: CommunicatorImpl? = null
            private set
    }

    private lateinit var dbHelper: RetrackDbHelper
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var usageRepo: UsageRepository
    private lateinit var repoCommunicator: RepositoryCommunicatorImpl
    private lateinit var sessionTracker: SessionSwipeTracker
    private lateinit var quotaService: QuotaLogicService
    private lateinit var limiterService: LimiterLogicService

    lateinit var communicator: CommunicatorImpl
        private set

    private var liveBadgeView: LiveCounterBadgeView? = null
    private var habitModalView: HabitReplacementModalView? = null

    private val handler = Handler(Looper.getMainLooper())
    private val hysteresisRunnable = object : Runnable {
        override fun run() {
            val exited = quotaService.handleExitHysteresisCheck()
            if (exited) {
                liveBadgeView?.hide()
            }
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        dbHelper = RetrackDbHelper(applicationContext)
        settingsRepo = SettingsRepository(dbHelper)
        usageRepo = UsageRepository(dbHelper)
        repoCommunicator = RepositoryCommunicatorImpl(usageRepo, settingsRepo)

        sessionTracker = SessionSwipeTracker(viewerHysteresisMs = 2000L)
        quotaService = QuotaLogicService(repoCommunicator, sessionTracker)
        limiterService = LimiterLogicService(quotaService)

        communicator = CommunicatorImpl(repoCommunicator, quotaService)
        communicator.setAccessibilityServiceActive(true)
        communicatorInstance = communicator

        liveBadgeView = LiveCounterBadgeView(this)
        habitModalView = HabitReplacementModalView(this)

        handler.post(hysteresisRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        communicator.setAccessibilityServiceActive(false)
        handler.removeCallbacks(hysteresisRunnable)
        liveBadgeView?.hide()
        habitModalView?.hide()
        instance = null
        communicatorInstance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return
        val platform = ContentClassifier.detectPlatform(pkg) ?: return

        // Always classify using rootInActiveWindow (Rule 4)
        val rootNode = rootInActiveWindow ?: return

        val isViewer = ContentClassifier.isShortFormViewer(rootNode, pkg)
        if (isViewer) {
            // Check restriction when limit reached (dispatches GLOBAL_ACTION_HOME, Rule 6)
            val restricted = limiterService.checkAndEnforceRestriction(this)
            if (restricted) {
                val settings = repoCommunicator.getMotivationalMessage()
                val habits = repoCommunicator.getHabits()
                habitModalView?.show(settings, habits)
                liveBadgeView?.hide()
                return
            }

            // Extract non-volatile text signature
            val signature = ContentClassifier.extractVideoSignature(rootNode)
            val (sessionCount, totalCount) = quotaService.recordVideoSwipe(platform, signature)

            // Show live counter badge overlay if enabled
            if (repoCommunicator.isOverlayBadgeEnabled()) {
                liveBadgeView?.showOrUpdate(totalCount, repoCommunicator.getDailyLimit())
            }
        }
    }

    override fun onInterrupt() {
        liveBadgeView?.hide()
        habitModalView?.hide()
    }
}
