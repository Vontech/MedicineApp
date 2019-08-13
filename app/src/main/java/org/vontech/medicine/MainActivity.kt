package org.vontech.medicine

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_day_view.view.*
import org.joda.time.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore
import org.joda.time.format.DateTimeFormat
import org.vontech.medicine.background.setNumberOfNotifications
import org.vontech.medicine.utils.EditState
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication

    // Upcoming medications
    private lateinit var upcomingLinearLayoutManager: LinearLayoutManager
    private lateinit var upcomingAdapterUpcomingMedication: UpcomingRecyclerAdapter
    private lateinit var medicationList: List<Medication>
    private lateinit var medicationStore: MedicationStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        medicationStore = MedicationStore(this)
        medicationList = medicationStore.getMedications()

        // TODO should not get 'lateinit var not assigned' error anymore, and should also have edit activity in edit mode
        // TODO set the 'tonight' text in the widget to change dynamically based on the next reminder time

        upcomingLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = upcomingLinearLayoutManager
        upcomingAdapterUpcomingMedication = UpcomingRecyclerAdapter(medicationList, this.applicationContext)
        recyclerView.adapter = upcomingAdapterUpcomingMedication

        if (intent.getSerializableExtra(getString(R.string.reset_notification_count)) == 1) {
            setNumberOfNotifications(0, this) // Reset notification count if user clicked notification
        }

        app = this.application as MedicineApplication
        // val isLoggedIn = app.attemptToLoadExistingSession(this)
        val isLoggedIn = true // TODO remove this later

    }

    override fun onResume() {
        super.onResume()
        medicationList = medicationStore.getMedications()
        if (nextReminderWidget.visibility == View.VISIBLE) upcomingAdapterUpcomingMedication.notifyDataSetChanged()

        // Render things ----------------------------
        renderHeader()
        renderButtons()
        renderNextMedication()
        renderMissingPane()

    }

    /**
     * Setup all buttons on the main page
     */
    private fun renderButtons() {
//        createMedicationButton.setOnClickListener {
//            val intent = Intent(this, EditMedicationActivity::class.java)
//            intent.putExtra(this.getString(R.string.edit_screen_state), EditState.ADDING)
//            startActivity(intent)
//        }
        viewAllMedicationsButton.setOnClickListener {
            val intent = Intent(this, ViewAllMedicationsActivity::class.java)
            startActivity(intent)
        }
        scanMedicationButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
        addNewMedicationButton.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            intent.putExtra(getString(R.string.edit_screen_state), EditState.ADDING)
            startActivity(intent)
        }
    }

    private fun renderMissingPane() {
        if (medicationList.isEmpty()) {
            noMedicinesContainer.visibility = View.VISIBLE
        } else {
            noMedicinesContainer.visibility = View.GONE
        }
    }

    /**
     * Renders the main widget for the next medication to take
     */
    private fun renderNextMedication() {

        val nextBatch = getNextReminder()
        if (nextBatch?.reminderTime == null) {
            nextReminderWidget.visibility = View.GONE
            recyclerView.visibility = View.GONE
        } else {
            nextReminderWidget.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            // Instantiate RecyclerView and set its adapter
            upcomingLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = upcomingLinearLayoutManager
            upcomingAdapterUpcomingMedication = UpcomingRecyclerAdapter(nextBatch.medicationList, this.applicationContext)
            recyclerView.adapter = upcomingAdapterUpcomingMedication

            Log.d("Next Batch", nextBatch.medicationList.toString())

            val fmt = DateTimeFormat.forPattern("h:mm aa")
            nextReminderTimeTextView.text = fmt.print(nextBatch.reminderTime)
            nextReminderNumMedsTextView.text = nextBatch.medicationList.size.toString()
        }

    }

    private fun renderHeader() {
        val dayOfWeek = DateTime().dayOfWeek()
        val day = dayOfWeek.getAsText(Locale.getDefault()).toUpperCase()
        this.headerDay.text = day
    }


    /**
     * Returns all of the medications that have the closest upcoming reminders (same time)
     */
    private fun getNextReminder(): ReminderBatch? {

        if (medicationList.isEmpty()) {
            return null
        }

        val now = LocalTime.now()
        println(medicationList)
        var overallClosestReminder = medicationList.first().times.min()
        val nextReminderMedications = mutableSetOf(medicationList.first())

        // Find the closest time to now, and return the list of medications
        // that are taken at that time
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

}
