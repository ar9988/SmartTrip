package com.neungi.domain.model

data class AiPlanningRequest(
    val userId: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
    val destination: List<String>,
    val preferences: Preferences
)

