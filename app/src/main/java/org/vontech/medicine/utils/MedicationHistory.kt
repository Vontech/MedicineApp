package org.vontech.medicine.utils

import android.content.Context
import android.util.Log
import com.google.gson.reflect.TypeToken
import org.joda.time.LocalTime
import org.vontech.medicine.R
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.pokos.MedicationEvent
import org.vontech.medicine.pokos.MedicationEventType
import org.vontech.medicine.security.SecurePreferencesBuilder
import java.util.ArrayList

class MedicationHistory(context: Context) {

    private val LT = "MedicationHistory" // Logging tag
    private val MEDICATIONS_HISTORY_KEY = context.getString(R.string.medication_list)
    private var prefs = SecurePreferencesBuilder(context).build()//context.getSharedPreferences(MED_KEY, Context.MODE_PRIVATE) //SecurePreferencesBuilder(context).build()
    private val gson = getSpecialGson()

    /**
     * Returns a list of medication events given a medication id. Optionally
     * filter by time range and event type as well
     */
    fun getEventsForMedication(medicationId: Int,
                               dateStart: LocalTime? = null,
                               dateEnd: LocalTime? = null,
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
        return filtered

    }

    /**
     * Returns the list of all events that have occurred
     */
    fun getAllEvents(): List<MedicationEvent> {
        Log.d(LT, "getAllEvents()")
        val json = prefs.getString(MEDICATIONS_HISTORY_KEY, null)
        val type = object : TypeToken<ArrayList<MedicationEvent>>() {}.type
        return if (getSpecialGson().fromJson<ArrayList<MedicationEvent>>(json, type) == null) {
            return arrayListOf()
        } else {
            getSpecialGson().fromJson<ArrayList<MedicationEvent>>(json, type)
        }
    }

}