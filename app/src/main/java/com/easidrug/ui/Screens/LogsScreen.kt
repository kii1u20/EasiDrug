package com.easidrug.ui.Screens

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.R
import com.easidrug.networking.AzureConnection
import com.easidrug.ui.UIConnector
import com.easidrug.ui.components.PopUpNotification
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.easidrug.ui.UIConnector.Time
import com.easidrug.ui.components.PullToRefreshView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

@Serializable
data class TimestampInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val day_of_week: String,
    val hour: Int,
    val minute: Int
)

@Serializable
data class LogData(
    val id: String,
    val variable_label: String,
    val value: Int,
    val timestamp: TimestampInfo,
    val isOnTime: String
)

val json = Json { ignoreUnknownKeys = true }

val simplifiedScheduleList = mutableStateListOf<UIConnector.SimplifiedScheduleEntry>()

private val scope = CoroutineScope(Dispatchers.IO)

var logsList by mutableStateOf(listOf<LogData>())
var groupedLogs by mutableStateOf(mapOf<String, List<LogData>>())
var text = mutableStateOf("")

private val refreshingLogs = mutableStateOf(false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogsScreen(connector: UIConnector) {

    simplifiedScheduleList.clear()
    simplifiedScheduleList.addAll(connector.formatScheduleList())

    if (connector.showPopup.value && connector.pagerState.value.currentPage == 2) {
        PopUpNotification(
            onDismissRequest = { connector.showPopup.value = false },
            connector = connector,
            day = connector.current_pill
        )
    }

//    LaunchedEffect(Unit) {
//        getLogs(connector)
//    }

    // Define the days of the week
    val daysOfWeek =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    PullToRefreshView(refreshing = refreshingLogs, onRefresh = {
        CoroutineScope(Dispatchers.IO).launch {
            getLogs(
                connector,
                refreshingLogs = refreshingLogs
            )
        }
    }, content = {
        Column() {
            Row(
                modifier = Modifier
                    .padding(top = 5.dp, start = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Logs",
                    fontSize = 25.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.log_icon),
                    contentDescription = "Medicines Icon",
                    modifier = Modifier.size(35.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Last synced: \n" + connector.networkHandler.lastSyncLogs.value,
                    modifier = Modifier.padding(end = 5.dp)
                )
            }
            Button(
                onClick = {
                    connector.pdfLogger.createPdf(logsList)
                },
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text("Export Logs")
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                daysOfWeek.forEach { day ->
                    // For each day, create a GroupedLogsBox for AM and PM
                    item {
                        val animatable = remember { Animatable(0f) }
                        LaunchedEffect(key1 = Unit) {
                            animatable.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy, // This will cause it to overshoot and then come to rest
                                    stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                                )
                            )
                        }
                        val amLabel = "$day AM"
                        val pmLabel = "$day PM"

                        val amLogs = groupedLogs[amLabel] ?: emptyList()
                        val pmLogs = groupedLogs[pmLabel] ?: emptyList()

                        GroupedLogsBox(label = amLabel, logs = amLogs, connector = connector)
                        GroupedLogsBox(label = pmLabel, logs = pmLogs, connector = connector)
                    }
                }
            }
        }
    })
}

@Composable
fun GroupedLogsBox(label: String, logs: List<LogData>, connector: UIConnector) {
    val pillsForDay =
        connector.getPillsForDay(label).joinToString(separator = ", ") { it.pillName }
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(key1 = Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
            )
        )
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .fillMaxSize()
            .padding(8.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(6.dp)
            )
            if (pillsForDay.isNotEmpty()) {
                Text(
                    text = "Pills to take: $pillsForDay",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(6.dp)
                )
            }
            Box(modifier = Modifier.heightIn(max = 200.dp)) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(logs) { log ->
                        timeStamp(time = log.timestamp, isOnTime = log.isOnTime)
                    }
                }
            }

        }
    }
}


@Composable
fun timeStamp(time: TimestampInfo, isOnTime: String) {
    val notOnTimeColour = Brush.verticalGradient(
        colors = listOf(
            Color(255, 59, 59, 255),
            Color(255, 40, 40, 26) // End color of the gradient
        )
    )
    val onTimeColour = Brush.verticalGradient(
        colors = listOf(
            Color(94, 212, 230, 255), // Start color of the gradient
            Color(118, 220, 235, 26) // End color of the gradient
        )
    )

    fun minutesToTime(minutes: Int): Time {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return Time(hours, remainingMinutes)
    }


    // Determine the box color based on whether the pill was taken on time
    val boxColor = if (isOnTime == "True") onTimeColour else notOnTimeColour

    val animatable = remember { Animatable(0.8f) }
    LaunchedEffect(key1 = Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, // This will cause it to overshoot and then come to rest
                stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
            )
        )
    }
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .fillMaxSize(1f)
//            .size(450.dp, 60.dp)
            .padding(6.dp)
            .clip(RoundedCornerShape(30.dp))
//            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(70.dp))
            .background(boxColor)
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = "The pills were taken on:",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            val minuteString = if (time.minute < 10) "0${time.minute}" else time.minute.toString()
            Text(
                text = time.day.toString() + "/" + time.month + "/" + time.year + " at " + time.hour.toString() + ":" + minuteString,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

    }
}

suspend fun getLogs(connector: UIConnector, refreshingLogs: MutableState<Boolean> = mutableStateOf(false)) {
    Log.d("Logs", "Getting logs from Azure")
    refreshingLogs.value = true
    text.value = connector.networkHandler.getLogs(connector.userSessionManager.selectedDevice.value)
    // Log.d("POPUP", text.value)
    logsList = try {
        json.decodeFromString(text.value)
    } catch (e: Exception) {
        listOf()
    }
    // Group logs by 'variable_label'
    groupedLogs = logsList.groupBy { it.variable_label }
    refreshingLogs.value = false
}