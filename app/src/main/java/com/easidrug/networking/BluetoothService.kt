package com.easidrug.networking

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.easidrug.ui.Screens.showPopUpBluetooth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class BluetoothService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var bluetoothSocket: BluetoothSocket? = null

    private val discoveredDevices = mutableStateListOf<BluetoothDevice>()

    val isConnected = mutableStateOf(false)

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // When a device is found
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    discoveredDevices.add(device)
                    Log.d("BLS", "device found")
                    // Notify your UI or listeners here
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    // When discovery starts
                    Log.d("BLS", "discovery started")
                    // Notify your UI or listeners here
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // When discovery is finished
                    Log.d("BLS", "discovery finished")
                    // Notify your UI or listeners here
                }
            }
        }
    }

    private val receiverPairing = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    when (device.bondState) {
                        BluetoothDevice.BOND_BONDING -> {
                            Log.d("BLS", "Bonding...")
                        }

                        BluetoothDevice.BOND_BONDED -> {
                            Log.d("BLS", "Bonded")
                            connectSockets(device)
                        }

                        BluetoothDevice.BOND_NONE -> {
                            Log.d("BLS", "Not bonded")
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        discoveredDevices.clear()
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        }
        context.registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
//        try {
//            context.unregisterReceiver(receiver)
//        } catch (e: Exception) {
//            //When called and the receiver is not register, the app crashes. It should not!
//        }
    }

    fun getDiscoveredDevices(): List<BluetoothDevice> {
        return discoveredDevices
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            }
            context.registerReceiver(receiverPairing, filter)
            device.createBond()
        } else {
            connectSockets(device)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectSockets(device: BluetoothDevice) {
        Thread {
            try {
                isConnected.value = false
                bluetoothSocket?.close()
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT)
                        .show()
                    showPopUpBluetooth.value = false
                }
                Log.d("BLS", "Successful connection to: " + device.name)
                isConnected.value = true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("BLS", "Error connecting to socket: ${e.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Failed to connect to ${device.name}", Toast.LENGTH_SHORT)
                        .show()
                }
                isConnected.value = false
                // Handle connection error
            }
        }.start()
    }

    fun disconnectSockets() {
        try {
            bluetoothSocket?.close()
            isConnected.value = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendMessage(message: String) {
        val outputStream = bluetoothSocket?.outputStream
        try {
            outputStream?.write(message.toByteArray())
            outputStream?.flush()
            Log.d("BLS", "Message \"$message\" sent")
        } catch (e: IOException) {
            // Handle the error
        }
    }

////---------------------Server---------------------
//    private var serverSocket: BluetoothServerSocket? = null
//    private var communicationSocket: BluetoothSocket? = null
//    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    private val appName: String = "MyBluetoothApp"
//    @SuppressLint("MissingPermission")
//    fun startServer() {
//        Thread {
//            try {
//                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(appName, sppUuid)
//
//                // Keep listening until exception occurs or a socket is returned
//                while (true) {
//                    try {
//                        communicationSocket = serverSocket?.accept()
//                        Log.d("BluetoothServer", "connection accepted")
//                        // If a connection was accepted
//                        if (communicationSocket != null) {
//                            // Manage the connected socket in a separate thread
//                            manageConnectedSocket(communicationSocket!!)
////                            serverSocket?.close()
//                            break
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                        break
//                    }
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }.start()
//    }
//    private fun manageConnectedSocket(socket: BluetoothSocket) {
//        val buffer = ByteArray(1024)  // buffer store for the stream
//        var bytes: Int  // bytes returned from read()
//
//        // Get the input stream from the socket
//        val inputStream: InputStream?
//
//        try {
//            inputStream = socket.inputStream
//        } catch (e: IOException) {
//            Log.e("BluetoothServer", "Error occurred when creating input stream", e)
//            return
//        }
//
//        // Keep listening to the InputStream until an exception occurs
//        while (true) {
//            try {
//                // Read from the InputStream
//                bytes = inputStream.read(buffer)
//                val incomingMessage = String(buffer, 0, bytes)
//                Log.d("BluetoothServer", "Incoming Message: $incomingMessage")
//            } catch (e: IOException) {
//                Log.e("BluetoothServer", "Input stream was disconnected", e)
//                break
//            }
//        }
//    }
////---------------------Server---------------------
}