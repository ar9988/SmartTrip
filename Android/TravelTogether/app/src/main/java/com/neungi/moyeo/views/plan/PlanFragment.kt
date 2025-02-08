package com.neungi.moyeo.views.plan

import com.neungi.moyeo.views.plan.adapter.TripAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.plan.tripviewmodel.TripUiEvent
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>(R.layout.fragment_plan) {

    private val tripViewModel: TripViewModel by activityViewModels()
    private val mainViewModel : MainViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = tripViewModel
        setupRecyclerView()

    }



    private fun handleUiEvent(event: TripUiEvent) {
        when (event) {
            is TripUiEvent -> {

            }
        }
    }


    override fun onResume() {
        super.onResume()
        tripViewModel.getTrip()
        mainViewModel.setBnvState(true)
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onItemClick = { trip ->
                scheduleViewModel.trip = trip
                Timber.d(trip.title)
                findNavController().navigateSafely(R.id.action_plan_to_planDetail)
            },
            onDeleteClick = { tripId ->
                // 삭제 처리 로직
                tripViewModel
                println("Delete trip with ID: $tripId")
            }
        )
        binding.recyclerViewTrips.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewTrips.adapter = tripAdapter
    }

}