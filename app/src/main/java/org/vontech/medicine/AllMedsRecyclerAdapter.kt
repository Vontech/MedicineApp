package org.vontech.medicine

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

class AllMedsRecyclerAdapter(private val medications: List<Medication>)
    : RecyclerView.Adapter<AllMedsRecyclerAdapter.MedicationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationHolder {
        val inflatedView = parent.inflate(R.layout.all_meds_recyclerview_item_row, false)
        return MedicationHolder(inflatedView)
    }

    override fun getItemCount(): Int { return medications.size }

    override fun onBindViewHolder(holder: MedicationHolder, position: Int) {
        val itemMedication = medications[position]
        holder.bindMedication(itemMedication)
    }

    // ViewHolder class for the UpcomingRecyclerAdapter
    class MedicationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
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
        fun bindMedication(medication: Medication) {
            this.medication = medication
            view.nameTextView.text = medication.name
            view.doseTextView.text = "${medication.dose.toString()} ${medication.doseType.name.toLowerCase()}"

            if (medication.pillImagePath.isNotEmpty()) {
                view.imageView.setImageURI(Uri.parse(medication.pillImagePath))
            } else {
                val icon = BitmapFactory.decodeResource(itemView.context.resources, R.drawable.placeholder)
                view.imageView.setImageBitmap(icon)
            }
        }
    }

}