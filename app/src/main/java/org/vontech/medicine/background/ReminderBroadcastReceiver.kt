package org.vontech.medicine.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * This class's onReceive method is called by the OS when an intent needs to be fired.
 * onReceive passes on the pendingIntent to ReminderIntentService for the notification to be fired.
 */
class ReminderBroadcastReceiver: BroadcastReceiver() {

    /**
     * Calls the intent to create a reminder notification when a broadcast is received
     * @param: context The current context
     * @param: intent The reminder intent class
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val remindIntent = Intent(context, ReminderIntentService::class.java)

        // Add the title and message from the ReminderManager to the intent passed to the ReminderIntentService
        val extras = Bundle()
//        extras.putInt("id", intent!!.getIntExtra("id", 0))
        extras.putString("title", intent!!.getStringExtra("title"))
        extras.putString("message", intent.getStringExtra("message"))

        remindIntent.putExtras(extras)
        context!!.startService(remindIntent)

        Log.e("Medicine", "received")
    }

}