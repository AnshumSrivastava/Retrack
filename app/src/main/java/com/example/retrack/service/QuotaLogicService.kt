package com.example.retrack.service

import com.example.retrack.data.communicator.ILimiterCommunicator
import com.example.retrack.data.communicator.LimiterState

class QuotaLogicService(
    private val repositoryCommunicator: com.example.retrack.data.communicator.IRepositoryCommunicator,
    private val sessionSwipeTracker: SessionSwipeTracker
) {

    fun evaluateLimiterState(): LimiterState {
        val total = repositoryCommunicator.getTodayTotalCount()
        val limit = repositoryCommunicator.getDailyLimit()
        return if (total >= limit) LimiterState.NOT_ALLOWED else LimiterState.ALLOWED
    }

    fun recordVideoSwipe(platform: String, signature: String?): Pair<Int, Int> {
        val currentTotal = repositoryCommunicator.getTodayTotalCount()
        val result = sessionSwipeTracker.onViewerActive(platform, signature, currentTotal)

        if (result.totalIncrement > 0) {
            repositoryCommunicator.recordSwipe(platform, result.totalIncrement)
        }

        val updatedTotal = repositoryCommunicator.getTodayTotalCount()
        return Pair(result.sessionCount, updatedTotal)
    }

    fun getDailyTotalCount(): Int {
        return repositoryCommunicator.getTodayTotalCount()
    }

    fun getDailyLimit(): Int {
        return repositoryCommunicator.getDailyLimit()
    }

    fun handleExitHysteresisCheck(): Boolean {
        return sessionSwipeTracker.checkHysteresisExit()
    }

    fun endSession() {
        sessionSwipeTracker.endSession()
    }
}
