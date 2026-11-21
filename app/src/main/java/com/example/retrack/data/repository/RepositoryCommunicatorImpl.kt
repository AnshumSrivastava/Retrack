package com.example.retrack.data.repository

import com.example.retrack.data.communicator.IRepositoryCommunicator

class RepositoryCommunicatorImpl(
    private val usageRepository: UsageRepository,
    private val settingsRepository: SettingsRepository
) : IRepositoryCommunicator {

    override fun getTodayTotalCount(): Int {
        return usageRepository.getTodayTotalCount()
    }

    override fun getTodayPlatformCount(platform: String): Int {
        return usageRepository.getTodayPlatformCount(platform)
    }

    override fun getPlatformBreakdown(): Map<String, Int> {
        return usageRepository.getPlatformBreakdown()
    }

    override fun recordSwipe(platform: String, countIncrement: Int) {
        usageRepository.recordSwipe(platform, countIncrement)
    }

    override fun resetTodayQuota() {
        usageRepository.resetTodayQuota()
    }

    override fun getDailyLimit(): Int {
        return settingsRepository.getUserSettings().dailyLimit
    }

    override fun setDailyLimit(limit: Int) {
        settingsRepository.setDailyLimit(limit)
    }

    override fun getMotivationalMessage(): String {
        return settingsRepository.getUserSettings().motivationalMessage
    }

    override fun setMotivationalMessage(message: String) {
        settingsRepository.setMotivationalMessage(message)
    }

    override fun getHabits(): List<String> {
        return settingsRepository.getUserSettings().habits
    }

    override fun addHabit(habit: String) {
        settingsRepository.addHabit(habit)
    }

    override fun removeHabit(habit: String) {
        settingsRepository.removeHabit(habit)
    }

    override fun getStreakDays(): Int {
        return usageRepository.getStreakDays()
    }

    override fun isOverlayBadgeEnabled(): Boolean {
        return settingsRepository.getUserSettings().overlayBadgeEnabled
    }

    override fun setOverlayBadgeEnabled(enabled: Boolean) {
        settingsRepository.setOverlayBadgeEnabled(enabled)
    }
}
