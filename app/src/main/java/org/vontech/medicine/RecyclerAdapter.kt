package org.vontech.medicine

import android.content.Intent
import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.pokos.MedicationEvent
import org.vontech.medicine.pokos.MedicationEventType
import org.vontech.medicine.utils.EditState
import org.vontech.medicine.utils.MedicationHistory
import android.graphics.BitmapFactory

class RecyclerAdapter(private val medications: List<Medication>)
    : RecyclerView.Adapter<RecyclerAdapter.MedicationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.MedicationHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return MedicationHolder(inflatedView)
    }

    override fun getItemCount(): Int { return medications.size }

    override fun onBindViewHolder(holder: MedicationHolder, position: Int) {
        val itemMedication = medications[position]
        holder.bindMedication(itemMedication)
    }

    // ViewHolder class for the RecyclerAdapter
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
            view.doseTextView.text = medication.dose.toString() + " mL"

            val icon = BitmapFactory.decodeResource(itemView.context.resources, R.drawable.placeholder)
            view.imageView.setImageBitmap(getRoundedCornerBitmap(icon))

            view.markAsDoneButton.setOnClickListener {
                handleMedicineTaken(medication)
            }
        }

        private fun handleMedicineTaken(medication: Medication) {

            // Log this as taken
            medicationHistoryStore.addEvent(MedicationEvent(
                medication.id,
                MedicationEventType.TAKEN
            ))

        }

        private fun getRoundedCornerBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)
            val roundPx = 1000f

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }
    }

}