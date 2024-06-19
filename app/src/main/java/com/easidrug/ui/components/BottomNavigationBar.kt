package com.easidrug.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.easidrug.bottomNavItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavigationBar(navController: NavController, pagerState: PagerState) {
    // Observe the current page of the pager to update the selected item
    val selectedItem = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()
    NavigationBar(
        modifier = Modifier
            .shadow(15.dp, RoundedCornerShape(30.dp))
            .padding(5.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = item.icon),
                        contentDescription = item.title,
                        Modifier.size(28.dp)
                    )
                },
                label = { Text(text = item.title) },
                selected = selectedItem == index,
                onClick = {
                    // Update pager state on click
                    coroutineScope.launch {
//                        pagerState.scrollToPage(index)
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}