package org.vontech.medicine.background

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.app.Notification
import android.util.Log
import org.vontech.medicine.MainActivity
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import org.vontech.medicine.security.SecurePreferencesBuilder
import org.vontech.medicine.utils.getPreferences
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.support.v4.app.JobIntentService


/**
 * Receives the intent from ReminderBroadcastReceiver, builds, and fires a notification
 */
class ReminderIntentService: JobIntentService() {



    companion object {

        private val notifcationId = 1

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, ReminderIntentService::class.java, notifcationId, work)
        }

    }


    override fun onHandleWork(intent: Intent) {
        val builder = Notification.Builder(this)
        val title = intent!!.getStringExtra("title")
//        val message = intent.getStringExtra("message")
        val id = intent.getIntExtra("medicationId", 1)

        val numNotifications = getNumberOfNotifications(this) + 1

        // Get number of medications there are
        val message = "You have medications to take"

        builder.setContentTitle(title)
        builder.setContentText(message)

        builder.setSmallIcon(android.R.drawable.sym_def_app_icon)
        builder.setAutoCancel(true) // Notification will be dismissed once clicked
        builder.setPriority(Notification.PRIORITY_HIGH)
        builder.setDefaults(Notification.DEFAULT_ALL)
        val name = "Medz"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(name, name, NotificationManager.IMPORTANCE_HIGH)
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(channel)
            builder.setChannelId(name)
        } else {
            TODO("VERSION.SDK_INT < N")
        }

        setIntents(builder)


        val notificationCompat = builder.build()
        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(notifcationId, notificationCompat)

        setNumberOfNotifications(numNotifications, this)

        val reminderManager = ReminderManager(this)
        val medicationStore = MedicationStore(this)
        val medication = medicationStore.getMedicationById(id)
        Log.e("GOT MED FROM NOTIF", medication.toString())
        reminderManager.scheduleReminder(medication!!, title, message)

        Log.e("Medicine", "showed notification")
    }

    private fun setIntents(builder: Notification.Builder) {
        // Clicking on notification opens MainActivity
        val contentIntent = Intent(this, MainActivity::class.java)
        val extras = Bundle()
        extras.putInt(getString(org.vontech.medicine.R.string.reset_notification_count), 1) // Catch this on the MainActivity
        contentIntent.putExtras(extras)

        val mainActivityPendingIntent = PendingIntent.getActivity(this, 0,
            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(mainActivityPendingIntent)
    }

}

private const val NOTIFICATIONS_KEY = "notifications_key"

private fun getNumberOfNotifications(context: Context): Int {
    val prefs = getPreferences(context)
    return prefs.getInt(NOTIFICATIONS_KEY, 0)
}

fun setNumberOfNotifications(numNotifications: Int, context: Context) {
    val editor = getPreferences(context).edit()
    editor.putInt(NOTIFICATIONS_KEY, numNotifications)
    editor.apply()
}