package com.example.retrack.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.retrack.data.model.UserSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RetrackDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "retrack.db"
        const val DATABASE_VERSION = 3

        const val TABLE_USAGE_LOGS = "usage_logs"
        const val COLUMN_LOG_ID = "id"
        const val COLUMN_LOG_DATE = "date"
        const val COLUMN_LOG_PLATFORM = "platform"
        const val COLUMN_LOG_COUNT = "count"

        const val TABLE_SETTINGS = "user_settings"
        const val COLUMN_SETTING_KEY = "setting_key"
        const val COLUMN_SETTING_VALUE = "setting_value"

        const val KEY_DAILY_LIMIT = "daily_limit"
        const val KEY_MOTIVATIONAL_MSG = "motivational_message"
        const val KEY_HABITS = "habits"
        const val KEY_OVERLAY_ENABLED = "overlay_enabled"

        fun getTodayString(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsageLogs = """
            CREATE TABLE $TABLE_USAGE_LOGS (
                $COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LOG_DATE TEXT NOT NULL,
                $COLUMN_LOG_PLATFORM TEXT NOT NULL,
                $COLUMN_LOG_COUNT INTEGER NOT NULL DEFAULT 0,
                UNIQUE($COLUMN_LOG_DATE, $COLUMN_LOG_PLATFORM) ON CONFLICT REPLACE
            )
        """.trimIndent()

        val createSettings = """
            CREATE TABLE $TABLE_SETTINGS (
                $COLUMN_SETTING_KEY TEXT PRIMARY KEY,
                $COLUMN_SETTING_VALUE TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createUsageLogs)
        db.execSQL(createSettings)

        // Populate default settings
        insertOrUpdateSetting(db, KEY_DAILY_LIMIT, "20")
        insertOrUpdateSetting(db, KEY_MOTIVATIONAL_MSG, "IT'S TIME TO FOCUS ON WHAT MATTERS!")
        insertOrUpdateSetting(db, KEY_HABITS, "Read a book|Go for a walk|Drink water|Meditate")
        insertOrUpdateSetting(db, KEY_OVERLAY_ENABLED, "true")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USAGE_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    private fun insertOrUpdateSetting(db: SQLiteDatabase, key: String, value: String) {
        val cv = ContentValues().apply {
            put(COLUMN_SETTING_KEY, key)
            put(COLUMN_SETTING_VALUE, value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun resetTodayQuota() {
        val db = writableDatabase
        val count = db.delete(TABLE_USAGE_LOGS, null, null)
        android.util.Log.d("RetrackDb", "resetTodayQuota: deleted $count rows")
    }

    fun incrementTodayCount(platform: String, increment: Int = 1) {
        val today = getTodayString()
        val db = writableDatabase
        val current = getPlatformCountForDate(today, platform)
        val newCount = current + increment
        val cv = ContentValues().apply {
            put(COLUMN_LOG_DATE, today)
            put(COLUMN_LOG_PLATFORM, platform)
            put(COLUMN_LOG_COUNT, newCount)
        }
        db.insertWithOnConflict(TABLE_USAGE_LOGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getPlatformCountForDate(date: String, platform: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USAGE_LOGS,
            arrayOf(COLUMN_LOG_COUNT),
            "$COLUMN_LOG_DATE = ? AND $COLUMN_LOG_PLATFORM = ?",
            arrayOf(date, platform),
            null, null, null
        )
        var count = 0
        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow(COLUMN_LOG_COUNT))
            }
        }
        return count
    }

    fun getTodayTotalCount(): Int {
        val today = getTodayString()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_LOG_COUNT) FROM $TABLE_USAGE_LOGS WHERE $COLUMN_LOG_DATE = ?",
            arrayOf(today)
        )
        var total = 0
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                total = it.getInt(0)
            }
        }
        return total
    }

    fun getTodayPlatformBreakdown(): Map<String, Int> {
        val today = getTodayString()
        val db = readableDatabase
        val map = mutableMapOf<String, Int>()
        val cursor = db.query(
            TABLE_USAGE_LOGS,
            arrayOf(COLUMN_LOG_PLATFORM, COLUMN_LOG_COUNT),
            "$COLUMN_LOG_DATE = ?",
            arrayOf(today),
            null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                val platform = it.getString(it.getColumnIndexOrThrow(COLUMN_LOG_PLATFORM))
                val count = it.getInt(it.getColumnIndexOrThrow(COLUMN_LOG_COUNT))
                map[platform] = count
            }
        }
        return map
    }

    fun getSetting(key: String, defaultValue: String): String {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SETTINGS,
            arrayOf(COLUMN_SETTING_VALUE),
            "$COLUMN_SETTING_KEY = ?",
            arrayOf(key),
            null, null, null
        )
        var value = defaultValue
        cursor.use {
            if (it.moveToFirst()) {
                value = it.getString(it.getColumnIndexOrThrow(COLUMN_SETTING_VALUE))
            }
        }
        return value
    }

    fun saveSetting(key: String, value: String) {
        val db = writableDatabase
        insertOrUpdateSetting(db, key, value)
    }

    fun getUserSettings(): UserSettings {
        val limit = getSetting(KEY_DAILY_LIMIT, "20").toIntOrNull() ?: 20
        val msg = getSetting(KEY_MOTIVATIONAL_MSG, "IT'S TIME TO FOCUS ON WHAT MATTERS!")
        val habitsRaw = getSetting(KEY_HABITS, "Read a book|Go for a walk|Drink water|Meditate")
        val habits = if (habitsRaw.isBlank()) emptyList() else habitsRaw.split("|").filter { it.isNotBlank() }
        val overlay = getSetting(KEY_OVERLAY_ENABLED, "true").toBoolean()
        return UserSettings(
            dailyLimit = limit,
            motivationalMessage = msg,
            habits = habits,
            overlayBadgeEnabled = overlay
        )
    }

    fun calculateStreakDays(): Int {
        val db = readableDatabase
        val limit = getSetting(KEY_DAILY_LIMIT, "20").toIntOrNull() ?: 20
        val cursor = db.rawQuery(
            "SELECT $COLUMN_LOG_DATE, SUM($COLUMN_LOG_COUNT) as total FROM $TABLE_USAGE_LOGS GROUP BY $COLUMN_LOG_DATE ORDER BY $COLUMN_LOG_DATE DESC",
            null
        )
        val dateTotals = mutableMapOf<String, Int>()
        cursor.use {
            while (it.moveToNext()) {
                val date = it.getString(0)
                val total = it.getInt(1)
                dateTotals[date] = total
            }
        }

        if (dateTotals.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var streak = 0
        val calendar = java.util.Calendar.getInstance()
        val earliestDateStr = dateTotals.keys.minOrNull() ?: return 0

        var maxLoop = 365
        while (maxLoop > 0) {
            maxLoop--
            val dateStr = dateFormat.format(calendar.time)
            val total = dateTotals[dateStr]
            if (total != null) {
                if (total <= limit) {
                    streak++
                } else {
                    break
                }
            } else {
                if (dateStr == getTodayString()) {
                    streak++
                } else {
                    break
                }
            }
            if (dateStr <= earliestDateStr) {
                break
            }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }
}
