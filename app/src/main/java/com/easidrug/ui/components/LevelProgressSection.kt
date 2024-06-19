package com.easidrug.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.easidrug.models.UserProgress

@Composable
fun LevelProgressSection(userProgress: UserProgress) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        LevelDisplay(userProgress = userProgress)
        Spacer(modifier = Modifier.width(16.dp)) // Space between the level display and progress bar
        LevelProgressBar(userProgress = userProgress, modifier = Modifier.weight(1f))
    }
}