package com.example.retrack.data.communicator

interface IUsageDataCommunicator {
    fun getDailyTotalCount(): Int
    fun getDailyLimit(): Int
    fun setDailyLimit(limit: Int)
    fun getPlatformBreakdown(): Map<String, Int>
    fun getMotivationalMessage(): String
    fun setMotivationalMessage(message: String)
    fun getHabitsList(): List<String>
    fun addHabit(habit: String)
    fun removeHabit(habit: String)
    fun getStreakDays(): Int
    fun isOverlayBadgeEnabled(): Boolean
    fun setOverlayBadgeEnabled(enabled: Boolean)
    fun isAccessibilityServiceActive(): Boolean
    fun resetTodayQuota()
}
