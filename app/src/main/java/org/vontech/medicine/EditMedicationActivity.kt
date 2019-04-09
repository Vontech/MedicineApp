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
import java.util.*

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var medications: ArrayList<Medication>
    private var edit: Boolean = false
    private lateinit var oldMedication: Medication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)
        // Get list of medications
        medications = getMedications(getString(R.string.medication_list))

        // Populate TextViews with medication values if editing a medication
        if (intent.getSerializableExtra(this.getString(R.string.edit_medication)) is Medication) {
            oldMedication = intent.getSerializableExtra(this.getString(R.string.edit_medication)) as Medication
            populateViews(oldMedication)
            // Only show delete button if editing an existing Medication
            deleteMedicationButton.visibility = View.VISIBLE
            edit = true
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
        var dose = 0
        if (!doseEditText.text.isEmpty()) { dose = doseEditText.text.toString().toInt() }

        // Create a new Medication object from the fields
        val newMedication = Medication(nameEditText.text.toString(), dose, notesEditText.text.toString())
        newMedication.calendarToJoda(dayPicker.selectedDays)

        // If editing a medication, replace the old medication with a new one. Otherwise, add it to the list
        if (edit) { medications[medications.indexOf(oldMedication)] = newMedication }
        else { medications.add(newMedication) }

        saveArrayList() // Save changes to ArrayList

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }

    /**
     * Returns the ArrayList of Medications from SharedPreferences
     */
    private fun getMedications(key: String): ArrayList<Medication> {
        val json = prefs.getString(key, null)
        val type = object : TypeToken<ArrayList<Medication>>() {}.type
        if (Gson().fromJson<ArrayList<Medication>>(json, type) == null) { return arrayListOf() }

        return Gson().fromJson<ArrayList<Medication>>(json, type)

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
     * Populates the activity's views with the values from the given Medication
     * @param medication the Medication used to populate the TextViews
     */
    private fun populateViews(medication: Medication) {
        nameEditText.setText(medication.name)
        doseEditText.setText(medication.dose.toString())
        notesEditText.setText(medication.notes)
        dayPicker.selectedDays = medication.jodaToCalendar()
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
