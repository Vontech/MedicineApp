package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_medication.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var medicationStore: MedicationStore
    private lateinit var medications: List<Medication>
    private var edit: Boolean = false
    private lateinit var oldMedication: Medication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        medicationStore = MedicationStore(this)
        // Get list of medications
        medications = medicationStore.getMedications()

        // Populate TextViews with medication values if editing a medication
        if (intent.getSerializableExtra(this.getString(R.string.edit_medication)) is Medication) {
            oldMedication = intent.getSerializableExtra(this.getString(R.string.edit_medication)) as Medication
            populateViews(oldMedication)
            // Only show delete button if editing an existing Medication
            deleteMedicationButton.visibility = View.VISIBLE
            edit = true
        }

        if (intent.getSerializableExtra(this.getString(R.string.scan_medication)) is Medication) {
            val medication = intent.getSerializableExtra(this.getString(R.string.scan_medication)) as Medication
            populateViews(medication)
        }

        // Set onClickListeners for buttons
        saveMedicationButton.setOnClickListener { saveMedication() }
        deleteMedicationButton.setOnClickListener { deleteMedication(oldMedication) }
    }

    /**
     * Updates a modified Medication if editing, or adds a new Medication to the ArrayList of Medications
     */
    private fun saveMedication() {
        // Do not allow medication to be created without a name
        if (nameEditText.text.isEmpty()) {
            Toast.makeText(this, "Must give medication a name", Toast.LENGTH_SHORT).show()
            return
        }

        // Dose has a default value to protect against converting a null value to an Int
        var dose = 0f
        if (!doseEditText.text.isEmpty()) { dose = doseEditText.text.toString().toFloat() }

        // Create a new Medication object from the fields
        val newMedication = Medication(nameEditText.text.toString(), dose, notesEditText.text.toString())
        newMedication.calendarToJoda(dayPicker.selectedDays)

        // If editing a medication, replace the old medication with a new one. Otherwise, add it to the list
        if (edit) {
            medicationStore.replaceMedication(oldMedication, newMedication)
        }
        else {
            medicationStore.saveMedication(newMedication)
        }

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }

    /**
     * Populates the activity's views with the values from the given Medication
     * @param medication the Medication used to populate the TextViews
     */
    private fun populateViews(medication: Medication) {
        if (medication.name != null) {
            nameEditText.setText(medication.name)
        }
        if (medication.dose != null) {
            doseEditText.setText(medication.dose.toString())
        }
        if (medication.notes != null) {
            notesEditText.setText(medication.notes)
        }
        dayPicker.selectedDays = medication.jodaToCalendar()
    }

    /**
     * Deletes a medication from the ArrayList and re-saves it to SharedPreferences
     * @param medication the Medication to remove from the ArrayList
     */
    private fun deleteMedication(medication: Medication) {
        medicationStore.deleteMedication(medication)

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show()
    }
}
