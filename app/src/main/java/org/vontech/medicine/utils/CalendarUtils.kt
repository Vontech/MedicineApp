package org.vontech.medicine.utils

import org.joda.time.LocalDate

fun getAllDatesInMonth(month: Int, year: Int) {

    assert(month < 12)

    val date = LocalDate.now().withMonthOfYear(month).withDayOfMonth(1).withYear(year)

}