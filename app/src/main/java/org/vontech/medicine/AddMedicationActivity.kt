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

class AddMedicationActivity : AppCompatActivity() {

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)

        // Create new medication with information from EditTexts when button is clicked
        saveMedicationButton.setOnClickListener {
            if (nameEditText.text.isEmpty()) {
                Toast.makeText(this, "Must give medication a name", Toast.LENGTH_SHORT).show()
            } else {
                // Default dose is 0 ml, override if user inputs value
                var dose = 0
                if (!doseEditText.text.isEmpty()) {
                    dose = doseEditText.text.toString().toInt()
                }

                saveMedication(nameEditText.text.toString(),
                    dose,
                    notesEditText.text.toString())

                // Return to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
            }
        }
    }

    /**
     * Creates a new medication, serializes the object, and saves it to local storage
     * @param name the name of the medication
     * @param dose the dosage of the medication in milligrams
     * @param notes extra information or instructions about the medication
     */
    private fun saveMedication(name: String, dose: Int, notes: String) {
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
    //TODO: Add recyclerView
    //TODO: Merge changes into branch
}
