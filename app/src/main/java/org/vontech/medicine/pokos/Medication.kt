package org.vontech.medicine.pokos

import org.joda.time.LocalTime
import java.io.Serializable
import kotlin.random.Random

data class Medication(var name: String?, var dose: Float?, var notes: String, val id: Int = Random.nextInt()) : Serializable {
    var days = mutableSetOf<Int>() // JodaTime weekday constants
    var times = mutableSetOf<LocalTime>() // JodaTime object
    var pillImagePath: String = "" // Picture of the pill shown on UI cards

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

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Medication

        return this.id == other.id

    }

    override fun hashCode(): Int{
        return this.id.hashCode()
    }

}