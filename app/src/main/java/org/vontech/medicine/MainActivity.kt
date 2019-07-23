package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore
import org.joda.time.format.DateTimeFormat
import java.time.temporal.ChronoUnit
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var medicationList: List<Medication>
    private lateinit var medicationStore: MedicationStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        medicationStore = MedicationStore(this)
        medicationList = medicationStore.getMedications()

        createMedicationButton.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            intent.putExtra(this.getString(R.string.add_medication), 1)
            startActivity(intent)
        }
        // TODO Figure out why this onclickListener code is not running
        // TODO Once this is working, make sure 'add_medication' is being caught and run in edit activity
        // TODO should not get 'lateinit var not assigned' error anymore, and should also have edit activity in edit mode
        // TODO test that widget updates programmatically again
        // TODO set the 'tonight' text in the widget to change dynamically based on the next reminder time

        Log.d("Medications", medicationList.toString())
        scanMedicationButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        val nextBatch = getNextReminder()
        if (nextBatch.reminderTime == null) {
            nextReminderWidget.visibility = View.GONE
        } else {
            nextReminderWidget.visibility = View.VISIBLE
            // Instantiate RecyclerView and set its adapter
            linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = linearLayoutManager
            adapter = RecyclerAdapter(nextBatch.medicationList)
            recyclerView.adapter = adapter

            val fmt = DateTimeFormat.forPattern("hh:mm a")
            nextReminderTimeTextView.text = fmt.print(nextBatch.reminderTime)
            nextReminderNumMedsTextView.text = nextBatch.medicationList.size.toString()
        }

        app = this.application as MedicineApplication
//        val isLoggedIn = app.attemptToLoadExistingSession(this)
        val isLoggedIn = true // TODO remove this later


        if (isLoggedIn) {
            Log.i("MainActivity.kt", "Logged in!")
            Log.i("MainActivity.kt", app.userSession.toString())
        }

//        val reminderManager = ReminderManager(this)
//        reminderManager.addReminder("My title", "My message", DateTime(), 12)
//        reminderManager.editReminder("Edited title", "Edited message", 12, DateTime())
        Log.i("Time", DateTimeZone.UTC.convertUTCToLocal(LocalTime().millisOfDay.toLong()).toString())
    }

    private fun myFunction() {
        val intent = Intent(this, EditMedicationActivity::class.java)
        intent.putExtra(this.getString(R.string.add_medication), 1)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        medicationList = medicationStore.getMedications()
        if (nextReminderWidget.visibility == View.VISIBLE) adapter.notifyDataSetChanged()
        recyclerView

        // Render things
        renderHeader()
        renderButtons()
    }

    private fun renderHeader() {
        val dayOfWeek = DateTime().dayOfWeek()
        val day = dayOfWeek.getAsText(Locale.getDefault()).toUpperCase()
        this.headerDay.text = day
    }

    private fun renderButtons() {
        createMedicationButton.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            startActivity(intent)
        }

        scanMedicationButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Returns all of the medications that have the closest upcoming reminders (same time)
     */
    private fun getNextReminder(): ReminderBatch {
        val now = LocalTime.now()
        var overallClosestReminder = medicationList.first().times.min()
        val nextReminderMedications = mutableSetOf(medicationList.first())

        for (medication in medicationList) {
            if (medication.times.isEmpty()) continue

            val currentMedTimesList = medication.times.toMutableList()
            currentMedTimesList.add(now)
            currentMedTimesList.sort()

            val nextTime =
                if (currentMedTimesList.indexOf(now) == currentMedTimesList.size - 1) currentMedTimesList.first()
                else currentMedTimesList[currentMedTimesList.indexOf(now) + 1]


            if (Minutes.minutesBetween(overallClosestReminder, now) > Minutes.minutesBetween(nextTime, now)) {
                overallClosestReminder = nextTime
                nextReminderMedications.clear()
                nextReminderMedications.add(medication)
            } else if (nextTime == overallClosestReminder) {
                nextReminderMedications.add(medication)
            }
        }
        return ReminderBatch(nextReminderMedications.toList(), overallClosestReminder)
    }

    data class ReminderBatch (val medicationList: List<Medication>, val reminderTime: LocalTime?)
    // TODO test the widget to ensure it updates properly and shows the most recent time with all the proper cards
}
