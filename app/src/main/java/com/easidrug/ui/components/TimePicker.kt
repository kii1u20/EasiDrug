package com.easidrug.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(hourState: MutableState<String>, minutesState: MutableState<String>) {
    Row(modifier = Modifier.fillMaxWidth()) { // This will ensure the Row uses the full width
        OutlinedTextField(
            value = hourState.value,
            onValueChange = { newHour ->
                // Add logic here to handle hour input, like checking if it's within 0-23
                hourState.value = newHour.filter { it.isDigit() }.take(2)
            },
            modifier = Modifier
                .weight(1f) // Each TextField gets half the width
                .padding(10.dp), // Add some space between the TextFields
            label = { Text("Hour") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = minutesState.value,
            onValueChange = { newMinute ->
                // Add logic here to handle minute input, like checking if it's within 0-59
                minutesState.value = newMinute.filter { it.isDigit() }.take(2)
            },
            modifier = Modifier
                .weight(1f)
                .padding(10.dp), // Each TextField gets half the width
            label = { Text("Minute") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}