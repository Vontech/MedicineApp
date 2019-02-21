package org.vontech.medicine.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderBroadcastReceiver: BroadcastReceiver() {

    /**
     * Calls the intent to create a reminder notification when a broadcast is received
     * @param: context The current context
     * @param: intent The reminder intent class
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val remindIntent = Intent(context, ReminderIntentService::class.java)
        context!!.startService(remindIntent)

        Log.e("Medicine", "received")
    }

}