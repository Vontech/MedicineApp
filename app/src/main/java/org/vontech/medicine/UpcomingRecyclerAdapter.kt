package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.upcoming_recyclerview_item_row.view.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.EditState
import org.vontech.medicine.utils.MedicationHistory
import android.graphics.BitmapFactory
import android.net.Uri
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.vontech.medicine.reminders.ReminderManager

class UpcomingRecyclerAdapter(
    private val medications: List<Medication>,
    private val medicationTimeIndices: List<Int>,
    context: Context,
    private val renderNextMedication: () -> Unit
)
    : RecyclerView.Adapter<UpcomingRecyclerAdapter.MedicationHolder>() {

    val myContext = context

    val timeFormatter = DateTimeFormat.forPattern("hh:mmaa").withZone(DateTimeZone.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationHolder {
        val inflatedView = parent.inflate(R.layout.upcoming_recyclerview_item_row, false)
        return MedicationHolder(inflatedView)
    }

    override fun getItemCount(): Int { return medications.size }

    override fun onBindViewHolder(holder: MedicationHolder, position: Int) {
        val itemMedication = medications[position]
        holder.bindMedication(itemMedication, itemMedication.times.sorted()[medicationTimeIndices[position]])
    }

    // ViewHolder class for the UpcomingRecyclerAdapter
    inner class MedicationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        // References to the inflated view to allow MedicationHolder to access textviews
        private var view: View = v
        private var medication: Medication? = null
        private val medicationHistoryStore = MedicationHistory(itemView.context)

        // Implement a custom onClickListener, since ViewHolders are responsible for their own event handling
        init { v.setOnClickListener(this) }

        override fun onClick(v: View) {
            // Open EditMedicationActivity with medication contents on item click
            val context = itemView.context
            val viewMedicationIntent = Intent(context, EditMedicationActivity::class.java)
            viewMedicationIntent.putExtra(context.getString(R.string.edit_screen_state), EditState.READ)
            viewMedicationIntent.putExtra(context.getString(R.string.view_medication), medication)
            context.startActivity(viewMedicationIntent)
        }

        /**
         * Set the views of the RecyclerView item row to this medication's values
         * @param medication the medication to populate the item row with
         */
        fun bindMedication(medication: Medication, timeToTake: LocalTime) {
            this.medication = medication
            view.nameTextView.text = medication.name
            view.doseTextView.text = "${medication.dose.toString()} ${medication.doseType.name.toLowerCase()}"
            view.reminderTimeTextView.text = timeToTake.toString(timeFormatter)

            if (medication.pillImagePath.isNotEmpty()) {
//                val pillImage: Bitmap = MediaStore.Images.Media.getBitmap(myContext.contentResolver, Uri.parse(medication.pillImagePath))
//                view.imageView.setImageBitmap(getRoundedCornerBitmap(pillImage))
//                view.imageView.setImageBitmap(EditMedicationActivity().maskImage(view.imageView, medication))
                view.imageView.setImageURI(Uri.parse(medication.pillImagePath))
            } else {
                val icon = BitmapFactory.decodeResource(itemView.context.resources, R.drawable.placeholder)
                view.imageView.setImageBitmap(icon)
            }

            view.markAsDoneButton.setOnClickListener {
                handleMedicineTaken(medication)
            }
        }

        private fun handleMedicineTaken(medication: Medication) {

            medicationHistoryStore.takeMedicationNow(medication)
            ReminderManager(itemView.context).resetAllReminders()
            renderNextMedication()

        }
    }

}