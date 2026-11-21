package com.example.retrack.data.communicator

interface IRepositoryCommunicator {
    fun getTodayTotalCount(): Int
    fun getTodayPlatformCount(platform: String): Int
    fun getPlatformBreakdown(): Map<String, Int>
    fun recordSwipe(platform: String, countIncrement: Int)
    fun resetTodayQuota()
    fun getDailyLimit(): Int
    fun setDailyLimit(limit: Int)
    fun getMotivationalMessage(): String
    fun setMotivationalMessage(message: String)
    fun getHabits(): List<String>
    fun addHabit(habit: String)
    fun removeHabit(habit: String)
    fun getStreakDays(): Int
    fun isOverlayBadgeEnabled(): Boolean
    fun setOverlayBadgeEnabled(enabled: Boolean)
}
