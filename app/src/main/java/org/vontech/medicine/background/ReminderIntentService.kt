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
import android.os.Bundle
import org.vontech.medicine.R
import org.vontech.medicine.security.SecurePreferencesBuilder


/**
 * Receives the intent from ReminderBroadcastReceiver, builds, and fires a notification
 */
class ReminderIntentService: IntentService("ReminderIntentService") {
    private val NOTIFICATIONS_KEY = "notifications_key"
    private var prefs = SecurePreferencesBuilder(this@ReminderIntentService).build() //context.getSharedPreferences(MED_KEY, Context.MODE_PRIVATE) //SecurePreferencesBuilder(context).build()


    private val id = 1

    override fun onHandleIntent(intent: Intent?) {
        val builder = Notification.Builder(this)
        val title = intent!!.getStringExtra("title")
//        val message = intent.getStringExtra("message")
//        val id = intent.getIntExtra("id", 1)

        // Get number of medications there are
        val message = "You have ${getNumberOfNotifications()+1} to take"

        builder.setContentTitle(title)
        builder.setContentText(message)

        builder.setSmallIcon(android.R.drawable.sym_def_app_icon)
        builder.setAutoCancel(true) // Notification will be dismissed once clicked


        setIntents(builder)


        val notificationCompat = builder.build()
        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(id, notificationCompat)

        setNumberOfNotifications(getNumberOfNotifications()+1)

        val reminderManager = ReminderManager(this)
        val medicationStore = MedicationStore(this)
        val medication = medicationStore.getMedicationById(id)
        reminderManager.scheduleReminder(medication!!, title, message)

        Log.e("Medicine", "showed notification")
    }

    private fun setIntents(builder: Notification.Builder) {
        // Clicking on notification opens MainActivity
        val contentIntent = Intent(this, MainActivity::class.java)
        val extras = Bundle()
        extras.putInt(getString(R.string.reset_notification_count), 1) // Catch this on the MainActivity
        contentIntent.putExtras(extras)

        val mainActivityPendingIntent = PendingIntent.getActivity(this, 0,
            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(mainActivityPendingIntent)
    }

    private fun getNumberOfNotifications(): Int {
        return prefs.getInt(NOTIFICATIONS_KEY, 0)
    }

    fun setNumberOfNotifications(numNotifications: Int) {
        val editor = prefs.edit()
        editor.putInt(NOTIFICATIONS_KEY, numNotifications)
        editor.apply()
    }

}