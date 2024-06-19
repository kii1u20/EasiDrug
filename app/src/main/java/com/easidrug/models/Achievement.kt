package com.easidrug.models

import kotlinx.serialization.Serializable

/**
 * Represents an achievement in the EasiDrug app.
 * Each achievement has an ID, name, description, assigned points, and tier.
 * Achievements can be locked or unlocked and may have prerequisites or lead to a next tier.
 */
@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val points: Int,
    val tier: Int,
    var isUnlocked: Boolean = false,
    val prerequisiteId: String? = null,
    val nextTierId: String? = null // ID of the next tier achievement
) {
    init {
        require(id.isNotBlank()) { "Achievement ID must not be blank" }
        require(name.isNotBlank()) { "Achievement name must not be blank" }
        require(description.isNotBlank()) { "Achievement description must not be blank" }
//        require(points >= 0) { "Points must be non-negative" }
        require(tier >= 0) { "Tier must be non-negative" }
    }
    fun unlock() {
        isUnlocked = true
    }

    fun lock() {
        isUnlocked = false
    }
}