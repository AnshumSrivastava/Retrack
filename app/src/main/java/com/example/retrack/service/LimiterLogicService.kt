package com.example.retrack.service

import android.accessibilityservice.AccessibilityService
import com.example.retrack.data.communicator.LimiterState

class LimiterLogicService(
    private val quotaLogicService: QuotaLogicService
) {

    /**
     * Called when a short-form video container is detected.
     * Evaluates LimiterState. If NOT_ALLOWED, performs GLOBAL_ACTION_HOME to prevent PiP floating window
     * and returns true to signal that modal overlay should be displayed.
     */
    fun checkAndEnforceRestriction(service: AccessibilityService): Boolean {
        val state = quotaLogicService.evaluateLimiterState()
        if (state == LimiterState.NOT_ALLOWED) {
            // Strictly dispatch GLOBAL_ACTION_HOME (never GLOBAL_ACTION_BACK to prevent PiP behavior)
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            return true
        }
        return false
    }
}
