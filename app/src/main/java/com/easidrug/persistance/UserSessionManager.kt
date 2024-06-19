package com.easidrug.persistance

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.easidrug.networking.AzureConnection
import com.easidrug.networking.BluetoothService
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserSessionManager(private val context: Context) {
    //    ----------------Azure----------------
    private val azure = AzureConnection()

    val azureResponse = mutableStateOf("")

    @Serializable
    data class ResponseData(
        val devices: List<String>
    )

    //    ----------------Azure----------------
    private val scope = CoroutineScope(Dispatchers.IO)
    var showDialog by mutableStateOf(false)

    //    ----------------User----------------
    @Serializable
    val userDevices = mutableStateListOf<String>()

    val selectedDevice = mutableStateOf("")

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE)

    val isUserSignedIn: MutableState<Boolean> = mutableStateOf(getSignedInUsername() != null)

    val wifiName: MutableState<String> = mutableStateOf("")
    val wifiPassword: MutableState<String> = mutableStateOf("")
    var bluetoothService: BluetoothService? = null

    val profilePictureUri = mutableStateOf<Uri?>(null)
//    ----------------User----------------

    private fun setUserSignedIn(username: String?) {
        sharedPreferences.edit().putString("SignedInUsername", username).apply()

        val userDevicesJson = Json.encodeToString(userDevices.toList())
        sharedPreferences.edit().putString("UserDevices", userDevicesJson).apply()

        sharedPreferences.edit().putString("ProfilePictureUri", profilePictureUri.value.toString())
            .apply()

        isUserSignedIn.value = username != null

        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_LONG).show()
        }
    }

    fun loadDeviceNameList() {
        val deviceNameList = sharedPreferences.getString("UserDevices", null)
        if (deviceNameList != null) {
            userDevices.addAll(
                Json.decodeFromString(
                    ListSerializer(String.serializer()),
                    deviceNameList
                )
            )
            selectedDevice.value = userDevices[0]
        }

        val uriString = sharedPreferences.getString("ProfilePictureUri", null)
        profilePictureUri.value = uriString?.let { Uri.parse(it) }
    }

//----------------App Settings----------------

    fun saveMedicinesView(boxView: Boolean) {
        sharedPreferences.edit().putBoolean("BoxView", boxView).apply()
    }

    fun loadMedicinesView(): Boolean {
        return sharedPreferences.getBoolean("BoxView", false)
    }

    //----------------App Settings----------------
    fun getSignedInUsername(): String? {
        return sharedPreferences.getString("SignedInUsername", null)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
        isUserSignedIn.value = false
        userDevices.clear()
        selectedDevice.value = ""
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Signed out successfully!", Toast.LENGTH_LONG).show()
        }
    }

//----------------Google Sign In----------------

    private var retrySignIn = true // Flag to control retry

    private var googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("769430406810-v39fdgclo8k8q25p6c6nuhf53hvt0ied.apps.googleusercontent.com")
        .build()

    private var signInWithGoogleOption: GetSignInWithGoogleOption =
        GetSignInWithGoogleOption.Builder("769430406810-v39fdgclo8k8q25p6c6nuhf53hvt0ied.apps.googleusercontent.com")
            .build()

    private val credentialManager = CredentialManager.create(context = context)

    private var getCredRequest: GetCredentialRequest = GetCredentialRequest.Builder()
        .setPreferImmediatelyAvailableCredentials(false)
//        .addCredentialOption(googleIdOption)
        .addCredentialOption(signInWithGoogleOption)
        .build()

    fun signIn() {
        scope.launch {
            try {
                val result = getCredentialFromManager()
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                handleSignInException(e)
            }
        }
    }

    private suspend fun getCredentialFromManager(): GetCredentialResponse {
        return credentialManager.getCredential(
            context = context,
            request = getCredRequest
        )
    }

    private fun handleSignInException(e: GetCredentialException) {
        Log.d("ERROR", e.message.toString())
        if (retrySignIn) {
            updateGoogleIdOptionForRetry()
            retrySignIn = false
            signIn()
        }
    }

    private fun updateGoogleIdOptionForRetry() {
        googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("769430406810-v39fdgclo8k8q25p6c6nuhf53hvt0ied.apps.googleusercontent.com")
            .build()
        getCredRequest = GetCredentialRequest.Builder()
            .setPreferImmediatelyAvailableCredentials(false)
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> handleCustomCredential(credential)
            else -> handleUnrecognizedCredential()
        }
    }

    private fun handleCustomCredential(credential: CustomCredential) {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                handleAzureResponse(googleIdTokenCredential)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("ERROR", "Received an invalid google id token response", e)
            }
        } else {
            Log.e("ERROR", "Unexpected type of credential")
        }
    }

    private fun handleAzureResponse(googleIdTokenCredential: GoogleIdTokenCredential) {
        scope.launch {
            val payload = createPayload(googleIdTokenCredential)
            azureResponse.value = azure.registerUser(payload)
            if (azureResponse.value != "" && azureResponse.value != "[]" && azureResponse.value != "Used device name") {
                handleUserRegistrationOrLogin(googleIdTokenCredential)
            } else if (azureResponse.value == "Used device name") {
                showDialog = true
            }
        }
    }

    private fun createPayload(googleIdTokenCredential: GoogleIdTokenCredential): String {
        return "{\"id\": \"${googleIdTokenCredential.id}\", \"devices\": ${
            Json.encodeToString(
                userDevices.toList()
            )
        }}"
    }

    private fun handleUserRegistrationOrLogin(googleIdTokenCredential: GoogleIdTokenCredential) {
        if (azureResponse.value != "Created user account and registered the device") {
            val parsedData = Json.decodeFromString<ResponseData>(azureResponse.value)
            userDevices.addAll(parsedData.devices)
            selectedDevice.value = userDevices[0]
        }
        profilePictureUri.value = googleIdTokenCredential.profilePictureUri
        bluetoothService?.sendMessage("{name:\"" + wifiName.value + "\"," + "password:\"" + wifiPassword.value + "\"," + "deviceName:\"" + selectedDevice.value + "\"}")
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Sent to information to device", Toast.LENGTH_SHORT).show()
        }
        setUserSignedIn(googleIdTokenCredential.givenName.toString())
    }

    private fun handleUnrecognizedCredential() {
        Log.e("ERROR", "Unexpected type of credential")
    }
//----------------Google Sign In----------------
}
