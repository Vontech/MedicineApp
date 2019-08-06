package org.vontech.medicine.views

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.joda.time.LocalDate
import org.joda.time.MutableDateTime
import org.joda.time.YearMonth
import org.vontech.medicine.R



class CalendarView: LinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        this.orientation = VERTICAL

        // Load the correct layout for this view
        //inflate(context, R.layout.view_chip, this)

        // Display attributes only after the view has been inflated
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CalendarView,
            defStyleAttr, 0)

        try {

            // Set the initial year and month for this calendar
            year = attributes.getInt(R.styleable.CalendarView_year, 1970)
            month = attributes.getInt(R.styleable.CalendarView_month, 1)

        } finally {
            attributes.recycle()
        }

    }

    var year : Int = 1970

        /**
         * Sets the year to use within this calendar view
         * @param value The year
         */
        set(value) {
            field = value
            render()
        }

    var month : Int = 1

        /**
         * Sets the month to use within this calendar view
         * Must be from [1, 12]
         * @param value The year
         */
        set(value) {
            assert(value in 1..12)
            field = value
            render()
        }

    var calendarEntryGenerator : CalendarEntryGenerator? = null

        set(value) {
            field = value
            render()
        }


    private fun render() {

        this.removeAllViews()

        val date = LocalDate(year, month, 1)
        val daysAcrossWeeks = getArrayOfDates(date)

        // Add the month above the calendar
        val titleTextView = TextView(context)
        titleTextView.text = YearMonth(date.year, date.monthOfYear).monthOfYear().asText.toUpperCase()
        _makeFillParentWidth(titleTextView)
//        _makeCenterGravity(titleTextView)
        _makeTitle(titleTextView)
        _makeProjectBlack(titleTextView)
        this.addView(titleTextView)

        // Add weekday headers
        val dayHeaders = LinearLayout(context)
        _makeFillParentWidth(dayHeaders)
        dayHeaders.orientation = HORIZONTAL
        for (i in 1..7) {
            val dayTextView = TextView(context)
            dayTextView.text = daysToAbbreviations[i]
            _setLayoutWeight(dayTextView, 1f)
            _makeCenterGravity(dayTextView)
            _makeProjectFont(dayTextView)
            dayHeaders.addView(dayTextView)
        }
        this.addView(dayHeaders)

        // Add days of the week
        for (week in daysAcrossWeeks) {
            val weekContainer = LinearLayout(context)
            weekContainer.orientation = HORIZONTAL
            for (day in week) {
                if (calendarEntryGenerator == null) {
                    weekContainer.addView(getDayView(day))
                } else {
                    weekContainer.addView(calendarEntryGenerator!!.create(day))
                }

            }
            giveEqualWeights((0 until weekContainer.childCount).map { weekContainer.getChildAt(it) })
            this.addView(weekContainer)
        }

    }

    private fun giveEqualWeights(viewList: List<View>) {
        viewList.forEach {
            _setLayoutWeight(it, 1f)
        }
    }

    private fun _makeFillParentWidth(view: View) {
        view.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT)
    }

    private fun _makeCenterGravity(textView: TextView) {
        textView.gravity = Gravity.CENTER
    }

    private fun _makeProjectFont(textView: TextView) {
        val typeface = ResourcesCompat.getFont(this.context, R.font.sf_medium)
        textView.typeface = typeface
    }

    private fun _makeTitle(textView: TextView) {
        val typeface = ResourcesCompat.getFont(this.context, R.font.sf_heavy)
        textView.typeface = typeface
        textView.textSize = 24f
    }

    private fun _makeProjectBlack(textView: TextView) {
        textView.setTextColor(resources.getColor(R.color.textColor))
    }

    private fun _setLayoutWeight(view: View, weight: Float) {
        val param = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            weight
        )
        view.setLayoutParams(param)
    }

    fun getDayView(dayOfMonth: LocalDate): View {
        val view = TextView(context)
        _makeProjectFont(view)
        _makeProjectBlack(view)
        _makeCenterGravity(view)
        view.text = dayOfMonth.dayOfMonth.toString()
        return view
    }

}


val daysToAbbreviations = mapOf(
    1 to "SUN",
    2 to "MON",
    3 to "TUE",
    4 to "WED",
    5 to "THU",
    6 to "FRI",
    7 to "SAT"
)

fun getArrayOfDates(timeAnchor: LocalDate): List<List<LocalDate>> {
    val month = timeAnchor.monthOfYear
    val year = timeAnchor.year

    // Construct the first day of the month
    val currentDay = MutableDateTime()
    currentDay.dayOfMonth = 1
    currentDay.monthOfYear = month
    currentDay.year = year

    val monthEntries = mutableListOf<MutableList<LocalDate>>(mutableListOf())

    val backReference = currentDay.copy()

    // Roll back to the beginning of the week
    while (backReference.dayOfWeek != 1) { backReference.addDays(-1) }

    // Add the days from last month
    while (backReference.dayOfMonth != 1) {
        monthEntries.last().add(LocalDate(backReference.copy()))
        backReference.addDays(1)
    }

    // Now add each day from this month
    while (currentDay.monthOfYear == month) {
        monthEntries.last().add(LocalDate(currentDay.copy()))
        if (currentDay.dayOfWeek == 7) {
            monthEntries.add(mutableListOf())
        }
        currentDay.addDays(1)
    }

    // Now add the final days from next month
    while (currentDay.dayOfWeek != 1) {
        monthEntries.last().add(LocalDate(currentDay.copy()))
        currentDay.addDays(1)
    }

    return monthEntries

}

interface CalendarEntryGenerator {
    fun create(day: LocalDate): View
}