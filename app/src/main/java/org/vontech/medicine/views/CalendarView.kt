package org.vontech.medicine.views

import android.view.ViewGroup
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.MutableDateTime

fun createCalendarView(viewGroup: ViewGroup, timeAnchor: LocalDate) {

    val month = timeAnchor.monthOfYear
    val year = timeAnchor.year

    // Construct the first day of the month
    val currentDay = MutableDateTime()
    currentDay.dayOfMonth = 1
    currentDay.monthOfYear = month
    currentDay.year = year

    val monthEntries = mutableListOf<List<Int>>()

    while (true) {

        val dayOfWeek = currentDay.dayOfWeek

        // If this is the first day, add days before this
        val backReference = currentDay.copy()
        for (i in dayOfWeek..0) {
            backReference.addDays(-1)


        }


    }


}