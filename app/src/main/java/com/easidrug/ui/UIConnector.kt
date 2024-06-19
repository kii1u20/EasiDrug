package com.easidrug.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.easidrug.MedicationScheduler
import com.easidrug.models.Achievement
import com.easidrug.models.UserProgress
import com.easidrug.networking.AzureConnection
import com.easidrug.persistance.PdfLogger
import com.easidrug.persistance.UserSessionManager
import com.easidrug.persistance.readStringFromFile
import com.easidrug.persistance.writeStringToFile
import com.easidrug.ui.Screens.LogData
import com.easidrug.ui.Screens.TimestampInfo
import com.easidrug.ui.Screens.json
import com.easidrug.ui.Screens.logsList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs


class UIConnector(val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val medicationScheduler = MedicationScheduler(context, this)

    val userSessionManager = UserSessionManager(context)

    val showPopup = mutableStateOf(false)

    var current_pill = "None"

    val achievements_list = listOf(
        Achievement(
            "7_day_streak", "7-Day Streak",
            "Take medication correctly for 7 consecutive days.", 10, 1, false, null, "14_day_streak"
        ),
        Achievement(
            "14_day_streak",
            "14-Day Streak",
            "Take medication correctly for 14 consecutive days.",
            20,
            2,
            false,
            "7_day_streak",
            "30_day_streak"
        ),
        Achievement(
            "30_day_streak",
            "30-Day Streak",
            "Take medication correctly for 30 consecutive days.",
            30,
            3,
            false,
            "14_day_streak",
            null
        ),
        Achievement(
            id = "early_bird",
            name = "Early Bird",
            description = "Take your morning medication before 8 AM for a week.",
            points = 15,
            tier = 1,
            isUnlocked = false,
            prerequisiteId = null,
            nextTierId = "early_bird_pro"
        ),
        Achievement(
            id = "early_bird_pro",
            name = "Early Bird Pro",
            description = "Take your morning medication before 8 AM for 30 days.",
            points = 25,
            tier = 2,
            isUnlocked = false,
            prerequisiteId = "early_bird",
            nextTierId = null
        ),
        Achievement(
            id = "perfect_week",
            name = "Perfect Week",
            description = "Take all medications at the right time for an entire week.",
            points = 20,
            tier = 1,
            isUnlocked = false,
            prerequisiteId = null,
            nextTierId = "perfect_month"
        ),
        Achievement(
            id = "perfect_month",
            name = "Perfect Month",
            description = "Take all medications at the right time for 30 consecutive days.",
            points = 50,
            tier = 2,
            isUnlocked = false,
            prerequisiteId = "perfect_week",
            nextTierId = null
        ),
        Achievement(
            id = "oops_i_did_it_again",
            name = "Oops, I Did It Again",
            description = "Miss two consecutive medications.",
            points = -30,
            tier = 1,
            isUnlocked = false,
            prerequisiteId = null,
            nextTierId = null
        ),
        Achievement(
            id = "pilled_up",
            name = "Pilled Up",
            description = "Have one or more unique medicine(s) scheduled.",
            points = 10,
            tier = 1,
            isUnlocked = false,
            prerequisiteId = null,
            nextTierId = "pilled_up_pro"
        ),
        Achievement(
            id = "pilled_up_pro",
            name = "Pilled Up Pro",
            description = "Have five or more unique medicines scheduled.",
            points = 20,
            tier = 2,
            isUnlocked = false,
            prerequisiteId = "pilled_up",
            nextTierId = "pilled_up_master"
        ),
        Achievement(
            id = "pilled_up_master",
            name = "Pilled Up Master",
            description = "Have ten or more unique medicines scheduled.",
            points = 30,
            tier = 3,
            isUnlocked = false,
            prerequisiteId = "pilled_up_pro",
            nextTierId = null
        )
    )


    @Serializable
    var achievements = mutableStateListOf<Achievement>()
        private set

    init {
        populateInitialAchievements()
    }

    private fun populateInitialAchievements() {
        achievements.addAll(achievements_list)
    }

    var lockedAchievements = mutableStateListOf<Achievement>()
        private set
    var unlockedAchievements = mutableStateListOf<Achievement>()
        private set

    fun updateAchievementLists() {
        lockedAchievements.clear()
        unlockedAchievements.clear()
        lockedAchievements.addAll(achievements.filter { !it.isUnlocked })
        unlockedAchievements.addAll(achievements.filter { it.isUnlocked })
    }

    fun unlockAchievement(achievementId: String) {
        val achievement = achievements.find { it.id == achievementId }
        achievement?.let {
            it.unlock()
            // addUserPoints(it.points)
//            saveAchievementsJsonToFile()
//            updateAchievementLists()
        }
    }

    fun lockAchievements() {
        achievements.forEach { it.lock() }
    }

    var userProgress = UserProgress(mutableStateOf(1), mutableStateOf(0), mutableStateOf(10))

    fun addUserPoints(points: Int) {
        var remainingPoints = userProgress.currentPoints.value + points
        if (points >= 0) {
            if (remainingPoints >= userProgress.pointsToNextLevel.value) {
                // Calculate excess points
                val excessPoints = remainingPoints - userProgress.pointsToNextLevel.value

                // User levels up and carry over excess points to the new level
                userProgress.currentPoints.value = excessPoints
                userProgress.currentLevel.value = userProgress.currentLevel.value + 1
                userProgress.pointsToNextLevel.value =
                    calculatePointsForNextLevel(userProgress.currentLevel.value + 1)
            } else {
                // Just update the points if they haven't reached the next level
                userProgress.currentPoints.value = remainingPoints
            }
        } else {
            while (remainingPoints < 0 && userProgress.currentLevel.value > 1) {
                userProgress.currentLevel.value = userProgress.currentLevel.value - 1
                userProgress.pointsToNextLevel.value =
                    calculatePointsForNextLevel(userProgress.currentLevel.value + 1)
                remainingPoints += userProgress.pointsToNextLevel.value
            }
            userProgress.currentPoints.value = Math.max(remainingPoints, 0)
        }
    }

    fun calculatePointsForNextLevel(level: Int): Int {
        return level * 10 // Example: each level requires 100 more points than the last
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UIConnector? = null

        fun getInstance(context: Context): UIConnector {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UIConnector(context).also { INSTANCE = it }
            }
        }
    }

    fun showPopupNotification(pillName: String) {
        showPopup.value = true
        current_pill = pillName
    }


    @Serializable
    data class Time(val hour: Int, val minute: Int) : Comparable<Time> {
        override fun compareTo(other: Time): Int {
            return when {
                hour != other.hour -> hour - other.hour
                else -> minute - other.minute
            }
        }

        operator fun minus(other: Time): Time {
            var diffHour = this.hour - other.hour
            var diffMinute = this.minute - other.minute

            if (diffMinute < 0) {
                diffMinute += 60
                diffHour -= 1
            }

            return Time(diffHour, diffMinute)
        }

        operator fun plus(other: Time): Time {
            var newHour = this.hour + other.hour
            var newMinute = this.minute + other.minute

            if (newMinute >= 60) {
                newMinute -= 60
                newHour += 1
            }

            // Handle the case where hours exceed 24, if needed
            // newHour %= 24

            return Time(newHour, newMinute)
        }

        override fun toString(): String {
            return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        }
    }

    @Serializable
    data class DayTimePair<T, U>(
        @SerialName("day") val day: String,
        @SerialName("time") val time: Time
    )

    @Serializable
    data class ScheduleEntry(
        val pillName: String,
        val schedule: MutableList<DayTimePair<String, Time>>,
        val gracePeriod: String
    )

    private val scheduleList = mutableStateListOf<ScheduleEntry>()

    @Serializable
    data class SimplifiedScheduleEntry(val day: DayTimePair<String, Time>, val gracePeriod: String)

    val networkHandler = AzureConnection()

    val showBoxView = mutableStateOf(false)

    @Serializable
    data class Settings(
        var gracePeriod: String,
        var useBuzzer: Boolean
    )

    val gracePeriod = mutableStateOf("0")
    val useBuzzer = mutableStateOf(true)

    var settings = Settings(gracePeriod.value, useBuzzer.value)

    val shownNotification = mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    val pagerState = mutableStateOf(PagerState(initialPage = 0))

    fun addSchedule(pillName: String, day: String, hour: Int, minute: Int) {
        val time = Time(hour, minute)
        val index = scheduleList.indexOfFirst { it.pillName == pillName }

        if (index != -1) {
            // Pill already exists, add to its schedule
            // Clone the current schedule and add the new time
            val updatedSchedule = scheduleList[index].schedule + (DayTimePair(day, time))
            // Create a new ScheduleEntry with the updated schedule
            val newEntry = scheduleList[index].copy(schedule = updatedSchedule.toMutableList())
            // Replace the existing entry with the new one
            scheduleList[index] = newEntry
        } else {
            // New pill, add to list
            scheduleList.add(ScheduleEntry(pillName, mutableListOf(DayTimePair(day, time)), "0"))
        }
        saveScheduleJsonToFile(scheduleListToJson(scheduleList))
        updateAlarms()
    }

    fun addScheduleEntry(entry: ScheduleEntry) {
        val index = scheduleList.indexOfFirst { it.pillName == entry.pillName }

        if (index != -1) {
            // Pill already exists, add to its schedule
            // Clone the current schedule and add the new time
            val updatedSchedule = scheduleList[index].schedule + entry.schedule
            // Create a new ScheduleEntry with the updated schedule
            val newEntry = scheduleList[index].copy(schedule = updatedSchedule.toMutableList())
            // Replace the existing entry with the new one
            scheduleList[index] = newEntry
        } else {
            // New pill, add to list
            scheduleList.add(entry)
        }
        saveScheduleJsonToFile(scheduleListToJson(scheduleList))
        updateAlarms()
    }

    fun updateSchedule(ogName: String, entry: ScheduleEntry) {
        val index = scheduleList.indexOfFirst { it.pillName == ogName }
        if (entry.schedule.size == 0) {
            deleteSchedule(ogName)
            return
        }
        scheduleList[index] = entry
        saveScheduleJsonToFile(scheduleListToJson(scheduleList))
        updateAlarms()
    }


    fun deleteSchedule(pillName: String) {
        val index = scheduleList.indexOfFirst { it.pillName == pillName }
        if (index != -1) {
            scheduleList.removeAt(index)
            saveScheduleJsonToFile(scheduleListToJson(scheduleList))
            updateAlarms()
        }
    }

    fun removeScheduleFromList(dayTime: String, pillName: String) {
        val (dayPart, timePart) = dayTime.split(" ")
        val isAM = timePart.equals("AM", ignoreCase = true)

        scheduleList.find { it.pillName == pillName }?.let { scheduleEntry ->
            // Create a new list with the entries that don't match the criteria
            val updatedSchedule = scheduleEntry.schedule.filterNot { entry ->
                val (entryDay, entryTime) = entry
                entryDay.equals(dayPart, ignoreCase = true) &&
                        ((isAM && entryTime.hour < 12) || (!isAM && entryTime.hour >= 12))
            }.toMutableList()

            // Create a new ScheduleEntry with the updated schedule
            val updatedEntry = ScheduleEntry(pillName, updatedSchedule, scheduleEntry.gracePeriod)
            // Use the updateSchedule function to update the entry in the scheduleList
            updateSchedule(pillName, updatedEntry)
        }
    }


    fun getScheduleList(): MutableList<ScheduleEntry> {
        return scheduleList
    }

    fun formatScheduleNew(dayTimePair: DayTimePair<String, Time>): String {
        val (day, time) = dayTimePair
        return "${day}, ${time.hour.toString().padStart(2, '0')}:${
            time.minute.toString().padStart(2, '0')
        }"
    }


    fun loadScheduleFromJsonFile() {
        val newJsonString = context.readStringFromFile("scheduleList.json")
        if (newJsonString != "") {
            scheduleList.clear()
            scheduleList.addAll(Json.decodeFromString(newJsonString))
        }
    }

    fun loadScheduleFromCloud(refreshing: MutableState<Boolean> = mutableStateOf(false)) {
        scope.launch {
            refreshing.value = true
            val scheduleJson =
                networkHandler.getScheduleFromCloud(userSessionManager.selectedDevice.value)
            if (scheduleJson != "") {
                scheduleList.clear()
                scheduleList.addAll(Json.decodeFromString(scheduleJson))
            }
            refreshing.value = false
        }
    }

    private fun saveScheduleJsonToFile(jsonString: String) {
        scope.launch {
            context.writeStringToFile("scheduleList.json", jsonString)
        }
        networkHandler.saveScheduleToCloud(
            "{\"id\": \"schedule\", \"schedule\": $jsonString}",
            userSessionManager.selectedDevice.value
        )
        networkHandler.saveScheduleToCloud(
            "{\"id\": \"simpleSchedule\", \"schedule\": ${
                Json.encodeToString(
                    formatScheduleList()
                )
            }", userSessionManager.selectedDevice.value
        )
    }

    fun formatScheduleList(): MutableList<SimplifiedScheduleEntry> {
        val uniqueDayTimePairs = scheduleList.flatMap { it.schedule }.distinct()
        val simplifiedSchedule = mutableListOf<SimplifiedScheduleEntry>()

        for (dayTime in uniqueDayTimePairs) {
            val pillsAtThisTime = scheduleList.filter { dayTime in it.schedule }
            val minGracePeriod = findMinimumGracePeriodForPills(pillsAtThisTime.map { it.pillName })
            simplifiedSchedule.add(SimplifiedScheduleEntry(dayTime, minGracePeriod))
        }

        return simplifiedSchedule
    }

    fun loadSettingsFromJsonFile() {
        val newJsonString = context.readStringFromFile("settings.json")
        if (newJsonString != "") {
            settings = Json.decodeFromString(Settings.serializer(), newJsonString)
            gracePeriod.value = settings.gracePeriod
            useBuzzer.value = settings.useBuzzer
        }
        showBoxView.value = userSessionManager.loadMedicinesView()
    }

    fun saveSettingsJsonToFile() {
        settings.gracePeriod = gracePeriod.value
        settings.useBuzzer = useBuzzer.value
        scope.launch {
            context.writeStringToFile(
                "settings.json",
                Json.encodeToString(Settings.serializer(), settings)
            )
            networkHandler.saveSettingsToCloud(
                "{\"id\": \"settings\", \"useBuzzer\": " + settings.useBuzzer,
                userSessionManager.selectedDevice.value
            )
        }
    }

    fun saveProgressJsonToFile() {
        scope.launch {
            context.writeStringToFile(
                "progress.json",
                Json.encodeToString(UserProgress.serializer(), userProgress)
            )
        }
    }

    fun saveAchievementsJsonToFile() {
        scope.launch {
            context.writeStringToFile(
                "achievements.json",
                Json.encodeToString(achievements.toList())
            )
        }
    }

    fun loadAchievementsFromJsonFile() {
        val jsonStr = context.readStringFromFile("achievements.json")
        val achievementsList: List<Achievement> =
            if (jsonStr == "") emptyList() else
                Json.decodeFromString(jsonStr)
        achievements.clear()
        achievements.addAll(achievementsList)
    }

    fun loadProgressFromJsonFile() {
        val newJsonString = context.readStringFromFile("progress.json")
        if (newJsonString != "") {
            userProgress = Json.decodeFromString(UserProgress.serializer(), newJsonString)
        }
    }


    fun getPillsForDay(label: String): List<ScheduleEntry> {
        val (day, whenPart) = label.split(" ")

        val timeRange = if (whenPart.equals("AM", ignoreCase = true)) {
            0 until 12
        } else {
            12 until 24
        }

        return scheduleList.filter { entry ->
            entry.schedule.any {
                it.day.equals(day, ignoreCase = true) && it.time.hour in timeRange
            }
        }
    }

    fun getPillsAfterCurrentTime(label: String, currentTime: Time): List<String> {
        val (day, whenPart) = label.split(" ")

        val timeRange = if (whenPart.equals("AM", ignoreCase = true)) {
            0 until 12
        } else {
            12 until 24
        }

        return scheduleList.flatMap { entry ->
            entry.schedule.filter {
                it.day.equals(day, ignoreCase = true) &&
                        it.time.hour in timeRange &&
                        (it.time.hour > currentTime.hour || (it.time.hour == currentTime.hour && it.time.minute > currentTime.minute))
            }.map { Pair(entry.pillName, it.time) }
        }.distinct()
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun scheduleListToJson(scheduleList: MutableList<ScheduleEntry>): String {
        return Json.encodeToString(scheduleList)
    }

    fun isTimeAMOrPM(timePair: DayTimePair<String, Time>, amOrPm: String): Boolean {
        val isAM = timePair.time.hour < 12
        return (isAM && amOrPm.equals("AM", ignoreCase = true)) ||
                (!isAM && amOrPm.equals("PM", ignoreCase = true))
    }

    fun findNextPill(label: String): Time? {
        val (day, whenPart) = label.split(" ")

        val timeRange = if (whenPart.equals("AM", ignoreCase = true)) {
            0 until 12
        } else {
            12 until 24
        }

        val currentTime = Time(LocalDateTime.now().hour, LocalDateTime.now().minute)

        return scheduleList
            .flatMap { entry ->
                entry.schedule.filter {
                    it.day.equals(day, ignoreCase = true) && it.time.hour in timeRange
                }.map { Pair(entry.pillName, it.time) }
            }
            .filter { it.second > currentTime }
            .minByOrNull { it.second }
            ?.second
    }

    fun findMinimumGracePeriodForPills(pillNames: List<String>): String {
        val filteredList = scheduleList.filter { it.pillName in pillNames }
        if (filteredList.isEmpty()) return ""

        val minMinutes = filteredList.minOf { it.gracePeriod.toInt() }
        return minMinutes.toString()
    }

    fun updateAlarms() {
        medicationScheduler.scheduleAllMedications()
        Log.d("Connector", "Updated alarms")
    }

    fun checkConsecutiveOnTime(daysRequired: Int): Boolean {
        val sortedLogs = logsList.sortedWith(compareBy<LogData> { it.timestamp.year }
            .thenBy { it.timestamp.month }
            .thenBy { it.timestamp.day })

        var maxStreak = 0
        var currentStreak = 0
        var lastDate: LocalDate? = null
        var wasOnTimeAnytimeDuringDay = false

        for (log in sortedLogs) {
            val currentDate =
                LocalDate.of(log.timestamp.year, log.timestamp.month, log.timestamp.day)
            val isOnTime = log.isOnTime.equals("True", ignoreCase = true) // Convert to Boolean

            // If it's the same day, check if the log is on time.
            if (lastDate != null && currentDate.isEqual(lastDate)) {
                if (isOnTime) {
                    wasOnTimeAnytimeDuringDay = true
                }
            } else {
                // New day: reset wasOnTimeAnytimeDuringDay.
                if (wasOnTimeAnytimeDuringDay) {
                    currentStreak++  // Increment streak only if the previous day was on time at least once.
                } else {
                    currentStreak = 0  // Reset streak if the previous day was never on time.
                }
                wasOnTimeAnytimeDuringDay = isOnTime  // Set for new day.
                lastDate = currentDate  // Update lastDate to current date.
            }

            // Update maxStreak if the currentStreak is greater.
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak
            }
        }

        // Handle the last day after loop ends.
        if (wasOnTimeAnytimeDuringDay) {
            currentStreak++
        }
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
        }

        return maxStreak >= daysRequired
    }

    fun checkMedicationTakenOnTime(daysRequired: Int, hour: Int, minute: Int): Boolean {
        val sortedLogs = logsList.sortedWith(compareBy<LogData> { it.timestamp.year }
            .thenBy { it.timestamp.month }
            .thenBy { it.timestamp.day })

        var consecutiveDaysOnTime = 0
        var lastDate: LocalDate? = null

        for (log in sortedLogs) {
            val currentDate =
                LocalDate.of(log.timestamp.year, log.timestamp.month, log.timestamp.day)
            val currentTime = LocalTime.of(log.timestamp.hour, log.timestamp.minute)
            val cutoffTime = LocalTime.of(hour, minute)  // 8 AM or any other time

            if (lastDate != null && currentDate.isEqual(lastDate.plusDays(1)) && currentTime.isBefore(
                    cutoffTime
                ) && log.isOnTime.equals("True", ignoreCase = true)
            ) {
                consecutiveDaysOnTime++
            } else if (lastDate == null || currentDate == null || !currentDate.isEqual(lastDate)) {
                consecutiveDaysOnTime = if (currentTime.isBefore(cutoffTime) && log.isOnTime.equals(
                        "True",
                        ignoreCase = true
                    )
                ) 1 else 0
            }

            if (consecutiveDaysOnTime >= daysRequired) {
                return true  // Condition met
            }

            lastDate = currentDate
        }

        return false  // Condition not met
    }

    fun checkPerfect(consecutiveDaysRequired: Int): Boolean {
        val sortedLogs = logsList.sortedWith(compareBy<LogData> { it.timestamp.year }
            .thenBy { it.timestamp.month }
            .thenBy { it.timestamp.day })

        var currentConsecutiveDays = 0
        var lastDay: TimestampInfo? = null
        var amTaken = false
        var pmTaken = false

        for (log in sortedLogs) {
            // Check if the current log is for a new day
            if (lastDay == null || isNewDay(lastDay, log.timestamp)) {
                if (amTaken && pmTaken) {
                    currentConsecutiveDays++
                } else {
                    currentConsecutiveDays = 0
                }

                // Reset for the new day
                amTaken = false
                pmTaken = false
                lastDay = log.timestamp

                if (currentConsecutiveDays >= consecutiveDaysRequired) {
                    return true
                }
            }

            // Update AM/PM taken status
            if (log.variable_label.split(" ")[1] == "AM" && log.isOnTime.equals(
                    "True",
                    ignoreCase = true
                )
            ) {
                amTaken = true
            } else if (log.variable_label.split(" ")[1] == "PM" && log.isOnTime.equals(
                    "True",
                    ignoreCase = true
                )
            ) {
                pmTaken = true
            }
        }

        // Check the last day in the list
        if (amTaken && pmTaken) {
            currentConsecutiveDays++
        }

        return currentConsecutiveDays >= consecutiveDaysRequired
    }

    fun isNewDay(lastDay: TimestampInfo, currentDay: TimestampInfo): Boolean {
        return lastDay.year != currentDay.year ||
                lastDay.month != currentDay.month ||
                lastDay.day != currentDay.day
    }

    fun checkTwoLogsNotOnTime(): Boolean {
        val sortedLogs = logsList.sortedWith(compareBy<LogData> { it.timestamp.year }
            .thenBy { it.timestamp.month }
            .thenBy { it.timestamp.day }
            .thenBy { it.timestamp.hour }
            .thenBy { it.timestamp.minute })

        var lastLogWasNotOnTime = false

        for (log in sortedLogs) {
            val isOnTime = log.isOnTime.equals("True", ignoreCase = true)

            if (!isOnTime) {
                if (lastLogWasNotOnTime) {
                    // Found two logs in a row that are not on time.
                    return true
                }
                // Current log is not on time, set the flag.
                lastLogWasNotOnTime = true
            } else {
                // Current log is on time, reset the flag.
                lastLogWasNotOnTime = false
            }
        }

        // No two consecutive logs were found that are not on time.
        return false
    }

    fun checkUniquePillNames(entries: List<ScheduleEntry>, amount: Int): Boolean {
        val uniquePillNames = entries.map { it.pillName }.toSet()
        return uniquePillNames.size >= amount
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    val updateAchievementsDispatcher = newSingleThreadContext("UpdateAchievementsThread")

    fun updateAchievements() {
        lockAchievements()

        if (checkConsecutiveOnTime(7)) {
            unlockAchievement("7_day_streak")
        }
        if (checkConsecutiveOnTime(14)) {
            unlockAchievement("14_day_streak")
        }
        if (checkMedicationTakenOnTime(7, 8, 0)) {
            unlockAchievement("early_bird")
        }
        if (checkMedicationTakenOnTime(30, 8, 0)) {
            unlockAchievement("early_bird_pro")
        }
        if (checkPerfect(7)) {
            unlockAchievement("perfect_week")
        }
        if (checkConsecutiveOnTime(30)) {
            unlockAchievement("perfect_month")
        }
        if (checkTwoLogsNotOnTime()) {
            unlockAchievement("oops_i_did_it_again")
        }
        if (checkUniquePillNames(scheduleList.toList(), 1)) {
            unlockAchievement("pilled_up")
        }
        if (checkUniquePillNames(scheduleList.toList(), 5)) {
            unlockAchievement("pilled_up_pro")
        }
        if (checkUniquePillNames(scheduleList.toList(), 10)) {
            unlockAchievement("pilled_up_master")
        }
        updateAchievementLists()
    }


    val pdfLogger = PdfLogger(context)
}
