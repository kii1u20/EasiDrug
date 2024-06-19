@file:OptIn(ExperimentalMaterial3Api::class)

package com.easidrug

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.easidrug.ui.components.BottomNavigationBar
import com.easidrug.ui.UIConnector
import com.easidrug.ui.Screens.AchievementsScreen
import com.easidrug.ui.Screens.HomeScreen
import com.easidrug.ui.Screens.LogsScreen
import com.easidrug.ui.Screens.MedicinesScreen
import com.easidrug.ui.Screens.SettingsScreen
import com.easidrug.ui.theme.EasiDrugTheme
import android.provider.Settings
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.easidrug.ui.Logo
import com.easidrug.ui.Screens.LoginScreen
import com.easidrug.ui.Screens.getLogs

class MainActivity : ComponentActivity() {
    lateinit var connector: UIConnector

    object GlobalVariables {
        val showNotificationsDialog = mutableStateOf(false)
        val showBatteryDialog = mutableStateOf(false)
    }

    @Composable
    fun showPermissionDialog() {
        if (GlobalVariables.showNotificationsDialog.value) {
            AlertDialog(
                onDismissRequest = { GlobalVariables.showNotificationsDialog.value = false },
                title = { Text("Permission Required") },
                text = { Text("This app requires the Alarms and Reminders permission. A new window will open, scroll until you see Alarms and Reminders, and allow it.") },
                confirmButton = {
                    TextButton(onClick = {
                        openAppInfoPage()
                        GlobalVariables.showNotificationsDialog.value = false
                    }) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        GlobalVariables.showNotificationsDialog.value = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    private val batteryOptimizationResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        GlobalVariables.showBatteryDialog.value = false
        connector.updateAlarms()
    }

    @Composable
    fun requestDisableBatteryOptimization() {
        if (GlobalVariables.showBatteryDialog.value) {
            AlertDialog(
                onDismissRequest = { GlobalVariables.showBatteryDialog.value = false },
                title = { Text("Battery Optimization") },
                text = { Text("You are going to get a pop up asking to disable battery optimisation for this app. This is necessary for the notifications to work properly. If you don't allow this, you will NOT receive any notifications!") },
                confirmButton = {
                    TextButton(onClick = {
                        val intent =
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${packageName}")
                            }
                        batteryOptimizationResultLauncher.launch(intent)
//                        GlobalVariables.showBatteryDialog.value = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { GlobalVariables.showBatteryDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            when (it.key) {
                android.Manifest.permission.POST_NOTIFICATIONS -> {
                    if (it.value) {
                        // Permission granted
                    } else {
                        // Permission denied
                    }
                }

                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (it.value) {
                        // Permission granted
                    } else {
                        // Permission denied
                    }
                }
            }
        }
    }
//    private fun requestNotificationsPermission() {
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (!notificationManager.areNotificationsEnabled()) {
//            Toast.makeText(this, "Notifications permission not granted!", Toast.LENGTH_SHORT).show()
//            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
//            startActivity(intent)
//        }
//    }

    private fun requestAllPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
        permissionLauncher.launch(permissions)
    }

    private fun checkPermissions(): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val bluetoothPermission =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH)

        return locationPermission == PackageManager.PERMISSION_GRANTED && bluetoothPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connector = UIConnector.getInstance(this)
        connector.loadSettingsFromJsonFile()
        connector.updateAchievementLists()
        createNotificationChannel(this)
        val medicationScheduler = MedicationScheduler(this, connector)

        try {
            medicationScheduler.scheduleAllMedications()
        } catch (e: SecurityException) {
            GlobalVariables.showBatteryDialog.value = true
        }

//        requestNotificationsPermission()
        requestAllPermissions()

        connector.userSessionManager.loadDeviceNameList()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(PowerManager::class.java)
            GlobalVariables.showBatteryDialog.value =
                !powerManager.isIgnoringBatteryOptimizations(packageName)
        }
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        setContent {
            EasiDrugTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    if (!alarmManager.canScheduleExactAlarms()) {
//                        Log.d("PERMISSION", "PERMISSION NOT GRANTED")
//                        GlobalVariables.showNotificationsDialog.value = true
//                        showPermissionDialog()
//                    }
                    requestDisableBatteryOptimization()
                    MainScreenView(connector)
                }
            }
        }
    }

    private fun openAppInfoPage() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

}

// Define the navigation items with their title, icon, and route
data class BottomNavItem(
    val title: String,
    val icon: Int,
    val route: String
)

// Create a list of bottom navigation items
val bottomNavItems = listOf(
    BottomNavItem("Home", R.drawable.home_icon, Screens.Home.route),
    BottomNavItem("Medicines", R.drawable.medication_icon, Screens.Medicines.route),
    BottomNavItem("Logs", R.drawable.log_icon, Screens.Logs.route),
    BottomNavItem("Score", R.drawable.achievement_icon, Screens.Achievements.route),
    BottomNavItem("Settings", R.drawable.settings_icon, Screens.Settings.route),
//    BottomNavItem("Settings", R.drawable.ic_launcher_foreground, Screens.Settings.route)
)

// Define the navigation routes
sealed class Screens(val route: String) {
    object Home : Screens("home_route")
    object Medicines : Screens("medicines_route")
    object Logs : Screens("logs_route")
    object Achievements : Screens("achievements_route")
    object Settings : Screens("settings_route")
}

// Create a composable function for the main screen view with a Scaffold
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenView(connector: UIConnector) {
    val refreshing = remember { mutableStateOf(true) }
    val refreshingLogs = remember { mutableStateOf(true) }
    if (connector.userSessionManager.isUserSignedIn.value) {
        LaunchedEffect(key1 = Unit) {
            connector.loadScheduleFromCloud(refreshing)
            getLogs(connector, refreshingLogs)
        }
        if (refreshing.value || refreshingLogs.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Logo(animated = true)
            }
        } else {
            val navController = rememberNavController()
            val pagerState = connector.pagerState.value

            Scaffold(
                bottomBar = {
                    BottomNavigationBar(navController = navController, pagerState = pagerState)
                }
            ) { contentPadding ->
                HorizontalPager(
                    pageCount = bottomNavItems.size,
                    state = pagerState,
                    modifier = Modifier.padding(contentPadding),
                    beyondBoundsPageCount = 2
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(connector = connector)
                        1 -> MedicinesScreen(connector = connector)
                        2 -> LogsScreen(connector = connector)
                        3 -> AchievementsScreen(connector = connector)
                        4 -> SettingsScreen(connector = connector)
                        else -> Text("Unknown Page")
                    }
                }
            }
        }
    } else {
        LoginScreen(connector = connector)
    }
}

