package com.example.retrack.service

class SessionSwipeTracker(
    val viewerHysteresisMs: Long = 2000L
) {
    var sessionCount: Int = 0
        private set

    val sessionHistory = mutableListOf<String>()

    private var inShortFormSession: Boolean = false
    private var lastViewerActiveTimestamp: Long = 0L
    private var currentVideoSignature: String? = null

    fun onViewerActive(platform: String, signature: String?, currentTotalCount: Int): SwipeResult {
        val now = System.currentTimeMillis()
        lastViewerActiveTimestamp = now

        // RULE 1: If signature is null or blank, DO NOT increment count or fabricate fake signatures.
        if (signature.isNullOrBlank()) {
            return SwipeResult(
                sessionCount = sessionCount.coerceAtLeast(1),
                totalIncrement = 0,
                actionType = SwipeActionType.NO_CHANGE
            )
        }

        // RULE 2: If signature is identical to current active video, return NO_CHANGE (zero false positives).
        if (inShortFormSession && signature == currentVideoSignature) {
            return SwipeResult(
                sessionCount = sessionCount,
                totalIncrement = 0,
                actionType = SwipeActionType.NO_CHANGE
            )
        }

        // Session start or feed refresh when sessionCount <= 0
        if (!inShortFormSession || sessionCount <= 0) {
            inShortFormSession = true
            sessionCount = 1
            sessionHistory.clear()
            sessionHistory.add(signature)
            currentVideoSignature = signature
            return SwipeResult(
                sessionCount = sessionCount,
                totalIncrement = 1,
                actionType = SwipeActionType.SESSION_START
            )
        }

        // Check if signature already exists in session history
        val existingIndex = sessionHistory.indexOf(signature)

        if (existingIndex != -1) {
            // Re-visiting a video already present in current session history
            val targetSessionIndex = existingIndex + 1
            val action = if (targetSessionIndex < sessionCount) {
                SwipeActionType.SWIPE_PREVIOUS
            } else {
                SwipeActionType.REVISIT_FORWARD
            }
            sessionCount = targetSessionIndex
            currentVideoSignature = signature

            return SwipeResult(
                sessionCount = sessionCount,
                totalIncrement = 0, // FREE REVISIT! Total quota unchanged
                actionType = action
            )
        } else {
            // Brand-new unique video signature in this session -> User swiped NEXT to new video
            sessionHistory.add(signature)
            sessionCount = sessionHistory.size
            currentVideoSignature = signature

            return SwipeResult(
                sessionCount = sessionCount,
                totalIncrement = 1,
                actionType = SwipeActionType.SWIPE_NEXT
            )
        }
    }

    /**
     * Checks if hysteresis window has passed when viewer is inactive.
     * Returns true if session was ended (cleared), false if still within hysteresis.
     */
    fun checkHysteresisExit(now: Long = System.currentTimeMillis()): Boolean {
        if (!inShortFormSession) return false

        if (now - lastViewerActiveTimestamp > viewerHysteresisMs) {
            endSession()
            return true
        }
        return false
    }

    fun endSession() {
        inShortFormSession = false
        sessionCount = 0
        sessionHistory.clear()
        currentVideoSignature = null
    }

    fun isInSession(): Boolean = inShortFormSession

    data class SwipeResult(
        val sessionCount: Int,
        val totalIncrement: Int,
        val actionType: SwipeActionType
    )

    enum class SwipeActionType {
        SESSION_START,
        SWIPE_NEXT,
        SWIPE_PREVIOUS,
        REVISIT_FORWARD,
        NO_CHANGE
    }
}
