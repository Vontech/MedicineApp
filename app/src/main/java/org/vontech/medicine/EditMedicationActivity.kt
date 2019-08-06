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
import kotlinx.android.synthetic.main.activity_edit_medication.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore
import java.lang.IllegalArgumentException
import android.util.Log
import android.widget.TimePicker
import kotlinx.android.synthetic.main.time_layout.view.*
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.vontech.medicine.utils.EditState

class EditMedicationActivity : AppCompatActivity() {

    val NOTIFICATION_TITLE = "Time to take your medicine!"
    val NOTIFICATION_MESSAGE = "Click to view this medication"
    val FN = "EditMedicationActivity"

    private lateinit var medicationStore: MedicationStore
    private var isEditing: Boolean = false
    private var isReplacing: Boolean = false
    private var todayShowing: Boolean = false
    private lateinit var medication: Medication
    private var preservedMedication: Medication? = null
    private lateinit var reminderManager: ReminderManager

    private lateinit var viewsShownDuringEditing: MutableList<View>
    private lateinit var viewsShownDuringViewing: MutableList<View>
    private lateinit var weekdayTextViews: List<TextView>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_medication)

        // Create access to storage operations
        medicationStore = MedicationStore(this)
        reminderManager = ReminderManager(this)

        // Create lists of views for convenience
        weekdayTextViews = arrayListOf(mondayTextView, tuesdayTextView, wednesdayTextView, thursdayTextView,
                                        fridayTextView, saturdayTextView, sundayTextView)
        viewsShownDuringEditing = arrayListOf(nameEditText, doseEditText, notesEditText, saveMedicationButton,
                                                addReminderButton)
        viewsShownDuringEditing.addAll(weekdayTextViews)
        viewsShownDuringViewing = arrayListOf(nameTextView, doseTextView, notesTextView, todayTextView)

        // Setup basic styles and interactions
        setupOnClickListeners()

        // Setup based on state
        when (intent.getSerializableExtra(getString(R.string.edit_screen_state))) {
            EditState.ADDING -> setupAddingState()
            EditState.SCANNING -> setupScanningState()
            EditState.READ -> setupViewingState()
            EditState.EDITING -> setupEditingState()
        }

        // Preserve whichever current medication is being held
        preserveMedication()

        refreshUI()

    }

    private fun refreshUI() {

        // First, hide and show primary form views based on editing and viewing state
        viewsShownDuringEditing.forEach {
            it.visibility = if (isEditing) View.VISIBLE else View.GONE
            it.isClickable = isEditing
        }
        viewsShownDuringViewing.forEach {it.visibility = if (isEditing) View.GONE else View.VISIBLE}

        // Show buttons based on state
        editMedicationButton.visibility = if (isReplacing) View.VISIBLE else View.GONE
        deleteMedicationButton.visibility = if (isReplacing && isEditing) View.VISIBLE else View.GONE
        editMedicationButton.text = if (isEditing) "cancel edits" else "edit medication"
        // if isEditing && !isReplacing, show the cancel adding button and return to MainActivity

        if (medication.name != null) {
            nameEditText.setText(medication.name!!)
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

        // Show each time
        medicationTimesLinearLayout.removeAllViews()
        medication.times.toList().sorted().forEach { time -> medicationTimesLinearLayout.addView(getTimeTextView(time)) }

        // Show the delete reminder icons for each time of this medication
        (0 until medicationTimesLinearLayout.childCount).forEach {
            medicationTimesLinearLayout.getChildAt(it).deleteReminderImgView.visibility = if (isEditing) View.VISIBLE else View.GONE
        }

        // Change the styling of each weekday for the medication
        weekdayTextViews.forEach { textView ->
            if (medication.days.contains(textViewToJoda(textView))) {
                textView.visibility = View.VISIBLE
                textView.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.disabledTextColor))
            }
        }

        todayTextView.visibility = View.GONE
        todayShowing = false

        // Make todayTextView visible if the today is one of the days this medication is taken
        if (medication.days.contains(DateTime().dayOfWeek)) {
            todayTextView.visibility = View.VISIBLE
            todayShowing = true
        }

    }

    private fun preserveMedication() {
        preservedMedication = Medication(
            medication.name,
            medication.dose,
            medication.notes,
            medication.id
        )
        preservedMedication!!.days = medication.days.filter {true}.toMutableSet()
        preservedMedication!!.times = medication.times.filter {true}.toMutableSet()
    }

    private fun loadOriginalMedication() {
        medication = preservedMedication!!

    }

    private fun setupOnClickListeners() {

        // Set onClickListeners for TextViews
        weekdayTextViews.forEach {
            it.setOnClickListener{ clickWeekday(it as TextView) }
        }

        // Set onClickListeners for buttons
        addReminderButton.setOnClickListener { showTimePickerDialog() }
        editMedicationButton.setOnClickListener {
            editMedication(!isEditing)
        }
        saveMedicationButton.setOnClickListener { saveMedication() }
        deleteMedicationButton.setOnClickListener { deleteMedication() }

        // Set edit text listeners
        nameEditText.afterTextChanged {
            medication.name = it
        }
        doseEditText.afterTextChanged {
            medication.dose = it.toFloat()
        }
        notesEditText.afterTextChanged {
            medication.notes = it
        }

    }

    private fun setupAddingState() {
        isEditing = true
        isReplacing = false
        medication = Medication(nameEditText.text.toString(), 0f, notesEditText.text.toString())
    }

    private fun setupScanningState() {
        medication = intent.getSerializableExtra(this.getString(R.string.scan_medication)) as Medication
        isEditing = false
        isReplacing = false
    }

    private fun setupEditingState() {
        isEditing = true
        isReplacing = true
    }

    /**
     * Populate TextViews with medication values if viewing an existing medication
     */
    private fun setupViewingState() {
        medication = intent.getSerializableExtra(this.getString(R.string.view_medication)) as Medication
        isEditing = false
        isReplacing = true
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
            refreshUI()
        }
        val timePickerDialog = TimePickerDialog(this, R.style.DialogTheme, myTimeListener, hour, minute, false)
        timePickerDialog.setTitle("Choose time for reminder")
        timePickerDialog.window.setBackgroundDrawableResource(R.color.mainCardBackground)
        timePickerDialog.show()
    }

    /**
     * Change the color of the weekday TextView when clicked
     * Add/remove the corresponding Calendar weekday from the list of selected days
     */
    private fun clickWeekday(textView: TextView) {
        // Remove this day from selectedDays
        if (textView.currentTextColor == ContextCompat.getColor(this, R.color.textColor)) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.disabledTextColor))
            medication.days.remove(textViewToJoda(textView))
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            medication.days.add(textViewToJoda(textView))
        }
        refreshUI()
    }



    /**
     * Sets the activity views to editable, so the medication can be modified
     */
    private fun editMedication(editing: Boolean) {
        isEditing = editing
        // The EditTexts and TextViews were already populated when onCreate() called populateViews()
        // Only need to set the correct visibility for editing

        if (isEditing) {
            preserveMedication()
        } else {
            loadOriginalMedication()
        }
        refreshUI()

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
        newMedication.days = medication.days
        newMedication.times = medication.times

        // If editing a medication, replace the old medication with a new one. Otherwise, add it to the list
        if (isReplacing) {
            Log.i(FN, "Replacing an existing mediciation")
            medicationStore.replaceMedication(medication, newMedication)
            scheduleReminder(newMedication, isReplacing = true)
        }
        else {
            Log.i(FN, "Saving a new medication")
            medicationStore.saveMedication(newMedication)
            scheduleReminder(newMedication)
        }

        // If adding, simply move back to home
        // If replacing, turn off editing
        if (!isReplacing) {
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
        }
        else {
            isReplacing = true
            medication = newMedication
            preserveMedication()
            editMedication(false)
        }

        refreshUI()

    }

    private fun getTimeTextView(time: LocalTime): View {
        val view = medicationTimesLinearLayout.inflate(R.layout.time_layout, false)
        val fmt = DateTimeFormat.forPattern("hh:mm a")
        view.timeTextView.text = fmt.print(time).toLowerCase()
        view.deleteReminderImgView.setOnClickListener { deleteReminder(time, view) }
        return view
    }

    private fun deleteReminder(time: LocalTime, view: View) {
        medication.times.remove(time)
        scheduleReminder(medication, isDeleting = true)
        refreshUI()
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

        refreshUI()
    }


    /***
     *
     *
     *  Utility functions
     *
     *
     */



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

}