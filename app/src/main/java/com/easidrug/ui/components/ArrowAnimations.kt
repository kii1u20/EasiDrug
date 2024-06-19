package com.easidrug.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AnimateArrowIcon(icon: ImageVector) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 10F,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Icon(
        imageVector = icon,
        contentDescription = "Scroll Indicator",
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .size(34.dp)
            .offset(y = offsetY.dp)
    )
}

//@Composable
//fun AnimateArrowUpIcon() {
//    val infiniteTransition = rememberInfiniteTransition(label = "")
//
//    // Start the animation with a negative offset to move the arrow up first
//    val offsetY by infiniteTransition.animateFloat(
//        initialValue = -10F,
//        targetValue = 0F,
//        animationSpec = infiniteRepeatable(
//            animation = tween(500, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ), label = ""
//    )
//
//    Icon(
//        imageVector = Icons.Default.KeyboardArrowUp,
//        contentDescription = "Scroll Indicator",
//        tint = MaterialTheme.colorScheme.onPrimaryContainer,
//        modifier = Modifier
//            .size(34.dp)
//            .offset(y = offsetY.dp)
//    )
//}
