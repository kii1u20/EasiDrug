package com.easidrug.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayOfWeekDropdownMenu(
    selectedDay: MutableState<String>,
    onDaySelected: (String) -> Unit,
    hourInput: MutableState<String>,
    onHourChanged: (String) -> Unit,
    minutesInput: MutableState<String>,
    onMinutesChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val daysOfWeek =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column {

        TextField(
            value = selectedDay.value,
            onValueChange = { },
            label = { Text("Day of Week") },
            readOnly = true, // Makes the TextField not editable and only selectable
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            daysOfWeek.forEach { day ->
                DropdownMenuItem(
                    onClick = {
                        onDaySelected(day)
                        selectedDay.value = day
                        expanded = false
                    },
                    text = {
                        Text(day)
                    }
                )
            }
        }

        Row(Modifier.fillMaxWidth()) {

            TextField(
                value = hourInput.value,
                onValueChange = { hourInput.value = it; onHourChanged(it)},
                modifier = Modifier.weight(1f).padding(20.dp),
                label = { Text("Hour") },
//                trailingIcon = {
//                    Icon(
//                        imageVector = Icons.Filled.ArrowDropDown,
//                        contentDescription = "Dropdown",
//                        modifier = Modifier.clickable { expanded = true }
//                    )
//                }
            )
            TextField(
                value = minutesInput.value,
                onValueChange = { minutesInput.value = it; onMinutesChanged(it)},
                modifier = Modifier.weight(1f).padding(20.dp),
                label = { Text("Minutes") },
//                trailingIcon = {
//                    Icon(
//                        imageVector = Icons.Filled.ArrowDropDown,
//                        contentDescription = "Dropdown",
//                        modifier = Modifier.clickable { expanded = true }
//                    )
//                }
            )
        }
    }
}
