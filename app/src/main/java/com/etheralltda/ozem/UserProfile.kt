package com.etheralltda.ozem

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    var name: String = "",
    var currentWeight: Float = 0.0f,
    var targetWeight: Float = 0.0f,
    var height: Float = 0.0f,
    var goalType: String = "",
    var activityLevel: String = "",
    var waterGoalLiters: Float = 0.0f,
    var isPremium: Boolean = false,
    var id: String? = null // ID opcional para o banco de dados
)