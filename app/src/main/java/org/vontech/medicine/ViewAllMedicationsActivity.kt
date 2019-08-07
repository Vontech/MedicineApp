package org.vontech.medicine

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore

class ViewAllMedicationsActivity : AppCompatActivity() {

    private lateinit var medicationListLinearLayout: LinearLayoutManager
    private lateinit var recyclerAdapter: AllMedsRecyclerAdapter
    private lateinit var allMedicationsList: List<Medication>
    private lateinit var medicationStore: MedicationStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_medications)

        renderMedications()
    }

    override fun onResume() {
        super.onResume()
        renderMedications()
    }

    private fun renderMedications() {
        medicationStore = MedicationStore(this)
        allMedicationsList = medicationStore.getMedications()

        medicationListLinearLayout = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = medicationListLinearLayout
        recyclerAdapter = AllMedsRecyclerAdapter(allMedicationsList)
        recyclerView.adapter = recyclerAdapter
    }
}
