package com.easidrug.ui.components

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.easidrug.R
import com.easidrug.ui.UIConnector
import com.easidrug.ui.UIConnector.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopUpWindow(
    onDismissRequest: () -> Unit,
    onAddMedication: (ScheduleEntry) -> Unit,
    name: String = "",
    connector: UIConnector,
    gracePeriod: String = connector.gracePeriod.value,
    selectedDays: List<String> = listOf(),
    initialSchedule: MutableList<DayTimePair<String, Time>> = mutableListOf(),
    updateMode: Boolean = false

) {
    val userInput = remember { mutableStateOf(name) }
    val selectedDaysState = remember { mutableStateOf(selectedDays) }
    val hourState = remember { mutableStateOf("") }
    val minutesState = remember { mutableStateOf("") }
    val schedule =
        remember { mutableStateListOf<DayTimePair<String, Time>>().apply { addAll(initialSchedule) } }
    val listState = rememberLazyListState()
    val gracePeriodState = remember { mutableStateOf(gracePeriod) }
    val showBiggerScheduleUI = remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismissRequest() }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.TOP)

        Box(
            modifier = Modifier
//                .size(400.dp, 650.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
            ) {
                Icon(
                    painterResource(R.drawable.medication_schedule),
                    contentDescription = "medication icon",
                    Modifier
                        .size(58.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = userInput.value,
                        onValueChange = { userInput.value = it },
                        modifier = Modifier
//                            .align(Alignment.Start)
                            .padding(bottom = 5.dp, start = 20.dp)
                            .weight(1.3f),
                        label = { Text("Medication name") },
                    )

                    OutlinedTextField(
                        value = gracePeriodState.value,
                        onValueChange = { gracePeriodState.value = it },
                        modifier = Modifier
//                            .align(Alignment.Start)
                            .padding(bottom = 5.dp, start = 20.dp, end = 20.dp)
                            .weight(1f),
                        label = { Text("Grace period") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Box(
                    modifier = Modifier
//                        .padding(start = 20.dp, end = 20.dp) //can probably enable this to make it look nicer
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(30.dp))
                        .size(400.dp, 90.dp)
                ) {
                    PopUpWindowScheduleList(
                        connector = connector,
                        allowLongPress = true,
                        showBiggerScheduleUI = showBiggerScheduleUI,
                        schedule = schedule,
                        listState = listState
                    )

                    val lastVisibleItemIndex by remember {
                        derivedStateOf {
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        }
                    }

                    val firstVisibleItemIndex by remember {
                        derivedStateOf {
                            listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                        }
                    }

                    val totalItemsCount by remember {
                        derivedStateOf { listState.layoutInfo.totalItemsCount }
                    }

                    ScrollIndicator(
                        lastVisibleItemIndex = lastVisibleItemIndex,
                        firstVisibleItemIndex = firstVisibleItemIndex,
                        totalItemsCount = totalItemsCount
                    )
                }

                WeekDaysCheckbox(selectedDaysState)
                TimePicker(hourState, minutesState)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Add schedule button
                    ElevatedButton(
                        onClick = {
                            selectedDaysState.value.forEach { day ->
                                schedule.add(
                                    DayTimePair(
                                        day,
                                        Time(hourState.value.toInt(), minutesState.value.toInt())
                                    )
                                )
                            }

                            selectedDaysState.value = listOf()
                            hourState.value = ""
                            minutesState.value = ""
                        }
                    ) {
                        Text("Add Schedule")
                    }
                    if (updateMode) {
                        // Update medication button
                        ElevatedButton(
                            onClick = {
                                if (userInput.value.isNotEmpty()) {
                                    if ((selectedDaysState.value.isNotEmpty() && hourState.value.isNotEmpty() && minutesState.value.isNotEmpty()) || schedule.isNotEmpty()) {
                                        selectedDaysState.value.forEach { day ->
                                            schedule.add(
                                                DayTimePair(
                                                    day, Time(
                                                        hourState.value.toInt(),
                                                        minutesState.value.toInt()
                                                    )
                                                )
                                            )
                                        }
                                        if (gracePeriodState.value.isEmpty()) {
                                            gracePeriodState.value = "0"
                                        }
                                        selectedDaysState.value = listOf()
                                        hourState.value = ""
                                        minutesState.value = ""
                                        val scheduleEntry = ScheduleEntry(
                                            userInput.value,
                                            schedule,
                                            gracePeriodState.value
                                        )
                                        connector.updateSchedule(name, scheduleEntry)
                                        onDismissRequest()
                                    }

                                }
                            }
                        ) {
                            Text("Update Medication Schedule")
                        }
                    } else {
                        // Add medication button
                        ElevatedButton(
                            onClick = {
                                if (userInput.value.isNotEmpty()) {
                                    if ((selectedDaysState.value.isNotEmpty() && hourState.value.isNotEmpty() && minutesState.value.isNotEmpty()) || schedule.isNotEmpty()) {
                                        selectedDaysState.value.forEach { day ->
                                            schedule.add(
                                                DayTimePair(
                                                    day, Time(
                                                        hourState.value.toInt(),
                                                        minutesState.value.toInt()
                                                    )
                                                )
                                            )
                                        }
                                        if (gracePeriodState.value.isEmpty()) {
                                            gracePeriodState.value = "0"
                                        }
                                        selectedDaysState.value = listOf()
                                        hourState.value = ""
                                        minutesState.value = ""
                                        val scheduleEntry = ScheduleEntry(
                                            userInput.value,
                                            schedule,
                                            gracePeriodState.value
                                        )
                                        onAddMedication(scheduleEntry)
                                        onDismissRequest()
                                    }
                                }
                            }
                        ) {
                            Text("Add Medication")
                        }
                    }
                }
                Spacer(modifier = Modifier.size(5.dp))
            }
        }
    }

    if (showBiggerScheduleUI.value) {
        val listStateBigUI = rememberLazyListState()
        Dialog(
            onDismissRequest = { showBiggerScheduleUI.value = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            40.dp
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Edit schedule",
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }

                Box(modifier = Modifier.size(400.dp, 650.dp)) {
                    PopUpWindowScheduleList(
                        connector = connector,
                        allowLongPress = false,
                        showBiggerScheduleUI = showBiggerScheduleUI,
                        schedule = schedule,
                        listState = listStateBigUI
                    )
                    val lastVisibleItemIndex by remember {
                        derivedStateOf {
                            listStateBigUI.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        }
                    }

                    val firstVisibleItemIndex by remember {
                        derivedStateOf {
                            listStateBigUI.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                        }
                    }

                    val totalItemsCount by remember {
                        derivedStateOf { listStateBigUI.layoutInfo.totalItemsCount }
                    }

                    ScrollIndicator(
                        lastVisibleItemIndex = lastVisibleItemIndex,
                        firstVisibleItemIndex = firstVisibleItemIndex,
                        totalItemsCount = totalItemsCount
                    )
                }

            }

        }
    }
}

