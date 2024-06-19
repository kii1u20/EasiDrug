package com.easidrug.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.R

@Composable
fun Logo(animated: Boolean) {
    Column {
        Text(
            text = "EasiDrug",
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        // Define the rotation degree state
        val pill1 = remember { Animatable(323.9f) }
        val pill2 = remember { Animatable(427.1f) }
        if (animated) {
            LaunchedEffect(key1 = Unit) {
                pill1.animateTo(
                    targetValue = 323.9f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 2000
                            323.9f at 0 with LinearEasing
                            -36.1f at 800 with LinearEasing
                            323.9f at 1200 with LinearEasing
                        }, repeatMode = RepeatMode.Restart
                    )
                )
            }
            LaunchedEffect(key1 = Unit) {
                pill2.animateTo(
                    targetValue = 427.1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 2000
                            427.1f at 0 with LinearEasing
                            67.1f at 800 with LinearEasing
                            427.1f at 1200 with LinearEasing
                        }, repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        Row() {
            Image(
                painter = painterResource(id = R.drawable.pillshape),
                contentDescription = "pill1",
                modifier = Modifier
                    .graphicsLayer(
                        rotationZ = pill1.value,
                        transformOrigin = TransformOrigin.Center
                    )
                    .size(100.dp)
//                            .rotate(-36.1f)
            )
            Image(
                painter = painterResource(id = R.drawable.pillshape),
                contentDescription = "pill2",
                modifier = Modifier
                    .graphicsLayer(
                        rotationZ = pill2.value,
                        transformOrigin = TransformOrigin.Center
                    )
                    .size(100.dp)
//                            .rotate(67.1f)
            )
        }
    }
}