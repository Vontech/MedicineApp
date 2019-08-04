package org.vontech.medicine.views

import io.kotlintest.specs.FeatureSpec
import org.joda.time.LocalDate
import org.joda.time.tz.UTCProvider
import org.joda.time.DateTimeZone



class CalendarViewTest: FeatureSpec({

    DateTimeZone.setProvider(UTCProvider())

    feature("the calendar view creator") {

        scenario("should find the right dates for each day of the week") {

            getArrayOfDates(LocalDate.now())

        }

    }

})