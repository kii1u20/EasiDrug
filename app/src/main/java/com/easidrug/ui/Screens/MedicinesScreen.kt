package com.easidrug.ui.Screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.R
import com.easidrug.debugAdd
import com.easidrug.ui.UIConnector
import com.easidrug.ui.components.MedicinesScreenBoxView
import com.easidrug.ui.components.MedicinesScreenListView
import com.easidrug.ui.components.PopUpBoxView
import com.easidrug.ui.components.PopUpNotification
import com.easidrug.ui.components.PopUpWindow
import com.easidrug.ui.components.PullToRefreshView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private val refreshing = mutableStateOf(false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedicinesScreen(connector: UIConnector) {
    if (connector.showPopup.value && connector.pagerState.value.currentPage == 1) {
        PopUpNotification(
            onDismissRequest = {
                connector.showPopup.value = false
                if (connector.shownNotification.value) {
                    connector.shownNotification.value = false

                }
            }, connector = connector,
            day = connector.current_pill
        )
    }
    val showBoxView = connector.showBoxView

    LaunchedEffect(Unit) {
//        connector.loadScheduleFromCloud(refreshing)
    }

    Column {
        Row(
            modifier = Modifier
                .padding(top = 5.dp, start = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val text = if (showBoxView.value) "Box" else "List"
            Text(
                text = "Medicines $text",
                fontSize = 25.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.medication_icon),
                contentDescription = "Medicines Icon",
                modifier = Modifier.size(35.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Last synced: \n" + connector.networkHandler.lastSyncSchedule.value,
                modifier = Modifier.padding(end = 5.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val listState = rememberLazyListState()
            val showDialog = remember { mutableStateOf(false) }
            val showEditDialog = remember { mutableStateOf(false) }
            val showBoxDialog = remember { mutableStateOf(false) }


            val editName = remember { mutableStateOf("") }

            val clickedDay = remember { mutableStateOf("") }

            PullToRefreshView(refreshing = refreshing, onRefresh = {
                connector.loadScheduleFromCloud(refreshing)
            }, content = {
                if (showBoxView.value) {
                    MedicinesScreenBoxView(connector, onClick = {
                        showBoxDialog.value = true
                        clickedDay.value = it
                    })
                } else {
                    MedicinesScreenListView(
                        connector = connector,
                        showEditDialog = showEditDialog,
                        editName = editName,
                        listState = listState
                    )
                }
            })

            ElevatedButton(
                onClick = { showDialog.value = true }, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(200.dp, 60.dp)
            ) {
                Text("Add new medication", fontSize = 16.sp)
            }

            ElevatedButton(
                onClick = {
                    showBoxView.value = !showBoxView.value
                    connector.userSessionManager.saveMedicinesView(showBoxView.value)
                }, modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp)
                    .size(90.dp, 60.dp)
            ) {
                Text("View", fontSize = 14.sp)
            }

            val showDeleteWarning = remember { mutableStateOf(false) }

            ElevatedButton(
                onClick = {
                    showDeleteWarning.value = true
                }, modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 20.dp)
                    .size(90.dp, 60.dp)
            ) {
                Text("Delete ALL", fontSize = 14.sp, textAlign = TextAlign.Center)
            }

            if (showDeleteWarning.value) {
                AlertDialog(
                    onDismissRequest = { showDeleteWarning.value = false },
                    title = { Text(text = "Warning!") },
                    text = { Text("Are you sure you want to DELETE all schedules? There is no going back!") },
                    confirmButton = {
                        Button(onClick = {
                            val scheduleListCopyTemp = connector.getScheduleList().toList()
                            for (entry in scheduleListCopyTemp) {
                                connector.deleteSchedule(entry.pillName)
                            }
                            showDeleteWarning.value = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteWarning.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showDialog.value) {
                PopUpWindow(
                    onDismissRequest = { showDialog.value = false },
                    onAddMedication = { scheduleEntry ->
                        connector.addScheduleEntry(scheduleEntry)
                    },
                    connector = connector
                )
            }

            if (showEditDialog.value) {
                PopUpWindow(
                    onDismissRequest = { showEditDialog.value = false },
                    onAddMedication = { scheduleEntry ->
                        connector.deleteSchedule(editName.value)
                        connector.addScheduleEntry(scheduleEntry)
                    },
                    name = editName.value,
                    gracePeriod = connector.getScheduleList()
                        .find { it.pillName == editName.value }?.gracePeriod ?: "0",
                    connector = connector,
//                selectedDays = connector.getScheduleList().find { it.pillName == editName.value }?.schedule?.map { it.day } ?: emptyList(),
                    initialSchedule = connector.getScheduleList()
                        .find { it.pillName == editName.value }?.schedule ?: mutableListOf(),
                    updateMode = true
                )
            }

            if (showBoxDialog.value) {
                showDialog.value = false
                PopUpBoxView(
                    onDismissRequest = { showBoxDialog.value = false },
                    connector = connector,
                    day = clickedDay.value
                )
            }
        }
    }
}

