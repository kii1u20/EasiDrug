package com.easidrug.ui.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.easidrug.ui.components.AchievementCard
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.R
import com.easidrug.models.UserProgress
import com.easidrug.ui.components.LevelProgressSection
import com.easidrug.ui.UIConnector
import com.easidrug.ui.components.PopUpNotification
import com.easidrug.ui.components.PullToRefreshView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val refreshing = mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AchievementsScreen(connector: UIConnector) {
    if (connector.showPopup.value && connector.pagerState.value.currentPage == 3) {
        PopUpNotification(
            onDismissRequest = {
                connector.showPopup.value = false
                if (connector.shownNotification.value) {
                    connector.shownNotification.value = false

                }
            }, connector = connector,
            day = connector.current_pill
        )
    }


    LaunchedEffect(key1 = Unit) {
        refreshAchievements(connector)
    }

    val userProgress = connector.userProgress
    val lockedAchievements = connector.lockedAchievements
    val unlockedAchievements = connector.unlockedAchievements

    val reachableLockedAchievements = lockedAchievements.filter { achievement ->
        // Achievement is reachable if it has no prerequisite, or the prerequisite is unlocked
        achievement.prerequisiteId == null || connector.achievements.any {
            it.id == achievement.prerequisiteId && it.isUnlocked
        }
    }


    // Use Scaffold for layout with BottomAppBar
    Scaffold(
    ) { innerPadding ->
        PullToRefreshView(onRefresh = {
            CoroutineScope(Dispatchers.IO).launch {
                refreshAchievements(
                    connector
                )
            }
        }, refreshing = refreshing, content = {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 5.dp, start = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Achievements",
                        fontSize = 25.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.achievement_icon),
                        contentDescription = "Achievements Icon",
                        modifier = Modifier.size(35.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
//                contentScale = ContentScale.FillHeight
                    )
                }
                LevelProgressSection(userProgress = userProgress)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyColumn {
                        item {
                            Text(
                                "Unlocked Achievements",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(unlockedAchievements) { achievement ->
                            val animatable = remember { Animatable(0f) }
                            LaunchedEffect(key1 = achievement) {
                                animatable.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                                        stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                                    )
                                )
                            }
                            AchievementCard(achievement = achievement, animatable = animatable)
                        }
                        item {
                            Text(
                                "Locked Achievements",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(reachableLockedAchievements) { achievement ->
                            val animatable = remember { Animatable(0f) }
                            LaunchedEffect(key1 = achievement) {
                                animatable.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy, // This will cause it to overshoot and then come to rest
                                        stiffness = Spring.StiffnessLow // Lower stiffness means a slower movement
                                    )
                                )
                            }
                            AchievementCard(achievement = achievement, animatable = animatable)
                        }
                    }
                }
            }
        })
    }
}

suspend fun refreshAchievements(connector: UIConnector) {
    refreshing.value = true
    connector.userProgress =
        UserProgress(mutableStateOf(1), mutableStateOf(0), mutableStateOf(10))

    getLogs(connector)

    connector.updateAchievements()

    for (achievement in connector.achievements.toList()) {
        if (achievement.isUnlocked) {
            connector.addUserPoints(achievement.points)
        }
    }
    for (log in logsList) {
        if (log.isOnTime.equals("True", ignoreCase = true)) {
            connector.addUserPoints(1)
        }
    }
    refreshing.value = false
}

