package com.example.retrack.service

import com.example.retrack.data.communicator.ILimiterCommunicator
import com.example.retrack.data.communicator.IRepositoryCommunicator
import com.example.retrack.data.communicator.IUsageDataCommunicator
import com.example.retrack.data.communicator.LimiterState

class CommunicatorImpl(
    private val repositoryCommunicator: IRepositoryCommunicator,
    private val quotaLogicService: QuotaLogicService
) : ILimiterCommunicator, IUsageDataCommunicator {

    private var accessibilityActive: Boolean = false

    fun setAccessibilityServiceActive(active: Boolean) {
        this.accessibilityActive = active
    }

    override fun evaluateLimiterState(): LimiterState {
        return quotaLogicService.evaluateLimiterState()
    }

    override fun recordVideoSwipe(platform: String, signature: String): Pair<Int, Int> {
        return quotaLogicService.recordVideoSwipe(platform, signature)
    }

    override fun getDailyTotalCount(): Int {
        return repositoryCommunicator.getTodayTotalCount()
    }

    override fun getDailyLimit(): Int {
        return repositoryCommunicator.getDailyLimit()
    }

    override fun setDailyLimit(limit: Int) {
        repositoryCommunicator.setDailyLimit(limit)
    }

    override fun getPlatformBreakdown(): Map<String, Int> {
        return repositoryCommunicator.getPlatformBreakdown()
    }

    override fun getMotivationalMessage(): String {
        return repositoryCommunicator.getMotivationalMessage()
    }

    override fun setMotivationalMessage(message: String) {
        repositoryCommunicator.setMotivationalMessage(message)
    }

    override fun getHabitsList(): List<String> {
        return repositoryCommunicator.getHabits()
    }

    override fun addHabit(habit: String) {
        repositoryCommunicator.addHabit(habit)
    }

    override fun removeHabit(habit: String) {
        repositoryCommunicator.removeHabit(habit)
    }

    override fun getStreakDays(): Int {
        return repositoryCommunicator.getStreakDays()
    }

    override fun isOverlayBadgeEnabled(): Boolean {
        return repositoryCommunicator.isOverlayBadgeEnabled()
    }

    override fun setOverlayBadgeEnabled(enabled: Boolean) {
        repositoryCommunicator.setOverlayBadgeEnabled(enabled)
    }

    override fun isAccessibilityServiceActive(): Boolean {
        return accessibilityActive
    }

    override fun resetTodayQuota() {
        repositoryCommunicator.resetTodayQuota()
    }
}
