package com.example.retrack

import com.example.retrack.data.communicator.IRepositoryCommunicator
import com.example.retrack.data.communicator.LimiterState
import com.example.retrack.service.QuotaLogicService
import com.example.retrack.service.SessionSwipeTracker
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuotaLogicServiceTest {

    private class FakeRepositoryCommunicator : IRepositoryCommunicator {
        var totalCount = 0
        var limit = 20
        var habitsList = mutableListOf("Read a book")
        var message = "FOCUS!"
        var overlay = true
        val platformMap = mutableMapOf<String, Int>()

        override fun getTodayTotalCount(): Int = totalCount
        override fun getTodayPlatformCount(platform: String): Int = platformMap[platform] ?: 0
        override fun getPlatformBreakdown(): Map<String, Int> = platformMap
        override fun recordSwipe(platform: String, countIncrement: Int) {
            totalCount += countIncrement
            platformMap[platform] = (platformMap[platform] ?: 0) + countIncrement
        }
        override fun resetTodayQuota() {
            totalCount = 0
            platformMap.clear()
        }
        override fun getDailyLimit(): Int = limit
        override fun setDailyLimit(limit: Int) { this.limit = limit }
        override fun getMotivationalMessage(): String = message
        override fun setMotivationalMessage(message: String) { this.message = message }
        override fun getHabits(): List<String> = habitsList
        override fun addHabit(habit: String) { habitsList.add(habit) }
        override fun removeHabit(habit: String) { habitsList.remove(habit) }
        override fun getStreakDays(): Int = 1
        override fun isOverlayBadgeEnabled(): Boolean = overlay
        override fun setOverlayBadgeEnabled(enabled: Boolean) { this.overlay = enabled }
    }

    private lateinit var fakeRepo: FakeRepositoryCommunicator
    private lateinit var tracker: SessionSwipeTracker
    private lateinit var quotaService: QuotaLogicService

    @Before
    fun setUp() {
        fakeRepo = FakeRepositoryCommunicator()
        tracker = SessionSwipeTracker()
        quotaService = QuotaLogicService(fakeRepo, tracker)
    }

    @Test
    fun testLimiterStateAllowedUnderLimit() {
        fakeRepo.totalCount = 5
        fakeRepo.limit = 10
        assertEquals(LimiterState.ALLOWED, quotaService.evaluateLimiterState())
    }

    @Test
    fun testLimiterStateNotAllowedAtOrOverLimit() {
        fakeRepo.totalCount = 10
        fakeRepo.limit = 10
        assertEquals(LimiterState.NOT_ALLOWED, quotaService.evaluateLimiterState())

        fakeRepo.totalCount = 12
        assertEquals(LimiterState.NOT_ALLOWED, quotaService.evaluateLimiterState())
    }

    @Test
    fun testRecordVideoSwipeIncrementsRepo() {
        quotaService.recordVideoSwipe("YouTube Shorts", "sig1")
        assertEquals(1, fakeRepo.totalCount)
        assertEquals(1, fakeRepo.getTodayPlatformCount("YouTube Shorts"))
    }
}
