package com.easidrug.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import java.time.LocalTime

@Composable
fun MedicationReminderPopup(isVisible: Boolean, onDismiss: () -> Unit) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss, // Directly use onDismiss for the dismiss request
            title = { Text("Medication Reminder") },
            text = { Text("It's time to take your medication.") },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

