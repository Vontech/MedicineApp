package org.vontech.medicine.pokos

import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.util.Calendar
import kotlin.random.Random

data class Medication(var name: String?, var dose: Float?, var notes: String?) : Serializable {
    val id = Random.nextInt()
    var days = mutableSetOf<Int>() // JodaTime weekday constants
    var times = mutableSetOf<LocalTime>() // JodaTime object

    /**
     *  Adapter method for converting from Joda weekdays to Calendar weekdays
     */
//    fun fromJoda() : List<Int> {
//        val calendarList = arrayListOf<Int>()
//        // for each day in days, add a calendar constant to the list
//        days.forEach {
//            when (it) {
//                DateTimeConstants.MONDAY -> calendarList.add(Calendar.MONDAY)
//                DateTimeConstants.TUESDAY -> calendarList.add(Calendar.TUESDAY)
//                DateTimeConstants.WEDNESDAY -> calendarList.add(Calendar.WEDNESDAY)
//                DateTimeConstants.THURSDAY -> calendarList.add(Calendar.THURSDAY)
//                DateTimeConstants.FRIDAY -> calendarList.add(Calendar.FRIDAY)
//                DateTimeConstants.SATURDAY -> calendarList.add(Calendar.SATURDAY)
//                DateTimeConstants.SUNDAY -> calendarList.add(Calendar.SUNDAY)
//                else -> throw IllegalArgumentException("Invalid weekday name")
//            }
//        }
//        return calendarList
//    }

//    fun toJoda(selectedDays : List<Int>) {
//        selectedDays.forEach {
//            when (it) {
//                Calendar.MONDAY -> days.add(DateTimeConstants.MONDAY)
//                Calendar.TUESDAY -> days.add(DateTimeConstants.TUESDAY)
//                Calendar.WEDNESDAY -> days.add(DateTimeConstants.WEDNESDAY)
//                Calendar.THURSDAY -> days.add(DateTimeConstants.THURSDAY)
//                Calendar.FRIDAY -> days.add(DateTimeConstants.FRIDAY)
//                Calendar.SATURDAY -> days.add(DateTimeConstants.SATURDAY)
//                Calendar.SUNDAY -> days.add(DateTimeConstants.SUNDAY)
//            }
//        }
//    }
}