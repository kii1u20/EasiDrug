package com.easidrug.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.easidrug.ui.UIConnector

@Composable
fun PopUpBoxViewScheduleList(
    connector: UIConnector,
    day: String,
    listState: LazyListState,
    pillsForDay: MutableList<UIConnector.ScheduleEntry>,
    allowLongPress: Boolean,
    showBiggerScheduleUI: MutableState<Boolean>
) {
    val showEditUI = remember { mutableStateOf(false) }
    val selectedScheduleEntry = remember { mutableStateOf<UIConnector.ScheduleEntry?>(null) }

    LazyColumn(
        modifier = Modifier

            .clip(RoundedCornerShape(30.dp))
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    40.dp
                )
            )
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (allowLongPress) {
                            showBiggerScheduleUI.value = true
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        val list = connector.getPillsForDay(day)
        if (list.isEmpty()) {
            item {
                Text(
                    text = "No medications added to this pill box",
                    modifier = Modifier.padding(top = 12.dp, end = 10.dp),
                )
            }
        } else {
            items(
                items = pillsForDay,
                key = { pill -> pill.pillName }
            ) { pill ->
                if (pillsForDay.first() == pill) {
                    Spacer(modifier = Modifier.size(10.dp))
                }
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
                    .padding(start = 10.dp, end = 10.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ))
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pill.pillName, modifier = Modifier
                                .padding(
                                    end = 10.dp, start = 10.dp
                                )
                                .weight(1.2f)
                        )
                        Column(modifier = Modifier.weight(2.0f)) {
                            val dayParts = day.split(" ")
                            pill.schedule.filter {
                                it.day == dayParts[0] && connector.isTimeAMOrPM(
                                    it,
                                    dayParts[1]
                                )
                            }.forEach { time ->
                                Row() {
                                    Text(
                                        text = "Scheduled for: " + time.time.toString(),
                                        modifier = Modifier.padding(
                                            end = 10.dp, start = 10.dp
                                        )
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                showEditUI.value = true
                                selectedScheduleEntry.value = pill
                            }, modifier = Modifier
                                .weight(0.4f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }

                        IconButton(
                            onClick = {
                                connector.removeScheduleFromList(
                                    dayTime = day, pillName = pill.pillName
                                )
                                pillsForDay.clear()
                                pillsForDay.apply { addAll(connector.getPillsForDay(day)) }
                            }, modifier = Modifier
                                .weight(0.4f)

                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
    if (showEditUI.value) {
        Dialog(
            onDismissRequest = { showEditUI.value = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            40.dp
                        )
                    )
            ) {
                Box(modifier = Modifier.size(400.dp, 600.dp)) {
                    selectedScheduleEntry.value?.let { PopUpBoxViewEdit(it, connector, day, pillsForDay, showEditUI) }

                }
            }
        }
    }
}