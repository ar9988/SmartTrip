package com.neungi.moyeo.views.aiplanning.selectduration

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.CalendarDayLayoutBinding
import com.neungi.moyeo.databinding.CalendarMonthHeaderLayoutBinding
import com.neungi.moyeo.databinding.FragmentAlbumDetailBinding
import com.neungi.moyeo.databinding.FragmentSelectPeriodBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.aiplanning.AIPlanningViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class SelectPeriodFragment : BaseFragment<FragmentSelectPeriodBinding>(R.layout.fragment_select_period) {

    private val viewModel: AIPlanningViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        // 캘린더 설정
        setupCalendar()
    }

    private fun setupCalendar() {
        var selectedStartDate: LocalDate? = null
        var selectedEndDate: LocalDate? = null
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)

        binding.calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        binding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            //        val textView = view.findViewById<TextView>(R.id.calendarDayText)

            lateinit var day: CalendarDay

            // With ViewBinding
            val textView = CalendarDayLayoutBinding.bind(view).tvDay

            init {
                view.setOnClickListener {
                    if (selectedStartDate == null) {
                        selectedStartDate = day.date
                        selectedEndDate = null
                    } else if (selectedEndDate == null) {
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
                    binding.calendarView.notifyCalendarChanged()
//                    viewModel.updateSelectedDateRange(selectedStartDate, selectedEndDate)
                }
            }
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.textView.text = day.date.dayOfMonth.toString()


                when {

                    day.date == selectedStartDate && selectedEndDate == null -> {
                        // 단일 선택일 때 원형 배경
                        container.textView.setTextColor(Color.WHITE)
                        container.textView.setBackgroundResource(R.drawable.selected_day_bg)
                    }
                    day.date == selectedStartDate && selectedStartDate == selectedEndDate -> {
                        container.textView.setTextColor(Color.WHITE)
                        container.textView.setBackgroundResource(R.drawable.selected_one_day_bg)
                    }
                    selectedStartDate != null && selectedEndDate != null &&
                            day.date > selectedStartDate!! && day.date < selectedEndDate!! -> {
                        // 범위 선택 중간 날짜
                        container.textView.setTextColor(Color.BLACK)
                        container.textView.setBackgroundResource(R.drawable.selected_range_bg)
                    }
                    day.date == selectedStartDate -> {
                        container.textView.setBackgroundResource(R.drawable.selected_range_start_bg)
                        container.textView.setTextColor(Color.WHITE)
                    }
                    day.date == selectedEndDate -> {
                        container.textView.setBackgroundResource(R.drawable.selected_range_end_bg)
                        container.textView.setTextColor(Color.WHITE)
                    }
                    else -> {
                        // 선택되지 않은 날짜
                        if (day.position != DayPosition.MonthDate) {
                            container.textView.setTextColor(Color.GRAY) // 회색
                            container.textView.background = null
                            return  // 현재 달이 아닌 날짜는 선택 불가능하도록
                        }else {
                            container.textView.setTextColor(
                                ContextCompat.getColor(
                                    container.textView.context,
                                    R.color.colorPrimary
                                )
                            )
                            container.textView.background = null
                        }
                    }
                }
            }
        }

        class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
            val binding = CalendarMonthHeaderLayoutBinding.bind(view)
            val monthText = binding.tvMonth
            val prevButton = binding.btnMonthPrevious
            val nextButton = binding.btnMonthNext
        }

        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderViewContainer> {
            override fun create(view: View) = MonthHeaderViewContainer(view)
            override fun bind(container: MonthHeaderViewContainer, month: CalendarMonth) {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
                container.monthText.text = month.yearMonth.format(formatter)


                container.prevButton.setOnClickListener {
                    binding.calendarView.findFirstVisibleMonth()?.let {
                        binding.calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
                    }
                }

                container.nextButton.setOnClickListener {
                    binding.calendarView.findFirstVisibleMonth()?.let {
                        binding.calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
                    }
                }
            }
        }
    }


// ViewContainer 클래스들


//    class MonthViewContainer(view: View) : ViewContainer(view) {
////        val textView = view.findViewById<TextView>(R.id.headerTextView)
//        val textView = CalendarMonthHeaderLayoutBinding.bind(view).tvMonthHeader
//    }




}