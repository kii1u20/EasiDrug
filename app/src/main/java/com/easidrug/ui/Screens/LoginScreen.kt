package com.easidrug.ui.Screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Space
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.easidrug.R
import com.easidrug.networking.BluetoothService
import com.easidrug.ui.Logo
import com.easidrug.ui.UIConnector
import com.easidrug.ui.components.PopUpNotification

val showPopUpBluetooth = mutableStateOf(false)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(connector: UIConnector) {
    val bluetoothService = remember { BluetoothService(connector.context) }

    val deviceName = remember { mutableStateOf("") }

    val wifiName = remember { mutableStateOf("") }
    val wifiPassword = remember { mutableStateOf("") }

    val sentToDevice = remember { mutableStateOf(false) }

    if (connector.userSessionManager.showDialog) {
        AlertDialog(
            onDismissRequest = { var showDialog = false },
            title = { Text("Error") },
            text = { Text("A device with that name already exists. Please enter a different one.") },
            confirmButton = {
                Button(onClick = {
                    connector.userSessionManager.showDialog = false
                    sentToDevice.value = false
                }) {
                    Text("Okay")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to EasiDrug, please sign in to continue",
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(end = 10.dp, top = 5.dp, start = 5.dp)
            )
            Spacer(modifier = Modifier.height(60.dp))
            Logo(animated = false)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = (connector.userSessionManager.azureResponse.value == "[]" || connector.userSessionManager.azureResponse.value == "Used device name")) {
                if (!bluetoothService.isConnected.value) {
                    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                        item {
                            Text(
                                text = "Let's first connect to your device using Bluetooth. Make sure your EasiDrug device is turned on, then click \"Search bluetooth devices\"",
                                textAlign = TextAlign.Center
                            )
                        }
                        item {
                            Button(onClick = {
                                bluetoothService.startDiscovery()
                                showPopUpBluetooth.value = true
                            }) {
                                Text(text = "Search bluetooth devices")
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        if (!sentToDevice.value) {
                            Text(
                                text = "Now, input the name and password of the Wi-Fi network you want to connect your device to, as well as a unique name for this EasiDrug device",
                                textAlign = TextAlign.Center
                            )
                            Row() {
                                OutlinedTextField(
                                    value = wifiName.value,
                                    onValueChange = { wifiName.value = it },
                                    modifier = Modifier
//                            .align(Alignment.CenterHorizontally),
                                        .padding(start = 10.dp, end = 10.dp)
                                        .weight(1f),
                                    label = { Text("Wi-Fi Name") },
                                )
                                OutlinedTextField(
                                    value = wifiPassword.value,
                                    onValueChange = { wifiPassword.value = it },
                                    modifier = Modifier
//                            .align(Alignment.CenterHorizontally),
                                        .padding(start = 10.dp, end = 10.dp)
                                        .weight(1f),
                                    label = { Text("Wi-Fi Password") },
                                )
                            }
                            OutlinedTextField(
                                value = deviceName.value,
                                onValueChange = { deviceName.value = it },
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 10.dp),
                                label = { Text("Device name") }
                            )
                            Button(onClick = {
                                connector.userSessionManager.signIn()
                                if (deviceName.value.isNotEmpty()) {
                                    connector.userSessionManager.userDevices.clear()
                                    connector.userSessionManager.userDevices.add(deviceName.value)
                                    connector.userSessionManager.selectedDevice.value =
                                        deviceName.value
                                    sentToDevice.value = true
                                    connector.userSessionManager.wifiName.value = wifiName.value
                                    connector.userSessionManager.wifiPassword.value = wifiPassword.value
                                    connector.userSessionManager.bluetoothService = bluetoothService
                                }
                            }) {
                                Text(text = "Send to device")
                            }
                        } else {
                            Text(
                                text = "Great, now sign in again to complete the process",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (connector.userSessionManager.azureResponse.value != "[]" && connector.userSessionManager.azureResponse.value != "Used device name" && !sentToDevice.value) {
            SignInButton(
                onClick = {
                    if (deviceName.value.isNotEmpty() && connector.userSessionManager.azureResponse.value != "Used device name") {
                        connector.userSessionManager.userDevices.add(deviceName.value)
                        connector.userSessionManager.selectedDevice.value = deviceName.value
                    }
                    connector.userSessionManager.signIn()
                }, modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 20.dp)
            )
        }
    }

    if (showPopUpBluetooth.value) {
        Dialog(onDismissRequest = {
            bluetoothService.stopDiscovery()
            showPopUpBluetooth.value = false
        }) {
            LazyColumn(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            40.dp
                        )
                    )
                    .size(600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    Text(
                        text = "Select the \"EasiDrug\" device when it's available",
                        textAlign = TextAlign.Center
                    )
                }
                items(items = bluetoothService.getDiscoveredDevices()) { device ->
                    val name = device.name
                    if (name !== null) {
                        Button(onClick = {
                            bluetoothService.stopDiscovery()
                            bluetoothService.disconnectSockets()
                            bluetoothService.connectToDevice(device)
                            Toast.makeText(
                                connector.context,
                                "Connecting to device...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Text(text = device.name)
                        }
                    }
                }
            }
        }
    }

//    LazyColumn() {
//        item {
//            Button(onClick = { bluetoothService.startDiscovery() }) {
//                Text(text = "Search bluetooth devices")
//            }
//            Button(onClick = {
//                bluetoothService.sendMessage("{name:\"S10\"" + "," + "password:\"12345678\"" + "," + "deviceName:\"demo7\"}")
//            }) {
//                Text(text = "send message")
//            }
//        }
//        items(items = bluetoothService.getDiscoveredDevices()) { device ->
//            val name = device.name
//            if (name !== null) {
//                Button(onClick = {
//                    bluetoothService.stopDiscovery()
//                    bluetoothService.disconnectSockets()
//                    bluetoothService.connectToDevice(device)
//                }) {
//                    Text(text = device.name)
//                }
//            } else {
////                Text(text = "null")
//            }
//        }
//    }


}

@Composable
fun SignInButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text("Sign in with Google")
    }
}
