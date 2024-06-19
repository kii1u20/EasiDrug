package com.easidrug.models

import androidx.compose.runtime.MutableState
import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val currentLevel: MutableState<Int>,
    val currentPoints: MutableState<Int>,
    val pointsToNextLevel: MutableState<Int>
) {
    init {
        require(currentLevel.value > 0) { "Current level must be positive." }
        require(currentPoints.value >= 0) { "Current points cannot be negative." }
        require(pointsToNextLevel.value > 0) { "Points to the next level must be positive." }
        require(currentPoints.value <= pointsToNextLevel.value) { "Current points must be less than or equal to the points required for the next level." }
    }

    val progress: Float
        get() = currentPoints.value / pointsToNextLevel.value.toFloat().also {
            require(it in 0f..1f) { "Progress must be between 0 and 1." }
        }
}
