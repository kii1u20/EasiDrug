package com.easidrug.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.easidrug.ui.UIConnector
import kotlinx.coroutines.delay


@Composable
fun MedicinesScreenListView(
    connector: UIConnector,
    showEditDialog: MutableState<Boolean>,
    editName: MutableState<String>,
    listState: LazyListState
) {
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
//            .size(400.dp, 500.dp)
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .fillMaxSize()
            .padding(top = 10.dp, bottom = 90.dp, end = 10.dp, start = 10.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))

    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(
                items = connector.getScheduleList(),
                key = { medicine -> medicine.pillName }
            ) { medicine ->
                val animatable = remember { Animatable(0f) }

                var isVisible by remember { mutableStateOf(true) }

                AnimatedVisibility(
                    visible = isVisible,
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically()
                ) {
                    MedicineCard(
                        connector = connector,
                        medicine = medicine,
                        animatable = animatable,
                        onDelete = {
                            isVisible = false
                        },
                        onEdit = {
                            showEditDialog.value = true
                            editName.value = medicine.pillName
                        })
                }

                LaunchedEffect(key1 = medicine) {
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                            stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                        )
                    )
                }

                LaunchedEffect(key1 = isVisible) {
                    if (!isVisible) {
                        delay(300)
                        connector.deleteSchedule(medicine.pillName)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
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
}