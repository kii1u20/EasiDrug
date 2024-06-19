package com.easidrug.ui.components

import android.view.Gravity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
fun PopUpBoxView(
    onDismissRequest: () -> Unit, connector: UIConnector, day: String
) {
    val hourState = remember { mutableStateOf("") }
    val minutesState = remember { mutableStateOf("") }
    val nameState = remember { mutableStateOf("") }
    val pillsForDay =
        remember { mutableStateListOf<ScheduleEntry>().apply { addAll(connector.getPillsForDay(day)) } }
    val showBiggerScheduleUI = remember { mutableStateOf(false) }
    val gracePeriodState = remember { mutableStateOf(connector.gracePeriod.value) }

    Dialog(
        onDismissRequest = { onDismissRequest() }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.TOP)
        Box(
            modifier = Modifier
//                .size(400.dp, 550.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
        ) {
            val listState = rememberLazyListState()
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
//                    .padding(30.dp)
            ) {
                Icon(
                    painterResource(R.drawable.medication_schedule),
                    contentDescription = "medication icon",
                    Modifier
                        .size(58.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = day,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
                )

                Box(Modifier.size(400.dp, 165.dp)) {
                    PopUpBoxViewScheduleList(
                        connector = connector,
                        day = day,
                        listState = listState,
                        pillsForDay = pillsForDay,
                        allowLongPress = true,
                        showBiggerScheduleUI = showBiggerScheduleUI
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
                Text(
                    "Enter the name of the medication",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp, top = 20.dp)
                )
                Row() {
                    OutlinedTextField(
                        value = nameState.value,
                        onValueChange = { nameState.value = it },
                        modifier = Modifier
//                            .align(Alignment.CenterHorizontally),
                            .padding(start = 10.dp, end = 10.dp)
                            .weight(1f),
                        label = { Text("Medication name") },
                    )
                    OutlinedTextField(
                        value = gracePeriodState.value,
                        onValueChange = { gracePeriodState.value = it },
                        modifier = Modifier
//                            .align(Alignment.CenterHorizontally),
                            .padding(start = 10.dp, end = 10.dp)
                            .weight(0.57f),
                        label = { Text("Grace period") },
                    )
                }

                TimePicker(hourState = hourState, minutesState = minutesState)
                // Add medication button
                ElevatedButton(
                    onClick = {
                        if (hourState.value == "" || minutesState.value == "" || nameState.value == "") {
                            return@ElevatedButton
                        }
                        val daySplit = day.split(" ")
                        val pair = DayTimePair<String, Time>(
                            daySplit[0], Time(
                                hourState.value.toInt(), minutesState.value.toInt()
                            )
                        )
                        if (connector.isTimeAMOrPM(pair, daySplit[1])) {
                            val list =
                                mutableStateListOf<DayTimePair<String, Time>>().apply { add(pair) }
                            val entry = ScheduleEntry(
                                nameState.value, list, gracePeriodState.value
                            )
                            connector.addScheduleEntry(entry)
                            pillsForDay.clear()
                            pillsForDay.apply { addAll(connector.getPillsForDay(day)) }
                        }
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Add Medication")
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
                        text = "Edit medications",
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                Box(modifier = Modifier.size(400.dp, 600.dp)) {
                    PopUpBoxViewScheduleList(
                        connector = connector,
                        day = day,
                        listState = listStateBigUI,
                        pillsForDay = pillsForDay,
                        allowLongPress = false,
                        showBiggerScheduleUI = showBiggerScheduleUI
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

