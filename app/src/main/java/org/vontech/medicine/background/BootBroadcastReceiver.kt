package org.vontech.medicine.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore


class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.i("MEDZ", "RESCHEDULING MEDICATION ALARMS")
        val reminderManager = ReminderManager(context)
        val medicationStore = MedicationStore(context)
        val message = "Don't forget to take your medication!"
        val title = "Time to take your medicine!"
        var uh = ""
        medicationStore.getMedications().forEach {
            uh += " " + it.name
            reminderManager.scheduleReminder(it, title, message)
        }

    }
}