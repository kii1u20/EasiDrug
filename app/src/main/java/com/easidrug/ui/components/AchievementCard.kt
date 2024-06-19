package com.easidrug.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import com.easidrug.models.Achievement

import androidx.compose.material3.Text
import com.easidrug.ui.UIConnector
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.easidrug.ui.components.AchievementCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AchievementCard(achievement: Achievement, animatable: Animatable<Float, AnimationVector1D>) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(255, 59, 59, 255),
            Color(255, 40, 40, 26) // End color of the gradient
        )
    )
    val gradient2 = Brush.verticalGradient(
        colors = listOf(
            Color(94, 212, 230, 255), // Start color of the gradient
            Color(118, 220, 235, 26) // End color of the gradient
        )
    )
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (achievement.isUnlocked) gradient2 else gradient)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(achievement.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(achievement.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                achievement.points.toString() + " Points - Tier " + achievement.tier.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (achievement.isUnlocked) "Unlocked" else "Locked",
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            // You can add more details and styling as needed
        }
    }
}