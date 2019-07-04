package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_medication.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.joda.time.DateTime
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore
import java.lang.IllegalArgumentException
import java.util.*

val NOTIFICATION_TITLE = "Time to take your medicine, hoe!"
val NOTIFICATION_MESSAGE = "Click to view this medication"

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var medicationStore: MedicationStore
    private lateinit var medications: List<Medication>
    private var edit: Boolean = false
    private lateinit var oldMedication: Medication
    private lateinit var reminderManager: ReminderManager
    private lateinit var weekdayTextViews: List<TextView>
    var selectedDays = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        medicationStore = MedicationStore(this)
        reminderManager = ReminderManager(this)
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

        weekdayTextViews = arrayListOf(mondayTextView, tuesdayTextView, wednesdayTextView, thursdayTextView,
            fridayTextView, saturdayTextView, sundayTextView)
        // Set onClickListeners for TextViews
        weekdayTextViews.forEach {
            it.setOnClickListener{ clickWeekday(it as TextView) }
        }

        // Set onClickListeners for buttons
        saveMedicationButton.setOnClickListener { saveMedication() }
        deleteMedicationButton.setOnClickListener { deleteMedication(oldMedication) }
    }

    // Change the color of the weekday TextView when clicked
    // Add/remove the corresponding Calendar weekday from the list of selected days
    private fun clickWeekday(textView: TextView) {
        // Remove this day from selectedDays
        if (textView.currentTextColor == ContextCompat.getColor(this, R.color.textColor)) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.disabledTextColor))
            selectedDays.remove(textViewToCalendar(textView))
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            selectedDays.add(textViewToCalendar(textView))
        }
        println("SelectedDays: $selectedDays")
    }

    // Convert the given TextView to its corresponding Calendar day to be saved
    private fun textViewToCalendar(textView: TextView): Int {
        when (textView) {
            mondayTextView -> return Calendar.MONDAY
            tuesdayTextView -> return Calendar.TUESDAY
            wednesdayTextView -> return Calendar.WEDNESDAY
            thursdayTextView -> return Calendar.THURSDAY
            fridayTextView -> return Calendar.FRIDAY
            saturdayTextView -> return Calendar.SATURDAY
            sundayTextView -> return Calendar.SUNDAY
        }
        throw IllegalArgumentException("Invalid weekday name")
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
        newMedication.toJoda(selectedDays)

        val now = DateTime.now()
        newMedication.times.add(now.plusSeconds(3).toLocalTime())
        newMedication.times.add(now.plusSeconds(7).toLocalTime())
        newMedication.times.add(now.plusSeconds(12).toLocalTime())

        // If editing a medication, replace the old medication with a new one. Otherwise, add it to the list
        if (edit) {
            medicationStore.replaceMedication(oldMedication, newMedication)
            scheduleReminder(newMedication, isReplacing = true)
        }
        else {
            medicationStore.saveMedication(newMedication)
            scheduleReminder(newMedication)
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
        //dayPicker.selectedDays = medication.jodaToCalendar()
    }

    /**
     * Deletes a medication from the ArrayList and re-saves it to SharedPreferences
     * @param medication the Medication to remove from the ArrayList
     */
    private fun deleteMedication(medication: Medication) {
        medicationStore.deleteMedication(medication)

        scheduleReminder(medication, isDeleting = true)

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleReminder(medication: Medication, isReplacing: Boolean = false, isDeleting: Boolean = false) {

        if (isDeleting) {
            reminderManager.deleteReminder(medication.id)
            return
        }

        val nextTime = reminderManager.getNextTime(medication)

        if (isReplacing) {
            reminderManager.editReminder(NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, medication.id, nextTime!!)
            return
        }

        reminderManager.addReminder(NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, medication.id, nextTime!!)

    }

}
