package com.example.retrack.data.model

data class UsageLog(
    val id: Long = 0,
    val date: String,
    val platform: String,
    val count: Int
)
