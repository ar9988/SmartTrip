package com.neungi.moyeo.views.aiplanning

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiSelectPeriodBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AiSelectPeriodFragment : BaseFragment<FragmentAiSelectPeriodBinding>(R.layout.fragment_ai_select_period) {

    private val viewModel: AIPlanningViewModel by activityViewModels()

    private val mainViewModel : MainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        addListener()
        collectEvent()

    }


    private fun addListener(){
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.calendarView.setOnDateRangeSelectedListener { startDate, endDate ->
            viewModel.updateSelectedDateRange(startDate, endDate)
        }
        binding.tvStartTime.setOnClickListener {
            showTimePickerDialog(true)
        }

        binding.tvEndTime.setOnClickListener {
            showTimePickerDialog(false)
        }
//        binding.btnNext.setOnClickListener {
//            Timber.d("next")
//            findNavController().navigateSafely(R.id.action_fragment_select_period_to_fragment_ai_select_local)
//        }
    }

    private fun showTimePickerDialog(isStartTime: Boolean) {
        val currentTime = if (isStartTime) viewModel.startTime.value else viewModel.endTime.value
        val (hour, minute) = parseTimeString(currentTime)

        TimePickerDialog(
            requireContext(),
            { _, newHour, newMinute ->
                viewModel.updateTime(isStartTime, newHour, newMinute)
            },
            hour, minute, false
        ).show()
    }

    private fun parseTimeString(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(" ")[1].split(":")
        var hour = parts[0].toInt()
        val minute = parts[1].toInt()

        if (timeString.startsWith("오후") && hour != 12) hour += 12
        if (timeString.startsWith("오전") && hour == 12) hour = 0

        return Pair(hour, minute)
    }

    private fun collectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.aiPlanningUiEvent.collect { event ->
                when (event) {
                    is AiPlanningUiEvent.GoToSelectLocal-> {
                        Timber.d("next")
                        findNavController().navigateSafely(R.id.action_fragment_select_period_to_fragment_ai_select_local)
                    }

                    else->{

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setBnvState(false)
    }





}