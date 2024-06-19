package com.easidrug.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easidrug.ui.UIConnector

@Composable
fun PopUpWindowScheduleList(
    connector: UIConnector,
    allowLongPress: Boolean,
    showBiggerScheduleUI: MutableState<Boolean>,
    schedule: MutableList<UIConnector.DayTimePair<String, UIConnector.Time>>,
    listState: LazyListState,
) {
    LazyColumn(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    40.dp
                )
            )
            .padding(start = 10.dp, end = 10.dp)
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
        state = listState
    ) {
        if (schedule.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No schedule added yet",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

        } else {
            schedule.forEach { pair ->
                item {
                    val animatable = remember { Animatable(0.8f) }
                    LaunchedEffect(key1 = Unit) {
                        animatable.animateTo(
                            targetValue = 1f, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy, // This will cause it to overshoot and then come to rest
                                stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                            )
                        )
                    }
                    if (schedule.first() == pair) {
                        Spacer(modifier = Modifier.size(5.dp))
                    }
                    Card(modifier = Modifier.graphicsLayer {
                        scaleX = animatable.value
                        scaleY = animatable.value
                    }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                connector.formatScheduleNew(pair),
                                modifier = Modifier.padding(
                                    top = 10.dp, end = 10.dp, start = 10.dp
                                )
                            )

                            Spacer(Modifier.weight(1f))

                            IconButton(onClick = { schedule.remove(pair) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(5.dp))
                }
            }
        }
    }
}