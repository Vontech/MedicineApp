package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.delete_dialog.*
import org.joda.time.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore
import org.joda.time.format.DateTimeFormat
import org.vontech.medicine.background.setNumberOfNotifications
import org.vontech.medicine.utils.EditState
import org.vontech.medicine.utils.MedicationHistory
import org.vontech.medicine.utils.buildDialog
import java.util.*

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
        upcomingAdapterUpcomingMedication = UpcomingRecyclerAdapter(medicationList, this.applicationContext) {
            this.renderNextMedication()
        }
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
            med.times.forEachIndexed { index, medTime ->

                // TODO: Do not include a medication if it was already taken
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
        val earliestMed = earliestCluster.start

        return ReminderBatch(medsToTake, earliestMed)

    }

}

data class ReminderBatch (val medicationList: List<Medication>, val reminderTime: LocalTime?)

data class ReminderClusterComponent (val medication: Medication, val timeIndex: Int)
data class ReminderCluster (var start: LocalTime, var end: LocalTime, val components: MutableList<ReminderClusterComponent>)

fun overlapsWithCluster(time: LocalTime, cluster: ReminderCluster, maxDistance: Int): Boolean {
    val start = cluster.start.minusMinutes(maxDistance).toDateTimeToday()
    val end = cluster.end.plusMinutes(maxDistance).toDateTimeToday()
    val interval = Interval(
        cluster.start.minusMinutes(maxDistance).toDateTimeToday(),
        cluster.end.plusMinutes(maxDistance).toDateTimeToday())
    return interval.contains(time.toDateTimeToday())
}