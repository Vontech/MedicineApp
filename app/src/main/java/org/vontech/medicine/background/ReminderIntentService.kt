package org.vontech.medicine.background

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.app.Notification
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.vontech.medicine.MainActivity
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore
import android.app.PendingIntent




/**
 * Receives the intent from ReminderBroadcastReceiver, builds, and fires a notification
 */
class ReminderIntentService: IntentService("ReminderIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val builder = Notification.Builder(this)
        val title = intent!!.getStringExtra("title")
        val message = intent.getStringExtra("message")
        val id = intent.getIntExtra("id", 1)
        builder.setContentTitle(title)
        builder.setContentText(message)

        builder.setSmallIcon(android.R.drawable.sym_def_app_icon)
        builder.setAutoCancel(true) // Notification will be dismissed once clicked

        // Clicking on notification opens MainActivity
        val contentIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(contentIntent)

        val notificationCompat = builder.build()
        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(id, notificationCompat)
        val reminderManager = ReminderManager(this)

        val medicationStore = MedicationStore(this)
        val medication = medicationStore.getMedicationById(id)
        reminderManager.scheduleReminder(medication!!, title, message)

        Log.e("Medicine", "showed notification")
    }

}