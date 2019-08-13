package org.vontech.medicine.pokos

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.text.SimpleDateFormat

data class MedicationEvent(
    val medicationId: Int,
    val eventType: MedicationEventType,
    val time: DateTime = DateTime.now(),
    val optionalIndex: Int? = null,
    val optionalReference: Int? = null
)

fun isEventOnDay(event: MedicationEvent, day: LocalDate): Boolean {
    val startOfDay = day.toDateTimeAtStartOfDay()
    val eventTime = event.time
    val givenDayInterval = Interval(startOfDay, startOfDay.plusDays(1))

    return givenDayInterval.contains(eventTime)
}