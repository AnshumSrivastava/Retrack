package com.example.retrack.data.repository

import com.example.retrack.data.db.RetrackDbHelper

class UsageRepository(private val dbHelper: RetrackDbHelper) {

    fun getTodayTotalCount(): Int {
        return dbHelper.getTodayTotalCount()
    }

    fun getTodayPlatformCount(platform: String): Int {
        val today = RetrackDbHelper.getTodayString()
        return dbHelper.getPlatformCountForDate(today, platform)
    }

    fun getPlatformBreakdown(): Map<String, Int> {
        return dbHelper.getTodayPlatformBreakdown()
    }

    fun recordSwipe(platform: String, countIncrement: Int = 1) {
        dbHelper.incrementTodayCount(platform, countIncrement)
    }

    fun resetTodayQuota() {
        dbHelper.resetTodayQuota()
    }

    fun getStreakDays(): Int {
        return dbHelper.calculateStreakDays()
    }
}
