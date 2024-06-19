package com.easidrug.ui.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.easidrug.ui.UIConnector
import kotlinx.coroutines.delay

@Composable
fun MedicinesScreenBoxView(connector: UIConnector, onClick: (String) -> Unit) {
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
//            .size(300.dp, 700.dp)
            .fillMaxSize()
            .padding(top = 10.dp, bottom = 90.dp, end = 10.dp, start = 10.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
    ) {
        WeekDaysGrid(connector = connector, onClick = onClick)
    }
}

@Composable
fun WeekDaysGrid(connector: UIConnector, onClick: (String) -> Unit) {
    val daysOfWeek = listOf(
        "Sunday AM",
        "Sunday PM",
        "Monday AM",
        "Monday PM",
        "Tuesday AM",
        "Tuesday PM",
        "Wednesday AM",
        "Wednesday PM",
        "Thursday AM",
        "Thursday PM",
        "Friday AM",
        "Friday PM",
        "Saturday AM",
        "Saturday PM"
    )

    val dayColors = listOf(
        Color(255, 59, 59, 255), // Sunday
        Color(250, 160, 33, 255), // Monday
        Color(255, 234, 45, 255), // Tuesday
        Color(40, 216, 105, 255), // Wednesday
        Color(94, 212, 230, 255), // Thursday
        Color(93, 230, 185, 255), // Friday
        Color(155, 65, 226, 255), // Saturday
    )
    val dayColorsEmpty = listOf(
        Color(255, 40, 40, 26), // Sunday - Lighter Red
        Color(250, 167, 50, 26),  // Monday - Lighter Orange
        Color(255, 235, 59, 26), // Tuesday - Lighter Yellow
        Color(58, 219, 118, 26),  // Wednesday - Lighter Green
        Color(118, 220, 235, 26), // Thursday - Lighter Blue
        Color(119, 230, 194, 26), // Friday - Lighter Teal
        Color(165, 85, 228, 26)  // Saturday - Lighter Purple
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
//        modifier = Modifier.padding(8.dp),
    ) {
        items(daysOfWeek.size) { index -> // Use the size of the daysOfWeek list
            DayBox(
                day = daysOfWeek[index],
                fullColor = dayColors[index / 2],
                emptyColor = dayColorsEmpty[index / 2],
                connector = connector,
                onClick = onClick
            )
        }
    }
}

@Composable
fun DayBox(
    day: String,
    fullColor: Color,
    emptyColor: Color,
    connector: UIConnector,
    onClick: (String) -> Unit
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val animatable = remember { Animatable(0f) }
    LaunchedEffect(key1 = day) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
            )
        )
    }

    val pillsList = connector.getPillsForDay(day)
    val isFull = pillsList.isNotEmpty()
    val density = LocalDensity.current
    val gradientStartOffset = with(density) { 40.dp.toPx() } // The gradient offset

    val gradientAnimatable = remember { Animatable(0f) }
    val totalTravelDistance = boxSize.width.toFloat() + gradientStartOffset
    val targetValue = totalTravelDistance / boxSize.width.toFloat()

    if (animatable.value >= 1f) {
        LaunchedEffect(key1 = isFull) {
            gradientAnimatable.animateTo(
                targetValue = if (isFull) targetValue else 0f,
                animationSpec = tween(
                    durationMillis = 2000,
                    easing = LinearOutSlowInEasing
                )
            )
        }
    }

    val backgroundBrush = if (gradientAnimatable.value < targetValue) {
        Brush.horizontalGradient(
            colors = listOf(fullColor, emptyColor),
            startX = -gradientStartOffset + (boxSize.width.toFloat() * gradientAnimatable.value),
            endX = boxSize.width.toFloat()
        )

    } else {
        SolidColor(fullColor)
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .height(180.dp)
            .padding(top = 10.dp, end = 5.dp, start = 5.dp, bottom = 10.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundBrush)
            .border(
                2.dp,
                MaterialTheme.colorScheme.tertiary,
                RoundedCornerShape(30.dp)
            )
            .clickable(onClick = { onClick(day) })
            .onSizeChanged { newSize ->
                boxSize = newSize
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(
                Alignment.Center
            )
        ) {
            val daySplit = day.split(" ")
            Text(
                text = daySplit[0], modifier = Modifier
                    .padding(top = 10.dp),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = daySplit[1],
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            val pillNames = pillsList.joinToString(separator = ", \n") { it.pillName }
            Text(
                text = pillNames,
                modifier = Modifier.padding(bottom = 5.dp, start = 10.dp, end = 10.dp),
                color = Color.Black,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
