package org.vontech.medicine.pokos

import org.joda.time.LocalTime

data class MedicationEvent(
    val medicationId: Int,
    val eventType: MedicationEventType,
    val time: LocalTime = LocalTime.now()
)