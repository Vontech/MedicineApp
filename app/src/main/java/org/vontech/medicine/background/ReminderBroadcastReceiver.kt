package org.vontech.medicine.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

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
        extras.putString("title", intent!!.getStringExtra("title"))
        extras.putString("message", intent.getStringExtra("message"))

        remindIntent.putExtras(extras)
        context!!.startService(remindIntent)

        Log.e("Medicine", "received")
    }

}