package com.easidrug.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.easidrug.R
import com.easidrug.ui.Screens.LogData
import com.easidrug.ui.Screens.getLogs
import com.easidrug.ui.Screens.groupedLogs
import com.easidrug.ui.Screens.json
import com.easidrug.ui.Screens.text
import com.easidrug.ui.UIConnector
import com.easidrug.ui.UIConnector.*
import kotlinx.serialization.decodeFromString
import java.time.Duration
import java.time.LocalDateTime
import java.util.Calendar

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopUpNotification(
    onDismissRequest: () -> Unit,
    connector: UIConnector,
    day: String
) {

    val isPopupVisible = remember { mutableStateOf(true) }
    var shouldDismiss = remember { mutableStateOf(false) }  // 1. Define the state for dismissal
    var groupedLogslength = -1
    val notificationShownTime = remember { mutableStateOf(LocalDateTime.now()) } // Store the time when the notification was first shown

    // Launch an effect that runs until the popup is dismissed
    LaunchedEffect(isPopupVisible.value) {
        while (isPopupVisible.value) {

            getLogs(connector)

            if (groupedLogslength == -1) { groupedLogslength = groupedLogs.values.flatten().size}
            else {

                if (groupedLogslength != groupedLogs.values.flatten().size){

                    val allLogData = groupedLogs.values.flatten()

                    val highestIdEntry = allLogData.maxByOrNull { it.id.toInt() }

                    val variableLabel = highestIdEntry?.variable_label

                    val currentDayOfWeek = java.time.LocalDate.now().dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }

                    val currentHour = java.time.LocalTime.now().hour
                    val currentAmPm = if (currentHour < 12) "AM" else "PM"

                    if (variableLabel == "$currentDayOfWeek $currentAmPm") {
                    shouldDismiss.value = true  // Update the dismissal state when the condition is met
                    break }
                    else {
                        groupedLogslength = groupedLogs.size
                    }
                }
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    if (shouldDismiss.value) {
        onDismissRequest()
        isPopupVisible.value = false  // Ensure the popup won't be visible anymore
    }

    if (isPopupVisible.value) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Box(
                modifier = Modifier
//                .size(400.dp, 600.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .verticalScroll(rememberScrollState())
                        .padding(30.dp)
                ) {
                    val bellRingingAnimation = remember { Animatable(0f) }
                    LaunchedEffect(key1 = Unit) {
                        bellRingingAnimation.animateTo(
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes {
                                    durationMillis = 1200
                                    0f at 0 with LinearEasing
                                    -30f at 100 with LinearEasing
                                    30f at 200 with LinearEasing
                                    -30f at 300 with LinearEasing
                                    30f at 400 with LinearEasing
                                    0f at 500 with LinearEasing
                                },
                                repeatMode = RepeatMode.Restart
                            )
                        )
                    }
                    Icon(
                        painterResource(R.drawable.medication_icon),
                        contentDescription = "medication icon",
                        Modifier
                            .graphicsLayer(
                                rotationZ = bellRingingAnimation.value,
                                transformOrigin = TransformOrigin.Center
                            )
                            .size(58.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "It's time to take:",
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 20.dp, top = 20.dp)
                    )
                    Text(
                        text = day,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 20.dp, top = 20.dp)
                    )
                    val gracePeriod = connector.findMinimumGracePeriodForPills(day.split(","))
                    val remainingGracePeriod = gracePeriod.toInt() - Duration.between(notificationShownTime.value, LocalDateTime.now()).toMinutes()

                    if (remainingGracePeriod.toInt() > 2 && !connector.shownNotification.value) {
                        ElevatedButton(
                            onClick = {
                                onDismissRequest()
                                connector.shownNotification.value = true
                                val calendar: Calendar = Calendar.getInstance()
                                calendar.add(Calendar.MINUTE, remainingGracePeriod.toInt() - 2)
                                val newTimeInMillis: Long = calendar.timeInMillis
                                connector.medicationScheduler.scheduleNotification(
                                    connector.context,
                                    newTimeInMillis,
                                    day
                                )
                            }, modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 20.dp)
                        ) {
                            Text(
                                "Snooze " + (remainingGracePeriod.toInt() - 2) + "m",
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }


                    ElevatedButton(
                        onClick = { onDismissRequest() }, modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp)
                    ) {
                        Text("Dismiss", fontSize = 17.sp)
                    }

//                Column(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                   Text(text = "eh;ourehekjhifohfkhf")
//                }
                }
            }
        }
    }
}