@Composable
fun WeekDaysCheckbox(selectedDays: MutableState<List<String>>) {
    val weekDays =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val isEverydayChecked = remember(selectedDays.value) {
        mutableStateOf(weekDays.toSet() == selectedDays.value.toSet())
    }
    Column {
        for (i in weekDays.indices step 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                DayWithCheckbox(
                    day = weekDays[i], selectedDays = selectedDays, modifier = Modifier.weight(1f)
                )
                if (i + 1 < weekDays.size) {
                    DayWithCheckbox(
                        day = weekDays[i + 1],
                        selectedDays = selectedDays,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    EveryDayCheckbox(
                        isEverydayChecked = isEverydayChecked,
                        selectedDays = selectedDays,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun EveryDayCheckbox(
    isEverydayChecked: MutableState<Boolean>,
    selectedDays: MutableState<List<String>>,
    modifier: Modifier
) {
    val weekDays =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.inversePrimary)
            .clickable(
                onClick = {
                    isEverydayChecked.value = !isEverydayChecked.value
                    selectedDays.value = if (isEverydayChecked.value) {
                        weekDays
                    } else {
                        emptyList()
                    }
                }
            )
    ) {
        Checkbox(checked = isEverydayChecked.value, onCheckedChange = { isChecked ->
            isEverydayChecked.value = isChecked
            selectedDays.value = if (isChecked) {
                weekDays
            } else {
                emptyList()
            }
        })
        Text(text = "Every day", modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun DayWithCheckbox(day: String, selectedDays: MutableState<List<String>>, modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.inversePrimary)
            .clickable(
                onClick = {
                    val updatedDays = selectedDays.value.toMutableList()
                    if (day in updatedDays) {
                        updatedDays.remove(day)
                    } else {
                        updatedDays.add(day)
                    }
                    selectedDays.value = updatedDays
                }
            )
    ) {
        Checkbox(checked = day in selectedDays.value, onCheckedChange = {
            val updatedDays = selectedDays.value.toMutableList()
            if (day in updatedDays) {
                updatedDays.remove(day)
            } else {
                updatedDays.add(day)
            }
            selectedDays.value = updatedDays
        })
        Text(text = day, modifier = Modifier.padding(start = 8.dp))
    }
}


