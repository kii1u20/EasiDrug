package com.easidrug.models

import java.time.LocalDateTime
import java.time.LocalTime

data class MedicationNotification(
    val medicationName: String,
    val boxNumber: Int,
    val timeWindowStart: LocalTime,
    val timeWindowEnd: LocalTime,
    var snoozed: Boolean = false
) {
    init {
        require(medicationName.isNotBlank()) { "Medication name must not be blank." }
        require(boxNumber >= 1) { "Box number must be positive and reflect the physical pillbox compartments." }
        require(timeWindowStart.isBefore(timeWindowEnd)) { "Start time must be before the end time." }
    }
}
