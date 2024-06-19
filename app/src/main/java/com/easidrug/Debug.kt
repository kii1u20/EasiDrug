package com.easidrug

import android.content.Context
import com.easidrug.ui.UIConnector
import kotlin.random.Random

fun debugAdd(connector: UIConnector) {

    val random = Random
    val randomString = ByteArray(10)

    connector.addSchedule(
        random.nextBytes(randomString).toString(),
        "Monday",
        random.nextInt(0, 24),
        random.nextInt(0, 60)
    )
}

fun testNotification(context: Context) {
    sendNotification(context, "Test Title", "Test Message")
}
