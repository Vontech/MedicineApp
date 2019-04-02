package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_medication.*
import org.vontech.medicine.pokos.Medication
import com.google.gson.reflect.TypeToken

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var medications: ArrayList<Medication>
    private var edit: Boolean = false
    private lateinit var tempMed: Medication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)
        // Get list of medications
        medications = getMedications(getString(R.string.medication_prefs))

        // Populate TextViews with medication values if editing a medication
        if (intent.getSerializableExtra(this.getString(R.string.edit_medication)) is Medication) {
            tempMed = intent.getSerializableExtra(this.getString(R.string.edit_medication)) as Medication
            populateTextViews(tempMed)
            // Only show delete button if editing an existing Medication
            deleteMedicationButton.visibility = View.VISIBLE
            edit = true
        }

        // Set onClickListeners for buttons
        saveMedicationButton.setOnClickListener { saveMedication() }
        deleteMedicationButton.setOnClickListener { deleteMedication() }
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

        // Default dose is 0 ml, override if user inputs value
        // Dose has a default value to protect against converting a null value to an Int
        var dose = 0
        if (!doseEditText.text.isEmpty()) {
            dose = doseEditText.text.toString().toInt()
        }

        // if editing an existing medication, update medication from intent with new values
        if (edit) {
            tempMed.name = nameEditText.text.toString()
            tempMed.dose = dose
            tempMed.notes = notesEditText.text.toString()
        } else {
            // If adding a new medication, create a Medication object and add it to the list
            tempMed = Medication(nameEditText.text.toString(), dose, notesEditText.text.toString())
            medications.add(tempMed)
        }

        saveArrayList() // Save changes to ArrayList

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }

    /**
     * Returns the ArrayList of Medications from SharedPreferences
     */
    private fun getMedications(key: String): ArrayList<Medication> {
        val gson = Gson()
        val json = prefs.getString(key, null)
        val type = object : TypeToken<ArrayList<Medication>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Saves the arraylist of Medications to SharedPreferences, overwriting the previous ArrayList
     */
    private fun saveArrayList() {
        val editor = prefs.edit()
        // Remove existing arraylist in SharedPrefs
        editor.remove(this.getString(R.string.medication_list))

        // Save new arraylist
        val gson = Gson()
        val json = gson.toJson(medications)
        editor.putString(this.getString(R.string.medication_list), json)
        editor.apply()

        Toast.makeText(this, "ArrayList saved!", Toast.LENGTH_SHORT).show()
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

    /**
     * Deletes a medication from the ArrayList and re-saves it to SharedPreferences
     * @param medication the Medication to remove from the ArrayList
     */
    private fun deleteMedication(medication: Medication) {
        medications.remove(medication)
        saveArrayList()

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show()
    }
}
