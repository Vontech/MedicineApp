package org.vontech.medicine.reminders

import android.app.AlarmManager
import org.vontech.medicine.pokos.Frequency
import java.util.*
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.vontech.medicine.background.ReminderBroadcastReceiver


class ReminderManager {

    /**
     * Adds a new reminder to the list of reminders
     * @param: title The title of the reminder
     * @param: message The body text of the reminder
     * @param: time The time to send the reminder at
     * @param: frequency The frequency for the reminder
     */
    fun addReminder(title: String, message: String, time: Date, frequency: Frequency, context: Context) {
        // Create the intent to send a broadcast
        val notifyIntent = Intent(context, ReminderBroadcastReceiver::class.java)

        // Create bundle to pass title and message through intent
        var extras = Bundle()
        extras.putString("title", title)
        extras.putString("message", message)
        notifyIntent.putExtras(extras)

        // Make it a pending intent so it can be fired at a given time
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Set the frequency/time to send the broadcast at
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.time + 5000, pendingIntent)

        Log.e("Medicine", "fired")

        //TODO Add the reminder to the list of reminders
    }

    fun editReminder() {

    }

    fun deleteReminder() {

    }

    //TODO: Add return type for list of reminders
    fun getReminders() {

    }
}