package com.easidrug.ui.Screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.R
import com.easidrug.ui.UIConnector
import com.easidrug.ui.components.PopUpNotification

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(connector: UIConnector) {
    if (connector.showPopup.value && connector.pagerState.value.currentPage == 4) {
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
    Column() {
        Row(
            modifier = Modifier
                .padding(top = 5.dp, start = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Settings",
                fontSize = 25.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.settings_icon),
                contentDescription = "Settings",
                modifier = Modifier.size(35.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
//                contentScale = ContentScale.FillHeight
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                settingsInputElement(connector, connector.gracePeriod, "Grace Period:")
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "Device:",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DeviceDropdown(connector)
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "Buzzer:",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = connector.useBuzzer.value,
                        onCheckedChange = {
                            connector.useBuzzer.value = it
                            connector.saveSettingsJsonToFile()
                        },
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            item {
                Button(
                    onClick = { connector.userSessionManager.clearSession() },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(text = "Sign Out")
                }
            }
        }
    }
}

@Composable
fun DeviceDropdown(connector: UIConnector) {
    val expanded = remember { mutableStateOf(false) }
    val selectedDevice = remember { connector.userSessionManager.selectedDevice }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { expanded.value = true },
//            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = selectedDevice.value.ifEmpty { "Select a device" },
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            connector.userSessionManager.userDevices.forEach { device ->
                DropdownMenuItem(text = { Text(text = device) },
                    onClick = {
                        selectedDevice.value = device
//                        connector.userSessionManager.selectedDevice.value = device
                        expanded.value = false
                        connector.loadScheduleFromCloud()
                    })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsInputElement(connector: UIConnector, inputValue: MutableState<String>, label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(color = MaterialTheme.colorScheme.primaryContainer)

    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Text(
                    text = label,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            TextField(
                value = inputValue.value,
                onValueChange = {
                    inputValue.value = it
                    connector.saveSettingsJsonToFile()
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(10.dp) // Add some space between the TextFields
                    .clip(RoundedCornerShape(30.dp))// Each TextField gets half the width
                    .weight(0.5f),
                label = { Text("Minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
