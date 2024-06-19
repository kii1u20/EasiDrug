package com.easidrug.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToRefreshView(content: @Composable () -> Unit, onRefresh: () -> Unit, refreshing: MutableState<Boolean>) {
    val refreshState = rememberPullRefreshState(refreshing = refreshing.value, onRefresh = {
        refreshing.value = true
        onRefresh()
    })

    Box(
        modifier = Modifier
            .pullRefresh(refreshState)

    ) {
        content()
        PullRefreshIndicator(
            state = refreshState,
            refreshing = refreshing.value,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}