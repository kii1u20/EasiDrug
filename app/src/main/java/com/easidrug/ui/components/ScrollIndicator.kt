package com.easidrug.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex

@Composable
fun ScrollIndicator(lastVisibleItemIndex: Int, firstVisibleItemIndex: Int, totalItemsCount: Int) {
    var bottomArrow = remember { mutableStateOf(0f) }
    var topArrow = remember { mutableStateOf(0f) }

    if (lastVisibleItemIndex != totalItemsCount - 1 && totalItemsCount > 0) {
        bottomArrow.value = 1f
    } else {
        bottomArrow.value = 0f
    }

    if (firstVisibleItemIndex > 0) {
        topArrow.value = 1f
    } else {
        topArrow.value = 0f
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .alpha(bottomArrow.value)
        ) {
            AnimateArrowIcon(Icons.Default.KeyboardArrowDown)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .alpha(topArrow.value)
        ) {
            AnimateArrowIcon(Icons.Default.KeyboardArrowUp)
        }
    }
}