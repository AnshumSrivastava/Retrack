package com.example.retrack

import com.example.retrack.service.ContentClassifier
import com.example.retrack.service.SessionSwipeTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionSwipeTrackerTest {

    private lateinit var tracker: SessionSwipeTracker

    @Before
    fun setUp() {
        tracker = SessionSwipeTracker(viewerHysteresisMs = 2000L)
    }

    @Test
    fun testSessionStart() {
        val result = tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 0)
        assertEquals(1, result.sessionCount)
        assertEquals(1, result.totalIncrement)
        assertEquals(SessionSwipeTracker.SwipeActionType.SESSION_START, result.actionType)
    }

    @Test
    fun testSwipeNextNewVideo() {
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 0)
        val result = tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig2", 1)

        assertEquals(2, result.sessionCount)
        assertEquals(1, result.totalIncrement)
        assertEquals(SessionSwipeTracker.SwipeActionType.SWIPE_NEXT, result.actionType)
    }

    @Test
    fun testSwipePreviousVisitedVideoDoesNotIncrementTotal() {
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 0)
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig2", 1)

        // User swiped PREVIOUS back to sig1
        val result = tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 2)

        assertEquals(1, result.sessionCount)
        assertEquals(0, result.totalIncrement) // TOTAL UNCHANGED!
        assertEquals(SessionSwipeTracker.SwipeActionType.SWIPE_PREVIOUS, result.actionType)
    }

    @Test
    fun testRevisitForwardDoesNotIncrementTotal() {
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 0)
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig2", 1)
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 2)

        // User swiped down back to sig2
        val result = tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig2", 2)

        assertEquals(2, result.sessionCount)
        assertEquals(0, result.totalIncrement)
        assertEquals(SessionSwipeTracker.SwipeActionType.REVISIT_FORWARD, result.actionType)
    }

    @Test
    fun testHysteresisExitWindow() {
        tracker.onViewerActive(ContentClassifier.PLATFORM_YOUTUBE, "sig1", 0)
        assertTrue(tracker.isInSession())

        val now = System.currentTimeMillis()
        // Check within hysteresis (1000ms < 2000ms)
        val exitedEarly = tracker.checkHysteresisExit(now + 1000L)
        assertFalse(exitedEarly)
        assertTrue(tracker.isInSession())

        // Check after hysteresis (2500ms > 2000ms)
        val exitedLate = tracker.checkHysteresisExit(now + 2500L)
        assertTrue(exitedLate)
        assertFalse(tracker.isInSession())
        assertEquals(0, tracker.sessionCount)
    }
}
