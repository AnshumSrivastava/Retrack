package com.example.retrack.data.model

data class UserSettings(
    val dailyLimit: Int = 20,
    val motivationalMessage: String = "IT'S TIME TO FOCUS ON WHAT MATTERS!",
    val habits: List<String> = listOf("Read a book", "Go for a walk", "Drink water", "Meditate"),
    val overlayBadgeEnabled: Boolean = true
)
