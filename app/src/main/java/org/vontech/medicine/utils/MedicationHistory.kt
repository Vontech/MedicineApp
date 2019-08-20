package org.vontech.medicine.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Period
import org.vontech.medicine.R
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.pokos.MedicationEvent
import org.vontech.medicine.pokos.MedicationEventType
import org.vontech.medicine.security.SecurePreferencesBuilder
import java.util.ArrayList

class MedicationHistory(context: Context) {

    private val LT = "MedicationHistory" // Logging tag
    private val MEDICATIONS_HISTORY_KEY = context.getString(R.string.medication_history)
    private var prefs = getPreferences(context)
    private val gson = getSpecialGson()

    fun addEvent(medicationEvent: MedicationEvent) {
        Log.d(LT, "Adding event $medicationEvent")
        val events = getAllEvents()
        events.add(medicationEvent)
        saveMedicationHistoryList(events)
    }

    fun addMedicationEditedEvent(medication: Medication) {
        val med = medication.createCopy()
        addEvent(MedicationEvent(
            med.id,
            MedicationEventType.EDITED,
            DateTime.now(),
            optionalMedicationReference = med
        ))
    }

    /**
     * Returns a list of medication events given a medication id. Optionally
     * filter by time range and event type as well
     * Guaranteed to be sorted!
     */
    fun getEventsForMedication(medicationId: Int,
                               dateStart: DateTime? = null,
                               dateEnd: DateTime? = null,
                               eventType: MedicationEventType? = null): List<MedicationEvent> {

        Log.d(LT, "getEventsForMedication()")
        val events = getAllEvents()
        val filtered = mutableListOf<MedicationEvent>()
        events.forEach {
            if (medicationId != it.medicationId) {
                return@forEach
            }
            if (eventType != null && it.eventType != eventType) {
                return@forEach
            }
            if (dateStart != null && it.time.isBefore(dateStart)) {
                return@forEach
            }
            if (dateEnd != null && it.time.isAfter(dateEnd)) {
                return@forEach
            }
            filtered.add(it)
        }
        return filtered.sortedBy { it.time }

    }

    fun getIndicesOfTimesTakenToday(medication: Medication): List<Int> {
        return this.getEventsForMedication(
            medication.id,
            DateTime.now().withTimeAtStartOfDay(),
            DateTime.now().withTimeAtStartOfDay().plusDays(1),
            MedicationEventType.TAKEN)
            .map {it.optionalIndex!!}
    }

    fun takeMedicationNow(medication: Medication) {
        // Find out which time this was closest to, after removing the times
        // that this was likely already take
        // TODO: Make sure this works on a day where this was edited??
        val takenIndices = this.getIndicesOfTimesTakenToday(medication)

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
        this.addEvent(MedicationEvent(
            medication.id,
            MedicationEventType.TAKEN,
            optionalIndex = closestIndex,
            optionalReference = medication.times.size
        ))

    }

    /**
     * Returns the list of all events that have occurred
     */
    fun getAllEvents(): MutableList<MedicationEvent> {
        Log.d(LT, "getAllEvents()")
        val json = prefs.getString(MEDICATIONS_HISTORY_KEY, null)
        val type = object : TypeToken<ArrayList<MedicationEvent>>() {}.type
        return if (getSpecialGson().fromJson<ArrayList<MedicationEvent>>(json, type) == null) {
            return arrayListOf()
        } else {
            getSpecialGson().fromJson<ArrayList<MedicationEvent>>(json, type)
        }
    }

    /**
     * Saves the list of medications to memory
     */
    private fun saveMedicationHistoryList(medicationHistory: List<MedicationEvent>) {
        Log.d(LT, "saveMedicationHistoryList()")
        val editor = prefs.edit()
        val json = gson.toJson(medicationHistory)
        editor.putString(MEDICATIONS_HISTORY_KEY, json)
        editor.apply()
    }

}