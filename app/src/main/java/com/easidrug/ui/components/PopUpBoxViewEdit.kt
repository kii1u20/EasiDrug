package com.easidrug.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.ui.UIConnector
import com.easidrug.ui.UIConnector.ScheduleEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopUpBoxViewEdit(
    entry: ScheduleEntry,
    connector: UIConnector,
    day: String,
    pillsForDay: MutableList<UIConnector.ScheduleEntry>,
    showEditUI: MutableState<Boolean>
) {
    val dayParts = day.split(" ")
    val filteredSchedule = entry.schedule.filter {
        it.day == dayParts[0] && connector.isTimeAMOrPM(it, dayParts[1])
    }
    val schedule =
        remember {
            mutableStateListOf<UIConnector.DayTimePair<String, UIConnector.Time>>().apply {
                addAll(
                    filteredSchedule
                )
            }
        }
    val listStateEditUI = rememberLazyListState()
    val pillName = remember { mutableStateOf(entry.pillName) }
    val gracePeriod = remember { mutableStateOf(entry.gracePeriod) }


    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(0.01f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Edit schedule:",
                modifier = Modifier.padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
            OutlinedTextField(
                value = pillName.value,
                onValueChange = { pillName.value = it },
                modifier = Modifier
                    .padding(end = 5.dp)
                    .weight(1.1f),
                label = { Text("Medication name") },
            )
            OutlinedTextField(
                value = gracePeriod.value,
                onValueChange = { gracePeriod.value = it },
                modifier = Modifier
                    .padding(end = 5.dp)
                    .weight(0.9f),
                label = { Text("Grace period") },
            )
//            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
        }
        Box(modifier = Modifier.weight(0.5f)) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                state = listStateEditUI
            ) {
                items(items = schedule) { time ->
                    val animatable = remember { Animatable(0.8f) }
                    LaunchedEffect(key1 = Unit) {
                        animatable.animateTo(
                            targetValue = 1f, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy, // This will cause it to overshoot and then come to rest
                                stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                            )
                        )
                    }
                    Card(modifier = Modifier
                        .graphicsLayer {
                            scaleX = animatable.value
                            scaleY = animatable.value
                        }
                        .padding(10.dp)
                        .fillMaxWidth()
                    )
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Scheduled for: " + time.time.toString(),
                                modifier = Modifier
                                    .padding(
                                        end = 10.dp, start = 10.dp
                                    )
                                    .weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.weight(0.8f))
                            IconButton(
                                onClick = {
                                    schedule.remove(time)
                                    entry.schedule.remove(time)
                                }, modifier = Modifier
                                    .weight(0.3f)

                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                }
            }
            val lastVisibleItemIndex by remember {
                derivedStateOf {
                    listStateEditUI.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                }
            }

            val firstVisibleItemIndex by remember {
                derivedStateOf {
                    listStateEditUI.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                }
            }

            val totalItemsCount by remember {
                derivedStateOf { listStateEditUI.layoutInfo.totalItemsCount }
            }

            ScrollIndicator(
                lastVisibleItemIndex = lastVisibleItemIndex,
                firstVisibleItemIndex = firstVisibleItemIndex,
                totalItemsCount = totalItemsCount
            )
        }

        ElevatedButton(onClick = {
            val updatedEntry = ScheduleEntry(pillName.value, entry.schedule, gracePeriod.value)
            connector.updateSchedule(entry.pillName, updatedEntry)
            pillsForDay.clear()
            pillsForDay.apply { addAll(connector.getPillsForDay(day)) }
            showEditUI.value = false
        }) {
            Text(text = "Save medication")
        }
    }
}