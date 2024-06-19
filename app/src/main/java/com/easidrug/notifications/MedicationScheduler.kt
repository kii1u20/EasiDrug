package com.easidrug

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.easidrug.ui.UIConnector
import java.util.*

class MedicationScheduler(private val context: Context, private val connector: UIConnector) {
    private val requestCodeMap = mutableMapOf<String, Int>()

    fun scheduleAllMedications() {
        clearAllAlarms()
        val scheduleList = connector.getScheduleList()
        val groupedSchedules = groupMedicationsByTime(scheduleList)
        try {
            for ((timeInMillis, pillNames) in groupedSchedules) {
                scheduleNotification(context, timeInMillis, pillNames.joinToString(", "))
            }
        } catch (e: SecurityException) {
            Log.e("MedScheduler", "Failed to schedule notifications", e)
            MainActivity.GlobalVariables.showBatteryDialog.value = true
        }
    }

    private fun groupMedicationsByTime(scheduleList: List<UIConnector.ScheduleEntry>): Map<Long, List<String>> {
        val grouped = mutableMapOf<Long, MutableList<String>>()

        for (scheduleEntry in scheduleList) {
            for (schedule in scheduleEntry.schedule) {
                val timeInMillis = getTimeInMillis(schedule.day, schedule.time.hour, schedule.time.minute)
                val pillNames = grouped.getOrPut(timeInMillis) { mutableListOf() }
                pillNames.add(scheduleEntry.pillName)
            }
        }

        return grouped
    }

    private fun getTimeInMillis(day: String, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayOfWeek = convertDayToCalendarDay(day)
        dayOfWeek?.let {
            calendar.set(Calendar.DAY_OF_WEEK, it)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    private fun convertDayToCalendarDay(day: String): Int? {
        return when (day.lowercase(Locale.ROOT)) {
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            "sunday" -> Calendar.SUNDAY
            else -> null
        }
    }

    private fun createNotificationIntent(pillName: String): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra("PILL_NAME", pillName)
            putExtra("CUSTOM_MESSAGE", "Time to take your $pillName")
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(context: Context, timeInMillis: Long, pillName: String) {
        val requestCode = generateRequestCode(pillName, timeInMillis)
        requestCodeMap[pillName] = requestCode
        val intent = createNotificationIntent(pillName)

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Log.d("MedScheduler", "Scheduled notification for $pillName at ${Date(timeInMillis)}")
    }


    private fun generateRequestCode(pillName: String, timeInMillis: Long): Int {
        val nameHash = pillName.hashCode()
        return ((timeInMillis and Int.MAX_VALUE.toLong()) + nameHash).toInt() // Combine time and name hash
    }


    private fun clearAllAlarms() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for ((_, requestCode) in requestCodeMap) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        requestCodeMap.clear() // Clear the map after canceling all alarms
    }


}
