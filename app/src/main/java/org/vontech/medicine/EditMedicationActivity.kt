package org.vontech.medicine

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_edit_medication.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.reminders.ReminderManager
import org.vontech.medicine.utils.MedicationStore
import java.lang.IllegalArgumentException
import android.util.Log
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.calendar_day_view.view.*
import kotlinx.android.synthetic.main.time_layout.view.*
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.vontech.medicine.utils.EditState
import org.vontech.medicine.views.CalendarEntryGenerator
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.android.synthetic.main.delete_dialog.*
import org.vontech.medicine.ocr.DosageType
import org.vontech.medicine.pokos.MedicationEvent
import org.vontech.medicine.pokos.MedicationEventType
import org.vontech.medicine.utils.MedicationHistory
import org.vontech.medicine.utils.buildDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class EditMedicationActivity : AppCompatActivity() {

    val NOTIFICATION_TITLE = "Time to take your medicine!"
    val NOTIFICATION_MESSAGE = "Click to view this medication"
    val FN = "EditMedicationActivity"

    val REQUEST_IMAGE_CAPTURE = 2
    private lateinit var photoPath: String

    private lateinit var medicationStore: MedicationStore
    private lateinit var medicationHistory: MedicationHistory
    private var isEditing: Boolean = false
    private var isReplacing: Boolean = false
    private var todayShowing: Boolean = false
    private lateinit var medication: Medication
    private var preservedMedication: Medication? = null
    private lateinit var reminderManager: ReminderManager

    private lateinit var viewsShownDuringEditing: MutableList<View>
    private lateinit var viewsShownDuringViewing: MutableList<View>
    private lateinit var weekdayTextViews: List<TextView>

    private lateinit var takePictureIntent: Intent
    private lateinit var photoUri: Uri

    private var currentMonthState: LocalDate = LocalDate.now().withDayOfMonth(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_medication)

        // Create access to storage operations
        medicationStore = MedicationStore(this)
        medicationHistory = MedicationHistory(this)
        reminderManager = ReminderManager(this)

        // Create lists of views for convenience
        weekdayTextViews = arrayListOf(mondayTextView, tuesdayTextView, wednesdayTextView, thursdayTextView,
                                        fridayTextView, saturdayTextView, sundayTextView)
        viewsShownDuringEditing = arrayListOf(nameEditText, doseEditText, notesEditText, saveMedicationButton,
                                                addReminderButton, selectAllDaysButton)
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
        refreshCalendarUI()

    }

    private fun refreshUI() {

        // First, hide and show primary form views based on editing and viewing state
        viewsShownDuringEditing.forEach {
            it.visibility = if (isEditing) View.VISIBLE else View.GONE
            it.isClickable = isEditing
        }
        viewsShownDuringViewing.forEach {it.visibility = if (isEditing) View.GONE else View.VISIBLE}
        // Special case for if notes are empty
        if (medication.notes.isEmpty() && !isEditing) notesTextView.visibility = View.GONE
        notesHeaderTextView.visibility = if (medication.notes.isEmpty() && !isEditing) View.GONE else View.VISIBLE

        // Show buttons based on state
        editMedicationButton.visibility = if (isReplacing) View.VISIBLE else View.GONE
        deleteMedicationButton.visibility = if (isReplacing && isEditing) View.VISIBLE else View.GONE
        editMedicationButton.text = if (isEditing) "cancel edits" else "edit medication"
        // if isEditing && !isReplacing, show the cancel adding button and return to MainActivity
        cancelAddingButton.visibility = if (isEditing && !isReplacing) View.VISIBLE else View.GONE
        pillImageCameraOverlay.visibility = if (isEditing) View.VISIBLE else View.GONE
//        pillImageView.isClickable = isEditing

        if (medication.pillImagePath.isNotEmpty()) {
            pillImageView.setImageURI(Uri.parse(medication.pillImagePath))
        }
        if (medication.name != null) {
            nameEditText.setText(medication.name!!)
            nameTextView.text = medication.name!!
        }
        if (medication.dose != null) {
            doseEditText.setText(medication.dose.toString())
            doseTextView.text = medication.dose.toString()
        }
        if (medication.notes != null) {
            notesEditText.setText(medication.notes)
            notesTextView.text = medication.notes
        }

        doseMeasureText.text = medication.doseType.name.toLowerCase()

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

        // hide and show history
        historyContainer.visibility = if (isEditing) View.GONE else View.VISIBLE
    }

    private fun refreshCalendarUI() {

        if (isEditing) return
        // First, we get the medication activity
        val events = medicationHistory.getEventsForMedication(medication.id)
        val creationDate = events.first {it.eventType == MedicationEventType.CREATED}
        println("EVENTS FOR THIS MEDICATION")
        println(events)

        // We also get the current month state
        calendar.month = currentMonthState.monthOfYear
        calendar.year = currentMonthState.year

        // Now update what is to be shown within the calendar
        calendar.calendarEntryGenerator = object : CalendarEntryGenerator {
            override fun create(day: LocalDate): View {

                val view =
                    LayoutInflater.from(this@EditMedicationActivity).inflate(R.layout.calendar_day_view, null, false)

                view.calendarDayText.text = day.dayOfMonth.toString()

                // Case 1: Before creation date or after current date
                if (day.isBefore(creationDate.time.toLocalDate()) || day.isAfter(LocalDate.now())) {
                    // DO NOTHING
                }

                // Case 2: Not today - Find out what happened on this day
                // 1) Figure out how many times it was taken
                // 2) Figure out how many times it should have been taken

                // Case 3: Is today

                when {
                    day.dayOfMonth % 2 == 0 -> view.calendarDayText.background = resources.getDrawable(R.drawable.calendar_some_taken)
                    day.dayOfMonth % 5 == 0 -> view.calendarDayText.background = resources.getDrawable(R.drawable.calendar_all_taken)
                    day.dayOfMonth % 7 == 0 -> view.calendarDayText.background = resources.getDrawable(R.drawable.calendar_none_taken)
                    else -> view.calendarDayText.setTextColor(resources.getColor(R.color.disabledTextColor))
                }

                view.calendarDayText.setPadding(0, 0, 0, 0)

                return view

            }
        }
    }

    private fun preserveMedication() {
        preservedMedication = Medication(
            medication.name,
            medication.dose,
            medication.notes,
            medication.id,
            medication.doseType
        )
        preservedMedication!!.days = medication.days.filter {true}.toMutableSet()
        preservedMedication!!.times = medication.times.filter {true}.toMutableSet()
        preservedMedication!!.pillImagePath = medication.pillImagePath
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
        editMedicationButton.setOnClickListener { editMedication(!isEditing) }
        saveMedicationButton.setOnClickListener { saveMedication() }
        deleteMedicationButton.setOnClickListener { deleteMedication() }
        cancelAddingButton.setOnClickListener {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            Toast.makeText(this, "Discarded medication", Toast.LENGTH_SHORT).show()
        }
        pillImageCameraOverlay.setOnClickListener { dispatchTakePictureIntent() }

        // Set edit text listeners
        nameEditText.afterTextChanged {
            medication.name = it
        }
        doseEditText.afterTextChanged {
            if (it.isNotEmpty()) {
                medication.dose = it.toFloat()
            } else {
                medication.dose = 0f
            }
        }
        notesEditText.afterTextChanged {
            medication.notes = it
        }

        previousMonthButton.setOnClickListener {
            currentMonthState = currentMonthState.plusMonths(-1)
            refreshCalendarUI()
        }

        nextMonthButton.setOnClickListener {
            currentMonthState = currentMonthState.plusMonths(1)
            refreshCalendarUI()
        }
        doseMeasureText.setOnClickListener {
            if (!isEditing) {
                return@setOnClickListener
            }
            val popup = PopupMenu(this, it)
            DosageType.values().forEach { dt ->
                popup.menu.add(dt.name).setTitle(dt.name.toLowerCase())
            }
            popup.setOnMenuItemClickListener { mi ->
                handleDosageTypeClicked(mi.title.toString())
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
        selectAllDaysButton.setOnClickListener {
            weekdayTextViews.forEach { tv ->
                if (textViewToJoda(tv) !in medication.days) {
                    clickWeekday(tv as TextView)
                }
            }

        }

    }

    private fun setupAddingState() {
        isEditing = true
        isReplacing = false
        medication = Medication(nameEditText.text.toString(), null, notesEditText.text.toString())
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

    private fun handleDosageTypeClicked(dosageType: String) {
        medication.doseType = DosageType.valueOf(dosageType.toUpperCase())
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
        } else { // Cancelling edits without saving, revert to previous version of medication
            loadOriginalMedication()
            scrollToTop()
            hideKeyboard()
        }
        refreshUI()

    }

    // Validates the medications before saving. Returns false if the form is not valid
    private fun validateForm(): Boolean {

        // Should have a name
        if (medication.name?.trim()?.length == 0) {
            showValidationError("A valid medication name must be entered.")
            return false
        }

        // Should have a dosage
        if (medication.dose == null || medication.dose!! < 0.000001) {
            showValidationError("A valid dosage amount greater than 0 must be entered.")
            return false
        }

        // Should have at least one day
        if (medication.days.size == 0) {
            showValidationError("The medication must be taken on at least one day.")
            return false
        }

        // Should have at least one time
        if (medication.times.size == 0) {
            showValidationError("The medication must have at least one time to be taken.")
            return false
        }

        return true

    }

    private fun showValidationError(message: String) {
        val dialog = buildDialog("Medication Incomplete", message)
        dialog.positiveButton.text = "OK"
        dialog.negativeButton.visibility = View.GONE
        dialog.positiveButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Updates a modified Medication if editing, or adds a new Medication to the ArrayList of Medications
     */
    private fun saveMedication() {

        if (!validateForm()) {
            return
        }

        // Do not allow medication to be created without a name
        if (nameEditText.text.isEmpty()) {
            Toast.makeText(this, "Must give medication a name", Toast.LENGTH_SHORT).show()
            return
        }

        // Dose has a default value to protect against converting a null value to an Int
        var dose = 0f
        if (doseEditText.text.isNotEmpty()) { dose = doseEditText.text.toString().toFloat() }

        // If editing a medication, replace the old medication with a new one. Otherwise, add it to the list
        if (isReplacing) {
            Log.i(FN, "Replacing an existing medication")
            medicationStore.replaceMedication(preservedMedication!!, medication)
            scheduleReminder(medication, isReplacing = true)
        }
        else {
            Log.i(FN, "Saving a new medication")
            medicationStore.saveMedication(medication)
            scheduleReminder(medication)
            medicationHistory.addEvent(MedicationEvent(
                medication.id,
                MedicationEventType.CREATED
            ))
        }

        // If adding, simply move back to home
        // If replacing, turn off editing
        if (!isReplacing) {
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            Toast.makeText(this, "Added new medication: " + nameEditText.text, Toast.LENGTH_SHORT).show()
        }
        else {
            isReplacing = true
            preserveMedication()
            editMedication(false)
            Toast.makeText(this, "Saved edits", Toast.LENGTH_SHORT).show()
        }

        refreshUI()
        scrollToTop()
        hideKeyboard()

    }

    private fun getTimeTextView(time: LocalTime): View {
        val view = medicationTimesLinearLayout.inflate(R.layout.time_layout, false)
        val fmt = DateTimeFormat.forPattern("hh:mm a")
        view.timeTextView.text = fmt.print(time).toLowerCase()
        view.deleteReminderImgView.setOnClickListener { deleteReminder(time) }
        return view
    }

    /**
     * Deletes the given reminder time from this medication
     * @param time: the time to be removed
     */
    private fun deleteReminder(time: LocalTime) {
        val dialog = buildDialog("Confirm Deletion", "Do you want to delete this reminder?")
        dialog.positiveButton.text = "DELETE"
        dialog.positiveButton.setOnClickListener {
            medication.times.remove(time)
            scheduleReminder(medication, isDeleting = true)
            refreshUI()
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Deletes a medication from the ArrayList and re-saves it to SharedPreferences
     */
    private fun deleteMedication() {
        val dialog = buildDialog("Confirm Deletion", "Do you want to delete this medication?")
        dialog.positiveButton.text = "DELETE"
        dialog.positiveButton.setOnClickListener {
            medicationStore.deleteMedication(medication)

            scheduleReminder(medication, isDeleting = true)
            dialog.dismiss()

            // Return to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
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
            reminderManager.editReminder(NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, medication.id, nextTime)
            return
        }
        reminderManager.addReminder(NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, medication.id, nextTime)

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

    /**
     * Creates a file with
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.absolutePath

        return image
    }

    /**
     * Configure intent for opening camera and photoFile Uri. Check if camera permissions
     * have been granted and launch the camera
     */
    private fun dispatchTakePictureIntent() {
        takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try { createImageFile() } catch (e: IOException) { null }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                    this, "com.example.android.fileprovider", photoFile
                )
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
                } else { openCamera() }
            }
        }
    }

    /**
     * Callback function called after app requests camera permissions from user.
     * If accepted, launch the camera
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            val numOfRequest = grantResults.size
            val isGranted = (numOfRequest == 1) &&
                    (PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1])

            if (isGranted) { openCamera() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            medication.pillImagePath = photoPath
            pillImageView.setImageURI(Uri.parse(photoPath))
        }
    }

    /**
     * Launch intent to open the camera for taking a picture of a pill
     */
    private fun openCamera() {
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    /**
     * Scroll to the top of the activity
     */
    private fun scrollToTop() { editScreenScrollView.scrollTo(0, 0) }

    /**
     * Hide the software keyboard, if it is showing
     */
    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

}