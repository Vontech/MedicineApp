package org.vontech.medicine.utils

import android.content.Context
import android.util.Log
import com.google.gson.reflect.TypeToken
import org.vontech.medicine.R
import org.vontech.medicine.pokos.Medication
import java.util.ArrayList

class MedicationStore(context: Context) {

    private val LT = "MedicationStore" // Logging tag
    private val MEDICATIONS_KEY = context.getString(R.string.medication_list)
    private var prefs = getPreferences(context)
    private val gson = getSpecialGson()

    /**
     * Saves a new medication to the list of medications
     */
    fun saveMedication(medication: Medication) {
        Log.d(LT, "saveMedication($medication)")
        val medications = getMedications().toMutableList()
        medications.add(medication)
        saveMedicationList(medications)
    }

    /**
     * Replaces an existing medications with an updated version
     */
    fun replaceMedication(old: Medication, new: Medication) {
        Log.d(LT, "replaceMedication($old, $new)")
        val medications = getMedications().toMutableList()
        val indexOfOld = medications.indexOf(old)
        if (indexOfOld < 0) {
            saveMedication(new)
            return
        }
        medications[indexOfOld] = new
        saveMedicationList(medications)
    }

    /**
     * Deletes the given medication through a property
     * equality match
     */
    fun deleteMedication(medication: Medication) {
        Log.d(LT, "deleteMedication($medication)")
        val medications = getMedications().toMutableList()
        medications.remove(medication)
        saveMedicationList(medications)
    }

    /**
     * Returns the list of current medications
     */
    fun getMedications(): List<Medication> {
        Log.d(LT, "getMedications()")
        val json = prefs.getString(MEDICATIONS_KEY, null)
        val type = object : TypeToken<ArrayList<Medication>>() {}.type
        return if (getSpecialGson().fromJson<ArrayList<Medication>>(json, type) == null) {
            return arrayListOf()
        } else {
            getSpecialGson().fromJson<ArrayList<Medication>>(json, type)
        }
    }

    fun getMedicationById(id: Int): Medication? {
        Log.d(LT, "getMedicationById()")
        val meds = getMedications()
        return meds.firstOrNull {it.id == id}
    }

    /**
     * Saves the list of medications to memory
     */
    private fun saveMedicationList(medications: List<Medication>) {
        Log.d(LT, "saveMedicationList()")
        val editor = prefs.edit()
        val json = gson.toJson(medications)
        editor.putString(MEDICATIONS_KEY, json)
        editor.apply()
    }

}