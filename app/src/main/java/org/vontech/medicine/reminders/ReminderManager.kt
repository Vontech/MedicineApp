package org.vontech.medicine.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.vontech.medicine.R
import org.vontech.medicine.background.ReminderBroadcastReceiver
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.security.SecurePreferencesBuilder
import org.vontech.medicine.utils.getSpecialGson
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.util.ArrayList

class ReminderManager(val context: Context) {

    private val REMINDER_IDS_KEY = context.getString(R.string.reminder_id_list)
    private var prefs = SecurePreferencesBuilder(context).build()//context.getSharedPreferences(IDS_KEY, Context.MODE_PRIVATE)
    private val gson = getSpecialGson()

    /**
     * Adds a new reminder to the list of reminders
     * @param: title The title of the reminder
     * @param: message The body text of the reminder
     * @param: time The time to send the reminder at
     * @param: frequency The frequency for the reminder
     */
    fun addReminder(title: String, message: String, id: Int, time: DateTime) {
        // Create the intent to send a broadcast
        val notifyIntent = Intent(context, ReminderBroadcastReceiver::class.java)

        Log.d("ADDING REMINDER", time.toString())

        // Create bundle to pass title and message through intent
        val extras = Bundle()
        extras.putInt("id", id)
        extras.putString("title", title)
        extras.putString("message", message)
        notifyIntent.putExtras(extras)

        // Make notifyIntent a PendingIntent so it can be fired at a given time
        val pendingIntent = PendingIntent.getBroadcast(context, id, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Set the time to send the broadcast at
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.millis, pendingIntent)

        Log.e("Medicine", "fired")

        // Save new ID to SharedPreferences
        val reminderIDs = getReminderIDs().toMutableList()
        reminderIDs.add(id)
        saveReminderIDs(reminderIDs)
    }

    /**
     * Delete the existing pendingIntent at the given ID and replace it with the new one
     */
    fun editReminder(newTitle: String, newMessage: String, id: Int, time: DateTime) {
        deleteReminder(id)
        addReminder(newTitle, newMessage, id, time)
    }

    /**
     * Delete the reminder for the given ID
     */
    fun deleteReminder(id: Int) {
        // Get reference to original intent by calling getBroadcast on a new one with the same data (considered equal)
        val cancelIntent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, id, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Delete the pendingIntent from AlarmManager
        pendingIntent.cancel()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        // Remove from list of IDs in SharedPreferences
        val reminderIDs = getReminderIDs().toMutableList()
        reminderIDs.remove(id)
        saveReminderIDs(reminderIDs)
    }

    /**
     * Returns the list reminder (PendingIntent) IDs
     */
    fun getReminderIDs(): List<Int> {
        val json = prefs.getString(REMINDER_IDS_KEY, null)
        val type = object : TypeToken<ArrayList<Int>>() {}.type
        return if (Gson().fromJson<ArrayList<Int>>(json, type) == null) {
            return arrayListOf()
        } else {
            Gson().fromJson<ArrayList<Int>>(json, type)
        }
    }

    /**
     * Returns the reminder (PendingIntent) associated with the given ID
     */
    fun getPendingIntentFromID(id: Int): PendingIntent {
        if (this.getReminderIDs().contains(id)) {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java)
            if (PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE) != null)
                return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE)
        }
        throw IllegalArgumentException("Given ID is not associated with a registered PendingIntent")
    }

    // Schedule the next reminder for this medication using addReminder
    fun scheduleReminder(medication: Medication, title: String, message: String) {
        addReminder(title, message, medication.id, getNextTime(medication)!!)
    }

    /**
     * Saves list of reminder IDs to memory (Cannot directly save pendingIntents)
     */
    private fun saveReminderIDs(reminderIds: List<Int>) {
        val editor = prefs.edit()
        val json = gson.toJson(reminderIds)
        editor.putString(REMINDER_IDS_KEY, json)
        editor.apply()
    }

    fun getNextTime(medication: Medication) : DateTime? {

        if (medication.days.isEmpty() || medication.times.isEmpty()) {
            return null
        }

        var today = DateTime()

        val times = ArrayList<DateTime>()
        medication.days.sorted().forEach {day ->
            medication.times.sorted().forEach {time ->
                var daysToAdd = day - today.dayOfWeek
                if (daysToAdd < 0) { daysToAdd += 7 }
                if (daysToAdd == 0 && LocalTime.now().isAfter(time)) { daysToAdd += 7 }
                var newDay = DateTime.now().plusDays(daysToAdd)
                newDay = newDay.withTime(time.hourOfDay, time.minuteOfHour, time.secondOfMinute, 0)
                times.add(newDay)
            }
        }

        val nextTime = times.sorted().firstOrNull()
        Log.d("NEXT TIME:", nextTime.toString())

        return nextTime

//
//
//        // If the medication needs to be taken later today
//        val remainingTimes = medication.times.filter { it.isAfter(day.toLocalTime()) }.sorted()
//        if (remainingTimes.isNotEmpty() && medication.days.contains(day.dayOfWeek)) {
//            return remainingTimes.first().toDateTimeToday()
//        }
//
//        // If the next reminder is on a different day
//        day = DateTime().plusDays(1)
//        for (i in 2..9) {
//            // If the medication needs to be taken on this day, schedule for the earliest time
//            if (medication.days.contains(day.dayOfWeek)) {
//                val time = medication.times.sorted().first()
//                return DateTime(day.year, day.monthOfYear, day.dayOfMonth,
//                    time.hourOfDay, time.minuteOfHour, time.secondOfMinute)
//            }
//
//            day = DateTime().plusDays(1)
//        }
//
//        throw RuntimeException("SOMEHOW REACHED BOTTOM OF getNextTime")
    }
}