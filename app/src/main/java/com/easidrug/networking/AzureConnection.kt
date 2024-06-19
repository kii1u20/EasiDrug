package com.easidrug.networking

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class AzureConnection() {
    private val scope = CoroutineScope(Dispatchers.IO) // Create a coroutine scope using the IO dispatcher

    val lastSyncLogs = mutableStateOf("")
    val lastSyncSchedule = mutableStateOf("")

    fun saveScheduleToCloud(payload: String, deviceName: String) {
        scope.launch { // Launch a coroutine in the scope
            try {
                val url =
                    URL("https://prod-12.uksouth.logic.azure.com/workflows/c7c7002e20f9413599e28aba8db01b4c/triggers/When_a_HTTP_request_is_received/paths/invoke/device/$deviceName?api-version=2016-10-01&sp=%2Ftriggers%2FWhen_a_HTTP_request_is_received%2Frun&sv=1.0&sig=HBy1Q7v8sZ1DhMm91pGCrCvn60zy5ft9cz34NK_229c") // Replace with your URL
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PUT"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(payload)
                        writer.flush()
                    }
                }

                val responseCode = conn.responseCode
                println("Response Code: $responseCode")

                // Handle the response accordingly
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    lastSyncSchedule.value = getCurrentDateTime()
                    // TODO: Handle successful response
                } else {
                    // TODO: Handle error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("azure", "Error saving schedule: ${e.message}")
            }
        }
    }

    fun saveSettingsToCloud(payload: String, deviceName: String) {
        scope.launch { // Launch a coroutine in the scope
            try {
                val url =
                    URL("https://prod-09.uksouth.logic.azure.com/workflows/ae47854b7dbe42fc841452ee0fee736c/triggers/When_a_HTTP_request_is_received/paths/invoke/device/$deviceName?api-version=2016-10-01&sp=%2Ftriggers%2FWhen_a_HTTP_request_is_received%2Frun&sv=1.0&sig=l06oZuOJhklqDPh33OZdwykSRmM2xXEMTfhg4mjXYos") // Replace with your URL
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PUT"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(payload)
                        writer.flush()
                    }
                }

                val responseCode = conn.responseCode
                println("Response Code: $responseCode")

                // Handle the response accordingly
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    lastSyncSchedule.value = getCurrentDateTime()
                    // TODO: Handle successful response
                } else {
                    // TODO: Handle error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("azure", "Error saving settings: ${e.message}")
            }
        }
    }

    suspend fun registerUser(payload: String): String {
        return withContext(Dispatchers.IO) { // Switch to background thread for network operation
            try {
                val url = URL("https://prod-03.uksouth.logic.azure.com:443/workflows/9e577ae3373446ccb1f78ba0efcb2c15/triggers/manual/paths/invoke?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=1ZVQ8eqwNYdQ_41aVskgc-YmIA5ZKisgWz3O7-2nPtY")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(payload)
                        writer.flush()
                    }
                }

                val responseCode = conn.responseCode
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }

                // Log response for debugging purposes
                Log.d("azure", "Response Code: $responseCode")
                Log.d("azure", "Response Body: $responseBody")

                responseBody // Return the response body
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("azure", "Error registering user: ${e.message}")
                "Error: ${e.message}" // Return error message as response
            }
        }
    }

    suspend fun getLogs(deviceName: String): String {
        return withContext(Dispatchers.IO) { // Use IO dispatcher for network operations
            val url = URL("https://prod-17.uksouth.logic.azure.com/workflows/aa31b37a9e634bcdbb3e6f8f6642fd87/triggers/manual/paths/invoke/device/$deviceName?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=RKsK6L4ZPnbOandExcprpOHRk88Cyl9i3pQPXgq_xJI")

            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val responseCode = conn.responseCode
                println("Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    lastSyncLogs.value = getCurrentDateTime()
                    BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                        reader.lines().collect(Collectors.joining("\n"))
                    }

                } else {
                    // Handle error accordingly
                    Log.e("azure", "Error getting logs: $responseCode")
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("azure", "Error getting logs: ${e.message}")
                ""
            }
        }
    }

    suspend fun getScheduleFromCloud(deviceName: String): String {
        return withContext(Dispatchers.IO) { // Use IO dispatcher for network operations
            val url = URL("https://prod-06.uksouth.logic.azure.com/workflows/1a2113915ec0484a9d233931d71c6742/triggers/manual/paths/invoke/device/${deviceName}?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=7tedp6FtlnBl5DCYvkD11ePXZeAna27oZNqihgsLTDw")

            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val responseCode = conn.responseCode
                println("Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    lastSyncSchedule.value = getCurrentDateTime()
                    BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                        reader.lines().collect(Collectors.joining("\n"))
                    }
                } else {
                    // Handle error accordingly
                    Log.e("azure", "Error getting schedule list: $responseCode")
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("azure", "Error getting schedule list: ${e.message}")
                ""
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, HH:mm")
        return currentDateTime.format(formatter)
    }
}