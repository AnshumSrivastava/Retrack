package com.example.retrack.data.repository

import com.example.retrack.data.db.RetrackDbHelper
import com.example.retrack.data.model.UserSettings

class SettingsRepository(private val dbHelper: RetrackDbHelper) {

    fun getUserSettings(): UserSettings {
        return dbHelper.getUserSettings()
    }

    fun setDailyLimit(limit: Int) {
        dbHelper.saveSetting(RetrackDbHelper.KEY_DAILY_LIMIT, limit.toString())
    }

    fun setMotivationalMessage(message: String) {
        dbHelper.saveSetting(RetrackDbHelper.KEY_MOTIVATIONAL_MSG, message)
    }

    fun addHabit(habit: String) {
        val currentHabits = dbHelper.getUserSettings().habits.toMutableList()
        if (!currentHabits.contains(habit)) {
            currentHabits.add(habit)
            dbHelper.saveSetting(RetrackDbHelper.KEY_HABITS, currentHabits.joinToString("|"))
        }
    }

    fun removeHabit(habit: String) {
        val currentHabits = dbHelper.getUserSettings().habits.toMutableList()
        if (currentHabits.remove(habit)) {
            dbHelper.saveSetting(RetrackDbHelper.KEY_HABITS, currentHabits.joinToString("|"))
        }
    }

    fun setOverlayBadgeEnabled(enabled: Boolean) {
        dbHelper.saveSetting(RetrackDbHelper.KEY_OVERLAY_ENABLED, enabled.toString())
    }
}
