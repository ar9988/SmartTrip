package com.neungi.moyeo.util

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.CalendarDayLayoutBinding
import com.neungi.moyeo.databinding.CalendarMonthHeaderLayoutBinding
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class CustomCalendarView (context: Context, attrs: AttributeSet) : CalendarView(context,attrs) {
    var selectedStartDate: LocalDate? = null
    var selectedEndDate: LocalDate? = null
    var maxDateRange: Int = 10

    private var onDateRangeSelected: ((LocalDate?, LocalDate?) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomCalendarView,
            0, 0
        ).apply {
            try {
                maxDateRange = getInteger(R.styleable.CustomCalendarView_periodLength, 10)
            } finally {
                recycle()
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)

        setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        scrollToMonth(currentMonth)
        setupDayBinder()
        setupHeaderBinder()
    }

    fun setOnDateRangeSelectedListener(listener: (LocalDate?, LocalDate?) -> Unit) {
        onDateRangeSelected = listener
    }

    private fun setupDayBinder() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.date < LocalDate.now() || day.position != DayPosition.MonthDate) {
                        return@setOnClickListener
                    }
                    if (selectedStartDate == null) {
                        selectedStartDate = day.date
                        selectedEndDate = null
                    } else if (selectedEndDate == null) {
                        val daysBetween = Math.abs(ChronoUnit.DAYS.between(selectedStartDate, day.date))
                        if (daysBetween >= maxDateRange) {
                            Toast.makeText(context,
                                "최대 ${maxDateRange}일까지 선택 가능합니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                        if (day.date < selectedStartDate!!) {
                            selectedEndDate = selectedStartDate
                            selectedStartDate = day.date
                        } else {
                            selectedEndDate = day.date
                        }
                    } else {
                        selectedStartDate = day.date
                        selectedEndDate = null
                    }
                    Timber.d(day.date.toString())
                    notifyCalendarChanged()
                    onDateRangeSelected?.invoke(selectedStartDate, selectedEndDate)
                }
            }
        }

        dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.binding.tvDay.text = day.date.dayOfMonth.toString()

                val isInSelectableRange = if (selectedStartDate != null && selectedEndDate == null) {
                    val daysBetween = Math.abs(ChronoUnit.DAYS.between(selectedStartDate, day.date))
                    daysBetween < maxDateRange
                } else true

                if (day.date < LocalDate.now() || !isInSelectableRange) {
                    container.binding.tvDay.setTextColor(Color.argb(128, 128, 128, 128))
                    container.binding.tvDay.background = null
                    return
                }

                when {
                    day.date == selectedStartDate && selectedEndDate == null -> {
                        container.binding.tvDay.setTextColor(Color.WHITE)
                        container.binding.tvDay.setBackgroundResource(R.drawable.selected_day_bg)
                    }
                    day.date == selectedStartDate && selectedStartDate == selectedEndDate -> {
                        container.binding.tvDay.setTextColor(Color.WHITE)
                        container.binding.tvDay.setBackgroundResource(R.drawable.selected_one_day_bg)
                    }
                    selectedStartDate != null && selectedEndDate != null &&
                            day.date > selectedStartDate!! && day.date < selectedEndDate!! -> {
                        container.binding.tvDay.setTextColor(Color.BLACK)
                        container.binding.tvDay.setBackgroundResource(R.drawable.selected_range_bg)
                    }
                    day.date == selectedStartDate -> {
                        container.binding.tvDay.setBackgroundResource(R.drawable.selected_range_start_bg)
                        container.binding.tvDay.setTextColor(Color.WHITE)
                    }
                    day.date == selectedEndDate -> {
                        container.binding.tvDay.setBackgroundResource(R.drawable.selected_range_end_bg)
                        container.binding.tvDay.setTextColor(Color.WHITE)
                    }
                    else -> {
                        if (day.position != DayPosition.MonthDate) {
                            container.binding.tvDay.setTextColor(Color.GRAY)
                            container.binding.tvDay.background = null
                            return
                        } else {
                            container.binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    container.binding.tvDay.context,
                                    R.color.colorPrimary
                                )
                            )
                            container.binding.tvDay.background = null
                        }
                    }
                }
            }
        }
    }

    private fun setupHeaderBinder() {
        class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
            val binding = CalendarMonthHeaderLayoutBinding.bind(view)
        }

        monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderViewContainer> {
            override fun create(view: View) = MonthHeaderViewContainer(view)
            override fun bind(container: MonthHeaderViewContainer, month: CalendarMonth) {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
                container.binding.tvMonth.text = month.yearMonth.format(formatter)

                container.binding.btnMonthPrevious.setOnClickListener {
                    findFirstVisibleMonth()?.let {
                        smoothScrollToMonth(it.yearMonth.minusMonths(1))
                    }
                }

                container.binding.btnMonthNext.setOnClickListener {
                    findFirstVisibleMonth()?.let {
                        smoothScrollToMonth(it.yearMonth.plusMonths(1))
                    }
                }
            }
        }
    }
}