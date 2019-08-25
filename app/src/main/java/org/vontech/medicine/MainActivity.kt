package org.vontech.medicine

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.delete_dialog.*
import org.joda.time.*
import org.vontech.medicine.pokos.Medication
import org.joda.time.format.DateTimeFormat
import org.vontech.medicine.background.setNumberOfNotifications
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import android.content.ActivityNotFoundException
import android.content.SharedPreferences
import android.widget.Toast
import org.vontech.medicine.utils.*
import org.vontech.medicine.views.makeHeightEqualWidth


class MainActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication

    // Upcoming medications
    private lateinit var upcomingLinearLayoutManager: LinearLayoutManager
    private lateinit var upcomingAdapterUpcomingMedication: UpcomingRecyclerAdapter
    private lateinit var medicationList: List<Medication>
    private lateinit var medicationStore: MedicationStore
    private lateinit var medicationHistory: MedicationHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        medicationStore = MedicationStore(this)
        medicationHistory = MedicationHistory(this)
        medicationList = medicationStore.getMedications()

        upcomingLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = upcomingLinearLayoutManager
        upcomingAdapterUpcomingMedication = UpcomingRecyclerAdapter(mutableListOf(), mutableListOf(), this) {renderNextMedication()}
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
        if (nextReminderHeader.visibility == View.VISIBLE) upcomingAdapterUpcomingMedication.notifyDataSetChanged()

        // Render things ----------------------------
        renderHeader()
        renderButtons()
        renderNextMedication()
        renderMissingPane()

        showWelcomeDialog()

    }

    private fun showWelcomeDialog() {

        val prefs = org.vontech.medicine.utils.getPreferences(this)

        if (prefs.getBoolean("first_time", true)) {
            val message = "Medz tracks and reminds you to take your medications. Get started by adding or scanning a new medication.\n\nNOTE: This app is still in development - provide feedback with the button at the bottom of the home screen!"
            val dialog = buildDialog("Welcome to Medz!", message)
            dialog.positiveButton.text = "DISMISS"
            dialog.negativeButton.visibility = View.GONE
            dialog.positiveButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

            prefs.edit().putBoolean("first_time", false).apply()

        }


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
        aboutUsButton.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:medz.app.feedback@gmail.com")
            try {
                startActivity(emailIntent)
            } catch (e: ActivityNotFoundException) {
                // Handle case where no email app is available
                Toast.makeText(this, "No email app detected :(", Toast.LENGTH_SHORT).show()
            }

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
            nextMedicationWidget.visibility = View.GONE
            noNextMedicationContainer.visibility = if (medicationList.isEmpty()) View.GONE else View.VISIBLE
        } else {
            nextMedicationWidget.visibility = View.VISIBLE
            noNextMedicationContainer.visibility = if (medicationList.isEmpty()) View.VISIBLE else View.GONE
            // Instantiate RecyclerView and set its adapter
            upcomingLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = upcomingLinearLayoutManager
            upcomingAdapterUpcomingMedication = UpcomingRecyclerAdapter(
                nextBatch.medicationList,
                nextBatch.timeIndices,
                this.applicationContext
            ) {renderNextMedication()}
            recyclerView.adapter = upcomingAdapterUpcomingMedication

            Log.d("Next Batch", nextBatch.medicationList.toString())

            val fmt = DateTimeFormat.forPattern("h:mm aa")
            nextReminderTimeTextView.text = fmt.print(nextBatch.reminderTime)
            nextReminderNumMedsTextView.text = nextBatch.medicationList.size.toString()

            markAllAsTakenButton.setOnClickListener {
                clickedTakeAll(nextBatch)
            }

        }

    }

    private fun clickedTakeAll(nextBatch: ReminderBatch) {

        val medNames = nextBatch.medicationList.map {"\u2022 ${it.name} (${it.dose} ${it.doseType.toString().toLowerCase()})"}
        val message = "Have you taken the following medications?\n\n${medNames.joinToString("\n")}"
        val dialog = buildDialog("Take Next Medications", message)
        dialog.positiveButton.text = "TAKE ALL"
        dialog.negativeButton.text = "CANCEL"
        dialog.positiveButton.setOnClickListener {
            nextBatch.medicationList.forEach { med ->
                medicationHistory.takeMedicationNow(med)
            }
            renderNextMedication()
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Sets the day of the week header text
     */
    private fun renderHeader() {
        val dayOfWeek = DateTime().dayOfWeek()
        val day = dayOfWeek.getAsText(Locale.getDefault()).toUpperCase()
        this.headerDay.text = day
        this.headerDay.setOnClickListener {
            generateFakeMedication(this)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        makeHeightEqualWidth(viewAllMedicationsButton)
        makeHeightEqualWidth(scanMedicationButton)
        makeHeightEqualWidth(addNewMedicationButton)

    }


    private val MEDICATION_TIME_NEARBY_THRESHOLD = 45

    /**
     * Returns all of the medications that have the closest upcoming reminders (same time)
     * This is done with the following logic:
     * - Get a list of all medications for today
     * - Combine into clusters of medicines that are taken within 45 minutes of each other
     * - Choose to display the earliest cluster, removing any already taken (using MedicationHistory)
     */
    private fun getNextReminder(): ReminderBatch? {

        val clusters = mutableListOf<ReminderCluster>()

        // First, create clusters
        medicationList.forEach { med ->

            // Skip medication if I don't take it today
            if (DateTime.now().dayOfWeek !in med.days) {
                return@forEach
            }

            med.times.sorted().forEachIndexed { index, medTime ->

                if (index in medicationHistory.getIndicesOfTimesTakenToday(med)) {
                    println("SKIPPING MED ${med.name} for time $medTime")
                    return@forEachIndexed
                }

                val comp = ReminderClusterComponent(med, index)

                val overlappingClusters = clusters.filter {
                    overlapsWithCluster(medTime, it, MEDICATION_TIME_NEARBY_THRESHOLD)
                }

                // Case 1: No clusters or no overlap with clusters
                if (overlappingClusters.isEmpty()) {
                    val cluster = ReminderCluster(medTime, medTime, mutableListOf(comp))
                    clusters.add(cluster)
                    return@forEachIndexed
                }

                // Case 2: Overlaps with one cluster
                val cluster = overlappingClusters.first()
                cluster.components.add(comp)
                cluster.start = if (medTime < cluster.start) medTime else cluster.start
                cluster.end = if (medTime > cluster.end) medTime else cluster.end

                // Case 3: Overlaps with multiple clusters
                if (overlappingClusters.size > 1) {
                    overlappingClusters.subList(1, overlappingClusters.size).forEach {
                        cluster.start = if (it.start < cluster.start) it.start else cluster.start
                        cluster.end = if (it.end > cluster.end) it.end else cluster.end
                        cluster.components.addAll(it.components)
                        clusters.remove(it)
                    }
                }

            }
        }
        println("CLUSTERS")
        print(clusters)

        if (clusters.isEmpty()) {
            return null
        }

        // Now find the cluster with the earliest time
        val earliestCluster = clusters.minBy { it.start }
        val medsToTake = earliestCluster!!.components.map {it.medication}
        val medTimeIndices = earliestCluster!!.components.map {it.timeIndex}
        val earliestMed = earliestCluster.start

        return ReminderBatch(medsToTake, earliestMed, medTimeIndices)

    }

}

data class ReminderBatch (val medicationList: List<Medication>, val reminderTime: LocalTime?, val timeIndices: List<Int>)

data class ReminderClusterComponent (val medication: Medication, val timeIndex: Int)
data class ReminderCluster (var start: LocalTime, var end: LocalTime, val components: MutableList<ReminderClusterComponent>)

fun millisToMinutes(millis: Int): Int {
    return floor((millis / 1000.0) / 60.0).toInt()
}

fun overlapsWithCluster(time: LocalTime, cluster: ReminderCluster, maxDistance: Int): Boolean {

    val minutesToSubtract = min(maxDistance, millisToMinutes(cluster.start.millisOfDay))
    val minutesToAdd = min(maxDistance, (24*60) - millisToMinutes(cluster.end.millisOfDay))

    val start = cluster.start.minusMinutes(minutesToSubtract).toDateTimeToday()
    val end = cluster.end.plusMinutes(minutesToAdd).toDateTimeToday()


    val interval = Interval(start, end)
    return interval.contains(time.toDateTimeToday())
}