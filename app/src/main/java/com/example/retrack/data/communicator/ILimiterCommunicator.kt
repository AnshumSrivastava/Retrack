package com.example.retrack.data.communicator

enum class LimiterState {
    ALLOWED,
    NOT_ALLOWED
}

interface ILimiterCommunicator {
    fun evaluateLimiterState(): LimiterState
    fun recordVideoSwipe(platform: String, signature: String): Pair<Int, Int>
    fun getDailyTotalCount(): Int
    fun getDailyLimit(): Int
}
