package com.easidrug.ui.Screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.easidrug.R
import com.easidrug.ui.UIConnector
import com.easidrug.ui.components.PopUpNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(connector: UIConnector) {
    if (connector.showPopup.value && connector.pagerState.value.currentPage == 0) {
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
    val currentTime = remember { mutableStateOf(LocalDateTime.now()) }

    PeriodicTimeUpdater(intervalMillis = 1_000) { // Update every minute
        currentTime.value = LocalDateTime.now()
    }

    Text("Current time: ${currentTime.value}", modifier = Modifier.size(0.dp))

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, " + connector.userSessionManager.getSignedInUsername() + "!",
                fontSize = 25.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            connector.userSessionManager.profilePictureUri.value?.let { UriImage(uri = it) }
        }


        val animatable1 = remember { Animatable(0f) }
        val animatable2 = remember { Animatable(0f) }
        val animatable3 = remember { Animatable(0f) }
        val animatable4 = remember { Animatable(0f) }

        LaunchedEffect(key1 = Unit) {
            animatable1.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                    stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                )
            )
        }
        LaunchedEffect(key1 = Unit) {
            delay(100)
            animatable2.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                    stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                )
            )
        }
        LaunchedEffect(key1 = Unit) {
            delay(200)
            animatable3.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                    stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                )
            )
        }
        LaunchedEffect(key1 = Unit) {
            delay(300)
            animatable4.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                    stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                )
            )
        }
        Spacer(modifier = Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatable1.value
                    scaleY = animatable1.value
                }
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .shadow(15.dp, RoundedCornerShape(30.dp))
                .padding(5.dp)
                .clip(RoundedCornerShape(30.dp))
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                styledText(text = "Medications to take from " + getCurrentDayAndTime() + ":")

                val pills = connector.getPillsAfterCurrentTime(
                    getCurrentDayAndTime(),
                    UIConnector.Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
                ).joinToString(separator = ", ")

                styledText(text = pills.ifEmpty { "No medications left to be taken" })
            }
        }
        Spacer(modifier = Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatable2.value
                    scaleY = animatable2.value
                }
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally)
                .shadow(15.dp, RoundedCornerShape(30.dp))
                .padding(5.dp)
                .clip(RoundedCornerShape(30.dp))
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.primary),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val nextPillTime = connector.findNextPill(getCurrentDayAndTime())
                if (nextPillTime != null) {
                    styledText(
                        text = LocalDateTime.now().dayOfWeek.toString() + " " + LocalDateTime.now().dayOfMonth.toString() + getDayOfMonthSuffix(
                            LocalDateTime.now().dayOfMonth
                        )
                    )
                    styledText(text = "At")

                    styledText(
                        text = connector.formatScheduleNew(
                            UIConnector.DayTimePair(
                                "None",
                                nextPillTime
                            )
                        ).split(" ")[1], fontSize = 50.sp
                    )
                } else {
                    styledText(text = "No upcoming medications")
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatable3.value
                    scaleY = animatable3.value
                }
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .shadow(15.dp, RoundedCornerShape(30.dp))
                .padding(5.dp)
                .clip(RoundedCornerShape(30.dp))
                .weight(0.3f)
                .background(color = MaterialTheme.colorScheme.primary),
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                val timeUntil = connector.findNextPill(getCurrentDayAndTime())?.minus(
                    UIConnector.Time(
                        LocalDateTime.now().hour,
                        LocalDateTime.now().minute
                    )
                )
                if (timeUntil != null) {
                    styledText(text = "Time until: " + timeUntil.hour.toString() + "h " + timeUntil.minute.toString() + "m")
                } else {
                    styledText(text = "No upcoming medications")
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatable4.value
                    scaleY = animatable4.value
                }
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .shadow(15.dp, RoundedCornerShape(30.dp))
                .padding(5.dp)
                .clip(RoundedCornerShape(30.dp))
                .weight(0.3f)
                .background(color = MaterialTheme.colorScheme.primary),
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                val pills = connector.getPillsAfterCurrentTime(
                    getCurrentDayAndTime(),
                    UIConnector.Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
                )
                val graceP = connector.findMinimumGracePeriodForPills(pills);
                var text =
                    "No upcoming medications" //Maybe say something different as to not repeat with the box above?
                if (graceP.isNotEmpty()) {
                    text = graceP + "m grace period either side"
                }
                styledText(text = text)
            }
        }
        Spacer(modifier = Modifier.weight(0.1f))
    }
//    }
}

@Composable
fun UriImage(uri: Uri) {
    val painter = rememberImagePainter(data = uri)
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(key1 = Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy, // This will cause it to overshoot and then come to rest
                stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
            )
        )
    }

    Image(
        painter = painter,
        contentDescription = "Image from Uri",
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .size(35.dp)
            .clip(RoundedCornerShape(50))
    )
}

@Composable
fun styledText(text: String, fontSize: TextUnit = 25.sp) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .padding(8.dp)
    )
}

@Composable
fun PeriodicTimeUpdater(intervalMillis: Long, updateState: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            while (isActive) {
                delay(intervalMillis)
                updateState()
            }
        }
        onDispose {
            job.cancel()
        }
    }
}

fun getCurrentDayAndTime(): String {
    val day = LocalDateTime.now().dayOfWeek.toString()
    val hour = LocalDateTime.now().hour

    return if (hour < 12) {
        "$day AM"
    } else {
        "$day PM"
    }
}

fun getDayOfMonthSuffix(day: Int): String {
    if (day in 11..13) {
        return "th"
    }
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
