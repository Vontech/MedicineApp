package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.vontech.medicine.reminders.ReminderManager
import java.util.*
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore

class MainActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var medicineList: List<Medication>
    private lateinit var medicationStore: MedicationStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        medicationStore = MedicationStore(this)
        medicineList = medicationStore.getMedications()

        newMedicationButton.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            startActivity(intent)
        }

        Log.d("Medications", medicineList.toString())
        scanMedicationButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        // Instantiate RecyclerView and set its adapter
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(medicineList)
        recyclerView.adapter = adapter

        app = this.application as MedicineApplication
        val isLoggedIn = app.attemptToLoadExistingSession(this)

        if (isLoggedIn) {
            Log.i("MainActivity.kt", "Logged in!")
            Log.i("MainActivity.kt", app.userSession.toString())
        }

        // TODO Test ReminderManager methods
        val reminderManager = ReminderManager(this)
        reminderManager.addReminder("My title", "My message", Calendar.getInstance().time)
        Log.d("Reminder IDs", reminderManager.getReminderIDs().toString())
    }

    override fun onResume() {
        super.onResume()
        medicineList = medicationStore.getMedications()
        adapter.notifyDataSetChanged()
        recyclerView
    }

}
