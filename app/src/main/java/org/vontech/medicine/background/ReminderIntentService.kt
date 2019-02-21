package org.vontech.medicine.background

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.app.Notification
import android.util.Log


/**
 * Creates a notification for the reminder
 */
class ReminderIntentService: IntentService("ReminderIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val builder = Notification.Builder(this)
        builder.setContentTitle("My Title")
        builder.setContentText("This is the Body")
        builder.setSmallIcon(android.R.drawable.sym_def_app_icon)
//        val notifyIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        //to be able to launch your activity from the notification
//        builder.setContentIntent(pendingIntent)
        val notificationCompat = builder.build()
        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(1, notificationCompat)

        Log.e("Medicine", "showed notification")
    }

}