package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_medication.*
import org.vontech.medicine.pokos.Medication

class EditMedicationActivity : AppCompatActivity() {

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)

        // Populate TextViews with medication values if editing a medication
        if (intent.getSerializableExtra(this.getString(R.string.edit_medication)) is Medication) {
            populateTextViews(intent.getSerializableExtra(this.getString(R.string.edit_medication)) as Medication)
        }

        // Create new medication with values from EditTexts when button is clicked
        saveMedicationButton.setOnClickListener { saveMedication() }
    }

    /**
     * Check that user has entered required data for making a new Medication, and creates/saves it if so
     */
    private fun saveMedication() {
        // Do not allow medication to be created without a name
        if (nameEditText.text.isEmpty()) {
            Toast.makeText(this, "Must give medication a name", Toast.LENGTH_SHORT).show()
        } else {
            /* Default dose is 0 ml, override if user inputs value
               Dose in particular has a default value because it is converted to an Int when made into a Medication;
               the default value protects against possibly converting a null value to an Int */
            var dose = 0
            if (!doseEditText.text.isEmpty()) {
                dose = doseEditText.text.toString().toInt()
            }

            // Serialize and save medication to local storage
            serializeMedication(nameEditText.text.toString(), dose, notesEditText.text.toString())

            // Return to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
        }
    }

    /**
     * Creates a new medication, serializes the object, and saves it to local storage
     * @param name the name of the medication
     * @param dose the dosage of the medication in milligrams
     * @param notes extra information or instructions about the medication
     */
    private fun serializeMedication(name: String, dose: Int, notes: String) {
        // Serialize the medication object
        val gson = Gson()
        var newMedication = Medication(name, dose, notes)
        val json = gson.toJson(newMedication)

        // Save the JSON string to local storage
        val prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)
        with (prefs.edit()) {
            putString(nameEditText.text.toString(), json)
            apply()
        }
        Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Populates the activity's TextView's with the values from the given Medication
     * @param medication the Medication used to populate the TextViews
     */
    private fun populateTextViews(medication: Medication) {
        nameEditText.setText(medication.name)
        doseEditText.setText(medication.dose.toString())
        notesEditText.setText(medication.notes)
    }
}
