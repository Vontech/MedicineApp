package org.vontech.medicine.background

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.app.Notification
import android.util.Log
import org.vontech.medicine.MainActivity
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore


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
//        val notifyIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        //to be able to launch your activity from the notification
//        builder.setContentIntent(pendingIntent)
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