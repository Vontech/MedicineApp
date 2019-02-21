package org.vontech.medicine

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.vontech.medicine.pokos.Frequency
import org.vontech.medicine.reminders.ReminderManager
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val reminderManager = ReminderManager()
        reminderManager.addReminder("Aaron", "Vontell", Date(), Frequency.DAILY, this)
    }
}
