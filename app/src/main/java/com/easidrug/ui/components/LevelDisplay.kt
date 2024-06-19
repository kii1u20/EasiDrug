package com.easidrug.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.easidrug.models.UserProgress

@Composable
fun LevelDisplay(userProgress: UserProgress) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = "Lvl. " + userProgress.currentLevel.value.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontSize = 14.sp // Decrease font size if necessary
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}
