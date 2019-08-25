package org.vontech.medicine.utils

import android.content.Context
import android.util.Log
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.vontech.medicine.ocr.DosageType
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.pokos.MedicationEvent
import org.vontech.medicine.pokos.MedicationEventType

fun generateFakeMedication(context: Context) {

    val medStore = MedicationStore(context)
    val history = MedicationHistory(context)

    val med = Medication(
        name = "Test Medication",
        dose = 125f,
        notes = "This is a test medication",
        doseType = DosageType.MCG
    )
    med.days = mutableSetOf(1,2,3,4,5,6,7) // Taken every day
    med.times = mutableSetOf(LocalTime.parse("11:00:00"), LocalTime.parse("20:00:00"))

    // Create creation event for a few weeks ago
    DateTime.now().withTimeAtStartOfDay().plusHours(10)
    val creationTime = DateTime.now().withTimeAtStartOfDay().plusHours(10).minusDays(21)
    medStore.saveMedication(med)
    history.addEvent(
        MedicationEvent(
            med.id,
            MedicationEventType.CREATED,
            time = creationTime
        )
    )
    history.addEvent(MedicationEvent(
        med.id,
        MedicationEventType.EDITED,
        creationTime,
        optionalMedicationReference = med
    ))

    // Until up to today, create some events
    val currentTime = creationTime.toMutableDateTime()
    val now = DateTime.now()
    while (currentTime.isBefore(now)) {
        val dateTime = currentTime.copy()
        Log.e("GENERATING TIME", currentTime.toString())

        if (dateTime.dayOfWeek == 3) {
            currentTime.addDays(1)
            continue
        }

        // Add that I took it in the morning
        dateTime.setTime(11, 0, 0, 0)
        history.addEvent(
            MedicationEvent(
                med.id,
                MedicationEventType.TAKEN,
                time = dateTime.toDateTime(),
                optionalIndex = 0,
                optionalReference = med.times.size
            ))

        if (dateTime.dayOfWeek == 5) {
            currentTime.addDays(1)
            continue
        }

        dateTime.setTime(20, 0, 0, 0)
        history.addEvent(
            MedicationEvent(
                med.id,
                MedicationEventType.TAKEN,
                time = dateTime.toDateTime(),
                optionalIndex = 1,
                optionalReference = med.times.size
            ))

        currentTime.addDays(1)
    }

}