package com.easidrug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.easidrug.ui.UIConnector

class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {

        val connector = UIConnector.getInstance(context)

        val pillName = intent.getStringExtra("PILL_NAME") ?: "Medication"

        connector.showPopupNotification(pillName)

        Log.d("NotificationReceiver", "Received notification for $pillName")
        val customMessage = intent.getStringExtra("CUSTOM_MESSAGE") ?: "It's time for your medication"

        sendNotification(context, pillName, customMessage)
    }
}
