package org.vontech.medicine

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_medication.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore
import java.lang.IllegalArgumentException
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.text.Html
import android.widget.TimePicker
import kotlinx.android.synthetic.main.time_layout.view.*
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*

val NOTIFICATION_TITLE = "Time to take your medicine, hoe!"
val NOTIFICATION_MESSAGE = "Click to view this medication"

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var medicationStore: MedicationStore
//    private lateinit var medications: List<Medication>
    private var edit: Boolean = false
    private var todayShowing: Boolean = false
    private lateinit var medication: Medication
    private lateinit var reminderManager: ReminderManager
    private lateinit var weekdayTextViews: List<TextView>
    private var selectedDays = mutableSetOf<Int>()
    private var timeViews = mutableSetOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        medicationStore = MedicationStore(this)
        reminderManager = ReminderManager(this)
        // Get list of medications
//        medications = medicationStore.getMedications()
        weekdayTextViews = arrayListOf(mondayTextView, tuesdayTextView, wednesdayTextView, thursdayTextView,
                                        fridayTextView, saturdayTextView, sundayTextView)

        // Underline header TextViews
        doseHeaderTextView.text = Html.fromHtml("<u>Dose:</u> ")
        weekdayHeaderTextView.paintFlags = weekdayHeaderTextView.paintFlags or UNDERLINE_TEXT_FLAG
        medicationTimesHeaderTextView.paintFlags = weekdayHeaderTextView.paintFlags or UNDERLINE_TEXT_FLAG
        notesHeaderTextView.paintFlags = notesHeaderTextView.paintFlags or UNDERLINE_TEXT_FLAG

        // Populate TextViews with medication values if viewing an existing medication
        if (intent.getSerializableExtra(this.getString(R.string.view_medication)) is Medication) {
            medication = intent.getSerializableExtra(this.getString(R.string.view_medication)) as Medication
            populateViews()
        }

        // Populate TextViews found by scanning a medication (creating new medication)
        else if (intent.getSerializableExtra(this.getString(R.string.scan_medication)) is Medication) {
            medication = intent.getSerializableExtra(this.getString(R.string.scan_medication)) as Medication
            edit = false
            populateViews()
        }

        // Set onClickListeners for TextViews
        weekdayTextViews.forEach {
            it.setOnClickListener{ clickWeekday(it as TextView) }
        }

        // Set onClickListeners for buttons
        addReminderButton.setOnClickListener { showTimePickerDialog() }
        editMedicationButton.setOnClickListener { editMedication() }
        saveMedicationButton.setOnClickListener { saveMedication() }
        deleteMedicationButton.setOnClickListener { deleteMedication() }
    }

    /**
     * Displays the time picker dialog, and adds the selected time to this medication's list of reminder times.
     * Also displays the newly set reminder time in the list of all of the reminders
     */
    private fun showTimePickerDialog() {
        val time = LocalTime()
        val hour = time.hourOfDay
        val minute = time.minuteOfHour

        val myTimeListener = OnTimeSetListener { timePicker: TimePicker, hour: Int, minute: Int ->
            medication.times.add(LocalTime(hour, minute))
            addTimeTextView(LocalTime(hour, minute))
        }
        val timePickerDialog = TimePickerDialog(this, R.style.DialogTheme, myTimeListener, hour, minute, false)
        timePickerDialog.setTitle("Choose time for reminder")
        timePickerDialog.window.setBackgroundDrawableResource(R.color.mainCardBackground)
        timePickerDialog.show()
    }

    /**
     * Only shows the name and dose EditTexts if the medication is being edited, and only shows the TextViews if not.
     * Also shows all weekday TextViews if editing, only selected day TextViews if not
     */
    private fun setViewVisibility() {
        if (edit) {
            nameEditText.visibility = View.VISIBLE
            doseEditText.visibility = View.VISIBLE
            notesEditText.visibility = View.VISIBLE
            nameTextView.visibility = View.GONE
            doseTextView.visibility = View.GONE
            notesTextView.visibility = View.GONE

            // Make all weekday TextViews visible and set their text colors based on if they had been selected or not
            weekdayTextViews.forEach { textView ->
                textView.visibility = View.VISIBLE
                if (selectedDays.contains(textViewToJoda(textView))) {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.textColor))
                } else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.disabledTextColor))
                }
            }

            // Show the delete reminder icons for each time of this medication
            timeViews.forEach { view -> view.deleteReminderImgView.visibility = View.VISIBLE }
        } else {
            nameEditText.visibility = View.GONE
            doseEditText.visibility = View.GONE
            notesEditText.visibility = View.GONE
            nameTextView.visibility = View.VISIBLE
            doseTextView.visibility = View.VISIBLE
            notesTextView.visibility = View.VISIBLE

            // Show only the TextViews that are in selectedDays
            weekdayTextViews.forEach{ textView ->
                if (selectedDays.contains(textViewToJoda(textView))) {
                    textView.visibility = View.VISIBLE
                    textView.setTextColor(ContextCompat.getColor(this, R.color.textColor))
                } else {
                    textView.visibility = View.GONE
                }
            }

            // Hide the delete reminder icons for each time of this medication
            timeViews.forEach { view -> view.deleteReminderImgView.visibility = View.GONE }
        }

        // Add the "(today!)" text to the current TextView  text
        if (selectedDays.contains(DateTime().dayOfWeek) && !todayShowing) {
            val text = "<font color=#5AA3FF>(today!)</font>"
            calendarToTextView(DateTime().dayOfWeek).append(" ")
            calendarToTextView(DateTime().dayOfWeek).append(Html.fromHtml(text))
            todayShowing = true
        }
    }

    /**
     * Change the color of the weekday TextView when clicked
     * Add/remove the corresponding Calendar weekday from the list of selected days
     */
    private fun clickWeekday(textView: TextView) {
        // Remove this day from selectedDays
        if (textView.currentTextColor == ContextCompat.getColor(this, R.color.textColor)) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.disabledTextColor))
            selectedDays.remove(textViewToJoda(textView))
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            selectedDays.add(textViewToJoda(textView))
        }
        println("SelectedDays: $selectedDays")
    }

    /**
     * Convert the given TextView to its corresponding JodaTime weekday constant to be saved
     * @param textView the TextView to get the JodaTime weekday from
     */
    private fun textViewToJoda(textView: TextView): Int {
        when (textView) {
            mondayTextView -> return DateTimeConstants.MONDAY
            tuesdayTextView -> return DateTimeConstants.TUESDAY
            wednesdayTextView -> return DateTimeConstants.WEDNESDAY
            thursdayTextView -> return DateTimeConstants.THURSDAY
            fridayTextView -> return DateTimeConstants.FRIDAY
            saturdayTextView -> return DateTimeConstants.SATURDAY
            sundayTextView -> return DateTimeConstants.SUNDAY
        }
        throw IllegalArgumentException("Invalid weekday name")
    }

    private fun calendarToTextView(weekday: Int): TextView {
        when (weekday) {
            DateTimeConstants.MONDAY -> return mondayTextView
            DateTimeConstants.TUESDAY -> return tuesdayTextView
            DateTimeConstants.WEDNESDAY -> return wednesdayTextView
            DateTimeConstants.THURSDAY -> return thursdayTextView
            DateTimeConstants.FRIDAY -> return fridayTextView
            DateTimeConstants.SATURDAY -> return saturdayTextView
            DateTimeConstants.SUNDAY -> return sundayTextView
        }
        throw IllegalArgumentException("Invalid weekday name")
    }

    /**
     * Sets the activity views to editable, so the medication can be modified
     */
    private fun editMedication() {
        editMedicationButton.visibility = View.GONE
        saveMedicationButton.visibility = View.VISIBLE
        deleteMedicationButton.visibility = View.VISIBLE
        addReminderButton.visibility = View.VISIBLE
        edit = true
        // The EditTexts and TextViews were already populated when onCreate() called populateViews()
        // Only need to set the correct visibility for editing
        setViewVisibility()
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
        if (doseEditText.text.isNotEmpty()) { dose = doseEditText.text.toString().toFloat() }

        // Create a new Medication object from the fields
        val newMedication = Medication(nameEditText.text.toString(), dose, notesEditText.text.toString())
        newMedication.days = selectedDays
        newMedication.times = medication.times

        // If editing a medication, replace the old medication with a new one. Otherwise, add it to the list
        if (edit) {
            medicationStore.replaceMedication(medication, newMedication)
            scheduleReminder(newMedication, isReplacing = true)
        }
        else {
            medicationStore.saveMedication(newMedication)
            scheduleReminder(newMedication)
        }

        editMedicationButton.visibility = View.VISIBLE
        saveMedicationButton.visibility = View.GONE
        addReminderButton.visibility = View.VISIBLE

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }

    /**
     * Populates the activity's views with the values from the given Medication
     */
    private fun populateViews() {
        if (medication.name != null) {
            nameEditText.setText(medication.name!!.toUpperCase())
            nameTextView.text = medication.name!!.toUpperCase()
        }
        if (medication.dose != null) {
            doseEditText.setText(medication.dose.toString())
            doseTextView.text = "${medication.dose.toString()} mL"
        }
        if (medication.notes != null) {
            notesEditText.setText(medication.notes)
            notesTextView.text = medication.notes
        }
        if (medication.times.isNotEmpty()) {
            medication.times.toList().sorted().forEach { time -> addTimeTextView(time) }
        }
        // Set the visibility of the name, dose, and weekday views based on if the medication is being edited
        selectedDays = medication.days
        setViewVisibility()
    }

    private fun addTimeTextView(time: LocalTime) {
        val view = medicationTimesLinearLayout.inflate(R.layout.time_layout, false)
        val fmt = DateTimeFormat.forPattern("hh:mm a")
        view.timeTextView.text = fmt.print(time)
        medicationTimesLinearLayout.addView(view)
        view.deleteReminderImgView.setOnClickListener { deleteReminder(time, view) }
        if (edit) view.deleteReminderImgView.visibility = View.VISIBLE else view.deleteReminderImgView.visibility = View.GONE
        timeViews.add(view)
    }

    private fun deleteReminder(time: LocalTime, view: View) {
        medication.times.remove(time)
        timeViews.remove(view)
        medicationTimesLinearLayout.removeView(view)
        scheduleReminder(medication, isDeleting = true)
    }

    /**
     * Deletes a medication from the ArrayList and re-saves it to SharedPreferences
     */
    private fun deleteMedication() {
        medicationStore.deleteMedication(medication)

        scheduleReminder(medication, isDeleting = true)

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show()
    }

    /**
     * Executes the given action on the medication
     * @param medication the medication to modify the reminders for
     * @param isReplacing whether the reminder needs to be replaced or not
     * @param isDeleting whether the reminder needs to be removed or not
     */
    private fun scheduleReminder(medication: Medication, isReplacing: Boolean = false, isDeleting: Boolean = false) {
        val nextTime = reminderManager.getNextTime(medication)

        if (isDeleting || nextTime == null) {
            reminderManager.deleteReminder(medication.id)
            return
        }

        if (isReplacing) {
            reminderManager.editReminder(NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, medication.id, nextTime!!)
            return
        }
        reminderManager.addReminder(NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, medication.id, nextTime!!)
    }
}