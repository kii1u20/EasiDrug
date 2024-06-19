package com.easidrug.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.easidrug.models.UserProgress

@Composable
fun LevelProgressBar(userProgress: UserProgress, modifier: Modifier = Modifier) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        // Progress Filling Part
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = calculateProgressFraction(userProgress))
                .background(MaterialTheme.colorScheme.primary)
                .clip(RoundedCornerShape(12.dp))
        )

        // Progress text (e.g., "Lv 6 - 6/10 EXP")
        Text(
            text = "Lv ${userProgress.currentLevel.value} - ${userProgress.currentPoints.value}/${userProgress.pointsToNextLevel.value} EXP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

fun calculateProgressFraction(userProgress: UserProgress): Float {
    return userProgress.currentPoints.value.toFloat() / userProgress.pointsToNextLevel.value.toFloat()
}