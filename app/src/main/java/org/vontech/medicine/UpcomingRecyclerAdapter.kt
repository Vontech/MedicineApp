package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.upcoming_recyclerview_item_row.view.*
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.pokos.MedicationEvent
import org.vontech.medicine.pokos.MedicationEventType
import org.vontech.medicine.utils.EditState
import org.vontech.medicine.utils.MedicationHistory
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.joda.time.*

class UpcomingRecyclerAdapter(
    private val medications: List<Medication>,
    context: Context,
    private val renderNextMedication: () -> Unit
)
    : RecyclerView.Adapter<UpcomingRecyclerAdapter.MedicationHolder>() {

    val myContext = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationHolder {
        val inflatedView = parent.inflate(R.layout.upcoming_recyclerview_item_row, false)
        return MedicationHolder(inflatedView)
    }

    override fun getItemCount(): Int { return medications.size }

    override fun onBindViewHolder(holder: MedicationHolder, position: Int) {
        val itemMedication = medications[position]
        holder.bindMedication(itemMedication)
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
        fun bindMedication(medication: Medication) {
            this.medication = medication
            view.nameTextView.text = medication.name
            view.doseTextView.text = "${medication.dose.toString()} ${medication.doseType.name.toLowerCase()}"

            if (medication.pillImagePath.isNotEmpty()) {
//                val pillImage: Bitmap = MediaStore.Images.Media.getBitmap(myContext.contentResolver, Uri.parse(medication.pillImagePath))
//                view.imageView.setImageBitmap(getRoundedCornerBitmap(pillImage))
//                view.imageView.setImageBitmap(EditMedicationActivity().maskImage(view.imageView, medication))
                view.imageView.setImageURI(Uri.parse(medication.pillImagePath))
            } else {
                val icon = BitmapFactory.decodeResource(itemView.context.resources, R.drawable.placeholder)
                view.imageView.setImageBitmap(getRoundedCornerBitmap(icon))
            }

            view.markAsDoneButton.setOnClickListener {
                handleMedicineTaken(medication)
            }
        }

        private fun handleMedicineTaken(medication: Medication) {

            // Find out which time this was closest to, after removing the times
            // that this was likely already take
            // TODO: Make sure this works on a day where this was edited??
            val takenIndices = medicationHistoryStore.getIndicesOfTimesTakenToday(medication)

            val indexedTimesToTake = mutableMapOf<Int, LocalTime>()
            medication.times.sorted().forEachIndexed { index, localTime ->
                if (index !in takenIndices) {
                    indexedTimesToTake[index] = localTime
                }
            }

            if (indexedTimesToTake.isEmpty()) {
                Log.e("NEXT REMINDER", "Something went horribly wrong! Times to take is already empty")
                return
            }

            val now = DateTime.now().toLocalTime()
            var closestIndex = 0
            var timeDistance = Int.MAX_VALUE
            indexedTimesToTake.forEach {
                val distance = Period.fieldDifference(now, it.value)
                if (distance.millis < timeDistance) {
                    timeDistance = distance.millis
                    closestIndex = it.key
                }
            }

            // Log this as taken
            medicationHistoryStore.addEvent(MedicationEvent(
                medication.id,
                MedicationEventType.TAKEN,
                optionalIndex = closestIndex,
                optionalReference = medication.times.size
            ))

            renderNextMedication()

        }

        private fun getRoundedCornerBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)
            val roundPx = (bitmap.width / 4f)

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