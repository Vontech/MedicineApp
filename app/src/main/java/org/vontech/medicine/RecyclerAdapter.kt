package org.vontech.medicine

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import org.vontech.medicine.pokos.Medication

class RecyclerAdapter(private val medications: ArrayList<Medication>)
    : RecyclerView.Adapter<RecyclerAdapter.MedicationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.MedicationHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return MedicationHolder(inflatedView)
    }

    override fun getItemCount(): Int { return medications.size }

    override fun onBindViewHolder(holder: RecyclerAdapter.MedicationHolder, position: Int) {
        val itemMedication = medications[position]
        holder.bindMedication(itemMedication)
    }

    // ViewHolder class for the RecyclerAdapter
    class MedicationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        // References to the inflated view to allow MedicationHolder to access textviews
        private var view: View = v
        private var medication: Medication? = null

        // Implement a custom onClickListener, since ViewHolders are responsible for their own event handling
        init { v.setOnClickListener(this) }

        override fun onClick(v: View) {
            // Open EditMedicationActivity with medication contents on item click
            val context = itemView.context
            val editMedicationIntent = Intent(context, EditMedicationActivity::class.java)
            editMedicationIntent.putExtra(context.getString(R.string.edit_medication), medication)
            context.startActivity(editMedicationIntent)
        }

        /**
         * Set the views of the RecyclerView item row to this medication's values
         * @param medication the medication to populate the item row with
         */
        fun bindMedication(medication: Medication) {
            this.medication = medication
            view.nameTextView.text = medication.name
            view.doseTextView.text = medication.dose.toString()
            view.notesTextView.text = medication.notes
        }
    }
}