package com.example.airsenseco2_theapp.model

data class TrainingSession(
    val id: Long = System.currentTimeMillis(),  // Unikt ID för passet
    val measurements: List<Measurement>,
    val startTime: Long,
    val duration: Long,
    val formattedStartTime: String = ""
)

data class Measurement(
    val co2: Float,
    val temperature: Float,
    val timestamp: Long
)