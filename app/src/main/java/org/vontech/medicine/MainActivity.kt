package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.vontech.medicine.reminders.ReminderManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.time_layout.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore
import org.joda.time.DateTimeFieldType.dayOfWeek
import org.joda.time.format.DateTimeFormat
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

        Log.d("Medications", medicationList.toString())
        scanMedicationButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        val nextBatch = getNextReminder()

        // Instantiate RecyclerView and set its adapter
        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(nextBatch.medicationList)
        recyclerView.adapter = adapter

        val fmt = DateTimeFormat.forPattern("hh:mm a")
        nextReminderTimeTextView.text = fmt.print(nextBatch.reminderTime)
        nextReminderNumMedsTextView.text = nextBatch.medicationList.size.toString()

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

    override fun onResume() {
        super.onResume()
        medicationList = medicationStore.getMedications()
        adapter.notifyDataSetChanged()
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
        // For each medication, sort its reminder times in order, pick the first time and compare it to a temp variable that is storing the earliest time seen so far
        // if this time is closer, replace that temp variable and replace the temp list medications with this medication, then start looking for this time in the rest
        var closestReminder = medicationList.first().times.min()
        var nextReminderMedications = mutableSetOf(medicationList.first())


        medicationList.forEach { medication ->
            val minTime = medication.times.min()!!
            if (minTime < closestReminder) {
                closestReminder = minTime
                nextReminderMedications.clear()
                nextReminderMedications.add(medication)
            } else if (minTime == closestReminder) {
                nextReminderMedications.add(medication)
            }
        }
        return ReminderBatch(nextReminderMedications.toList(), closestReminder!!)
    }

    data class ReminderBatch (val medicationList: List<Medication>, val reminderTime: LocalTime)
    // TODO test the widget to ensure it updates properly and shows the most recent time with all the proper cards
}
